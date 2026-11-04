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
