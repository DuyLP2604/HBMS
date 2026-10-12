package service;

import dao.BookingDAO;
import entity.Booking;
import entity.Payment;
import entity.Paymentmethod;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.io.Serializable;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.PersistenceManager;

public class PaymentService
{
    private static final Logger LOGGER = Logger.getLogger(PaymentService.class.getName());

    public Booking getBookingForPayment(String bookingID, int userID)
    {
        if (bookingID == null || bookingID.isBlank() || userID <= 0)
        {
            return null;
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            TypedQuery<Booking> query = em.createQuery("SELECT b FROM Booking b JOIN FETCH b.customerID c JOIN FETCH c.userID WHERE b.bookingID = :bookingID AND c.userID.userID = :userID", Booking.class);
            query.setParameter("bookingID", bookingID.trim());
            query.setParameter("userID", userID);
            query.setHint("jakarta.persistence.cache.retrieveMode", jakarta.persistence.CacheRetrieveMode.BYPASS);
            query.setHint("jakarta.persistence.cache.storeMode", jakarta.persistence.CacheStoreMode.REFRESH);
            List<Booking> bookings = query.setMaxResults(1).getResultList();
            return bookings.isEmpty() ? null : bookings.get(0);
        }
    }

    public Map<String, Object> getPaymentSummary(String bookingID, int userID)
    {
        Booking booking = getBookingForPayment(bookingID, userID);
        if (booking == null)
        {
            throw new IllegalArgumentException("The requested booking was not found.");
        }
        BookingDAO dao = new BookingDAO();
        if ("PENDING_PAYMENT".equals(booking.getBookingStatus()))
        {
            dao.expirePendingBookings(booking.getCustomerID().getCustomerID());
        }
        Map<String, Object> summary = dao.getPaymentSummary(booking.getBookingID());
        if (summary.isEmpty())
        {
            throw new IllegalStateException("The booking payment summary is unavailable.");
        }
        return summary;
    }

    public PaymentRequest createPaymentRequest(String bookingID, int userID)
    {
        Map<String, Object> summary = getPaymentSummary(bookingID, userID);
        String status = String.valueOf(summary.get("bookingStatus"));
        boolean initial = summary.get("firstPaidAt") == null;
        if ((initial && !"PENDING_PAYMENT".equals(status)) || (!initial && !Set.of("CONFIRMED", "ASSIGNED", "CHECKED_IN").contains(status)))
        {
            throw new IllegalStateException("This booking no longer accepts payments.");
        }
        String type = initial ? String.valueOf(summary.get("paymentOption")) : "BALANCE";
        if (!Set.of("DEPOSIT", "FULL", "BALANCE").contains(type))
        {
            throw new IllegalStateException("The booking payment option is invalid.");
        }
        Object value = summary.get(initial ? "initialRequiredAmount" : "remainingAmount");
        if (!(value instanceof BigDecimal) || ((BigDecimal) value).signum() <= 0)
        {
            throw new IllegalStateException("This booking has no outstanding payment.");
        }
        return new PaymentRequest(bookingID.trim(), userID, type, (BigDecimal) value);
    }

    // Compatibility entry point for initial payments. Balance payments require a saved PaymentRequest.
    public Payment processPayment(String bookingID, String methodID, int userID)
    {
        PaymentRequest paymentRequest = createPaymentRequest(bookingID, userID);
        if ("BALANCE".equals(paymentRequest.getPaymentType()))
        {
            throw new IllegalStateException("Open the payment page to pay the remaining balance.");
        }
        return processPayment(paymentRequest, methodID, userID);
    }

