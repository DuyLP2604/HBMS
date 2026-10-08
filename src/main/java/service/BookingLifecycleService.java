package service;

import dao.BookingDAO;
import entity.Booking;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.PersistenceManager;

public class BookingLifecycleService
{
    private static final Logger LOGGER = Logger.getLogger(BookingLifecycleService.class.getName());

    public List<Booking> getOperationalBookings()
    {
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT b FROM Booking b JOIN FETCH b.customerID WHERE b.bookingStatus IN :statuses ORDER BY b.checkInDate ASC, b.bookingDate ASC";
            TypedQuery<Booking> query = em.createQuery(jpql, Booking.class);
            query.setParameter("statuses", Arrays.asList("CONFIRMED", "ASSIGNED", "CHECKED_IN"));
            query.setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
            query.setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.REFRESH);
            return query.getResultList();
        }
    }

    public void checkIn(String bookingID, int actorUserID)
    {
        updateStayStatus(bookingID, "CHECKED_IN", actorUserID);
    }

    public void checkOut(String bookingID, int actorUserID)
    {
        updateStayStatus(bookingID, "CHECKED_OUT", actorUserID);
    }

    @Deprecated
    public void checkIn(String bookingID)
    {
        throw new IllegalStateException("Pass the authenticated staff UserID to checkIn(bookingID, userID).");
    }

    @Deprecated
    public void checkOut(String bookingID)
    {
        throw new IllegalStateException("Pass the authenticated staff UserID to checkOut(bookingID, userID).");
    }

    public void cancelByCustomer(String bookingID, int userID)
    {
        cancelByCustomerWithResult(bookingID, userID);
    }

    public Map<String, Object> cancelByCustomerWithResult(String bookingID, int userID)
    {
        String id = requireBookingID(bookingID);
        requireUserID(userID);
        BookingDAO dao = new BookingDAO();
        Booking booking = dao.getByIdWithDetails(id);
        if (!isOwnedBy(booking, userID))
        {
            throw new IllegalArgumentException("The requested booking was not found or is not yours.");
        }
        boolean alreadyCancelled = "CANCELLED".equals(booking.getBookingStatus());
        try
        {
            // SQL handles the refund, wallet credit and cancellation counters in one transaction.
            dao.cancelBooking(id, userID, "CUSTOMER_REQUEST");
        }
        catch (BookingDAO.BookingOperationException exception)
        {
            if (exception.getSqlErrorCode() >= 51000 && exception.getSqlErrorCode() <= 51007)
            {
                throw new IllegalStateException(operationError(exception.getSqlErrorCode()), exception);
            }
            if (!isCancellationConfirmed(dao, id, userID))
            {
                throw new IllegalStateException("Cancellation could not be confirmed. Check your booking before trying again.", exception);
            }
        }
        catch (RuntimeException exception)
        {
            if (!isCancellationConfirmed(dao, id, userID))
            {
                throw new IllegalStateException("Cancellation could not be confirmed. Check your booking before trying again.", exception);
            }
        }
        Map<String, Object> summary = Collections.emptyMap();
        Map<String, Object> access = Collections.emptyMap();
        try
        {
            summary = dao.getPaymentSummary(id);
        }
        catch (RuntimeException exception)
        {
            LOGGER.log(Level.WARNING, "Booking was cancelled, but refund details could not be loaded.", exception);
        }
        try
        {
            access = dao.getBookingAccess(userID);
        }
        catch (RuntimeException exception)
        {
            LOGGER.log(Level.WARNING, "Booking was cancelled, but booking access details could not be loaded.", exception);
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("bookingID", id);
        result.put("alreadyCancelled", alreadyCancelled);
        result.put("bookingSummary", summary);
        result.put("bookingAccess", access);
        result.put("message", cancellationMessage(summary, access, alreadyCancelled));
        return result;
    }

    private void updateStayStatus(String bookingID, String status, int actorUserID)
    {
        String id = requireBookingID(bookingID);
        requireUserID(actorUserID);
        try
        {
            // SQL checks staff role, assigned room counts and full settlement before check-out.
            new BookingDAO().updateStatus(id, status, actorUserID);
        }
        catch (BookingDAO.BookingOperationException exception)
        {
            throw new IllegalStateException(operationError(exception.getSqlErrorCode()), exception);
        }
    }

    private boolean isCancellationConfirmed(BookingDAO dao, String bookingID, int userID)
    {
        try
        {
            Booking booking = dao.getByIdWithDetails(bookingID);
            return isOwnedBy(booking, userID) && "CANCELLED".equals(booking.getBookingStatus());
        }
        catch (RuntimeException exception)
        {
            LOGGER.log(Level.WARNING, "Unable to confirm the booking cancellation state.", exception);
            return false;
        }
    }

    private boolean isOwnedBy(Booking booking, int userID)
    {
        return booking != null && booking.getCustomerID() != null && booking.getCustomerID().getUserID() != null && Integer.valueOf(userID).equals(booking.getCustomerID().getUserID().getUserID());
    }

    private String cancellationMessage(Map<String, Object> summary, Map<String, Object> access, boolean alreadyCancelled)
    {
        StringBuilder message = new StringBuilder(alreadyCancelled ? "Your booking was already cancelled." : "Your booking was cancelled successfully.");
        if (summary.isEmpty())
        {
            message.append(" Check your booking and wallet for refund details.");
        }
        else
        {
            Object refunded = summary.get("refundedAmount");
            Object paid = summary.get("totalPaidAmount");
            if (refunded instanceof BigDecimal && ((BigDecimal) refunded).signum() > 0)
            {
                String amount = NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(refunded);
                message.append(" A refund of ").append(amount).append(" VND is recorded in your system wallet.");
            }
            else if ("NO_REFUND".equals(summary.get("paymentStatus")))
            {
                message.append(" No refund applies under the 24-hour refund policy.");
            }
            else if (paid instanceof BigDecimal && ((BigDecimal) paid).signum() == 0)
            {
                message.append(" No payment was collected, so there is no amount to refund.");
            }
            else
            {
                message.append(" Check your booking and wallet for refund details.");
            }
        }
        Object lockedUntil = access.get("bookingLockedUntil");
        if (lockedUntil instanceof Date)
        {
            message.append(" Creating new bookings is temporarily locked until ").append(new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format((Date) lockedUntil)).append(".");
        }
        return message.toString();
    }

    private String requireBookingID(String bookingID)
    {
        if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6)
        {
            throw new IllegalArgumentException("A valid booking ID is required.");
        }
        return bookingID.trim();
    }

    private void requireUserID(int userID)
    {
        if (userID <= 0)
        {
            throw new IllegalArgumentException("An authenticated session UserID is required.");
        }
    }

    private String operationError(int code)
    {
        switch (code)
        {
            case 51000:
                return "The booking system is busy. Please try again.";
            case 51001:
            case 51003:
            case 51061:
                return "The requested booking was not found or you are not authorized.";
            case 51004:
            case 51060:
                return "Only staff or admin can perform this action.";
            case 51006:
                return "This booking can no longer be cancelled after check-in.";
            case 51007:
                return "Your wallet is unavailable. The cancellation was not completed.";
            case 51063:
                return "The current booking status does not allow this action.";
            case 51064:
                return "Assign all required rooms before check-in.";
            case 51065:
                return "Pay the remaining balance before check-out.";
            default:
                return "The booking operation could not be completed. Check your booking before trying again.";
        }
    }
}