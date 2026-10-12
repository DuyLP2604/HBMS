package dao;

import entity.Booking;
import entity.BookingDetail;
import jakarta.persistence.CacheRetrieveMode;
import jakarta.persistence.CacheStoreMode;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import util.PersistenceManager;

public class BookingDAO extends DAOFramework<Booking>
{
    public BookingDAO()
    {
        super(Booking.class);
    }

    public Booking getByIdWithDetails(String bookingID)
    {
        if (bookingID == null || bookingID.isBlank())
        {
            return null;
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            TypedQuery<Booking> query = fresh(em.createQuery("SELECT b FROM Booking b JOIN FETCH b.customerID WHERE b.bookingID = :bookingID", Booking.class));
            List<Booking> bookings = query.setParameter("bookingID", bookingID.trim()).getResultList();
            if (bookings.isEmpty())
            {
                return null;
            }
            TypedQuery<BookingDetail> detailQuery = fresh(em.createQuery("SELECT DISTINCT d FROM BookingDetail d JOIN FETCH d.roomTypeID LEFT JOIN FETCH d.roomAssignmentCollection a LEFT JOIN FETCH a.roomID WHERE d.bookingID.bookingID = :bookingID ORDER BY d.bookingDetailID", BookingDetail.class));
            List<BookingDetail> details = detailQuery.setParameter("bookingID", bookingID.trim()).getResultList();
            Booking booking = bookings.get(0);
            booking.setBookingDetailCollection(new ArrayList<>(details));
            return booking;
        }
        catch (Exception exception)
        {
            throw new IllegalStateException("Unable to load booking details.", exception);
        }
    }

    public List<Booking> getByUserId(String customerID)
    {
        return getByCustomerId(customerID);
    }