    // External methods are SIMULATED, matching the existing project. Replace this with verified provider callbacks for real payments.
    public Payment processPayment(PaymentRequest paymentRequest, String methodID, int userID)
    {
        if (paymentRequest == null || paymentRequest.getUserID() != userID || userID <= 0)
        {
            throw new IllegalArgumentException("The payment request is invalid. Reload the payment page.");
        }
        String selectedMethod = methodID == null ? "" : methodID.trim();
        if (selectedMethod.isEmpty() || selectedMethod.length() > 4)
        {
            throw new IllegalArgumentException("Please select a valid payment method.");
        }
        if ("PT01".equals(selectedMethod))
        {
            throw new IllegalArgumentException("Cash payments must be recorded by staff.");
        }
        synchronized (paymentRequest)
        {
            if (paymentRequest.methodID != null && !paymentRequest.methodID.equals(selectedMethod))
            {
                throw new IllegalStateException("This request already uses another payment method. Reload the payment page to change it.");
            }
            Booking booking = getBookingForPayment(paymentRequest.getBookingID(), userID);
            if (booking == null)
            {
                throw new IllegalArgumentException("The requested booking was not found.");
            }
            if ("PENDING_PAYMENT".equals(booking.getBookingStatus()))
            {
                new BookingDAO().expirePendingBookings(booking.getCustomerID().getCustomerID());
            }
            Paymentmethod method;
            try (EntityManager em = PersistenceManager.createEntityManager())
            {
                method = em.find(Paymentmethod.class, selectedMethod);
            }
            if (method == null)
            {
                throw new IllegalArgumentException("The selected payment method does not exist.");
            }
            paymentRequest.methodID = selectedMethod;
            try (Connection connection = PersistenceManager.openConnection())
            {
                connection.setAutoCommit(false);
                try
                {
                    // Hold the SAME SQL application lock as all booking/wallet procedures while allocating PMxxxx.
                    execute(connection, "SELECT TOP (1) UserID FROM dbo.USERS; DECLARE @LockResult int; EXEC @LockResult = sys.sp_getapplock @Resource = N'HBMS:booking-wallet-write', @LockMode = 'Exclusive', @LockOwner = 'Transaction', @LockTimeout = 10000; IF @LockResult < 0 THROW 51000, 'System busy. Please retry the request.', 1;", null);
                    if (paymentRequest.paymentID == null || isIDUsedByAnotherRequest(connection, paymentRequest))
                    {
                        paymentRequest.paymentID = generatePaymentID(connection);
                    }
                    execute(connection, "EXEC dbo.SP_RECORD_PAYMENT @PaymentID = ?, @BookingID = ?, @ActorUserID = ?, @PaymentType = ?, @Amount = ?, @MethodID = ?, @TransactionCode = ?;", paymentRequest);
                    Payment payment = readPayment(connection, paymentRequest, booking, method);
                    connection.commit();
                    return payment;
                }
                catch (SQLException | RuntimeException exception)
                {
                    try
                    {
                        connection.rollback();
                    }
                    catch (SQLException rollbackError)
                    {
                        exception.addSuppressed(rollbackError);
                    }
                    throw exception;
                }
            }
            catch (SQLException exception)
            {
                throw new IllegalStateException(paymentError(exception), exception);
            }
            finally
            {
                try
                {
                    PersistenceManager.getEMF().getCache().evictAll();
                }
                catch (RuntimeException exception)
                {
                    LOGGER.log(Level.WARNING, "Unable to clear the payment cache.", exception);
                }
            }
        }
    }

