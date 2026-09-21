package controller;

import dto.BookingCart;
import dto.BookingCartItem;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import util.BookingCartSession;

@WebServlet(
        name = "BookingCartServlet",
        urlPatterns = {"/booking-cart"}
)
public class BookingCartServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        moveFlashMessage(request);

        BookingCart cart
                = BookingCartSession.getOrCreate(session);

        Map<String, BigDecimal> subtotals
                = new HashMap<>();

        for (BookingCartItem item : cart.getItems()) {
            subtotals.put(
                    item.getRoomTypeID(),
                    item.calculateSubtotal(
                            cart.getNumberOfNights()
                    )
            );
        }

        request.setAttribute("cart", cart);
        request.setAttribute("subtotals", subtotals);

        request.getRequestDispatcher(
                "/WEB-INF/views/booking-cart.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession();

        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        BookingCart cart
                = BookingCartSession.getOrCreate(session);

        String action = request.getParameter("action");

        if ("remove".equals(action)) {
            String roomTypeID
                    = request.getParameter("roomTypeID");

            boolean removed
                    = cart.removeItem(roomTypeID);

            if (removed) {
                session.setAttribute(
                        "bookingSuccess",
                        "The room type was removed from your cart."
                );
            } else {
                session.setAttribute(
                        "bookingError",
                        "The requested cart item was not found."
                );
            }
        } else if ("clear".equals(action)) {
            cart.clear();

            session.setAttribute(
                    "bookingSuccess",
                    "Your booking cart was cleared."
            );
        } else {
            session.setAttribute(
                    "bookingError",
                    "Unsupported booking cart action."
            );
        }

        BookingCartSession.save(session, cart);

        response.sendRedirect(
                request.getContextPath() + "/booking-cart"
        );
    }

    private void moveFlashMessage(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return;
        }

        Object success
                = session.getAttribute("bookingSuccess");

        Object error
                = session.getAttribute("bookingError");

        if (success != null) {
            request.setAttribute(
                    "successMessage",
                    success.toString()
            );

            session.removeAttribute("bookingSuccess");
        }

        if (error != null) {
            request.setAttribute(
                    "errorMessage",
                    error.toString()
            );

            session.removeAttribute("bookingError");
        }
    }
}