    public List<Booking> getByCustomerId(String customerID)
    {
        String value = required(customerID, "Customer ID", 6);
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            return fresh(em.createQuery("SELECT b FROM Booking b JOIN FETCH b.customerID WHERE b.customerID.customerID = :customerID ORDER BY b.bookingDate DESC, b.bookingID DESC", Booking.class)).setParameter("customerID", value).getResultList();
        }
    }

    public List<Booking> getByAccountUserId(int userID)
    {
        requireActor(userID);
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            return fresh(em.createQuery("SELECT b FROM Booking b JOIN FETCH b.customerID WHERE b.customerID.userID.userID = :userID ORDER BY b.bookingDate DESC, b.bookingID DESC", Booking.class)).setParameter("userID", userID).getResultList();
        }
    }

    public Booking getLatestBookingByUserId(int userID)
    {
        return latest(userID, false);
    }

    public Booking getLatestPendingBookingByUserId(int userID)
    {
        return latest(userID, true);
    }

    public List<Booking> getByStatus(String bookingStatus)
    {
        String status = normalizedStatus(bookingStatus);
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            return fresh(em.createQuery("SELECT b FROM Booking b JOIN FETCH b.customerID WHERE b.bookingStatus = :status ORDER BY b.checkInDate, b.bookingID", Booking.class)).setParameter("status", status).getResultList();
        }
    }

    public Booking createBooking(Booking booking, List<BookingDetail> rooms, int actorUserID)
    {
        requireActor(actorUserID);
        if (booking == null || booking.getCustomerID() == null)
        {
            throw new IllegalArgumentException("Booking and customer are required.");
        }
        String bookingID = required(booking.getBookingID(), "Booking ID", 6);
        String customerID = required(booking.getCustomerID().getCustomerID(), "Customer ID", 6);
        String option = required(booking.getPaymentOption(), "Payment option", 10).toUpperCase(Locale.ROOT);
        if (!Set.of("DEPOSIT", "FULL").contains(option))
        {
            throw new IllegalArgumentException("Payment option must be DEPOSIT or FULL.");
        }
        if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null || booking.getPaymentDeadline() == null)
        {
            throw new IllegalArgumentException("Check-in, check-out and payment deadline are required.");
        }
        if (rooms == null || rooms.isEmpty() || rooms.size() > 600)
        {
            throw new IllegalArgumentException("Provide between 1 and 600 room-type requests.");
        }
        Set<String> types = new HashSet<>();
        List<String> roomTypes = new ArrayList<>();
        for (BookingDetail room : rooms)
        {
            if (room == null || room.getRoomTypeID() == null || room.getQuantity() <= 0 || room.getGuestCount() <= 0)
            {
                throw new IllegalArgumentException("Each room type needs a positive quantity and guest count.");
            }
            String type = required(room.getRoomTypeID().getRoomTypeID(), "Room type ID", 4).toUpperCase(Locale.ROOT);
            if (!types.add(type))
            {
                throw new IllegalArgumentException("Combine duplicate room types into one request.");
            }
            roomTypes.add(type);
        }
        StringBuilder sql = new StringBuilder("DECLARE @Rooms dbo.BOOKING_ROOM_INPUT; INSERT INTO @Rooms (RoomTypeID, Quantity, GuestCount) VALUES ");
        for (int index = 0; index < rooms.size(); index++)
        {
            if (index > 0)
            {
                sql.append(", ");
            }
            sql.append("(?, ?, ?)");
        }
        sql.append("; EXEC dbo.SP_CREATE_BOOKING @BookingID = ?, @CustomerID = ?, @ActorUserID = ?, @CheckInDate = ?, @CheckOutDate = ?, @PaymentOption = ?, @Rooms = @Rooms, @PaymentDeadline = ?;");
        execute(sql.toString(), statement ->
        {
            int parameter = 1;
            for (int index = 0; index < rooms.size(); index++)
            {
                statement.setString(parameter++, roomTypes.get(index));
                statement.setInt(parameter++, rooms.get(index).getQuantity());
                statement.setInt(parameter++, rooms.get(index).getGuestCount());
            }
            statement.setString(parameter++, bookingID);
            statement.setString(parameter++, customerID);
            statement.setInt(parameter++, actorUserID);
            statement.setDate(parameter++, new java.sql.Date(booking.getCheckInDate().getTime()));
            statement.setDate(parameter++, new java.sql.Date(booking.getCheckOutDate().getTime()));
            statement.setString(parameter++, option);
            statement.setTimestamp(parameter, new Timestamp(booking.getPaymentDeadline().getTime()));
        }, true);
        return getByIdWithDetails(bookingID);
    }

    public Booking cancelBooking(String bookingID, int actorUserID)
    {
        return cancelBooking(bookingID, actorUserID, "CUSTOMER_REQUEST");
    }

    public Booking cancelBooking(String bookingID, int actorUserID, String reason)
    {
        requireActor(actorUserID);
        String id = required(bookingID, "Booking ID", 6);
        String value = required(reason, "Cancellation reason", 30).toUpperCase(Locale.ROOT);
        if (!Set.of("CUSTOMER_REQUEST", "STAFF_REQUEST").contains(value))
        {
            throw new IllegalArgumentException("Use expirePendingBookings for payment timeouts.");
        }
        execute("EXEC dbo.SP_CANCEL_BOOKING @BookingID = ?, @ActorUserID = ?, @Reason = ?, @ReturnResult = 0;", statement ->
        {
            statement.setString(1, id);
            statement.setInt(2, actorUserID);
            statement.setString(3, value);
        }, true);
        return getByIdWithDetails(id);
    }

    public void updateStatus(String bookingID, String bookingStatus, int actorUserID)
    {
        requireActor(actorUserID);
        String id = required(bookingID, "Booking ID", 6);
        String status = normalizedStatus(bookingStatus);
        if (!Set.of("ASSIGNED", "CHECKED_IN", "CHECKED_OUT").contains(status))
        {
            throw new IllegalArgumentException("Use createBooking, payment procedures or cancelBooking for this status.");
        }
        execute("EXEC dbo.SP_SET_BOOKING_STATUS @BookingID = ?, @NewStatus = ?, @ActorUserID = ?;", statement ->
        {
            statement.setString(1, id);
            statement.setString(2, status);
            statement.setInt(3, actorUserID);
        }, true);
    }

    @Deprecated
    public void updateStatus(String bookingID, String bookingStatus)
    {
        throw new UnsupportedOperationException("Pass the authenticated session UserID to updateStatus, or use cancelBooking.");
    }

    public int expirePendingBookings()
    {
        return expirePendingBookings(null);
    }

    public int expirePendingBookings(String customerID)
    {
        String id = customerID == null ? null : required(customerID, "Customer ID", 6);
        Map<String, Object> result = execute("EXEC dbo.SP_EXPIRE_PENDING_BOOKINGS @CustomerID = ?, @ReturnResult = 1;", statement ->
        {
            if (id == null)
            {
                statement.setNull(1, Types.CHAR);
            }
            else
            {
                statement.setString(1, id);
            }
        }, true);
        Object count = result.get("expiredBookingCount");
        if (!(count instanceof Number))
        {
            throw new IllegalStateException("The expiry procedure returned no booking count.");
        }
        return ((Number) count).intValue();
    }

    public Map<String, Object> getPaymentSummary(String bookingID)
    {
        String id = required(bookingID, "Booking ID", 6);
        return execute("SELECT * FROM dbo.V_BOOKING_PAYMENT_SUMMARY WHERE BookingID = ?", statement -> statement.setString(1, id), false);
    }

    public Map<String, Object> getBookingAccess(int userID)
    {
        requireActor(userID);
        return execute("SELECT * FROM dbo.V_USER_BOOKING_ACCESS WHERE UserID = ?", statement -> statement.setInt(1, userID), false);
    }

    private Booking latest(int userID, boolean pendingOnly)
    {
        requireActor(userID);
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT b FROM Booking b JOIN FETCH b.customerID WHERE b.customerID.userID.userID = :userID";
            if (pendingOnly)
            {
                jpql += " AND b.bookingStatus = 'PENDING_PAYMENT'";
            }
            jpql += " ORDER BY b.bookingDate DESC, b.bookingID DESC";
            List<Booking> results = fresh(em.createQuery(jpql, Booking.class)).setParameter("userID", userID).setMaxResults(1).getResultList();
            return results.isEmpty() ? null : results.get(0);
        }
    }

    private <T> TypedQuery<T> fresh(TypedQuery<T> query)
    {
        query.setHint("jakarta.persistence.cache.retrieveMode", CacheRetrieveMode.BYPASS);
        query.setHint("jakarta.persistence.cache.storeMode", CacheStoreMode.REFRESH);
        return query;
    }

    private String required(String value, String label, int maxLength)
    {
        if (value == null || value.isBlank() || value.trim().length() > maxLength)
        {
            throw new IllegalArgumentException(label + " is required and must not exceed " + maxLength + " characters.");
        }
        return value.trim();
    }

    private void requireActor(int userID)
    {
        if (userID <= 0)
        {
            throw new IllegalArgumentException("An authenticated session UserID is required.");
        }
    }

    private String normalizedStatus(String bookingStatus)
    {
        String status = required(bookingStatus, "Booking status", 30).toUpperCase(Locale.ROOT);
        if (!Set.of("PENDING_PAYMENT", "CONFIRMED", "ASSIGNED", "CHECKED_IN", "CHECKED_OUT", "CANCELLED").contains(status))
        {
            throw new IllegalArgumentException("Invalid booking status: " + bookingStatus);
        }
        return status;
    }

    private Map<String, Object> execute(String sql, StatementBinder binder, boolean write)
    {
        try (Connection connection = PersistenceManager.openConnection(); PreparedStatement statement = connection.prepareStatement(sql))
        {
            binder.bind(statement);
            Map<String, Object> firstRow = new LinkedHashMap<>();
            boolean captured = false;
            boolean hasResult = statement.execute();
            while (true)
            {
                if (hasResult)
                {
                    try (ResultSet result = statement.getResultSet())
                    {
                        ResultSetMetaData metadata = result.getMetaData();
                        while (result.next())
                        {
                            if (!captured)
                            {
                                for (int column = 1; column <= metadata.getColumnCount(); column++)
                                {
                                    String label = metadata.getColumnLabel(column);
                                    String key = label.substring(0, 1).toLowerCase(Locale.ROOT) + label.substring(1);
                                    firstRow.put(key, result.getObject(column));
                                }
                                captured = true;
                            }
                        }
                    }
                }
                else if (statement.getUpdateCount() == -1)
                {
                    break;
                }
                hasResult = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
            }
            return firstRow;
        }
        catch (SQLException exception)
        {
            throw new BookingOperationException(exception);
        }
        finally
        {
            if (write)
            {
                PersistenceManager.getEMF().getCache().evictAll();
            }
        }
    }

    @FunctionalInterface
    private interface StatementBinder
    {
        void bind(PreparedStatement statement) throws SQLException;
    }

    public static class BookingOperationException extends IllegalStateException
    {
        private final int sqlErrorCode;

        public BookingOperationException(SQLException cause)
        {
            super("Booking operation failed (SQL " + cause.getErrorCode() + ").", cause);
            this.sqlErrorCode = cause.getErrorCode();
        }

        public int getSqlErrorCode()
        {
            return sqlErrorCode;
        }
    }
}