    private boolean isIDUsedByAnotherRequest(Connection connection, PaymentRequest request) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("SELECT BookingID, PaymentType, Amount, MethodID, TransactionCode FROM dbo.PAYMENT WHERE PaymentID = ?"))
        {
            statement.setString(1, request.paymentID);
            try (ResultSet result = statement.executeQuery())
            {
                if (!result.next())
                {
                    return false;
                }
                String transactionCode = "PT09".equals(request.methodID) ? "WALLET-" + request.paymentID : request.transactionCode;
                return !request.bookingID.equals(result.getString("BookingID").trim()) || !request.paymentType.equals(result.getString("PaymentType")) || request.amount.compareTo(result.getBigDecimal("Amount")) != 0 || !request.methodID.equals(result.getString("MethodID")) || !transactionCode.equals(result.getString("TransactionCode"));
            }
        }
    }

    private String generatePaymentID(Connection connection) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("SELECT COALESCE(MAX(TRY_CONVERT(int, SUBSTRING(PaymentID, 3, 4))), 0) FROM dbo.PAYMENT WHERE PaymentID LIKE 'PM[0-9][0-9][0-9][0-9]'" ); ResultSet result = statement.executeQuery())
        {
            if (!result.next() || result.getInt(1) >= 9999)
            {
                throw new IllegalStateException("The payment ID limit has been reached.");
            }
            return String.format(Locale.ROOT, "PM%04d", result.getInt(1) + 1);
        }
    }

    private void execute(Connection connection, String sql, PaymentRequest request) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            if (request != null)
            {
                statement.setString(1, request.paymentID);
                statement.setString(2, request.bookingID);
                statement.setInt(3, request.userID);
                statement.setString(4, request.paymentType);
                statement.setBigDecimal(5, request.amount);
                statement.setString(6, request.methodID);
                statement.setString(7, request.transactionCode);
            }
            boolean hasResult = statement.execute();
            while (true)
            {
                if (hasResult)
                {
                    try (ResultSet result = statement.getResultSet())
                    {
                        while (result.next())
                        {
                            // Consume procedure results so errors after earlier result sets are not missed.
                        }
                    }
                }
                else if (statement.getUpdateCount() == -1)
                {
                    break;
                }
                hasResult = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
            }
        }
    }

    private Payment readPayment(Connection connection, PaymentRequest request, Booking booking, Paymentmethod method) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("SELECT PaymentTime, Amount, Status, TransactionCode FROM dbo.PAYMENT WHERE PaymentID = ? AND BookingID = ?"))
        {
            statement.setString(1, request.paymentID);
            statement.setString(2, request.bookingID);
            try (ResultSet result = statement.executeQuery())
            {
                if (!result.next())
                {
                    throw new IllegalStateException("The payment result could not be confirmed.");
                }
                Payment payment = new Payment();
                payment.setPaymentID(request.paymentID);
                payment.setBookingID(booking);
                payment.setMethodID(method);
                payment.setPaymentTime(result.getTimestamp("PaymentTime"));
                payment.setAmount(result.getBigDecimal("Amount"));
                payment.setStatus(result.getString("Status"));
                payment.setTransactionCode(result.getString("TransactionCode"));
                return payment;
            }
        }
    }

    private String paymentError(SQLException exception)
    {
        switch (exception.getErrorCode())
        {
            case 51000:
                return "The payment system is busy. Retry the same payment request.";
            case 51030:
            case 51031:
                return "The requested booking was not found or is not yours.";
            case 51034:
                return "The selected payment method does not exist.";
            case 51035:
                return "Cash payments must be recorded by staff.";
            case 51039:
                return "This booking no longer accepts payments.";
            case 51040:
                return "The payment deadline has expired.";
            case 51042:
            case 51043:
            case 51044:
            case 51045:
                return "The booking amount or payment state has changed. Reload the payment page.";
            case 51046:
                return "Your internal wallet balance is insufficient.";
            default:
                return "Payment could not be confirmed. Check your booking or retry the same request.";
        }
    }

    public static final class PaymentRequest implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final String bookingID;
        private final int userID;
        private final String paymentType;
        private final BigDecimal amount;
        private final String transactionCode = "SIM-" + UUID.randomUUID();
        private String paymentID;
        private String methodID;

        private PaymentRequest(String bookingID, int userID, String paymentType, BigDecimal amount)
        {
            this.bookingID = bookingID;
            this.userID = userID;
            this.paymentType = paymentType;
            this.amount = amount;
        }

        public String getBookingID()
        {
            return bookingID;
        }

        public int getUserID()
        {
            return userID;
        }

        public String getPaymentType()
        {
            return paymentType;
        }

        public BigDecimal getAmount()
        {
            return amount;
        }
    }
}
