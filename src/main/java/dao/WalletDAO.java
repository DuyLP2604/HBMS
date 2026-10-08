package dao;

import dto.WalletSummary;
import dto.WalletTransactionItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import util.PersistenceManager;

public final class WalletDAO
{
    // UserID must come from the authenticated session, never from request parameters.
    public WalletSummary getWalletByUserID(int userID)
    {
        if (userID <= 0)
        {
            throw new IllegalArgumentException("Invalid customer session.");
        }
        String sql = "SELECT w.WalletID, w.CustomerID, w.Balance, w.CreatedAt FROM dbo.V_WALLET_BALANCE w JOIN dbo.CUSTOMER c ON c.CustomerID = w.CustomerID WHERE c.UserID = ?";
        try (Connection connection = PersistenceManager.openConnection(); PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userID);
            statement.setQueryTimeout(15);
            try (ResultSet result = statement.executeQuery())
            {
                return result.next() ? new WalletSummary(result.getLong("WalletID"), clean(result.getString("CustomerID")), result.getBigDecimal("Balance"), result.getTimestamp("CreatedAt")) : null;
            }
        }
        catch (SQLException exception)
        {
            throw new IllegalStateException("Unable to load the wallet balance.", exception);
        }
    }

    // Fetch one extra row so the servlet can show Next without counting the entire ledger.
    public List<WalletTransactionItem> getTransactionsByUserID(int userID, int page, int pageSize)
    {
        if (userID <= 0 || page < 1 || page > 100000 || pageSize < 1 || pageSize > 100)
        {
            throw new IllegalArgumentException("Invalid wallet history request.");
        }
        String sql = "SELECT t.WalletTransactionID, t.TransactionType, t.Amount, t.TransactionTime, COALESCE(r.BookingID, p.BookingID) AS BookingID, t.PaymentID FROM dbo.WALLET_TRANSACTION t JOIN dbo.CUSTOMER_WALLET w ON w.WalletID = t.WalletID JOIN dbo.CUSTOMER c ON c.CustomerID = w.CustomerID LEFT JOIN dbo.BOOKING_REFUND r ON r.RefundID = t.RefundID LEFT JOIN dbo.PAYMENT p ON p.PaymentID = t.PaymentID WHERE c.UserID = ? ORDER BY t.TransactionTime DESC, t.WalletTransactionID DESC OFFSET ? ROWS FETCH NEXT ? ROWS ONLY";
        try (Connection connection = PersistenceManager.openConnection(); PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userID);
            statement.setInt(2, (page - 1) * pageSize);
            statement.setInt(3, pageSize + 1);
            statement.setQueryTimeout(15);
            List<WalletTransactionItem> items = new ArrayList<>();
            try (ResultSet result = statement.executeQuery())
            {
                while (result.next())
                {
                    items.add(new WalletTransactionItem(result.getLong("WalletTransactionID"), result.getString("TransactionType"), result.getBigDecimal("Amount"), result.getTimestamp("TransactionTime"), clean(result.getString("BookingID")), clean(result.getString("PaymentID"))));
                }
            }
            return items;
        }
        catch (SQLException exception)
        {
            throw new IllegalStateException("Unable to load wallet history.", exception);
        }
    }

    private String clean(String value)
    {
        return value == null ? null : value.trim();
    }

}
