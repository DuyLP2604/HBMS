package dao;

import java.math.BigDecimal;
import java.sql.*;
import util.PersistenceManager;

public class PayOsOrderDAO {

    public static final class Order {

        public long orderCode;
        public String bookingID, paymentType, status, reference, paymentID;
        public int userID;
        public BigDecimal amount, walletAmount, externalAmount;
        public long checkInExpiresAt;
        public String checkInQrCode, checkInCheckoutUrl;
    }

    public void insert(Order o, String paymentLinkId) throws SQLException {
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

    public Order find(long orderCode) throws SQLException {
        try (Connection c = PersistenceManager.openConnection(); PreparedStatement ps = c.prepareStatement("SELECT BookingID, UserID, PaymentType, Amount, WalletAmount, ExternalAmount, Status, Reference, PaymentID FROM dbo.PAYOS_ORDER WHERE OrderCode = ?")) {
            ps.setLong(1, orderCode);
            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) {
                    return null;
                }
                Order o = new Order();
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

    public Order findCheckInOrder(String bookingID, Long orderCode) throws SQLException {
        try (Connection c = PersistenceManager.openConnection()) {
            return findCheckInOrder(c, bookingID, orderCode);
        }
    }

    public Order findCheckInOrder(Connection c, String bookingID, Long orderCode) throws SQLException {
        String filter = orderCode == null
                ? "AND o.Status <> 'PAID' AND o.CheckInExpiresAt > ? ORDER BY o.CreatedAt DESC, o.OrderCode DESC"
                : "AND o.OrderCode = ?";
        try (PreparedStatement ps = c.prepareStatement("SELECT TOP (1) o.OrderCode, o.PaymentID, o.ExternalAmount, "
                + "CASE WHEN o.Status IN ('PENDING', 'PROCESSING') AND (b.BookingStatus <> 'ASSIGNED' "
                + "OR b.PaymentOption <> 'DEPOSIT' OR r.RoomRemainingAmount <> o.ExternalAmount) "
                + "THEN 'FAILED' ELSE o.Status END AS Status, "
                + "o.CheckInExpiresAt, o.CheckInQrCode, o.CheckInCheckoutUrl FROM dbo.PAYOS_ORDER o "
                + "JOIN dbo.BOOKING b ON b.BookingID = o.BookingID "
                + "JOIN dbo.V_BOOKING_ROOM_BALANCE r ON r.BookingID = o.BookingID "
                + "WHERE o.BookingID = ? AND o.CheckInStaffUserID IS NOT NULL " + filter)) {
            ps.setString(1, bookingID);
            ps.setLong(2, orderCode == null ? java.time.Instant.now().getEpochSecond() : orderCode);
            try (ResultSet r = ps.executeQuery()) {
                if (!r.next()) return null;
                Order order = new Order();
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

    public void insertCheckInOrder(Connection c, Order order, int staffUserID, String paymentLinkId) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("INSERT INTO dbo.PAYOS_ORDER "
                + "(OrderCode, BookingID, UserID, PaymentType, Amount, WalletAmount, ExternalAmount, PaymentLinkId, "
                + "CheckInStaffUserID, CheckInExpiresAt, CheckInQrCode, CheckInCheckoutUrl) "
                + "VALUES (?, ?, ?, 'BALANCE', ?, 0, ?, ?, ?, ?, ?, ?)")) {
            ps.setLong(1, order.orderCode);
            ps.setString(2, order.bookingID);
            ps.setInt(3, order.userID);
            ps.setBigDecimal(4, order.externalAmount);
            ps.setBigDecimal(5, order.externalAmount);
            ps.setString(6, paymentLinkId);
            ps.setInt(7, staffUserID);
            ps.setLong(8, order.checkInExpiresAt);
            ps.setString(9, order.checkInQrCode);
            ps.setString(10, order.checkInCheckoutUrl);
            ps.executeUpdate();
        }
    }

    @FunctionalInterface
    public interface CheckInOperation<T> {
        T run(Connection connection) throws Exception;
    }

    public <T> T withCheckInLock(String bookingID, CheckInOperation<T> operation) throws Exception {
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

    public void confirm(long orderCode, String paymentLinkId, BigDecimal externalAmount, String reference) throws SQLException {
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
