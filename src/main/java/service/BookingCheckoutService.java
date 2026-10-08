package service;

import dao.BookingDAO;
import dto.BookingCartItem;
import dto.BookingWishList;
import entity.Booking;
import entity.BookingDetail;
import entity.Customer;
import entity.RoomType;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.sql.Date;
import java.sql.SQLException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import util.PersistenceManager;

public class BookingCheckoutService
{
    private static final long PAYMENT_TIMEOUT_MINUTES = 15;
    private static final int MAX_BOOKING_ID_ATTEMPTS = 5;

    public Booking createPendingBooking(BookingWishList cart, int userID)
    {
        return createPendingBooking(cart, userID, "FULL");
    }

    public Booking createPendingBooking(BookingWishList cart, int userID, String paymentOption)
    {
        validateCart(cart);
        if (userID <= 0)
        {
            throw new IllegalArgumentException("A valid authenticated account is required.");
        }
        String option = validatePaymentOption(paymentOption);
        Date checkInDate = Date.valueOf(cart.getCheckInDate());
        Date checkOutDate = Date.valueOf(cart.getCheckOutDate());
        Customer customer;
        List<BookingDetail> roomRequests;
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            customer = findCustomerByUserID(em, userID);
            if (customer == null)
            {
                throw new IllegalStateException("No customer profile is associated with this account.");
            }
            roomRequests = buildRoomRequests(em, cart);
        }
        Booking booking = new Booking();
        booking.setCustomerID(customer);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setPaymentOption(option);
        booking.setBookingStatus("PENDING_PAYMENT");
        booking.setPaymentDeadline(java.util.Date.from(Instant.now().plus(PAYMENT_TIMEOUT_MINUTES, ChronoUnit.MINUTES)));
        BookingDAO bookingDAO = new BookingDAO();
        for (int attempt = 1; attempt <= MAX_BOOKING_ID_ATTEMPTS; attempt++)
        {
            booking.setBookingID(generateBookingID());
            try
            {
                Booking created = bookingDAO.createBooking(booking, roomRequests, userID);
                if (created == null)
                {
                    throw new IllegalStateException("The booking was submitted, but its details could not be loaded. Check your bookings before trying again.");
                }
                return created;
            }
            catch (BookingDAO.BookingOperationException exception)
            {
                if (isBookingIDCollision(exception))
                {
                    if (attempt < MAX_BOOKING_ID_ATTEMPTS)
                    {
                        continue;
                    }
                    throw new IllegalStateException("The booking system is busy. Please try again.", exception);
                }
                throw translateBookingError(exception);
            }
        }
        throw new IllegalStateException("Unable to allocate a booking ID.");
    }

    private Customer findCustomerByUserID(EntityManager em, int userID)
    {
        TypedQuery<Customer> query = em.createQuery("SELECT c FROM Customer c WHERE c.userID.userID = :userID", Customer.class);
        query.setParameter("userID", userID);
        query.setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        query.setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.REFRESH);
        List<Customer> customers = query.setMaxResults(1).getResultList();
        return customers.isEmpty() ? null : customers.get(0);
    }

    private List<BookingDetail> buildRoomRequests(EntityManager em, BookingWishList cart)
    {
        List<BookingDetail> requests = new ArrayList<>();
        Set<String> roomTypeIDs = new HashSet<>();
        for (BookingCartItem item : cart.getItems())
        {
            if (item == null || item.getRoomTypeID() == null || item.getRoomTypeID().isBlank())
            {
                throw new IllegalArgumentException("Each booking item must contain a valid room type.");
            }
            String roomTypeID = item.getRoomTypeID().trim().toUpperCase(Locale.ROOT);
            if (roomTypeID.length() > 4)
            {
                throw new IllegalArgumentException("Invalid room type ID.");
            }
            Integer quantity = item.getQuantity();
            Integer guestCount = item.getGuestCount();
            if (quantity == null || guestCount == null || quantity <= 0 || guestCount <= 0)
            {
                throw new IllegalArgumentException("Room quantity and guest count must be positive.");
            }
            if (!roomTypeIDs.add(roomTypeID))
            {
                throw new IllegalArgumentException("Combine duplicate room types into one booking item.");
            }
            RoomType roomType = em.find(RoomType.class, roomTypeID);
            if (roomType == null)
            {
                throw new IllegalStateException("Room type " + roomTypeID + " no longer exists.");
            }
            BookingDetail detail = new BookingDetail();
            detail.setRoomTypeID(roomType);
            detail.setQuantity(quantity);
            detail.setGuestCount(guestCount);
            requests.add(detail);
        }
        if (requests.isEmpty())
        {
            throw new IllegalArgumentException("Your booking cart is empty.");
        }
        return requests;
    }

    private String generateBookingID()
    {
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            TypedQuery<String> query = em.createQuery("SELECT b.bookingID FROM Booking b ORDER BY b.bookingID DESC", String.class);
            query.setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
            List<String> results = query.setMaxResults(1).getResultList();
            if (results.isEmpty())
            {
                return "BK0001";
            }
            String latestID = results.get(0).trim();
            if (!latestID.matches("BK\\d{4}"))
            {
                throw new IllegalStateException("The existing booking ID format is invalid.");
            }
            int number = Integer.parseInt(latestID.substring(2));
            if (number >= 9999)
            {
                throw new IllegalStateException("The booking ID limit has been reached.");
            }
            return String.format(Locale.ROOT, "BK%04d", number + 1);
        }
    }

    private boolean isBookingIDCollision(BookingDAO.BookingOperationException exception)
    {
        Throwable cause = exception.getCause();
        while (cause != null)
        {
            if (cause instanceof SQLException)
            {
                for (SQLException sql = (SQLException) cause; sql != null; sql = sql.getNextException())
                {
                    String message = sql.getMessage();
                    if ((sql.getErrorCode() == 2601 || sql.getErrorCode() == 2627) && message != null && message.toUpperCase(Locale.ROOT).contains("'PK_BOOKING'"))
                    {
                        return true;
                    }
                }
            }
            cause = cause.getCause();
        }
        return false;
    }

    private IllegalStateException translateBookingError(BookingDAO.BookingOperationException exception)
    {
        String message;
        switch (exception.getSqlErrorCode())
        {
            case 51000:
                message = "The booking system is busy. Please try again.";
                break;
            case 51010:
                message = "No customer profile is associated with this account.";
                break;
            case 51011:
                message = "You are not authorized to create this booking.";
                break;
            case 51012:
                message = "Please select a 30% deposit or full payment.";
                break;
            case 51013:
                message = "The booking dates are invalid.";
                break;
            case 51014:
                message = "The payment deadline has expired. Please start checkout again.";
                break;
            case 51015:
                message = "Your booking cart is empty.";
                break;
            case 51016:
                message = "Your account is temporarily blocked from creating bookings. Please wait until the booking lock expires.";
                break;
            case 51017:
                message = "Please pay the deposit or full amount for your pending booking, or cancel it, before creating another booking.";
                break;
            case 51018:
                message = "A room type is invalid or the guest count exceeds its capacity.";
                break;
            case 51019:
                message = "Some requested rooms are no longer available for these dates. Please update your booking cart.";
                break;
            case 51020:
                message = "The booking amount must be positive.";
                break;
            default:
                message = "Unable to confirm the booking. Check your bookings before trying again.";
                break;
        }
        return new IllegalStateException(message, exception);
    }

    private String validatePaymentOption(String paymentOption)
    {
        String option = paymentOption == null ? "" : paymentOption.trim().toUpperCase(Locale.ROOT);
        if (!"DEPOSIT".equals(option) && !"FULL".equals(option))
        {
            throw new IllegalArgumentException("Please select a 30% deposit or full payment.");
        }
        return option;
    }

    private void validateCart(BookingWishList cart)
    {
        if (cart == null || cart.isEmpty() || cart.getItems() == null)
        {
            throw new IllegalArgumentException("Your booking cart is empty.");
        }
        if (cart.getCheckInDate() == null || cart.getCheckOutDate() == null || !cart.hasValidDates())
        {
            throw new IllegalArgumentException("The booking dates are invalid.");
        }
        if (cart.getNumberOfNights() <= 0)
        {
            throw new IllegalArgumentException("The booking must contain at least one night.");
        }
    }
}