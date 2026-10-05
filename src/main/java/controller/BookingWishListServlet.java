package controller;

import dao.RoomTypeDAO;
import dto.BookingWishList;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import util.BookingWishListSession;

@WebServlet(name = "BookingWishListServlet", urlPatterns = {"/booking-wish-list"})
public class BookingWishListServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        sendMessage(request);

        BookingWishList wishlist = BookingWishListSession.getOrCreate(session);
        Map<String, BigDecimal> subtotals = new HashMap<>();

        for (BookingCartItem item : wishlist.getItems()) {
            subtotals.put(
                    item.getRoomTypeID(),
                    item.calculateSubtotal(wishlist.getNumberOfNights())
            );
        }

        request.setAttribute("wishList", wishlist);
        request.setAttribute("subtotals", subtotals);

        request.getRequestDispatcher("/WEB-INF/views/booking-wish-list.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            session.setAttribute("bookingError", "Please sign in before modifying your wishlist.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        BookingWishList wishlist = BookingWishListSession.getOrCreate(session);

        if ("add".equals(action)) {
            handleAdd(request, response, session, wishlist);
        } else if ("remove".equals(action)) {
            handleRemove(request, response, session, wishlist);
        } else if ("clear".equals(action)) {
            handleClear(request, response, session, wishlist);
        } else {
            session.setAttribute("bookingError", "Unsupported wishlist action.");
            BookingWishListSession.save(session, wishlist);
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");
        }
    }

    private void handleAdd(HttpServletRequest request, HttpServletResponse response, HttpSession session, BookingWishList wishlist) throws IOException {
        RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
        String checkInValue = request.getParameter("checkInDate");
        String checkOutValue = request.getParameter("checkOutDate");
        String roomTypeID = request.getParameter("roomTypeID");
        String quantityValue = request.getParameter("quantity");
        String guestCountValue = request.getParameter("guestCount");

        String returnURL = buildReturnURL(request, checkInValue, checkOutValue);

        try {
            LocalDate checkInDate = LocalDate.parse(checkInValue);
            LocalDate checkOutDate = LocalDate.parse(checkOutValue);

            if (!checkOutDate.isAfter(checkInDate)) {
                throw new IllegalArgumentException("Check-out date must be later than check-in date.");
            }
            if (roomTypeID == null || roomTypeID.trim().isEmpty()) {
                throw new IllegalArgumentException("Room type is required.");
            }

            int quantity = Integer.parseInt(quantityValue);
            int guestCount = Integer.parseInt(guestCountValue);

            if (quantity <= 0) {
                throw new IllegalArgumentException("Room quantity must be greater than zero.");
            }
            if (guestCount <= 0) {
                throw new IllegalArgumentException("Guest count must be greater than zero.");
            }

            RoomType roomType = roomTypeDAO.getById(roomTypeID);
            if (roomType == null) {
                throw new IllegalArgumentException("The selected room type does not exist.");
            }

            List<RoomTypeAvailability> availabilityList = roomTypeDAO.getAvailability(checkInDate, checkOutDate);
            RoomTypeAvailability availability = findAvailability(availabilityList, roomTypeID);

            if (availability == null) {
                throw new IllegalArgumentException("Availability information is unavailable.");
            }
            if (quantity > availability.getAvailableRooms()) {
                throw new IllegalArgumentException("Only " + availability.getAvailableRooms() + " room(s) are available for this room type.");
            }

            int maximumGuests = roomType.getCapacity() * quantity;
            if (guestCount > maximumGuests) {
                throw new IllegalArgumentException("The selected rooms can accommodate a maximum of " + maximumGuests + " guest(s).");
            }

            if (!wishlist.isEmpty()) {
                boolean sameDates = checkInDate.equals(wishlist.getCheckInDate()) && checkOutDate.equals(wishlist.getCheckOutDate());
                if (!sameDates) {
                    throw new IllegalArgumentException("Your wishlist contains rooms for different dates. Please clear it before changing the stay dates.");
                }
            } else {
                wishlist.setStayDates(checkInDate, checkOutDate);
            }

            BookingCartItem item = new BookingCartItem(
                    roomType.getRoomTypeID(),
                    roomType.getTypeName(),
                    roomType.getCapacity(),
                    roomType.getPrice(),
                    quantity,
                    guestCount
            );

            wishlist.addOrUpdateItem(item);
            BookingWishListSession.save(session, wishlist);

            session.setAttribute("bookingSuccess", roomType.getTypeName() + " was added to your wishlist.");
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");

        } catch (NumberFormatException ex) {
            session.setAttribute("bookingError", "Room quantity and guest count must be valid numbers.");
            response.sendRedirect(returnURL);
        } catch (IllegalArgumentException ex) {
            session.setAttribute("bookingError", ex.getMessage());
            response.sendRedirect(returnURL);
        } catch (IOException ex) {
            session.setAttribute("bookingError", "Unable to add the selected room to your wishlist.");
            response.sendRedirect(returnURL);
        }
    }

    private void handleRemove(HttpServletRequest request, HttpServletResponse response, HttpSession session, BookingWishList wishlist) throws IOException {
        String roomTypeID = request.getParameter("roomTypeID");
        boolean removed = wishlist.removeItem(roomTypeID);

        if (removed) {
            session.setAttribute("bookingSuccess", "The room type was removed from your wishlist.");
        } else {
            session.setAttribute("bookingError", "The requested item was not found.");
        }
        BookingWishListSession.save(session, wishlist);
        response.sendRedirect(request.getContextPath() + "/booking-wish-list");
    }

    private void handleClear(HttpServletRequest request, HttpServletResponse response, HttpSession session, BookingWishList wishlist) throws IOException {
        wishlist.clear();
        session.setAttribute("bookingSuccess", "Your wishlist was cleared.");
        BookingWishListSession.save(session, wishlist);
        response.sendRedirect(request.getContextPath() + "/booking-wish-list");
    }

    private void sendMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }

        Object success = session.getAttribute("bookingSuccess");
        Object error = session.getAttribute("bookingError");

        if (success != null) {
            request.setAttribute("successMessage", success.toString());
            session.removeAttribute("bookingSuccess");
        }
        if (error != null) {
            request.setAttribute("errorMessage", error.toString());
            session.removeAttribute("bookingError");
        }
    }

    private RoomTypeAvailability findAvailability(List<RoomTypeAvailability> list, String roomTypeID) {
        for (RoomTypeAvailability item : list) {
            if (item.getRoomTypeID().equalsIgnoreCase(roomTypeID.trim())) {
                return item;
            }
        }
        return null;
    }

    /**
     * Build URL redirect to last URL customer seen(skip choose check-in and
     * check-out date)
     *
     * @param request
     * @param checkInDate
     * @param checkOutDate
     * @return
     */
    private String buildReturnURL(HttpServletRequest request, String checkInDate, String checkOutDate) {
        String url = request.getContextPath() + "/room-types";
        if (checkInDate != null && checkOutDate != null) {
            url += "?checkInDate=" + checkInDate + "&checkOutDate=" + checkOutDate;
        }
        return url;
    }
}
