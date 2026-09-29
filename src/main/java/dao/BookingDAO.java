package dao;

import entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import util.PersistenceManager;

public class BookingDAO {

    public List<Booking> getAll() {
        return new DAOFramework<>(Booking.class).getAll().reversed();
    }

    public Booking getById(String bookingID) {
        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            return null;
        }
        return new DAOFramework<>(Booking.class).findById(bookingID);
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

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.close();
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

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.close();
        }

    }

    /*
     * Finds bookings by account UserID.
     * This is different from CustomerID.
     */
    public List<Booking> getByAccountUserId(
            int userID) {

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.close();
        }
    }

    public Booking getLatestBookingByUserId(
            int userID) {

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.close();
        }
    }

    /*
     * Used while the customer is selecting services
     * before payment.
     */
    public Booking getLatestPendingBookingByUserId(
            int userID) {

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.close();
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

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.close();
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

        EntityManager em = PersistenceManager.createEntityManager();
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
            em.getTransaction().rollback();

            throw new IllegalStateException(
                    "Unable to update booking status.",
                    exception
            );
        } finally {
            em.close();
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

    public static void main(String[] args) {
        System.out.println(new BookingDAO().getAll());
    }
}
