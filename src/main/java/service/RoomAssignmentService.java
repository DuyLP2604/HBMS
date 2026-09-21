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

public class RoomAssignmentService {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    private static final List<String> BLOCKING_STATUSES
            = Arrays.asList(
                    "CONFIRMED",
                    "ASSIGNED",
                    "CHECKED_IN"
            );
    
    private final EmployeeDAO employeeDAO
        = new EmployeeDAO();

    public List<Booking> getWaitingBookings() {
        EntityManager em = EMF.createEntityManager();

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

            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public Booking getBooking(String bookingID) {
        EntityManager em = EMF.createEntityManager();

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

        EntityManager em = EMF.createEntityManager();

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

            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<Room> getAvailableRooms(
            Integer bookingDetailID) {

        EntityManager em = EMF.createEntityManager();

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

            return query.getResultList();
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public void assignRooms(
            String bookingID,
            Map<Integer, List<String>> selectedRooms,
            int userID) {

        EntityManager em = EMF.createEntityManager();

        if (!employeeDAO.isReceptionistByUserId(userID)) {
            throw new SecurityException(
                    "Only receptionists can assign rooms."
            );
        }

        try {
            em.getTransaction().begin();

            Booking booking = em.find(
                    Booking.class,
                    bookingID,
                    LockModeType.PESSIMISTIC_WRITE
            );

            if (booking == null) {
                throw new IllegalStateException(
                        "The requested booking was not found."
                );
            }

            if (!"CONFIRMED".equals(
                    booking.getBookingStatus())) {

                throw new IllegalStateException(
                        "Only confirmed bookings can be assigned."
                );
            }

            Employee employee = findEmployeeByUserID(
                    em,
                    userID
            );

            if (employee == null) {
                throw new IllegalStateException(
                        "No employee profile is associated with this account."
                );
            }

            List<BookingDetail> details
                    = loadDetails(
                            em,
                            bookingID
                    );

            if (details.isEmpty()) {
                throw new IllegalStateException(
                        "This booking does not contain any room requests."
                );
            }

            Long existingAssignments
                    = countAssignments(
                            em,
                            bookingID
                    );

            if (existingAssignments > 0) {
                throw new IllegalStateException(
                        "Rooms have already been assigned to this booking."
                );
            }

            Set<String> allSelectedRoomIDs
                    = new HashSet<>();

            for (BookingDetail detail : details) {
                List<String> roomIDs
                        = selectedRooms.get(
                                detail.getBookingDetailID()
                        );

                if (roomIDs == null) {
                    throw new IllegalArgumentException(
                            "Please select rooms for "
                            + detail.getRoomTypeID()
                                    .getTypeName()
                            + "."
                    );
                }

                Set<String> uniqueRoomIDs
                        = new HashSet<>(roomIDs);

                if (uniqueRoomIDs.size()
                        != detail.getQuantity()) {

                    throw new IllegalArgumentException(
                            "Please select exactly "
                            + detail.getQuantity()
                            + " room(s) for "
                            + detail.getRoomTypeID()
                                    .getTypeName()
                            + "."
                    );
                }

                for (String roomID : uniqueRoomIDs) {
                    if (!allSelectedRoomIDs.add(roomID)) {
                        throw new IllegalArgumentException(
                                "The same room cannot be assigned twice."
                        );
                    }

                    Room room = em.find(
                            Room.class,
                            roomID,
                            LockModeType.PESSIMISTIC_WRITE
                    );

                    validateRoom(
                            em,
                            room,
                            detail,
                            booking
                    );

                    RoomAssignment assignment
                            = new RoomAssignment();

                    assignment.setBookingDetailID(detail);
                    assignment.setRoomID(room);
                    assignment.setEmployeeID(employee);
                    assignment.setAssignedAt(new Date());

                    em.persist(assignment);
                }
            }

            booking.setBookingStatus("ASSIGNED");

            em.flush();
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }

            if (ex instanceof IllegalArgumentException) {
                throw (IllegalArgumentException) ex;
            }

            if (ex instanceof IllegalStateException) {
                throw (IllegalStateException) ex;
            }

            throw new RuntimeException(
                    "Unable to assign rooms.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
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
