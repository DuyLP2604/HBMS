package controller;

import dao.CustomerBookingDAO;
import dto.CustomerBookingView;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import service.BookingExpirationService;

@WebServlet(
        name = "MyBookingServlet",
        urlPatterns = {"/my-bookings"}
)
public class MyBookingServlet extends HttpServlet {

    private final CustomerBookingDAO bookingDAO
            = new CustomerBookingDAO();

    private final BookingExpirationService expirationService
            = new BookingExpirationService();

    @Override
    protected void doGet(
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

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            showBookingList(
                    request,
                    response,
                    user
            );

            return;
        }

        showBookingDetail(
                request,
                response,
                user,
                bookingID.trim()
        );

        /*
        * Update expired bookings before loading the page.
        */
        expirationService.expirePendingBookings();
        moveFlashMessage(request, session);
    }

    private void showBookingList(
            HttpServletRequest request,
            HttpServletResponse response,
            Users user)
            throws ServletException, IOException {

        List<CustomerBookingView> bookings
                = bookingDAO.getByUserID(
                        user.getUserID()
                );

        request.setAttribute(
                "bookings",
                bookings
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/my-bookings.jsp"
        ).forward(request, response);
    }

    private void showBookingDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            Users user,
            String bookingID)
            throws ServletException, IOException {

        CustomerBookingView booking
                = bookingDAO.getDetail(
                        bookingID,
                        user.getUserID()
                );

        if (booking == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "The requested booking was not found."
            );

            return;
        }

        request.setAttribute(
                "booking",
                booking
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/my-booking-detail.jsp"
        ).forward(request, response);
    }

    private void moveFlashMessage(
            HttpServletRequest request,
            HttpSession session) {

        Object success = session.getAttribute(
                "customerBookingSuccess"
        );

        Object error = session.getAttribute(
                "customerBookingError"
        );

        if (success != null) {
            request.setAttribute(
                    "successMessage",
                    success
            );

            session.removeAttribute(
                    "customerBookingSuccess"
            );
        }

        if (error != null) {
            request.setAttribute(
                    "errorMessage",
                    error
            );

            session.removeAttribute(
                    "customerBookingError"
            );
        }
    }
}
