package service;

import entity.Booking;
import entity.Hotel;
import entity.Payment;
import entity.Paymentmethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.PessimisticLockException;
import jakarta.persistence.TypedQuery;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PaymentService {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    private static final int PAYMENT_LOCK_TIMEOUT_MS
            = 10000;

    /**
     * Loads a booking belonging to the currently logged-in customer.
     */
    public Booking getBookingForPayment(
            String bookingID,
            int userID) {

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            return null;
        }

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT b FROM Booking b "
                    + "WHERE b.bookingID = :bookingID "
                    + "AND b.customerID.userID.userID = :userID";

            TypedQuery<Booking> query
                    = em.createQuery(
                            jpql,
                            Booking.class
                    );

            query.setParameter(
                    "bookingID",
                    bookingID.trim()
            );

            query.setParameter(
                    "userID",
                    userID
            );

            /*
             * Ensures the payment page does not display an old booking
             * status from EclipseLink's shared cache.
             */
            query.setHint(
                    "eclipselink.refresh",
                    true
            );

            return query.getSingleResult();

        } catch (NoResultException ex) {
            return null;
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Processes a simulated successful payment.
     *
     * Payment creation and booking confirmation are committed in the same
     * database transaction.
     */
    public Payment processPayment(
            String bookingID,
            String methodID,
            int userID) {

        validatePaymentRequest(
                bookingID,
                methodID
        );

        String normalizedBookingID
                = bookingID.trim();

        String normalizedMethodID
                = methodID.trim();

        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();

            /*
             * The project has one hotel. Locking H01 serializes payment
             * ID generation and follows the same lock order used by
             * BookingCheckoutService.
             */
            lockPaymentProcessing(em);

            Booking booking = findAndLockBooking(
                    em,
                    normalizedBookingID,
                    userID
            );

            if (booking == null) {
                throw new IllegalStateException(
                        "The requested booking was not found."
                );
            }

            /*
             * Check an existing successful payment before checking the
             * booking status. This gives a clearer response after a
             * customer double-clicks the payment button.
             */
            long paidPaymentCount
                    = countPaidPayments(
                            em,
                            booking
                    );

            if (paidPaymentCount > 0) {
                throw new IllegalStateException(
                        "This booking has already been paid."
                );
            }

            if (!"PENDING_PAYMENT".equals(
                    booking.getBookingStatus())) {

                throw new IllegalStateException(
                        "This booking is not awaiting payment."
                );
            }

            if (booking.getPaymentDeadline() == null) {
                throw new IllegalStateException(
                        "The payment deadline is unavailable."
                );
            }

            Date paymentTime = new Date();

            /*
             * Deadline is inclusive: payment is rejected when the
             * current time is equal to or later than the deadline.
             */
            if (!booking.getPaymentDeadline()
                    .after(paymentTime)) {

                booking.setBookingStatus(
                        "CANCELLED"
                );

                em.flush();
                em.getTransaction().commit();

                throw new IllegalStateException(
                        "The payment time limit has expired."
                );
            }

            Paymentmethod paymentmethod
                    = em.find(
                            Paymentmethod.class,
                            normalizedMethodID
                    );

            if (paymentmethod == null) {
                throw new IllegalStateException(
                        "The selected payment method does not exist."
                );
            }

            Payment payment = new Payment();

            payment.setPaymentID(
                    generatePaymentID(em)
            );

            payment.setBookingID(booking);
            payment.setMethodID(paymentmethod);
            payment.setPaymentTime(paymentTime);
            payment.setAmount(
                    booking.getTotalAmount()
            );
            payment.setStatus("PAID");

            payment.setTransactionCode(
                    generateTransactionCode()
            );

            em.persist(payment);

            /*
             * The booking becomes confirmed only when the payment is
             * persisted successfully in this same transaction.
             */
            booking.setBookingStatus(
                    "CONFIRMED"
            );

            /*
             * Booking is already managed because it was loaded by the
             * current EntityManager. Calling merge() is unnecessary.
             */
            em.flush();
            em.getTransaction().commit();

            return payment;

        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            if (ex instanceof LockTimeoutException
                    || ex instanceof PessimisticLockException) {

                throw new IllegalStateException(
                        "The payment system is busy. Please try again.",
                        ex
                );
            }

            if (ex instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) ex;
            }

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            throw new RuntimeException(
                    "Unable to process the payment.",
                    ex
            );

        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    /**
     * Uses the single HOTEL row as a short global payment lock.
     *
     * This prevents two different bookings from generating the same sequential
     * PaymentID.
     */
    private void lockPaymentProcessing(
            EntityManager em) {

        Map<String, Object> lockProperties
                = new HashMap<>();

        lockProperties.put(
                "jakarta.persistence.lock.timeout",
                PAYMENT_LOCK_TIMEOUT_MS
        );

        Hotel hotel = em.find(
                Hotel.class,
                "H01",
                LockModeType.PESSIMISTIC_WRITE,
                lockProperties
        );

        if (hotel == null) {
            throw new IllegalStateException(
                    "The hotel configuration is missing."
            );
        }
    }

    /**
     * Loads and locks the booking so that two payment requests cannot process
     * the same booking simultaneously.
     */
    private Booking findAndLockBooking(
            EntityManager em,
            String bookingID,
            int userID) {

        try {
            String jpql
                    = "SELECT b FROM Booking b "
                    + "WHERE b.bookingID = :bookingID "
                    + "AND b.customerID.userID.userID = :userID";

            TypedQuery<Booking> query
                    = em.createQuery(
                            jpql,
                            Booking.class
                    );

            query.setParameter(
                    "bookingID",
                    bookingID
            );

            query.setParameter(
                    "userID",
                    userID
            );

            query.setLockMode(
                    LockModeType.PESSIMISTIC_WRITE
            );

            query.setHint(
                    "jakarta.persistence.lock.timeout",
                    PAYMENT_LOCK_TIMEOUT_MS
            );

            query.setHint(
                    "eclipselink.refresh",
                    true
            );

            return query.getSingleResult();

        } catch (NoResultException ex) {
            return null;
        }
    }

    private long countPaidPayments(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT COUNT(p) FROM Payment p "
                + "WHERE p.bookingID = :booking "
                + "AND p.status = :status";

        TypedQuery<Long> query
                = em.createQuery(
                        jpql,
                        Long.class
                );

        query.setParameter(
                "booking",
                booking
        );

        query.setParameter(
                "status",
                "PAID"
        );

        Long result = query.getSingleResult();

        return result != null ? result : 0L;
    }

    /**
     * Payment IDs follow the PY0001 format used by the database seed.
     *
     * The H01 pessimistic lock is already held while this method runs, so two
     * transactions cannot generate the same ID.
     */
    private String generatePaymentID(
            EntityManager em) {

        String jpql
                = "SELECT p.paymentID FROM Payment p "
                + "WHERE p.paymentID LIKE :prefix "
                + "ORDER BY p.paymentID DESC";

        TypedQuery<String> query
                = em.createQuery(
                        jpql,
                        String.class
                );

        query.setParameter(
                "prefix",
                "PM%"
        );

        query.setMaxResults(1);

        List<String> results
                = query.getResultList();

        if (results.isEmpty()) {
            return "PM0001";
        }

        String latestID
                = results.get(0).trim();

        if (!latestID.matches("PM\\d{4}")) {
            throw new IllegalStateException(
                    "The existing payment ID format is invalid."
            );
        }

        int number = Integer.parseInt(
                latestID.substring(2)
        );

        if (number >= 9999) {
            throw new IllegalStateException(
                    "The payment ID limit has been reached."
            );
        }

        return String.format(
                "PM%04d",
                number + 1
        );
    }

    private String generateTransactionCode() {
        String randomPart
                = UUID.randomUUID()
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

        return "TXN-" + randomPart;
    }

    private void validatePaymentRequest(
            String bookingID,
            String methodID) {

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Booking ID is required."
            );
        }

        if (methodID == null
                || methodID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Payment method is required."
            );
        }
    }
}
