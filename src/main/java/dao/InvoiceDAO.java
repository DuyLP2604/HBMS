package dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import util.PersistenceManager;

public class InvoiceDAO
{
    private static final Logger LOGGER = Logger.getLogger(InvoiceDAO.class.getName());
    private static final String HOTEL_NAME = "(SELECT TOP (1) HotelName FROM dbo.HOTEL)";

    public List<Map<String, String>> getOccupiedRooms()
    {
        String sql = "SELECT b.BookingID AS bookingID, c.FullName AS customerName, n.NationalityName AS nationality, " + HOTEL_NAME + " AS hotelName, CONVERT(VARCHAR(10), b.CheckInDate, 23) AS checkInDate, CONVERT(VARCHAR(10), b.CheckOutDate, 23) AS checkOutDate FROM dbo.BOOKING b JOIN dbo.CUSTOMER c ON c.CustomerID = b.CustomerID LEFT JOIN dbo.NATIONALITY n ON n.NationalityID = c.NationalityID WHERE b.BookingStatus = 'CHECKED_IN' ORDER BY b.CheckInDate, b.BookingID";
        List<Map<String, String>> rooms = query(sql);
        for (Map<String, String> room : rooms)
        {
            addRoomInfo(room);
        }
        return rooms;
    }

    public List<Map<String, String>> getPaidInvoices()
    {
        String sql = "SELECT TOP (10) i.InvoiceID AS invoiceID, i.BookingID AS bookingID, c.FullName AS customerName, " + HOTEL_NAME + " AS hotelName, i.TotalAmount AS totalAmount, CONVERT(VARCHAR(10), i.InvoiceDate, 23) AS invoiceDate FROM dbo.INVOICE i JOIN dbo.BOOKING b ON b.BookingID = i.BookingID JOIN dbo.CUSTOMER c ON c.CustomerID = b.CustomerID JOIN dbo.V_BOOKING_PAYMENT_SUMMARY ps ON ps.BookingID = b.BookingID WHERE i.Status = N'Có hiệu lực' AND b.BookingStatus = 'CHECKED_OUT' AND ps.PaymentStatus = 'FULLY_PAID' ORDER BY i.InvoiceDate DESC, i.InvoiceID DESC";
        List<Map<String, String>> invoices = query(sql);
        for (Map<String, String> invoice : invoices)
        {
            addRoomInfo(invoice);
        }
        return invoices;
    }

    public Map<String, String> getInvoiceByBooking(String bookingID)
    {
        String id = requireBookingID(bookingID);
        List<Map<String, String>> invoices = query("SELECT InvoiceID AS invoiceID, BookingID AS bookingID, TotalAmount AS totalAmount, CONVERT(VARCHAR(10), InvoiceDate, 23) AS invoiceDate, EmployeeID AS employeeID, Status AS status FROM dbo.INVOICE WHERE BookingID = ? AND Status = N'Có hiệu lực'", id);
        return invoices.isEmpty() ? null : invoices.get(0);
    }

    public boolean isBookingPaid(String bookingID)
    {
        Map<String, Object> summary = new BookingDAO().getPaymentSummary(requireBookingID(bookingID));
        return summary != null && "FULLY_PAID".equals(summary.get("paymentStatus")) && !"CANCELLED".equals(summary.get("bookingStatus"));
    }

    public List<Map<String, String>> getServicesByBooking(String bookingID)
    {
        return query("SELECT bs.ServiceID AS serviceID, s.ServiceName AS serviceName, bs.Quantity AS quantity, bs.UnitPrice AS unitPrice, bs.Subtotal AS subtotal FROM dbo.BOOKING_SERVICE bs JOIN dbo.SERVICE s ON s.ServiceID = bs.ServiceID WHERE bs.BookingID = ? ORDER BY s.ServiceName, bs.ServiceID", requireBookingID(bookingID));
    }

    public Map<String, String> getRoomDetailByBooking(String bookingID)
    {
        String sql = "SELECT b.BookingID AS bookingID, c.FullName AS customerName, " + HOTEL_NAME + " AS hotelName, CONVERT(VARCHAR(10), b.CheckInDate, 23) AS checkInDate, CONVERT(VARCHAR(10), b.CheckOutDate, 23) AS checkOutDate, DATEDIFF(DAY, b.CheckInDate, b.CheckOutDate) AS totalDays, COALESCE((SELECT SUM(d.Subtotal) FROM dbo.BOOKING_DETAIL d WHERE d.BookingID = b.BookingID), 0) AS roomTotal, COALESCE((SELECT SUM(s.Subtotal) FROM dbo.BOOKING_SERVICE s WHERE s.BookingID = b.BookingID), 0) AS serviceTotal, b.TotalAmount AS baseTotal FROM dbo.BOOKING b JOIN dbo.CUSTOMER c ON c.CustomerID = b.CustomerID WHERE b.BookingID = ?";
        List<Map<String, String>> rooms = query(sql, requireBookingID(bookingID));
        if (rooms.isEmpty())
        {
            return null;
        }
        Map<String, String> room = rooms.get(0);
        addRoomInfo(room);
        return room;
    }

