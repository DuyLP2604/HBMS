package dao;

import entity.Booking;
import entity.BookingService;
import entity.BookingServicePK;
import entity.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import util.PersistenceManager;

public class ServiceDAO {

    public List<Service> getAllServices() {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT s FROM Service s "
                    + "ORDER BY s.serviceName";

            TypedQuery<Service> query = em.createQuery(
                    jpql,
                    Service.class
            );

            return query.getResultList();
        } finally {
            close(em);
        }
    }

    public Service findByID(String serviceID) {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            return em.find(Service.class, serviceID);
        } finally {
            close(em);
        }
    }

    public String generateServiceID() {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT s.serviceID "
                    + "FROM Service s";

            TypedQuery<String> query = em.createQuery(
                    jpql,
                    String.class
            );

            List<String> serviceIDs
                    = query.getResultList();

            int largestNumber = 0;

            for (String serviceID : serviceIDs) {
                if (serviceID == null) {
                    continue;
                }

                String normalizedID = serviceID.trim();

                if (!normalizedID.matches("S\\d+")) {
                    continue;
                }

                int number = Integer.parseInt(
                        normalizedID.substring(1)
                );

                if (number > largestNumber) {
                    largestNumber = number;
                }
            }

            return String.format(
                    "S%02d",
                    largestNumber + 1
            );
        } catch (NumberFormatException exception) {
            throw new IllegalStateException(
                    "Unable to generate a new service ID.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    public void insertService(Service service) {
        if (service == null) {
            throw new IllegalArgumentException(
                    "Service must not be null."
            );
        }

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            em.persist(service);

            em.getTransaction().commit();
        } catch (Exception exception) {
            rollback(em);

            throw new IllegalStateException(
                    "Unable to add the service.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * This overload keeps existing code working.
     * The default quantity is one.
     */
    public void insertBookingService(
            String bookingID,
            String serviceID) {

        insertBookingService(
                bookingID,
                serviceID,
                1
        );
    }

    public void insertBookingService(
            String bookingID,
            String serviceID,
            int quantity) {

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Booking ID must not be empty."
            );
        }

        if (serviceID == null
                || serviceID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Service ID must not be empty."
            );
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Service quantity must be greater than zero."
            );
        }

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            String normalizedBookingID
                    = bookingID.trim();

            String normalizedServiceID
                    = serviceID.trim();

            Booking booking = em.find(
                    Booking.class,
                    normalizedBookingID
            );

            Service service = em.find(
                    Service.class,
                    normalizedServiceID
            );

            if (booking == null) {
                throw new IllegalArgumentException(
                        "Booking not found: "
                        + normalizedBookingID
                );
            }

            if (service == null) {
                throw new IllegalArgumentException(
                        "Service not found: "
                        + normalizedServiceID
                );
            }

            BookingServicePK primaryKey
                    = new BookingServicePK(
                            normalizedBookingID,
                            normalizedServiceID
                    );

            BookingService bookingService = em.find(
                    BookingService.class,
                    primaryKey
            );

            if (bookingService == null) {

                bookingService = new BookingService();
                bookingService.setBooking(booking);
                bookingService.setService(service);
                bookingService.setQuantity(quantity);
                bookingService.setUnitPrice(service.getUnitPrice());

                em.persist(bookingService);
            } else {
                int newQuantity
                        = bookingService.getQuantity()
                        + quantity;

                bookingService.setQuantity(newQuantity);
            }

            em.getTransaction().commit();
        } catch (IllegalArgumentException exception) {
            rollback(em);

            throw new IllegalStateException(
                    "Unable to add the service "
                    + "to the booking.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    private void rollback(EntityManager em) {
        if (em != null
                && em.getTransaction().isActive()) {

            em.getTransaction().rollback();
        }
    }

    private void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
