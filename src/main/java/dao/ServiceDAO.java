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
import util.PersistenceManager;

public class ServiceDAO {

    public List<Service> getAllServices() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT s FROM Service s ORDER BY s.serviceName ASC";
            TypedQuery<Service> query = em.createQuery(jpql, Service.class);
            return query.getResultList();
        } finally {
            close(em);
        }
    }

    public Service findByID(String serviceID) {
        if (serviceID == null || serviceID.trim().isEmpty()) {
            return null;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.find(Service.class, serviceID.trim());
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
            String jpql = "SELECT bs FROM BookingService bs WHERE bs.booking.bookingID = :bookingID";
            TypedQuery<BookingService> query = em.createQuery(jpql, BookingService.class);
            query.setParameter("bookingID", bookingID.trim());
            return query.getResultList();
        } catch (Exception e) {
            return Collections.emptyList();
        } finally {
            close(em);
        }
    }

    public List<BookingService> getAllBookServices() {
        return getAllBookedServices();
    }

    public List<BookingService> getAllBookedServices() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT bs FROM BookingService bs "
                    + "JOIN FETCH bs.service "
                    + "JOIN FETCH bs.booking b "
                    + "JOIN FETCH b.customerID "
                    + "WHERE b.bookingStatus <> 'CANCELLED' "
                    + "ORDER BY b.bookingID DESC";
            TypedQuery<BookingService> query = em.createQuery(jpql, BookingService.class);
            return query.getResultList();
        } catch (Exception e) {
            return Collections.emptyList();
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
                if (sid != null) {
                    String trimmed = sid.trim();
                    if (trimmed.matches("S\\d+")) {
                        try {
                            int num = Integer.parseInt(trimmed.substring(1));
                            if (num > max) {
                                max = num;
                            }
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
            }
            return String.format("S%02d", max + 1);
        } finally {
            close(em);
        }
    }

    public void createService(String serviceName, double unitPrice) {
        if (serviceName == null || serviceName.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên dịch vụ không được để trống.");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ không được âm.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            String serviceID = generateNextServiceID();
            Service service = new Service();
            service.setServiceID(serviceID);
            service.setServiceName(serviceName.trim());
            service.setUnitPrice(BigDecimal.valueOf(unitPrice));
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

    public void updateServicePrice(String serviceID, double unitPrice) {
        if (serviceID == null || serviceID.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
        }
        if (unitPrice < 0) {
            throw new IllegalArgumentException("Đơn giá dịch vụ không được âm.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Service service = em.find(Service.class, serviceID.trim());
            if (service == null) {
                throw new IllegalArgumentException("Không tìm thấy dịch vụ: " + serviceID);
            }
            service.setUnitPrice(BigDecimal.valueOf(unitPrice));
            em.getTransaction().commit();
            PersistenceManager.getEMF().getCache().evictAll();
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
            Long count = em.createQuery("SELECT COUNT(bs) FROM BookingService bs WHERE bs.bookingServicePK.serviceID = :sid", Long.class)
                    .setParameter("sid", serviceID.trim())
                    .getSingleResult();
            if (count != null && count > 0) {
                throw new IllegalStateException("Không thể xóa: Dịch vụ này đã có trong đơn đặt phòng của khách.");
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

    public void insertBookingService(String bookingID, String serviceID, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Booking booking = em.find(Booking.class, bookingID.trim());
            Service service = em.find(Service.class, serviceID.trim());
            if (booking == null || service == null) {
                throw new IllegalArgumentException("Không tìm thấy booking hoặc dịch vụ.");
            }
            BookingServicePK pk = new BookingServicePK(booking.getBookingID(), service.getServiceID());
            BookingService bs = em.find(BookingService.class, pk);
            if (bs == null) {
                bs = new BookingService(pk);
                bs.setBooking(booking);
                bs.setService(service);
                bs.setQuantity(quantity);
                bs.setUnitPrice(service.getUnitPrice());
                bs.setSubtotal(service.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
                em.persist(bs);
            } else {
                int newQty = bs.getQuantity() + quantity;
                bs.setQuantity(newQty);
                bs.setSubtotal(bs.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
            }
            em.getTransaction().commit();
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (Exception e) {
            rollback(em);
            throw new IllegalStateException("Không thể thêm dịch vụ: " + e.getMessage(), e);
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