    public boolean processCheckout(String bookingID, int actorUserID)
    {
        String id = requireBookingID(bookingID);
        if (actorUserID <= 0)
        {
            throw new IllegalArgumentException("An authenticated staff UserID is required.");
        }
        boolean confirmed = false;
        try (Connection connection = PersistenceManager.openConnection(); PreparedStatement statement = connection.prepareStatement("EXEC dbo.SP_CHECKOUT_WITH_INVOICE @BookingID = ?, @ActorUserID = ?;"))
        {
            statement.setString(1, id);
            statement.setInt(2, actorUserID);
            boolean resultSet = statement.execute();
            while (true)
            {
                if (resultSet)
                {
                    try (ResultSet result = statement.getResultSet())
                    {
                        while (result.next())
                        {
                            String invoiceID = result.getString("InvoiceID");
                            confirmed = invoiceID != null && !invoiceID.isBlank();
                        }
                    }
                }
                else if (statement.getUpdateCount() == -1)
                {
                    break;
                }
                resultSet = statement.getMoreResults();
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException(checkoutError(ex), ex);
        }
        finally
        {
            try
            {
                PersistenceManager.getEMF().getCache().evictAll();
            }
            catch (RuntimeException ex)
            {
                LOGGER.log(Level.WARNING, "Unable to refresh the persistence cache after checkout.", ex);
            }
        }
        if (!confirmed)
        {
            throw new IllegalStateException("Checkout could not be confirmed. Check the booking and invoice before retrying.");
        }
        return true;
    }

    @Deprecated
    public boolean processCheckout(String bookingID, String employeeID)
    {
        throw new IllegalStateException("Use processCheckout(bookingID, authenticatedUserID).");
    }

    @Deprecated
    public boolean processCheckout(String bookingID, double ignoredTotalAmount, String employeeID)
    {
        throw new IllegalStateException("Use processCheckout(bookingID, authenticatedUserID). The database calculates the invoice total.");
    }

    private void addRoomInfo(Map<String, String> target)
    {
        List<Map<String, String>> rooms = query("SELECT r.RoomID AS roomID, r.RoomNumber AS roomNumber, rt.TypeName AS roomType, d.UnitPrice AS price FROM dbo.BOOKING_DETAIL d JOIN dbo.ROOM_TYPE rt ON rt.RoomTypeID = d.RoomTypeID LEFT JOIN dbo.ROOM_ASSIGNMENT ra ON ra.BookingDetailID = d.BookingDetailID LEFT JOIN dbo.ROOM r ON r.RoomID = ra.RoomID WHERE d.BookingID = ? ORDER BY r.RoomNumber, d.BookingDetailID", target.get("bookingID"));
        for (String key : new String[]{"roomID", "roomNumber", "roomType", "price"})
        {
            Set<String> values = new LinkedHashSet<>();
            for (Map<String, String> room : rooms)
            {
                String value = room.get(key);
                if (value != null && !value.isBlank())
                {
                    values.add(value);
                }
            }
            target.put(key, values.isEmpty() && "roomNumber".equals(key) ? "Pending assignment" : String.join(", ", values));
        }
    }

    private List<Map<String, String>> query(String sql, String... parameters)
    {
        try (Connection connection = PersistenceManager.openConnection(); PreparedStatement statement = connection.prepareStatement(sql))
        {
            for (int index = 0; index < parameters.length; index++)
            {
                statement.setString(index + 1, parameters[index]);
            }
            try (ResultSet result = statement.executeQuery())
            {
                List<Map<String, String>> rows = new ArrayList<>();
                ResultSetMetaData metadata = result.getMetaData();
                while (result.next())
                {
                    Map<String, String> row = new LinkedHashMap<>();
                    for (int index = 1; index <= metadata.getColumnCount(); index++)
                    {
                        String value = result.getString(index);
                        row.put(metadata.getColumnLabel(index), value == null ? "" : value.trim());
                    }
                    rows.add(row);
                }
                return rows;
            }
        }
        catch (SQLException ex)
        {
            throw new IllegalStateException("Unable to load invoice data.", ex);
        }
    }

    private String requireBookingID(String bookingID)
    {
        if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6)
        {
            throw new IllegalArgumentException("A valid booking ID is required.");
        }
        return bookingID.trim();
    }

    private String checkoutError(SQLException exception)
    {
        for (SQLException ex = exception; ex != null; ex = ex.getNextException())
        {
            switch (ex.getErrorCode())
            {
                case 51000: return "The booking system is busy. Please try again.";
                case 51100: return "Only staff or admin can issue invoices.";
                case 51101: return "Your account is not linked to an employee profile.";
                case 51102: return "The requested booking was not found.";
                case 51103: return "The current booking status does not allow checkout.";
                case 51104: return "Complete the remaining payment before checkout.";
                case 51105: return "The booking has no successful payment.";
                case 51106: return "The active invoice total differs from the booking total. Review the invoice before continuing.";
                case 51107: return "The invoice ID range is exhausted.";
                default: break;
            }
        }
        return "Checkout could not be confirmed. Check the booking and invoice before retrying.";
    }
}