package controller;

import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import service.BookingLifecycleService;

@WebServlet(
        name = "CancelBookingServlet",
        urlPatterns = {"/my-bookings/cancel"}
)
public class CancelBookingServlet extends HttpServlet {

    private final BookingLifecycleService lifecycleService
            = new BookingLifecycleService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        Users user = session == null
                ? null
                : (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        String bookingID
                = request.getParameter("bookingID");

        try {
            lifecycleService.cancelByCustomer(
                    bookingID,
                    user.getUserID()
            );

            session.setAttribute(
                    "customerBookingSuccess",
                    "Your booking was cancelled successfully."
            );
        } catch (IllegalStateException ex) {
            session.setAttribute(
                    "customerBookingError",
                    ex.getMessage()
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            session.setAttribute(
                    "customerBookingError",
                    "The booking could not be cancelled."
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/my-bookings"
        );
    }
}