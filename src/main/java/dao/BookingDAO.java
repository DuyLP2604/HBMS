package dao;

import entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class BookingDAO {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    public List<Booking> getAll() {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b "
                    + "FROM Booking b "
                    + "ORDER BY b.bookingDate DESC";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            return query.getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load bookings.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    public Booking getById(String bookingID) {
        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            return null;
        }

        EntityManager em = EMF.createEntityManager();

        try {
            return em.find(
                    Booking.class,
                    bookingID.trim()
            );
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to find the booking.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Loads the booking together with room-type requests
     * and assigned rooms for the booking-detail page.
     */
    public Booking getByIdWithDetails(
            String bookingID) {

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            return null;
        }

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT DISTINCT b "
                    + "FROM Booking b "
                    + "JOIN FETCH b.customerID c "
                    + "LEFT JOIN FETCH "
                    + "b.bookingDetailCollection bd "
                    + "LEFT JOIN FETCH bd.roomTypeID rt "
                    + "LEFT JOIN FETCH "
                    + "bd.roomAssignmentCollection ra "
                    + "LEFT JOIN FETCH ra.roomID r "
                    + "WHERE b.bookingID = :bookingID";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            query.setParameter(
                    "bookingID",
                    bookingID.trim()
            );

            List<Booking> results
                    = query.getResultList();

            return results.isEmpty()
                    ? null
                    : results.get(0);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load booking details.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * The parameter is CustomerID, for example KH01.
     * This method is retained for compatibility.
     */
    public List<Booking> getByUserId(
            String customerID) {

        return getByCustomerId(customerID);
    }

    public List<Booking> getByCustomerId(
            String customerID) {

        if (customerID == null
                || customerID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Customer ID is required."
            );
        }

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b "
                    + "FROM Booking b "
                    + "WHERE b.customerID.customerID "
                    + "= :customerID "
                    + "ORDER BY b.bookingDate DESC";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            query.setParameter(
                    "customerID",
                    customerID.trim()
            );

            return query.getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load customer bookings.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Finds bookings by account UserID.
     * This is different from CustomerID.
     */
    public List<Booking> getByAccountUserId(
            int userID) {

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b "
                    + "FROM Booking b "
                    + "WHERE b.customerID.userID.userID "
                    + "= :userID "
                    + "ORDER BY b.bookingDate DESC";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            query.setParameter("userID", userID);

            return query.getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load user bookings.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    public Booking getLatestBookingByUserId(
            int userID) {

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b "
                    + "FROM Booking b "
                    + "WHERE b.customerID.userID.userID "
                    + "= :userID "
                    + "ORDER BY b.bookingDate DESC";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            query.setParameter("userID", userID);
            query.setMaxResults(1);

            List<Booking> results
                    = query.getResultList();

            return results.isEmpty()
                    ? null
                    : results.get(0);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to find the latest booking.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Used while the customer is selecting services
     * before payment.
     */
    public Booking getLatestPendingBookingByUserId(
            int userID) {

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b "
                    + "FROM Booking b "
                    + "WHERE b.customerID.userID.userID "
                    + "= :userID "
                    + "AND b.bookingStatus "
                    + "= :bookingStatus "
                    + "ORDER BY b.bookingDate DESC";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            query.setParameter("userID", userID);

            query.setParameter(
                    "bookingStatus",
                    "PENDING_PAYMENT"
            );

            query.setMaxResults(1);

            List<Booking> results
                    = query.getResultList();

            return results.isEmpty()
                    ? null
                    : results.get(0);
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to find a pending booking.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Used by staff to view bookings waiting
     * for room assignment.
     */
    public List<Booking> getByStatus(
            String bookingStatus) {

        if (bookingStatus == null
                || bookingStatus.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Booking status is required."
            );
        }

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b "
                    + "FROM Booking b "
                    + "JOIN FETCH b.customerID c "
                    + "WHERE b.bookingStatus "
                    + "= :bookingStatus "
                    + "ORDER BY b.checkInDate";

            TypedQuery<Booking> query = em.createQuery(
                    jpql,
                    Booking.class
            );

            query.setParameter(
                    "bookingStatus",
                    bookingStatus.trim()
            );

            return query.getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load bookings by status.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    public void updateStatus(
            String bookingID,
            String bookingStatus) {

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Booking ID is required."
            );
        }

        validateStatus(bookingStatus);

        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();

            Booking booking = em.find(
                    Booking.class,
                    bookingID.trim()
            );

            if (booking == null) {
                throw new IllegalArgumentException(
                        "Booking not found: "
                        + bookingID
                );
            }

            booking.setBookingStatus(
                    bookingStatus.trim()
            );

            em.getTransaction().commit();
        } catch (Exception exception) {
            rollback(em);

            throw new IllegalStateException(
                    "Unable to update booking status.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    private void validateStatus(
            String bookingStatus) {

        if (bookingStatus == null) {
            throw new IllegalArgumentException(
                    "Booking status is required."
            );
        }

        String status = bookingStatus.trim();

        boolean valid
                = "PENDING_PAYMENT".equals(status)
                || "CONFIRMED".equals(status)
                || "ASSIGNED".equals(status)
                || "CHECKED_IN".equals(status)
                || "CHECKED_OUT".equals(status)
                || "CANCELLED".equals(status);

        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid booking status: "
                    + bookingStatus
            );
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