package service;

import dao.EmployeeDAO;
import entity.Booking;
import entity.BookingDetail;
import entity.Employee;
import entity.Room;
import entity.RoomAssignment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.LockModeType;
import jakarta.persistence.NoResultException;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import util.PersistenceManager;

public class RoomAssignmentService {

    private static final List<String> BLOCKING_STATUSES
            = Arrays.asList(
                    "CONFIRMED",
                    "ASSIGNED",
                    "CHECKED_IN",
                    "CHECKED_OUT",
                    "PENDING_PAYMENT"
            );

    private final EmployeeDAO employeeDAO
            = new EmployeeDAO();

    public List<Booking> getWaitingBookings() {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT b FROM Booking b "
                    + "WHERE b.bookingStatus = :status "
                    + "ORDER BY b.checkInDate ASC, "
                    + "b.bookingDate ASC";

            TypedQuery<Booking> query
                    = em.createQuery(
                            jpql,
                            Booking.class
                    );

            query.setParameter(
                    "status",
                    "CONFIRMED"
            );

            query.setHint("jakarta.persistence.cache.retrieveMode", jakarta.persistence.CacheRetrieveMode.BYPASS);
            query.setHint("jakarta.persistence.cache.storeMode", jakarta.persistence.CacheStoreMode.REFRESH);
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public Booking getBooking(String bookingID) {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            return em.find(
                    Booking.class,
                    bookingID
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<BookingDetail> getBookingDetails(
            String bookingID) {

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT bd FROM BookingDetail bd "
                    + "WHERE bd.bookingID.bookingID = :bookingID "
                    + "ORDER BY bd.bookingDetailID ASC";

            TypedQuery<BookingDetail> query
                    = em.createQuery(
                            jpql,
                            BookingDetail.class
                    );

            query.setParameter(
                    "bookingID",
                    bookingID
            );

            query.setHint("jakarta.persistence.cache.retrieveMode", jakarta.persistence.CacheRetrieveMode.BYPASS);
            query.setHint("jakarta.persistence.cache.storeMode", jakarta.persistence.CacheStoreMode.REFRESH);
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Room> getAvailableRooms(
            Integer bookingDetailID) {

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            BookingDetail detail = em.find(
                    BookingDetail.class,
                    bookingDetailID
            );

            if (detail == null) {
                return new ArrayList<>();
            }

            Booking booking = detail.getBookingID();

            String jpql
                    = "SELECT r FROM Room r "
                    + "WHERE r.roomTypeID = :roomType "
                    + "AND r.status = :roomStatus "
                    + "AND NOT EXISTS ("
                    + "    SELECT ra.assignmentID "
                    + "    FROM RoomAssignment ra "
                    + "    WHERE ra.roomID = r "
                    + "    AND ra.bookingDetailID.bookingID."
                    + "bookingStatus IN :statuses "
                    + "    AND ra.bookingDetailID.bookingID."
                    + "checkInDate < :checkOutDate "
                    + "    AND ra.bookingDetailID.bookingID."
                    + "checkOutDate > :checkInDate"
                    + ") "
                    + "ORDER BY r.roomNumber ASC";

            TypedQuery<Room> query
                    = em.createQuery(
                            jpql,
                            Room.class
                    );

            query.setParameter(
                    "roomType",
                    detail.getRoomTypeID()
            );

            query.setParameter(
                    "roomStatus",
                    "ACTIVE"
            );

            query.setParameter(
                    "statuses",
                    BLOCKING_STATUSES
            );

            query.setParameter(
                    "checkInDate",
                    booking.getCheckInDate()
            );

            query.setParameter(
                    "checkOutDate",
                    booking.getCheckOutDate()
            );

            query.setHint("jakarta.persistence.cache.retrieveMode", jakarta.persistence.CacheRetrieveMode.BYPASS);
            query.setHint("jakarta.persistence.cache.storeMode", jakarta.persistence.CacheStoreMode.REFRESH);
            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void assignRooms(String bookingID, Map<Integer, List<String>> selectedRooms, int userID) {
        if (!employeeDAO.isReceptionistByUserId(userID)) {
            throw new SecurityException("Only receptionists can assign rooms.");
        }
        List<BookingDetail> details = getBookingDetails(bookingID);
        if (details.isEmpty() || selectedRooms == null || selectedRooms.size() != details.size()) {
            throw new IllegalArgumentException("Select rooms for every requested room type.");
        }
        Set<String> unique = new HashSet<>();
        for (BookingDetail detail : details) {
            List<String> rooms = selectedRooms.get(detail.getBookingDetailID());
            if (rooms == null || rooms.size() != detail.getQuantity()) {
                throw new IllegalArgumentException("Select exactly " + detail.getQuantity() + " room(s) for "
                        + detail.getRoomTypeID().getTypeName() + ".");
            }
            for (String room : rooms) {
                if (room == null || room.isBlank() || room.trim().length() > 3 || !unique.add(room.trim())) {
                    throw new IllegalArgumentException("Each selected room must be valid and used only once.");
                }
            }
        }
        try (java.sql.Connection connection = PersistenceManager.openConnection()) {
            writeAssignments(connection, bookingID, details, selectedRooms, userID);
        } catch (java.sql.SQLException ex) {
            throw new IllegalStateException(assignmentError(ex), ex);
        }
        try {
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (RuntimeException ex) {
            java.util.logging.Logger.getLogger(RoomAssignmentService.class.getName())
                    .log(java.util.logging.Level.WARNING, "Assignment committed, but cache refresh failed.", ex);
        }
    }

    void writeAssignments(java.sql.Connection connection, String bookingID, List<BookingDetail> details,
            Map<Integer, List<String>> selectedRooms, int userID) throws java.sql.SQLException {
        connection.setAutoCommit(false);
        try {
            // Reading a real table starts the SQL Server JDBC implicit transaction before the transaction-owned lock.
            executeAssignment(connection,
                    "SELECT TOP (1) UserID FROM dbo.USERS; "
                    + "DECLARE @LockResult int; "
                    + "EXEC @LockResult = sys.sp_getapplock @Resource = N'HBMS:booking-wallet-write', "
                    + "@LockMode = 'Exclusive', @LockOwner = 'Transaction', @LockTimeout = 10000; "
                    + "IF @LockResult < 0 THROW 51000, 'System busy. Please retry the request.', 1;",
                    null, null, userID);
            for (BookingDetail detail : details) {
                for (String room : selectedRooms.get(detail.getBookingDetailID())) {
                    executeAssignment(connection,
                            "EXEC dbo.SP_ASSIGN_ROOM @BookingDetailID=?, @RoomID=?, @ActorUserID=?",
                            detail.getBookingDetailID(), room.trim(), userID);
                }
            }
            try (java.sql.PreparedStatement statement = connection.prepareStatement(
                    "EXEC dbo.SP_SET_BOOKING_STATUS @BookingID=?, @NewStatus='ASSIGNED', @ActorUserID=?")) {
                statement.setString(1, bookingID);
                statement.setInt(2, userID);
                drainAssignment(statement);
            }
            connection.commit();
        } catch (java.sql.SQLException | RuntimeException ex) {
            try { connection.rollback(); } catch (java.sql.SQLException rollback) { ex.addSuppressed(rollback); }
            throw ex;
        }
    }

    static String assignmentError(java.sql.SQLException error) {
        String suffix = " (SQL error " + error.getErrorCode() + ").";
        switch (error.getErrorCode()) {
            case 51000:
            case 1205:
            case 1222:
                return "The booking system is busy. Reload and retry" + suffix;
            case 229:
                return "The application database account lacks permission to assign rooms" + suffix;
            case 208:
            case 2812:
                return "A required database table or procedure is missing. Check the database migration" + suffix;
            case 51066:
                return "The current user is not recognized as a receptionist by the database" + suffix;
            case 51067:
            case 51063:
                return "This booking no longer accepts room assignment. Reload its current status" + suffix;
            case 51064:
                return "All requested rooms must be assigned before completing this step" + suffix;
            case 2601:
            case 2627:
                return "A selected room has already been assigned to this booking. Reload its current assignments" + suffix;
            default:
                return "Room assignment could not be saved" + suffix + " Check the server log for the database error.";
        }
    }

    private void executeAssignment(java.sql.Connection connection, String sql, Integer detailID,
            String roomID, int userID) throws java.sql.SQLException {
        try (java.sql.PreparedStatement statement = connection.prepareStatement(sql)) {
            if (detailID != null) {
                statement.setInt(1, detailID); statement.setString(2, roomID); statement.setInt(3, userID);
            }
            drainAssignment(statement);
        }
    }

    private void drainAssignment(java.sql.PreparedStatement statement) throws java.sql.SQLException {
        boolean result = statement.execute();
        while (true) {
            if (result) { try (java.sql.ResultSet rows = statement.getResultSet()) { while (rows.next()) { } } }
            else if (statement.getUpdateCount() == -1) break;
            result = statement.getMoreResults(java.sql.Statement.CLOSE_CURRENT_RESULT);
        }
    }

    private Employee findEmployeeByUserID(
            EntityManager em,
            int userID) {

        try {
            String jpql
                    = "SELECT e FROM Employee e "
                    + "WHERE e.userID.userID = :userID";

            TypedQuery<Employee> query
                    = em.createQuery(
                            jpql,
                            Employee.class
                    );

            query.setParameter("userID", userID);

            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    private List<BookingDetail> loadDetails(
            EntityManager em,
            String bookingID) {

        String jpql
                = "SELECT bd FROM BookingDetail bd "
                + "WHERE bd.bookingID.bookingID = :bookingID "
                + "ORDER BY bd.bookingDetailID ASC";

        TypedQuery<BookingDetail> query
                = em.createQuery(
                        jpql,
                        BookingDetail.class
                );

        query.setParameter(
                "bookingID",
                bookingID
        );

        return query.getResultList();
    }

    private Long countAssignments(
            EntityManager em,
            String bookingID) {

        String jpql
                = "SELECT COUNT(ra) "
                + "FROM RoomAssignment ra "
                + "WHERE ra.bookingDetailID.bookingID."
                + "bookingID = :bookingID";

        TypedQuery<Long> query
                = em.createQuery(
                        jpql,
                        Long.class
                );

        query.setParameter(
                "bookingID",
                bookingID
        );

        return query.getSingleResult();
    }

    private void validateRoom(
            EntityManager em,
            Room room,
            BookingDetail detail,
            Booking booking) {

        if (room == null) {
            throw new IllegalArgumentException(
                    "One of the selected rooms does not exist."
            );
        }

        if (!"ACTIVE".equals(room.getStatus())) {
            throw new IllegalArgumentException(
                    "Room "
                    + room.getRoomNumber()
                    + " is not active."
            );
        }

        String requiredType
                = detail.getRoomTypeID()
                        .getRoomTypeID();

        String actualType
                = room.getRoomTypeID()
                        .getRoomTypeID();

        if (!requiredType.equals(actualType)) {
            throw new IllegalArgumentException(
                    "Room "
                    + room.getRoomNumber()
                    + " does not match the required room type."
            );
        }

        if (!isRoomAvailable(
                em,
                room,
                booking)) {

            throw new IllegalStateException(
                    "Room "
                    + room.getRoomNumber()
                    + " is no longer available for these dates."
            );
        }
    }

    private boolean isRoomAvailable(
            EntityManager em,
            Room room,
            Booking booking) {

        String jpql
                = "SELECT COUNT(ra) "
                + "FROM RoomAssignment ra "
                + "WHERE ra.roomID = :room "
                + "AND ra.bookingDetailID.bookingID."
                + "bookingStatus IN :statuses "
                + "AND ra.bookingDetailID.bookingID."
                + "checkInDate < :checkOutDate "
                + "AND ra.bookingDetailID.bookingID."
                + "checkOutDate > :checkInDate";

        TypedQuery<Long> query
                = em.createQuery(
                        jpql,
                        Long.class
                );

        query.setParameter("room", room);

        query.setParameter(
                "statuses",
                BLOCKING_STATUSES
        );

        query.setParameter(
                "checkInDate",
                booking.getCheckInDate()
        );

        query.setParameter(
                "checkOutDate",
                booking.getCheckOutDate()
        );

        return query.getSingleResult() == 0;
    }
}
