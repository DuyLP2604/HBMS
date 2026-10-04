package controller;

import dto.BookingWishList;
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
import util.BookingWishListSession;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout"})
public class CheckoutServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Object bookingID = session.getAttribute("pendingBookingID");

        if (bookingID == null) {
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");
            return;
        }

        request.setAttribute("bookingID", bookingID.toString());
        session.removeAttribute("pendingBookingID");
        request.getRequestDispatcher("/WEB-INF/views/checkout-pending.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        Users user = (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        BookingWishList wishlist = BookingWishListSession.get(session);
        BookingCheckoutService checkoutService = new BookingCheckoutService();

        if (wishlist == null || wishlist.isEmpty()) {
            session.setAttribute("bookingError", "Your wish list is empty.");
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");
            return;
        }

        try {
            Booking booking = checkoutService.createPendingBooking(wishlist, user.getUserID());
            BookingWishListSession.remove(session); // Clear wishlist after transaction succeeds
            session.setAttribute("pendingBookingID", booking.getBookingID());
            response.sendRedirect(request.getContextPath() + "/checkout");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            session.setAttribute("bookingError", ex.getMessage());
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");
        } catch (IOException ex) {
            session.setAttribute("bookingError", "Checkout could not be completed.");
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");
        }
    }
}
