package dao;

import entity.Booking;
import entity.BookingDetail;
import entity.BookingService;
import entity.Employee;
import entity.Hotel;
import entity.Invoice;
import entity.RoomAssignment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.PersistenceManager;

public class InvoiceDAO {

    public List<Map<String, String>> getOccupiedRooms() {
        List<Map<String, String>> result
                = new ArrayList<>();

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT ra "
                    + "FROM RoomAssignment ra "
                    + "JOIN FETCH ra.roomID r "
                    + "JOIN FETCH r.roomTypeID rt "
                    + "JOIN FETCH ra.bookingDetailID bd "
                    + "JOIN FETCH bd.bookingID b "
                    + "JOIN FETCH b.customerID c "
                    + "LEFT JOIN FETCH c.nationalityID n "
                    + "WHERE b.bookingStatus = :status "
                    + "ORDER BY r.roomNumber";

            TypedQuery<RoomAssignment> query
                    = em.createQuery(
                            jpql,
                            RoomAssignment.class
                    );

            query.setParameter(
                    "status",
                    "CHECKED_IN"
            );

            List<RoomAssignment> assignments
                    = query.getResultList();

            Hotel hotel = em.find(Hotel.class, "H01");

            String hotelName = hotel != null
                    ? hotel.getHotelName()
                    : "";

            for (RoomAssignment assignment : assignments) {
                BookingDetail detail
                        = assignment.getBookingDetailID();

                Booking booking = detail.getBookingID();

                Map<String, String> room
                        = new HashMap<>();

                room.put(
                        "bookingID",
                        booking.getBookingID()
                );

                room.put(
                        "customerName",
                        booking.getCustomerID().getFullName()
                );

                room.put("hotelName", hotelName);

                room.put(
                        "roomID",
                        assignment.getRoomID().getRoomID()
                );

                room.put(
                        "roomNumber",
                        assignment.getRoomID().getRoomNumber()
                );

                room.put(
                        "roomType",
                        assignment.getRoomID()
                                .getRoomTypeID()
                                .getTypeName()
                );

                String nationality = "";

                if (booking.getCustomerID()
                        .getNationalityID() != null) {

                    nationality = booking.getCustomerID()
                            .getNationalityID()
                            .getNationalityName();
                }

                room.put("nationality", nationality);

                room.put(
                        "price",
                        detail.getUnitPrice().toString()
                );

                room.put(
                        "checkInDate",
                        booking.getCheckInDate().toString()
                );

                room.put(
                        "checkOutDate",
                        booking.getCheckOutDate().toString()
                );

                result.add(room);
            }

            return result;
        } finally {
            close(em);
        }
    }

    public List<Map<String, String>> getPaidInvoices() {
        List<Map<String, String>> result
                = new ArrayList<>();

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT i "
                    + "FROM Invoice i "
                    + "JOIN FETCH i.bookingID b "
                    + "JOIN FETCH b.customerID c "
                    + "WHERE EXISTS ("
                    + "    SELECT p.paymentID "
                    + "    FROM Payment p "
                    + "    WHERE p.bookingID = b "
                    + "    AND p.status = :paymentStatus"
                    + ") "
                    + "ORDER BY i.invoiceDate DESC";

            TypedQuery<Invoice> query = em.createQuery(
                    jpql,
                    Invoice.class
            );

            query.setParameter(
                    "paymentStatus",
                    "PAID"
            );

            query.setMaxResults(10);

            Hotel hotel = em.find(Hotel.class, "H01");

            String hotelName = hotel != null
                    ? hotel.getHotelName()
                    : "";

            for (Invoice invoiceEntity
                    : query.getResultList()) {

                Booking booking
                        = invoiceEntity.getBookingID();

                Map<String, String> invoice
                        = new HashMap<>();

                invoice.put(
                        "invoiceID",
                        invoiceEntity.getInvoiceID()
                );

                invoice.put(
                        "bookingID",
                        booking.getBookingID()
                );

                invoice.put(
                        "customerName",
                        booking.getCustomerID()
                                .getFullName()
                );

                invoice.put("hotelName", hotelName);

                invoice.put(
                        "roomNumber",
                        getAssignedRoomNumbers(
                                em,
                                booking
                        )
                );

                invoice.put(
                        "totalAmount",
                        invoiceEntity.getTotalAmount()
                                .toString()
                );

                invoice.put(
                        "invoiceDate",
                        invoiceEntity.getInvoiceDate()
                                .toString()
                );

                result.add(invoice);
            }

            return result;
        } finally {
            close(em);
        }
    }

    public boolean isBookingPaid(String bookingID) {
        EntityManager em = PersistenceManager.createEntityManager();

        try {
            Booking booking = em.find(
                    Booking.class,
                    bookingID
            );

            if (booking == null) {
                return false;
            }

            BigDecimal totalPaid = getTotalPaid(
                    em,
                    booking
            );

            return totalPaid.compareTo(
                    booking.getTotalAmount()
            ) >= 0;
        } finally {
            close(em);
        }
    }

    public List<Map<String, String>>
            getServicesByBooking(String bookingID) {

        List<Map<String, String>> result
                = new ArrayList<>();

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT bs "
                    + "FROM BookingService bs "
                    + "JOIN FETCH bs.serviceID s "
                    + "WHERE bs.bookingID.bookingID "
                    + "= :bookingID "
                    + "ORDER BY s.serviceName";

            TypedQuery<BookingService> query
                    = em.createQuery(
                            jpql,
                            BookingService.class
                    );

            query.setParameter(
                    "bookingID",
                    bookingID
            );

            for (BookingService bookingService
                    : query.getResultList()) {

                Map<String, String> serviceMap
                        = new HashMap<>();

                serviceMap.put(
                        "serviceID",
                        bookingService.getService()
                                .getServiceID()
                );

                serviceMap.put(
                        "serviceName",
                        bookingService.getService()
                                .getServiceName()
                );

                serviceMap.put(
                        "quantity",
                        "" + bookingService.getQuantity());

                serviceMap.put(
                        "unitPrice",
                        bookingService.getUnitPrice()
                                .toString()
                );

                serviceMap.put(
                        "subtotal",
                        bookingService.getSubtotal()
                                .toString()
                );

                result.add(serviceMap);
            }

            return result;
        } finally {
            close(em);
        }
    }

    public Map<String, String>
            getRoomDetailByBooking(String bookingID) {

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            Booking booking = em.find(
                    Booking.class,
                    bookingID
            );

            if (booking == null) {
                return null;
            }

            List<BookingDetail> details
                    = getBookingDetails(em, booking);

            List<BookingService> services
                    = getBookingServices(em, booking);

            BigDecimal roomTotal = BigDecimal.ZERO;
            BigDecimal serviceTotal = BigDecimal.ZERO;

            List<String> prices = new ArrayList<>();

            for (BookingDetail detail : details) {
                roomTotal = roomTotal.add(
                        detail.getSubtotal()
                );

                prices.add(
                        detail.getUnitPrice().toString()
                );
            }

            for (BookingService service : services) {
                serviceTotal = serviceTotal.add(
                        service.getSubtotal()
                );
            }

            LocalDate checkIn = toLocalDate(
                    booking.getCheckInDate()
            );

            LocalDate checkOut = toLocalDate(
                    booking.getCheckOutDate()
            );

            long totalDays = ChronoUnit.DAYS.between(
                    checkIn,
                    checkOut
            );

            Map<String, String> room
                    = new HashMap<>();

            room.put(
                    "bookingID",
                    booking.getBookingID()
            );

            room.put(
                    "customerName",
                    booking.getCustomerID().getFullName()
            );

            room.put(
                    "roomNumber",
                    getAssignedRoomNumbers(em, booking)
            );

            room.put(
                    "price",
                    String.join(", ", prices)
            );

            room.put(
                    "checkInDate",
                    booking.getCheckInDate().toString()
            );

            room.put(
                    "checkOutDate",
                    booking.getCheckOutDate().toString()
            );

            room.put(
                    "totalDays",
                    String.valueOf(totalDays)
            );

            room.put(
                    "roomTotal",
                    roomTotal.toString()
            );

            room.put(
                    "serviceTotal",
                    serviceTotal.toString()
            );

            room.put(
                    "baseTotal",
                    booking.getTotalAmount().toString()
            );

            return room;
        } finally {
            close(em);
        }
    }

    /*
     * This overload keeps existing servlet code working.
     * The amount received from the browser is intentionally
     * ignored. The trusted total is read from the database.
     */
    public boolean processCheckout(
            String bookingID,
            double ignoredTotalAmount,
            String employeeID) {

        return processCheckout(
                bookingID,
                employeeID
        );
    }

    public boolean processCheckout(
            String bookingID,
            String employeeID) {

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            em.getTransaction().begin();

            Booking booking = em.find(
                    Booking.class,
                    bookingID
            );

            if (booking == null) {
                throw new IllegalArgumentException(
                        "Booking not found: " + bookingID
                );
            }

            if (!"CHECKED_IN".equals(
                    booking.getBookingStatus())) {

                throw new IllegalStateException(
                        "Only a checked-in booking "
                        + "can be checked out."
                );
            }

            Employee employee = em.find(
                    Employee.class,
                    employeeID
            );

            if (employee == null) {
                throw new IllegalArgumentException(
                        "Employee not found: " + employeeID
                );
            }

            BigDecimal totalPaid = getTotalPaid(
                    em,
                    booking
            );

            if (totalPaid.compareTo(
                    booking.getTotalAmount()
            ) < 0) {

                throw new IllegalStateException(
                        "The booking has not been fully paid."
                );
            }

            Invoice invoice = findInvoiceByBooking(
                    em,
                    booking
            );

            if (invoice == null) {
                invoice = new Invoice();

                invoice.setInvoiceID(
                        generateInvoiceID(em)
                );

                invoice.setInvoiceType(
                        "Checkout Invoice"
                );

                invoice.setInvoiceDate(new Date());
                invoice.setBookingID(booking);

                em.persist(invoice);
            }

            invoice.setTotalAmount(
                    booking.getTotalAmount()
            );

            invoice.setEmployeeID(employee);

            booking.setBookingStatus("CHECKED_OUT");

            /*
             * Do not change CheckOutDate here.
             * It represents the booked stay date.
             *
             * Do not change Room.Status to AVAILABLE.
             * Room.Status only represents ACTIVE,
             * MAINTENANCE or INACTIVE.
             */
            em.getTransaction().commit();

            return true;
        } catch (IllegalArgumentException | IllegalStateException exception) {
            rollback(em);

            throw new IllegalStateException(
                    "Unable to process checkout.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    private Invoice findInvoiceByBooking(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT i "
                + "FROM Invoice i "
                + "WHERE i.bookingID = :booking";

        TypedQuery<Invoice> query = em.createQuery(
                jpql,
                Invoice.class
        );

        query.setParameter("booking", booking);
        query.setMaxResults(1);

        List<Invoice> invoices
                = query.getResultList();

        return invoices.isEmpty()
                ? null
                : invoices.get(0);
    }

    private List<BookingDetail> getBookingDetails(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT bd "
                + "FROM BookingDetail bd "
                + "WHERE bd.bookingID = :booking";

        TypedQuery<BookingDetail> query
                = em.createQuery(
                        jpql,
                        BookingDetail.class
                );

        query.setParameter("booking", booking);

        return query.getResultList();
    }

    private List<BookingService> getBookingServices(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT bs "
                + "FROM BookingService bs "
                + "WHERE bs.bookingID = :booking";

        TypedQuery<BookingService> query
                = em.createQuery(
                        jpql,
                        BookingService.class
                );

        query.setParameter("booking", booking);

        return query.getResultList();
    }

    private String getAssignedRoomNumbers(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT ra.roomID.roomNumber "
                + "FROM RoomAssignment ra "
                + "WHERE ra.bookingDetailID.bookingID "
                + "= :booking "
                + "ORDER BY ra.roomID.roomNumber";

        TypedQuery<String> query = em.createQuery(
                jpql,
                String.class
        );

        query.setParameter("booking", booking);

        List<String> roomNumbers
                = query.getResultList();

        if (roomNumbers.isEmpty()) {
            return "Pending assignment";
        }

        return String.join(", ", roomNumbers);
    }

    private BigDecimal getTotalPaid(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT p.amount "
                + "FROM Payment p "
                + "WHERE p.bookingID = :booking "
                + "AND p.status = :status";

        TypedQuery<BigDecimal> query
                = em.createQuery(
                        jpql,
                        BigDecimal.class
                );

        query.setParameter("booking", booking);
        query.setParameter("status", "PAID");

        BigDecimal totalPaid = BigDecimal.ZERO;

        for (BigDecimal amount
                : query.getResultList()) {

            if (amount != null) {
                totalPaid = totalPaid.add(amount);
            }
        }

        return totalPaid;
    }

    private String generateInvoiceID(
            EntityManager em) {

        String jpql
                = "SELECT i.invoiceID "
                + "FROM Invoice i";

        List<String> invoiceIDs = em.createQuery(
                jpql,
                String.class
        ).getResultList();

        int largestNumber = 0;

        for (String invoiceID : invoiceIDs) {
            if (invoiceID == null) {
                continue;
            }

            String normalizedID = invoiceID.trim();

            if (!normalizedID.matches("HD\\d+")) {
                continue;
            }

            int number = Integer.parseInt(
                    normalizedID.substring(2)
            );

            if (number > largestNumber) {
                largestNumber = number;
            }
        }

        return String.format(
                "HD%04d",
                largestNumber + 1
        );
    }

    private LocalDate toLocalDate(Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void rollback(EntityManager em) {
        if (em != null
                && em.getTransaction().isActive()) {

            em.getTransaction().rollback();
        }
    }

    private void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
