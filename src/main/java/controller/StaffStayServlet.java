package controller;

import entity.Booking;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import service.BookingLifecycleService;

@WebServlet(
        name = "StaffStayServlet",
        urlPatterns = {"/staff/stays"}
)
public class StaffStayServlet extends HttpServlet {

    private final BookingLifecycleService lifecycleService
            = new BookingLifecycleService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        if (!hasPermission(session)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this page."
            );

            return;
        }

        moveFlashMessage(request, session);

        List<Booking> bookings
                = lifecycleService
                        .getOperationalBookings();

        request.setAttribute(
                "bookings",
                bookings
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/staff-stays.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        if (!hasPermission(session)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to perform this action."
            );

            return;
        }

        String bookingID
                = request.getParameter("bookingID");

        String action
                = request.getParameter("action");

        try {
            if ("checkIn".equals(action)) {
                lifecycleService.checkIn(bookingID);

                session.setAttribute(
                        "lifecycleSuccess",
                        "The guest was checked in successfully."
                );
            } else if ("checkOut".equals(action)) {
                lifecycleService.checkOut(bookingID);

                session.setAttribute(
                        "lifecycleSuccess",
                        "The guest was checked out successfully."
                );
            } else {
                throw new IllegalArgumentException(
                        "Unsupported booking action."
                );
            }
        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            session.setAttribute(
                    "lifecycleError",
                    ex.getMessage()
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            session.setAttribute(
                    "lifecycleError",
                    "The booking status could not be updated."
            );
        }

        response.sendRedirect(
                request.getContextPath()
                + "/staff/stays"
        );
    }

    private boolean hasPermission(
            HttpSession session) {

        if (session == null) {
            return false;
        }

        Users user
                = (Users) session.getAttribute("user");

        if (user == null) {
            return false;
        }

        String role
                = (String) session.getAttribute("role");

        if (role == null) {
            role = user.getRole();
        }

        return "Staff".equalsIgnoreCase(role)
                || "Admin".equalsIgnoreCase(role);
    }

    private void moveFlashMessage(
            HttpServletRequest request,
            HttpSession session) {

        Object success
                = session.getAttribute(
                        "lifecycleSuccess"
                );

        Object error
                = session.getAttribute(
                        "lifecycleError"
                );

        if (success != null) {
            request.setAttribute(
                    "successMessage",
                    success
            );

            session.removeAttribute(
                    "lifecycleSuccess"
            );
        }

        if (error != null) {
            request.setAttribute(
                    "errorMessage",
                    error
            );

            session.removeAttribute(
                    "lifecycleError"
            );
        }
    }
}