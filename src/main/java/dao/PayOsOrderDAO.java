package dao;

import java.math.BigDecimal;
import java.sql.*;
import util.PersistenceManager;

public class PayOsOrderDAO {

    public static final class Order {

        public long orderCode;
        public String bookingID, paymentType, status, reference;
        public int userID;
        public BigDecimal amount, walletAmount, externalAmount;
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
        try (Connection c = PersistenceManager.openConnection(); PreparedStatement ps = c.prepareStatement("SELECT * FROM dbo.PAYOS_ORDER WHERE OrderCode = ?")) {
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
                return o;
            }
        }
    }

    /**
     * Atomic claim -> guarantees a retried webhook is processed only once.
     *
     * @param orderCode
     * @return
     * @throws java.sql.SQLException
     */
    public boolean claim(long orderCode) throws SQLException {
        return update("UPDATE dbo.PAYOS_ORDER SET Status='PROCESSING' WHERE OrderCode=? AND Status='PENDING'", orderCode, null) == 1;
    }

    public void markPaid(long orderCode, String reference) throws SQLException {
        update("UPDATE dbo.PAYOS_ORDER SET Status='PAID', PaidAt=SYSDATETIME(), Reference=? WHERE OrderCode=?", orderCode, reference);
    }

    public void markFailed(long orderCode, String message) throws SQLException {
        update("UPDATE dbo.PAYOS_ORDER SET Status='FAILED', ErrorMessage=? WHERE OrderCode=?", orderCode,
                message == null ? null : message.substring(0, Math.min(300, message.length())));
    }

    /**
     * Latest order status of a booking for this user (used by polling).
     */
    public String latestStatus(String bookingID, int userID) throws SQLException {
        try (Connection c = PersistenceManager.openConnection(); PreparedStatement ps = c.prepareStatement("SELECT TOP 1 Status FROM dbo.PAYOS_ORDER WHERE BookingID=? AND UserID=? ORDER BY CreatedAt DESC")) {
            ps.setString(1, bookingID);
            ps.setInt(2, userID);
            try (ResultSet r = ps.executeQuery()) {
                return r.next() ? r.getString(1) : "NONE";
            }
        }
    }

    private int update(String sql, long orderCode, String param) throws SQLException {
        try (Connection c = PersistenceManager.openConnection(); PreparedStatement ps = c.prepareStatement(sql)) {
            int i = 1;
            if (sql.contains("Reference=?") || sql.contains("ErrorMessage=?")) {
                ps.setString(i++, param);
            }
            ps.setLong(i, orderCode);
            return ps.executeUpdate();
        }
    }
}
