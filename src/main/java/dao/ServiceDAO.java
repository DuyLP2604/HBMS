package dao;

import entity.Booking;
import entity.BookingService;
import entity.BookingServicePK;
import entity.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.PersistenceManager;

public class ServiceDAO {

    private static final Logger LOGGER = Logger.getLogger(ServiceDAO.class.getName());

    public List<Service> getAllServices() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT s FROM Service s ORDER BY s.serviceName";
            TypedQuery<Service> query = em.createQuery(jpql, Service.class);
            return query.getResultList();
        } finally {
            close(em);
        }
    }

    public List<BookingService> getServicesByBookingId(String bookingID) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            return Collections.emptyList();
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT bs FROM BookingService bs JOIN FETCH bs.service WHERE bs.booking.bookingID = :bookingID ORDER BY bs.service.serviceName";
            TypedQuery<BookingService> query = em.createQuery(jpql, BookingService.class);
            query.setParameter("bookingID", bookingID.trim());
            return query.getResultList();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting services for booking: " + bookingID, e);
            return Collections.emptyList();
        } finally {
            close(em);
        }
    }

    public void insertBookingService(String bookingID, String serviceID, int quantity) {
        if (bookingID == null || bookingID.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking ID must not be empty.");
        }
        if (serviceID == null || serviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Service ID must not be empty.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Service quantity must be greater than zero.");
        }

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            String normalizedBookingID = bookingID.trim();
            String normalizedServiceID = serviceID.trim();
            Booking booking = em.find(Booking.class, normalizedBookingID);
            Service service = em.find(Service.class, normalizedServiceID);

            if (booking == null) {
                throw new IllegalArgumentException("Booking not found: " + normalizedBookingID);
            }
            if (service == null) {
                throw new IllegalArgumentException("Service not found: " + normalizedServiceID);
            }

            BookingServicePK primaryKey = new BookingServicePK(normalizedBookingID, normalizedServiceID);
            BookingService bookingService = em.find(BookingService.class, primaryKey);

            if (bookingService == null) {
                bookingService = new BookingService();
                bookingService.setBookingServicePK(primaryKey);
                bookingService.setBooking(booking);
                bookingService.setService(service);
                bookingService.setQuantity(quantity);
                bookingService.setUnitPrice(service.getUnitPrice());
                BigDecimal subtotal = service.getUnitPrice().multiply(BigDecimal.valueOf(quantity));
                bookingService.setSubtotal(subtotal);

                em.persist(bookingService);
            } else {
                int newQuantity = bookingService.getQuantity() + quantity;
                bookingService.setQuantity(newQuantity);
                BigDecimal subtotal = bookingService.getUnitPrice().multiply(BigDecimal.valueOf(newQuantity));
                bookingService.setSubtotal(subtotal);
            }

            em.getTransaction().commit();
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (IllegalArgumentException exception) {
            rollback(em);
            throw exception;
        } catch (Exception exception) {
            rollback(em);
            throw new IllegalStateException("Unable to add the service to the booking.", exception);
        } finally {
            close(em);
        }
    }

    public String generateNextServiceID() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT s.serviceID FROM Service s";
            TypedQuery<String> query = em.createQuery(jpql, String.class);
            List<String> serviceIDs = query.getResultList();
            int max = 0;
            for (String sid : serviceIDs) {
                if (sid != null && sid.trim().matches("S\\d+")) {
                    try {
                        int num = Integer.parseInt(sid.trim().substring(1));
                        if (num > max) max = num;
                    } catch (NumberFormatException ignored) {}
                }
            }
            return String.format("S%02d", max + 1);
        } finally {
            close(em);
        }
    }

    public void createService(String serviceName, BigDecimal unitPrice) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ không hợp lệ.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            String serviceID = generateNextServiceID();
            Service service = new Service(serviceID, serviceName.trim(), unitPrice);
            em.persist(service);
            em.getTransaction().commit();
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (Exception e) {
            rollback(em);
            throw new IllegalStateException("Không thể tạo dịch vụ mới: " + e.getMessage(), e);
        } finally {
            close(em);
        }
    }

    public void updateServicePrice(String serviceID, BigDecimal unitPrice) {
        if (serviceID == null || serviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        if (unitPrice == null || unitPrice.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá không được âm.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Service service = em.find(Service.class, serviceID.trim());
            if (service == null) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ: " + serviceID);
            }
            service.setUnitPrice(unitPrice);
            em.getTransaction().commit();
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (IllegalArgumentException e) {
            rollback(em);
            throw e;
        } catch (Exception e) {
            rollback(em);
            throw new IllegalStateException("Không thể cập nhật giá dịch vụ: " + e.getMessage(), e);
        } finally {
            close(em);
        }
    }

    public void deleteService(String serviceID) {
        if (serviceID == null || serviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Service service = em.find(Service.class, serviceID.trim());
            if (service == null) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ: " + serviceID);
            }
            Long count = em.createQuery("SELECT COUNT(bs) FROM BookingService bs WHERE bs.service.serviceID = :sid", Long.class)
                    .setParameter("sid", serviceID.trim())
                    .getSingleResult();
            if (count != null && count > 0) {
                throw new IllegalStateException("Không thể xóa dịch vụ này vì đã có trong đơn đặt phòng. Vui lòng cập nhật đơn giá về 0 hoặc ngưng sử dụng.");
            }
            em.remove(service);
            em.getTransaction().commit();
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (IllegalArgumentException | IllegalStateException e) {
            rollback(em);
            throw e;
        } catch (Exception e) {
            rollback(em);
            throw new IllegalStateException("Không thể xóa dịch vụ: " + e.getMessage(), e);
        } finally {
            close(em);
        }
    }

    private void rollback(EntityManager em) {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
