package service;

import entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.Arrays;
import java.util.List;

public class BookingLifecycleService {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    public List<Booking> getOperationalBookings() {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b FROM Booking b "
                    + "WHERE b.bookingStatus IN :statuses "
                    + "ORDER BY b.checkInDate ASC, "
                    + "b.bookingDate ASC";

            TypedQuery<Booking> query
                    = em.createQuery(
                            jpql,
                            Booking.class
                    );

            query.setParameter(
                    "statuses",
                    Arrays.asList(
                            "CONFIRMED",
                            "ASSIGNED",
                            "CHECKED_IN"
                    )
            );

            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void checkIn(String bookingID) {
        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();

            Booking booking = findAndLockBooking(
                    em,
                    bookingID
            );

            if (!"ASSIGNED".equals(
                    booking.getBookingStatus())) {

                throw new IllegalStateException(
                        "Only assigned bookings can be checked in."
                );
            }

            long requiredRooms
                    = countRequiredRooms(
                            em,
                            bookingID
                    );

            long assignedRooms
                    = countAssignedRooms(
                            em,
                            bookingID
                    );

            if (requiredRooms != assignedRooms) {
                throw new IllegalStateException(
                        "All required rooms must be assigned before check-in."
                );
            }

            booking.setBookingStatus("CHECKED_IN");

            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            rollback(em);

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            throw new RuntimeException(
                    "Unable to check in this booking.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void checkOut(String bookingID) {
        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();

            Booking booking = findAndLockBooking(
                    em,
                    bookingID
            );

            if (!"CHECKED_IN".equals(
                    booking.getBookingStatus())) {

                throw new IllegalStateException(
                        "Only checked-in bookings can be checked out."
                );
            }

            booking.setBookingStatus("CHECKED_OUT");

            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            rollback(em);

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            throw new RuntimeException(
                    "Unable to check out this booking.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void cancelByCustomer(
            String bookingID,
            int userID) {

        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();

            Booking booking
                    = findOwnedBookingAndLock(
                            em,
                            bookingID,
                            userID
                    );

            if (booking == null) {
                throw new IllegalStateException(
                        "The requested booking was not found."
                );
            }

            String status
                    = booking.getBookingStatus();

            boolean cancellable
                    = "PENDING_PAYMENT".equals(status)
                    || "CONFIRMED".equals(status)
                    || "ASSIGNED".equals(status);

            if (!cancellable) {
                throw new IllegalStateException(
                        "This booking can no longer be cancelled."
                );
            }

            /*
             * For the coursework demo, a paid payment is marked
             * as refunded automatically.
             */
            String updatePayment
                    = "UPDATE Payment p "
                    + "SET p.status = :refundedStatus "
                    + "WHERE p.bookingID = :booking "
                    + "AND p.status = :paidStatus";

            em.createQuery(updatePayment)
                    .setParameter(
                            "refundedStatus",
                            "REFUNDED"
                    )
                    .setParameter(
                            "paidStatus",
                            "PAID"
                    )
                    .setParameter(
                            "booking",
                            booking
                    )
                    .executeUpdate();

            booking.setBookingStatus("CANCELLED");

            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            rollback(em);

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            throw new RuntimeException(
                    "Unable to cancel this booking.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private Booking findAndLockBooking(
            EntityManager em,
            String bookingID) {

        Booking booking = em.find(
                Booking.class,
                bookingID,
                LockModeType.PESSIMISTIC_WRITE
        );

        if (booking == null) {
            throw new IllegalStateException(
                    "The requested booking was not found."
            );
        }

        return booking;
    }

    private Booking findOwnedBookingAndLock(
            EntityManager em,
            String bookingID,
            int userID) {

        List<Booking> results = em.createQuery(
                "SELECT b FROM Booking b "
                + "WHERE b.bookingID = :bookingID "
                + "AND b.customerID.userID.userID = :userID",
                Booking.class
        )
                .setParameter("bookingID", bookingID)
                .setParameter("userID", userID)
                .setLockMode(LockModeType.PESSIMISTIC_WRITE)
                .getResultList();

        return results.isEmpty()
                ? null
                : results.get(0);
    }

    private long countRequiredRooms(
            EntityManager em,
            String bookingID) {

        String jpql
                = "SELECT COALESCE(SUM(bd.quantity), 0) "
                + "FROM BookingDetail bd "
                + "WHERE bd.bookingID.bookingID = :bookingID";

        Long result = em.createQuery(
                jpql,
                Long.class
        )
                .setParameter(
                        "bookingID",
                        bookingID
                )
                .getSingleResult();

        return result == null ? 0 : result;
    }

    private long countAssignedRooms(
            EntityManager em,
            String bookingID) {

        String jpql
                = "SELECT COUNT(ra) "
                + "FROM RoomAssignment ra "
                + "WHERE ra.bookingDetailID.bookingID."
                + "bookingID = :bookingID";

        Long result = em.createQuery(
                jpql,
                Long.class
        )
                .setParameter(
                        "bookingID",
                        bookingID
                )
                .getSingleResult();

        return result == null ? 0 : result;
    }

    private void rollback(EntityManager em) {
        if (em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }
}