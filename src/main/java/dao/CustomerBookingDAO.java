package dao;

import dto.CustomerBookingItem;
import dto.CustomerBookingView;
import entity.Booking;
import entity.BookingDetail;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import util.PersistenceManager;

public class CustomerBookingDAO {

    public List<CustomerBookingView> getByUserID(
            int userID) {

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            String jpql
                    = "SELECT b FROM Booking b "
                    + "WHERE b.customerID.userID.userID = :userID "
                    + "ORDER BY b.bookingDate DESC";

            TypedQuery<Booking> query
                    = em.createQuery(
                            jpql,
                            Booking.class
                    );

            query.setParameter("userID", userID);

            query.setHint(
                    "eclipselink.refresh",
                    Boolean.TRUE
            );

            List<Booking> bookings
                    = query.getResultList();

            List<CustomerBookingView> result
                    = new ArrayList<>();

            for (Booking booking : bookings) {
                CustomerBookingView view
                        = mapBooking(
                                em,
                                booking
                        );

                result.add(view);
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to load your bookings.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public CustomerBookingView getDetail(
            String bookingID,
            int userID) {

        EntityManager em = PersistenceManager.createEntityManager();

        try {
            Booking booking = findOwnedBooking(
                    em,
                    bookingID,
                    userID
            );

            if (booking == null) {
                return null;
            }

            CustomerBookingView view
                    = mapBooking(em, booking);

            String detailJpql
                    = "SELECT bd FROM BookingDetail bd "
                    + "WHERE bd.bookingID = :booking "
                    + "ORDER BY bd.bookingDetailID ASC";

            TypedQuery<BookingDetail> detailQuery
                    = em.createQuery(
                            detailJpql,
                            BookingDetail.class
                    );

            detailQuery.setParameter(
                    "booking",
                    booking
            );

            List<BookingDetail> details
                    = detailQuery.getResultList();

            List<CustomerBookingItem> items
                    = new ArrayList<>();

            for (BookingDetail detail : details) {
                CustomerBookingItem item
                        = new CustomerBookingItem();

                item.setBookingDetailID(
                        detail.getBookingDetailID()
                );

                item.setRoomTypeID(
                        detail.getRoomTypeID()
                                .getRoomTypeID()
                );

                item.setRoomTypeName(
                        detail.getRoomTypeID()
                                .getTypeName()
                );

                item.setQuantity(
                        detail.getQuantity()
                );

                item.setGuestCount(
                        detail.getGuestCount()
                );

                item.setUnitPrice(
                        detail.getUnitPrice()
                );

                item.setSubtotal(
                        detail.getSubtotal()
                );

                item.setRoomNumbers(
                        loadAssignedRoomNumbers(
                                em,
                                detail
                        )
                );

                items.add(item);
            }

            view.setItems(items);

            return view;
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to load booking details.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    private Booking findOwnedBooking(
            EntityManager em,
            String bookingID,
            int userID) {

        try {
            String jpql
                    = "SELECT b FROM Booking b "
                    + "WHERE b.bookingID = :bookingID "
                    + "AND b.customerID.userID.userID = :userID";

            TypedQuery<Booking> query
                    = em.createQuery(
                            jpql,
                            Booking.class
                    );

            query.setParameter(
                    "bookingID",
                    bookingID
            );

            query.setParameter(
                    "userID",
                    userID
            );

            query.setHint(
                    "eclipselink.refresh",
                    Boolean.TRUE
            );

            return query.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    private CustomerBookingView mapBooking(
            EntityManager em,
            Booking booking) {

        CustomerBookingView view
                = new CustomerBookingView();

        view.setBookingID(
                booking.getBookingID()
        );

        view.setBookingDate(
                booking.getBookingDate()
        );

        view.setCheckInDate(
                booking.getCheckInDate()
        );

        view.setCheckOutDate(
                booking.getCheckOutDate()
        );

        view.setNumberOfNights(
                calculateNights(booking)
        );

        view.setBookingStatus(
                booking.getBookingStatus()
        );

        view.setTotalAmount(
                booking.getTotalAmount()
        );

        view.setPaymentStatus(
                getLatestPaymentStatus(
                        em,
                        booking
                )
        );

        view.setPaymentDeadline(
                booking.getPaymentDeadline()
        );

        return view;
    }

    private List<String> loadAssignedRoomNumbers(
            EntityManager em,
            BookingDetail detail) {

        String jpql
                = "SELECT ra.roomID.roomNumber "
                + "FROM RoomAssignment ra "
                + "WHERE ra.bookingDetailID = :detail "
                + "ORDER BY ra.roomID.roomNumber ASC";

        TypedQuery<String> query
                = em.createQuery(
                        jpql,
                        String.class
                );

        query.setParameter("detail", detail);

        return query.getResultList();
    }

    private String getLatestPaymentStatus(
            EntityManager em,
            Booking booking) {

        String jpql
                = "SELECT p.status FROM Payment p "
                + "WHERE p.bookingID = :booking "
                + "ORDER BY p.paymentTime DESC";

        TypedQuery<String> query
                = em.createQuery(
                        jpql,
                        String.class
                );

        query.setParameter("booking", booking);
        query.setMaxResults(1);

        List<String> result
                = query.getResultList();

        if (result.isEmpty()) {
            return "UNPAID";
        }

        return result.get(0);
    }

    private long calculateNights(
            Booking booking) {

        LocalDate checkIn
                = new Date(
                        booking.getCheckInDate()
                                .getTime()
                ).toLocalDate();

        LocalDate checkOut
                = new Date(
                        booking.getCheckOutDate()
                                .getTime()
                ).toLocalDate();

        return ChronoUnit.DAYS.between(
                checkIn,
                checkOut
        );
    }
}
