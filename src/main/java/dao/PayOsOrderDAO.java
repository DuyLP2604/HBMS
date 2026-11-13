package dao;

import dto.PayOsOrder;
import java.math.BigDecimal;
import java.sql.*;
import util.PersistenceManager;
import util.CounterPaymentOperation;

/** Stores PayOS orders and confirms transfers through SP_CONFIRM_PAYOS_ORDER. */
public class PayOsOrderDAO {

    public void insert(PayOsOrder o, String paymentLinkId) throws SQLException {
        try (Connection c = PersistenceManager.openConnection(); PreparedStatement ps = c.prepareStatement("INSERT INTO dbo.PAYOS_ORDER (OrderCode, BookingID, UserID, PaymentType, Amount, WalletAmount, ExternalAmount, PaymentLinkId) VALUES (?,?,?,?,?,?,?,?)")) {
            ps.setLong(1, o.orderCode);
            ps.setString(2, o.bookingID);
            ps.setInt(3, o.userID);
            ps.setString(4, o.paymentType);
            ps.setBigDecimal(5, o.amount);
            ps.setBigDecimal(6, o.walletAmount);
            ps.setBigDecimal(7, o.externalAmount);
            ps.setString(8, paymentLinkId);
            ps.executeUpdate();
        }
    }

    /** Load a PayOS order by its gateway order code. */
    public PayOsOrder find(long orderCode) throws SQLException {
        try (Connection c = PersistenceManager.openConnection(); PreparedStatement ps = c.prepareStatement("SELECT BookingID, UserID, PaymentType, Amount, WalletAmount, ExternalAmount, Status, Reference, PaymentID FROM dbo.PAYOS_ORDER WHERE OrderCode = ?")) {
            ps.setLong(1, orderCode);
            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) {
                    return null;
                }
                PayOsOrder o = new PayOsOrder();
                o.orderCode = orderCode;
                o.bookingID = r.getString("BookingID").trim();
                o.userID = r.getInt("UserID");
                o.paymentType = r.getString("PaymentType");
                o.amount = r.getBigDecimal("Amount");
                o.walletAmount = r.getBigDecimal("WalletAmount");
                o.externalAmount = r.getBigDecimal("ExternalAmount");
                o.status = r.getString("Status");
                o.reference = r.getString("Reference");
                o.paymentID = r.getString("PaymentID");
                return o;
            }
        }
    }

    public PayOsOrder findCheckInOrder(String bookingID, Long orderCode) throws SQLException {
        try (Connection c = PersistenceManager.openConnection()) {
            return findCheckInOrder(c, bookingID, orderCode);
        }
    }

    public PayOsOrder findCheckInOrder(Connection c, String bookingID, Long orderCode) throws SQLException {
        return findCounterOrder(c, bookingID, orderCode, "o.CheckInStaffUserID IS NOT NULL",
                "b.BookingStatus <> 'ASSIGNED' OR b.PaymentOption <> 'DEPOSIT' OR r.RoomRemainingAmount <> o.ExternalAmount");
    }

    public PayOsOrder findInitialCounterOrder(String bookingID, Long orderCode) throws SQLException {
        try (Connection c = PersistenceManager.openConnection()) {
            return findInitialCounterOrder(c, bookingID, orderCode);
        }
    }

    public PayOsOrder findInitialCounterOrder(Connection c, String bookingID, Long orderCode) throws SQLException {
        return findCounterOrder(c, bookingID, orderCode, "o.CheckInStaffUserID IS NULL AND o.CheckInQrCode IS NOT NULL "
                + "AND o.PaymentType IN ('DEPOSIT', 'FULL')",
                "b.BookingStatus <> 'PENDING_PAYMENT' OR b.FirstPaidAt IS NOT NULL "
                + "OR b.PaymentOption <> o.PaymentType OR b.InitialRequiredAmount <> o.ExternalAmount");
    }

    private PayOsOrder findCounterOrder(Connection c, String bookingID, Long orderCode, String stage, String balanceChanged)
            throws SQLException {
        String filter = orderCode == null
                ? "AND ((o.Status <> 'PAID' AND o.CheckInExpiresAt > ?) "
                        + "OR (o.Status = 'PAID' AND o.PaymentType IN ('DEPOSIT', 'FULL') AND b.FirstPaidAt IS NOT NULL)) "
                        + "ORDER BY o.CreatedAt DESC, o.OrderCode DESC"
                : "AND o.OrderCode = ?";
        try (PreparedStatement ps = c.prepareStatement("SELECT TOP (1) o.OrderCode, o.PaymentID, o.ExternalAmount, "
                + "CASE WHEN o.Status IN ('PENDING', 'PROCESSING') AND (" + balanceChanged + ") "
                + "THEN 'FAILED' ELSE o.Status END AS Status, "
                + "o.CheckInExpiresAt, o.CheckInQrCode, o.CheckInCheckoutUrl FROM dbo.PAYOS_ORDER o "
                + "JOIN dbo.V_BOOKING_PAYMENT_SUMMARY b ON b.BookingID = o.BookingID "
                + "JOIN dbo.V_BOOKING_ROOM_BALANCE r ON r.BookingID = o.BookingID "
                + "WHERE o.BookingID = ? AND " + stage + " " + filter)) {
            ps.setString(1, bookingID);
            ps.setLong(2, orderCode == null ? java.time.Instant.now().getEpochSecond() : orderCode);
            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) return null;
                PayOsOrder order = new PayOsOrder();
                order.bookingID = bookingID;
                order.orderCode = r.getLong("OrderCode");
                order.status = r.getString("Status");
                order.paymentID = r.getString("PaymentID");
                order.externalAmount = r.getBigDecimal("ExternalAmount");
                order.checkInExpiresAt = r.getLong("CheckInExpiresAt");
                order.checkInQrCode = r.getString("CheckInQrCode");
                order.checkInCheckoutUrl = r.getString("CheckInCheckoutUrl");
                return order;
            }
        }
    }

    public boolean hasOpenPaymentOrder(Connection c, String bookingID, String paymentType, boolean includeOnline) throws SQLException {
        String source = includeOnline ? "" : "AND CheckInQrCode IS NOT NULL ";
        try (PreparedStatement ps = c.prepareStatement("SELECT TOP (1) 1 FROM dbo.PAYOS_ORDER "
                + "WHERE BookingID = ? AND PaymentType = ? AND CheckInStaffUserID IS NULL "
                + source + "AND Status IN ('PENDING', 'PROCESSING', 'FAILED') "
                + "AND (CheckInExpiresAt > ? OR (CheckInExpiresAt IS NULL "
                + "AND CreatedAt > DATEADD(MINUTE, -15, SYSDATETIME())))")) {
            ps.setString(1, bookingID);
            ps.setString(2, paymentType);
            ps.setLong(3, java.time.Instant.now().getEpochSecond());
            try (ResultSet r = ps.executeQuery()) {
                return r.next();
            }
        }
    }

    // Counter bookings reuse the QR fields with no check-in staff ID.
    public void insertCounterOrder(Connection c, PayOsOrder order, Integer checkInStaffUserID, String paymentLinkId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO dbo.PAYOS_ORDER "
                + "(OrderCode, BookingID, UserID, PaymentType, Amount, WalletAmount, ExternalAmount, PaymentLinkId, "
                + "CheckInStaffUserID, CheckInExpiresAt, CheckInQrCode, CheckInCheckoutUrl) "
                + "VALUES (?, ?, ?, ?, ?, 0, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, order.orderCode);
            ps.setString(2, order.bookingID);
            ps.setInt(3, order.userID);
            ps.setString(4, order.paymentType);
            ps.setBigDecimal(5, order.externalAmount);
            ps.setBigDecimal(6, order.externalAmount);
            ps.setString(7, paymentLinkId);
            ps.setObject(8, checkInStaffUserID, Types.INTEGER);
            ps.setLong(9, order.checkInExpiresAt);
            ps.setString(10, order.checkInQrCode);
            ps.setString(11, order.checkInCheckoutUrl);
            ps.executeUpdate();
        }
    }

    /** Use sp_getapplock and sp_releaseapplock to serialize front desk payments for one booking. */
    public <T> T withCounterPaymentLock(String bookingID, CounterPaymentOperation<T> operation) throws Exception {
        // Keep one front desk payment active for each booking.
        String resource = "HBMS:checkin-qr:" + bookingID.toUpperCase(java.util.Locale.ROOT);
        try (Connection c = PersistenceManager.openConnection();
                PreparedStatement lock = c.prepareStatement("DECLARE @result INT; "
                        + "EXEC @result = sys.sp_getapplock @Resource=?, @LockMode='Exclusive', "
                        + "@LockOwner='Session', @LockTimeout=5000; SELECT @result;");
                PreparedStatement release = c.prepareStatement("EXEC sys.sp_releaseapplock @Resource=?, @LockOwner='Session';")) {
            lock.setString(1, resource);
            try (ResultSet r = lock.executeQuery()) {
                if (!r.next() || r.getInt(1) < 0) {
                    throw new IllegalStateException("Another payment is in progress. Check the booking and retry.");
                }
            }
            release.setString(1, resource);
            try {
                return operation.run(c);
            } finally {
                release.execute();
            }
        }
    }

    /** Confirm a PayOS order using the transfer details received from the webhook. */
    public void confirm(long orderCode, String paymentLinkId, BigDecimal externalAmount, String reference) throws SQLException {
        // SP_CONFIRM_PAYOS_ORDER confirms the transfer and applies any wallet contribution.
        try (Connection c = PersistenceManager.openConnection();
                PreparedStatement ps = c.prepareStatement("EXEC dbo.SP_CONFIRM_PAYOS_ORDER ?, ?, ?, ?;")) {
            ps.setLong(1, orderCode);
            ps.setString(2, paymentLinkId);
            ps.setBigDecimal(3, externalAmount);
            ps.setString(4, reference);
            boolean rows = ps.execute();
            while (rows || ps.getUpdateCount() != -1) {
                rows = ps.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
            }
        }
    }
}
