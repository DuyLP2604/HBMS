package controller;

import dto.BookingCart;
import entity.Booking;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import service.BookingCheckoutService;
import util.BookingCartSession;

@WebServlet(
        name = "CheckoutServlet",
        urlPatterns = {"/checkout"}
)
public class CheckoutServlet extends HttpServlet {

    private final BookingCheckoutService checkoutService
            = new BookingCheckoutService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        Object bookingID
                = session.getAttribute(
                        "pendingBookingID"
                );

        if (bookingID == null) {
            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-cart"
            );

            return;
        }

        request.setAttribute(
                "bookingID",
                bookingID.toString()
        );

        session.removeAttribute(
                "pendingBookingID"
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/checkout-pending.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession();

        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        BookingCart cart
                = BookingCartSession.get(session);

        if (cart == null || cart.isEmpty()) {
            session.setAttribute(
                    "bookingError",
                    "Your booking cart is empty."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-cart"
            );

            return;
        }

        try {
            Booking booking
                    = checkoutService.createPendingBooking(
                            cart,
                            user.getUserID()
                    );

            /*
             * Only clear the cart after the transaction succeeds.
             */
            BookingCartSession.remove(session);

            session.setAttribute(
                    "pendingBookingID",
                    booking.getBookingID()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/checkout"
            );
        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            session.setAttribute(
                    "bookingError",
                    ex.getMessage()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-cart"
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            session.setAttribute(
                    "bookingError",
                    "Checkout could not be completed."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/booking-cart"
            );
        }
    }
}