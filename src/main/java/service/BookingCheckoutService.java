package service;

import dto.BookingCart;
import dto.BookingCartItem;
import entity.Booking;
import entity.BookingDetail;
import entity.Customer;
import entity.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.NoResultException;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Persistence;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import entity.Hotel;
import jakarta.persistence.LockModeType;
import jakarta.persistence.LockTimeoutException;
import jakarta.persistence.PessimisticLockException;

public class BookingCheckoutService {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    private static final long PAYMENT_TIMEOUT_MINUTES
            = 15;

    private static final int CHECKOUT_LOCK_TIMEOUT_MS = 10000;

    public Booking createPendingBooking(
            BookingCart cart,
            int userID) {

        validateCart(cart);

        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();
            lockCheckout(em);

            Customer customer = findCustomerByUserID(
                    em,
                    userID
            );

            if (customer == null) {
                throw new IllegalStateException(
                        "No customer profile is associated with this account."
                );
            }

            Map<String, Long> availabilityMap
                    = loadAvailability(
                            em,
                            cart
                    );

            long numberOfNights
                    = cart.getNumberOfNights();

            BigDecimal totalAmount
                    = BigDecimal.ZERO;

            Map<String, RoomType> roomTypeMap
                    = new HashMap<>();

            Map<String, BigDecimal> subtotalMap
                    = new HashMap<>();

            /*
             * Recheck every room type and recalculate all prices
             * from the database.
             */
            for (BookingCartItem item : cart.getItems()) {
                RoomType roomType = em.find(
                        RoomType.class,
                        item.getRoomTypeID()
                );

                if (roomType == null) {
                    throw new IllegalStateException(
                            "Room type "
                            + item.getRoomTypeID()
                            + " no longer exists."
                    );
                }

                long availableRooms
                        = availabilityMap.getOrDefault(
                                item.getRoomTypeID(),
                                0L
                        );

                if (item.getQuantity() > availableRooms) {
                    throw new IllegalStateException(
                            "Only "
                            + availableRooms
                            + " "
                            + roomType.getTypeName()
                            + " room(s) are currently available."
                    );
                }

                int maximumGuests
                        = roomType.getCapacity()
                        * item.getQuantity();

                if (item.getGuestCount() > maximumGuests) {
                    throw new IllegalStateException(
                            roomType.getTypeName()
                            + " can accommodate a maximum of "
                            + maximumGuests
                            + " guest(s)."
                    );
                }

                BigDecimal subtotal
                        = roomType.getPrice()
                                .multiply(
                                        BigDecimal.valueOf(
                                                item.getQuantity()
                                        )
                                )
                                .multiply(
                                        BigDecimal.valueOf(
                                                numberOfNights
                                        )
                                );

                roomTypeMap.put(
                        item.getRoomTypeID(),
                        roomType
                );

                subtotalMap.put(
                        item.getRoomTypeID(),
                        subtotal
                );

                totalAmount = totalAmount.add(subtotal);
            }

            Booking booking = new Booking();

            booking.setBookingID(
                    generateBookingID(em)
            );

            Instant now = Instant.now();

            java.util.Date bookingTime = java.util.Date.from(now);

            java.util.Date paymentDeadline
                    = java.util.Date.from(
                            now.plus(
                                    PAYMENT_TIMEOUT_MINUTES,
                                    ChronoUnit.MINUTES
                            )
                    );

            booking.setBookingDate(bookingTime);
            booking.setPaymentDeadline(paymentDeadline);

            booking.setCheckInDate(
                    Date.valueOf(cart.getCheckInDate())
            );

            booking.setCheckOutDate(
                    Date.valueOf(cart.getCheckOutDate())
            );

            booking.setBookingStatus(
                    "PENDING_PAYMENT"
            );

            booking.setCustomerID(customer);
            booking.setTotalAmount(totalAmount);

            em.persist(booking);

            for (BookingCartItem item : cart.getItems()) {
                RoomType roomType = roomTypeMap.get(
                        item.getRoomTypeID()
                );

                BigDecimal subtotal = subtotalMap.get(
                        item.getRoomTypeID()
                );

                BookingDetail detail
                        = new BookingDetail();

                detail.setBookingID(booking);
                detail.setRoomTypeID(roomType);
                detail.setQuantity(item.getQuantity());
                detail.setGuestCount(item.getGuestCount());
                detail.setUnitPrice(roomType.getPrice());
                detail.setSubtotal(subtotal);

                em.persist(detail);
            }

            /*
             * Forces SQL execution before commit so database errors
             * are caught inside this transaction.
             */
            em.flush();

            em.getTransaction().commit();

            return booking;
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            if (ex instanceof LockTimeoutException
                    || ex instanceof PessimisticLockException) {

                throw new IllegalStateException(
                        "The booking system is busy. Please try again.",
                        ex
                );
            }

            throw new RuntimeException(
                    "Unable to create the booking.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private Customer findCustomerByUserID(
            EntityManager em,
            int userID) {

        try {
            String jpql
                    = "SELECT c FROM Customer c "
                    + "WHERE c.userID.userID = :userID";

            TypedQuery<Customer> query
                    = em.createQuery(
                            jpql,
                            Customer.class
                    );

            query.setParameter("userID", userID);

            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    private Map<String, Long> loadAvailability(
            EntityManager em,
            BookingCart cart) {

        StoredProcedureQuery query
                = em.createStoredProcedureQuery(
                        "SP_GET_ROOM_TYPE_AVAILABILITY"
                );

        query.registerStoredProcedureParameter(
                "CheckInDate",
                Date.class,
                ParameterMode.IN
        );

        query.registerStoredProcedureParameter(
                "CheckOutDate",
                Date.class,
                ParameterMode.IN
        );

        query.setParameter(
                "CheckInDate",
                Date.valueOf(cart.getCheckInDate())
        );

        query.setParameter(
                "CheckOutDate",
                Date.valueOf(cart.getCheckOutDate())
        );

        @SuppressWarnings("unchecked")
        List<Object[]> rows = query.getResultList();

        Map<String, Long> result = new HashMap<>();

        for (Object[] row : rows) {
            String roomTypeID = row[0] == null
                    ? null
                    : row[0].toString().trim();

            long availableRooms = row[7] == null
                    ? 0L
                    : ((Number) row[7]).longValue();

            if (roomTypeID != null) {
                result.put(
                        roomTypeID,
                        availableRooms
                );
            }
        }

        return result;
    }

    private String generateBookingID(
            EntityManager em) {

        String jpql
                = "SELECT b.bookingID FROM Booking b "
                + "ORDER BY b.bookingID DESC";

        TypedQuery<String> query
                = em.createQuery(jpql, String.class);

        query.setMaxResults(1);

        List<String> results
                = query.getResultList();

        if (results.isEmpty()) {
            return "BK0001";
        }

        String latestID = results.get(0).trim();

        if (!latestID.matches("BK\\d{4}")) {
            throw new IllegalStateException(
                    "The existing booking ID format is invalid."
            );
        }

        int number = Integer.parseInt(
                latestID.substring(2)
        );

        if (number >= 9999) {
            throw new IllegalStateException(
                    "The booking ID limit has been reached."
            );
        }

        return String.format(
                "BK%04d",
                number + 1
        );
    }

    private void validateCart(BookingCart cart) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException(
                    "Your booking cart is empty."
            );
        }

        if (!cart.hasValidDates()) {
            throw new IllegalArgumentException(
                    "The booking dates are invalid."
            );
        }

        if (cart.getNumberOfNights() <= 0) {
            throw new IllegalArgumentException(
                    "The booking must contain at least one night."
            );
        }
    }

    private void lockCheckout(EntityManager em) {
        Map<String, Object> lockProperties = new HashMap<>();

        lockProperties.put(
                "jakarta.persistence.lock.timeout",
                CHECKOUT_LOCK_TIMEOUT_MS
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
}
