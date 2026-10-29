package service;

import dao.BookingDAO;
import dao.WalletDAO;
import dto.WalletSummary;
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

    public String getVietQR(BigDecimal amount, Booking booking) {
        return new VietQrService().createUrl(amount, "Thanh toan " + booking.getBookingID().trim());
    }

    public String getVietQR(BigDecimal amount, String reference) {
        return new VietQrService().createUrl(amount, reference);
    }

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
        // Before arrival, clear room charges only. Extra services remain due at checkout.
        String amountKey = initial ? "initialRequiredAmount"
                : ("CHECKED_IN".equals(status) ? "remainingAmount" : "roomRemainingAmount");
        Object value = summary.get(amountKey);
        if (!(value instanceof BigDecimal) || ((BigDecimal) value).signum() <= 0)
        {
            throw new IllegalStateException("This booking has no outstanding payment.");
        }
        BigDecimal walletBalance = null;
        try
        {
            walletBalance = loadWalletBalance(userID);
        }
        catch (IllegalStateException exception)
        {
            LOGGER.log(Level.WARNING, "Unable to offer wallet payment.", exception);
        }
        return new PaymentRequest(bookingID.trim(), userID, type, (BigDecimal) value, walletBalance);
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

    // Customer VietQR only displays a QR. Recording a transfer requires staff verification.
    public Payment processPayment(PaymentRequest paymentRequest, String methodID, int userID)
    {
        return processPayment(paymentRequest, methodID, userID, false);
    }

    public Payment processPayment(PaymentRequest paymentRequest, String methodID, int userID, boolean useWallet)
    {
        return processPayment(paymentRequest, methodID, userID, useWallet, userID, false);
    }

    public Payment collectAtCounter(PaymentRequest paymentRequest, String methodID, int actorUserID,
            boolean useWallet, boolean walletAuthorized, String reference) {
        if (paymentRequest == null) throw new IllegalArgumentException("Reload the payment page.");
        if (!isReceptionist(actorUserID)) {
            throw new SecurityException("Only receptionists can collect counter payments.");
        }
        if (useWallet && !walletAuthorized) {
            throw new IllegalArgumentException("The customer must authorize wallet use.");
        }
        if (!java.util.Set.of("PT03", "PT02", "PT01").contains(methodID)) {
            throw new IllegalArgumentException("Choose Cash, Internal Wallet or VietQR.");
        }
        if (useWallet && paymentRequest.getOfferedWalletAmount().compareTo(paymentRequest.getAmount()) >= 0) {
            methodID = "PT02";
        }
        synchronized (paymentRequest) {
            if (paymentRequest.methodID == null) {
                paymentRequest.transactionCode = "PT01".equals(methodID) ? reference : "COUNTER-" + UUID.randomUUID();
                paymentRequest.transactionID = "PT01".equals(methodID) ? reference : null;
            } else if ("PT01".equals(methodID) && !paymentRequest.transactionCode.equals(reference)) {
                throw new IllegalArgumentException("Retry with the same bank reference or reload the page.");
            }
            if (paymentRequest.transactionCode == null || paymentRequest.transactionCode.isBlank()
                    || paymentRequest.transactionCode.length() > 100) {
                throw new IllegalArgumentException("Enter a bank transaction reference (maximum 100 characters).");
            }
            return processPayment(paymentRequest, methodID, paymentRequest.getUserID(), useWallet, actorUserID, walletAuthorized);
        }
    }

    private Payment processPayment(PaymentRequest paymentRequest, String methodID, int userID,
            boolean useWallet, int actorUserID, boolean walletAuthorized) {
        if (paymentRequest == null || paymentRequest.getUserID() != userID || userID <= 0)
        {
            throw new IllegalArgumentException("The payment request is invalid. Reload the payment page.");
        }
        String selectedMethod = methodID == null ? "" : methodID.trim();
        BigDecimal walletAmount = useWallet ? paymentRequest.getOfferedWalletAmount() : BigDecimal.ZERO;
        if (useWallet && walletAmount.signum() <= 0)
        {
            throw new IllegalArgumentException("No wallet balance is available. Reload the page or pay using an external method.");
        }
        if (useWallet && walletAmount.compareTo(paymentRequest.amount) == 0)
        {
            selectedMethod = "PT02";
        }
        if ("PT02".equals(selectedMethod))
        {
            if (paymentRequest.getOfferedWalletAmount().compareTo(paymentRequest.amount) < 0)
            {
                throw new IllegalArgumentException("Your wallet cannot cover the full payment. Select an external method and use your wallet for part of it.");
            }
            walletAmount = paymentRequest.amount;
        }
        if (!Set.of("PT01", "PT02", "PT03").contains(selectedMethod))
        {
            throw new IllegalArgumentException("Please select a valid payment method.");
        }
        if ("PT01".equals(selectedMethod) && actorUserID == userID) {
            throw new IllegalArgumentException("VietQR is awaiting verification. Displaying or scanning the QR does not confirm a payment.");
        }
        if ("PT03".equals(selectedMethod) && actorUserID == userID)
        {
            throw new IllegalArgumentException("Cash payments must be recorded by staff.");
        }
        synchronized (paymentRequest)
        {
            if (paymentRequest.methodID != null && (!paymentRequest.methodID.equals(selectedMethod) || paymentRequest.walletAmount.compareTo(walletAmount) != 0))
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
            Paymentmethod method = findMethod(selectedMethod);
            if (method == null)
            {
                throw new IllegalArgumentException("The selected payment method does not exist.");
            }
            paymentRequest.actorUserID = actorUserID;
            paymentRequest.walletAuthorizedBy = walletAuthorized ? userID : null;
            paymentRequest.methodID = selectedMethod;
            paymentRequest.walletAmount = walletAmount;
            try (Connection connection = openConnection())
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
                    execute(connection, "EXEC dbo.SP_RECORD_PAYMENT @PaymentID = ?, @BookingID = ?, @ActorUserID = ?, @PaymentType = ?, @Amount = ?, @MethodID = ?, @TransactionCode = ?, @WalletAmount = ?, @WalletAuthorizedBy = ?, @TransactionID = ?;", paymentRequest);
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
                    clearCache();
                }
                catch (RuntimeException exception)
                {
                    LOGGER.log(Level.WARNING, "Unable to clear the payment cache.", exception);
                }
            }
        }
    }

    protected BigDecimal loadWalletBalance(int userID) {
        WalletSummary wallet = new WalletDAO().getWalletByUserID(userID);
        return wallet == null ? null : wallet.getBalance();
    }

    protected boolean isReceptionist(int userID) {
        return new dao.EmployeeDAO().isReceptionistByUserId(userID);
    }

    protected Paymentmethod findMethod(String methodID) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return em.find(Paymentmethod.class, methodID);
        }
    }

    protected Connection openConnection() throws SQLException { return PersistenceManager.openConnection(); }

    protected void clearCache() { PersistenceManager.getEMF().getCache().evictAll(); }

    private boolean isIDUsedByAnotherRequest(Connection connection, PaymentRequest request) throws SQLException
    {
        try (PreparedStatement statement = connection.prepareStatement("SELECT BookingID, PaymentType, Amount, WalletAmount, MethodID, TransactionCode FROM dbo.PAYMENT WHERE PaymentID = ?"))
        {
            statement.setString(1, request.paymentID);
            try (ResultSet result = statement.executeQuery())
            {
                if (!result.next())
                {
                    return false;
                }
                String transactionCode = "PT02".equals(request.methodID) ? "WALLET-" + request.paymentID : request.transactionCode;
                return !request.bookingID.equals(result.getString("BookingID").trim()) || !request.paymentType.equals(result.getString("PaymentType")) || request.amount.compareTo(result.getBigDecimal("Amount")) != 0 || request.walletAmount.compareTo(result.getBigDecimal("WalletAmount")) != 0 || !request.methodID.equals(result.getString("MethodID")) || !transactionCode.equals(result.getString("TransactionCode"));
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
                statement.setInt(3, request.actorUserID);
                statement.setString(4, request.paymentType);
                statement.setBigDecimal(5, request.amount);
                statement.setString(6, request.methodID);
                statement.setString(7, request.transactionCode);
                statement.setBigDecimal(8, request.walletAmount);
                if (request.walletAuthorizedBy == null) statement.setNull(9, java.sql.Types.INTEGER);
                else statement.setInt(9, request.walletAuthorizedBy);
                statement.setString(10, request.transactionID);
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
        try (PreparedStatement statement = connection.prepareStatement("SELECT PaymentTime, Amount, WalletAmount, Status, TransactionCode, TransactionID FROM dbo.PAYMENT WHERE PaymentID = ? AND BookingID = ?"))
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
                payment.setWalletAmount(result.getBigDecimal("WalletAmount"));
                payment.setPaymentType(request.paymentType);
                payment.setStatus(result.getString("Status"));
                payment.setTransactionCode(result.getString("TransactionCode"));
                payment.setTransactionID(result.getString("TransactionID"));
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
                return "Your wallet balance has changed or is insufficient. Reload the payment page to review the amounts.";
            case 51047:
                return "The wallet contribution is invalid. Reload the payment page.";
            case 51210:
                return "A verified bank transaction reference is required for VietQR.";
            case 51048:
                return "Only the wallet owner can authorize a wallet contribution.";
            default:
                return "Payment could not be confirmed. Check your booking or retry the same request.";
        }
    }

    public static final class PaymentRequest implements Serializable
    {
        private static final long serialVersionUID = 2L;
        private final String bookingID;
        private final int userID;
        private final String paymentType;
        private final BigDecimal amount;
        private final BigDecimal walletBalance;
        private String transactionCode = "PAYMENT-" + UUID.randomUUID();
        private int actorUserID;
        private Integer walletAuthorizedBy;
        private String paymentID;
        private String transactionID;
        private final String transferReference = "HBMS" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
        private String methodID;
        private BigDecimal walletAmount;

        private PaymentRequest(String bookingID, int userID, String paymentType, BigDecimal amount, BigDecimal walletBalance)
        {
            this.bookingID = bookingID;
            this.userID = userID;
            this.paymentType = paymentType;
            this.amount = amount;
            this.walletBalance = walletBalance;
        }

        public String getTransferReference() { return transferReference; }

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

        public BigDecimal getWalletBalance()
        {
            return walletBalance;
        }

        public BigDecimal getOfferedWalletAmount()
        {
            return walletBalance == null ? BigDecimal.ZERO : walletBalance.max(BigDecimal.ZERO).min(amount);
        }
    }
}
