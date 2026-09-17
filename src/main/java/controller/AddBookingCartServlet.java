package controller;

import dao.RoomTypeDAO;
import dto.BookingCart;
import dto.BookingCartItem;
import dto.RoomTypeAvailability;
import entity.RoomType;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import util.BookingCartSession;

@WebServlet(
        name = "AddBookingCartServlet",
        urlPatterns = {"/booking-cart/add"}
)
public class AddBookingCartServlet extends HttpServlet {

    private final RoomTypeDAO roomTypeDAO
            = new RoomTypeDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();

        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            session.setAttribute(
                    "bookingError",
                    "Please sign in before adding rooms."
            );

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        String checkInValue
                = request.getParameter("checkInDate");

        String checkOutValue
                = request.getParameter("checkOutDate");

        String roomTypeID
                = request.getParameter("roomTypeID");

        String quantityValue
                = request.getParameter("quantity");

        String guestCountValue
                = request.getParameter("guestCount");

        String returnURL = buildReturnURL(
                request,
                checkInValue,
                checkOutValue
        );

        try {
            LocalDate checkInDate
                    = LocalDate.parse(checkInValue);

            LocalDate checkOutDate
                    = LocalDate.parse(checkOutValue);

            if (!checkOutDate.isAfter(checkInDate)) {
                throw new IllegalArgumentException(
                        "Check-out date must be later than check-in date."
                );
            }

            if (roomTypeID == null
                    || roomTypeID.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Room type is required."
                );
            }

            int quantity
                    = Integer.parseInt(quantityValue);

            int guestCount
                    = Integer.parseInt(guestCountValue);

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Room quantity must be greater than zero."
                );
            }

            if (guestCount <= 0) {
                throw new IllegalArgumentException(
                        "Guest count must be greater than zero."
                );
            }

            RoomType roomType
                    = roomTypeDAO.getById(roomTypeID);

            if (roomType == null) {
                throw new IllegalArgumentException(
                        "The selected room type does not exist."
                );
            }

            List<RoomTypeAvailability> availabilityList
                    = roomTypeDAO.getAvailability(
                            checkInDate,
                            checkOutDate
                    );

            RoomTypeAvailability availability
                    = findAvailability(
                            availabilityList,
                            roomTypeID
                    );

            if (availability == null) {
                throw new IllegalArgumentException(
                        "Availability information is unavailable."
                );
            }

            if (quantity > availability.getAvailableRooms()) {
                throw new IllegalArgumentException(
                        "Only "
                        + availability.getAvailableRooms()
                        + " room(s) are available for this room type."
                );
            }

            int maximumGuests
                    = roomType.getCapacity() * quantity;

            if (guestCount > maximumGuests) {
                throw new IllegalArgumentException(
                        "The selected rooms can accommodate a maximum of "
                        + maximumGuests
                        + " guest(s)."
                );
            }

            BookingCart cart
                    = BookingCartSession.getOrCreate(session);

            if (!cart.isEmpty()) {
                boolean sameDates
                        = checkInDate.equals(
                                cart.getCheckInDate()
                        )
                        && checkOutDate.equals(
                                cart.getCheckOutDate()
                        );

                if (!sameDates) {
                    throw new IllegalArgumentException(
                            "Your cart contains rooms for different dates. "
                            + "Please clear the cart before changing the stay dates."
                    );
                }
            } else {
                cart.setStayDates(
                        checkInDate,
                        checkOutDate
                );
            }

            BookingCartItem item
                    = new BookingCartItem(
                            roomType.getRoomTypeID(),
                            roomType.getTypeName(),
                            roomType.getCapacity(),
                            roomType.getPrice(),
                            quantity,
                            guestCount
                    );

            cart.addOrUpdateItem(item);

            BookingCartSession.save(
                    session,
                    cart
            );

            session.setAttribute(
                    "bookingSuccess",
                    roomType.getTypeName()
                    + " was added to your booking cart."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-cart"
            );
        } catch (NumberFormatException ex) {
            session.setAttribute(
                    "bookingError",
                    "Room quantity and guest count must be valid numbers."
            );

            response.sendRedirect(returnURL);
        } catch (IllegalArgumentException ex) {
            session.setAttribute(
                    "bookingError",
                    ex.getMessage()
            );

            response.sendRedirect(returnURL);
        } catch (Exception ex) {
            ex.printStackTrace();

            session.setAttribute(
                    "bookingError",
                    "Unable to add the selected room to your cart."
            );

            response.sendRedirect(returnURL);
        }
    }

    private RoomTypeAvailability findAvailability(
            List<RoomTypeAvailability> list,
            String roomTypeID) {

        for (RoomTypeAvailability item : list) {
            if (item.getRoomTypeID()
                    .equalsIgnoreCase(roomTypeID.trim())) {

                return item;
            }
        }

        return null;
    }

    private String buildReturnURL(
            HttpServletRequest request,
            String checkInDate,
            String checkOutDate) {

        String url = request.getContextPath()
                + "/room-types";

        if (checkInDate != null
                && checkOutDate != null) {

            url += "?checkInDate=" + checkInDate
                    + "&checkOutDate=" + checkOutDate;
        }

        return url;
    }
}