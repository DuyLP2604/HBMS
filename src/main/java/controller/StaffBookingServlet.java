package controller;

import entity.Booking;
import entity.BookingDetail;
import entity.Room;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import service.RoomAssignmentService;

@WebServlet(
        name = "StaffBookingServlet",
        urlPatterns = {"/staff/bookings"}
)
public class StaffBookingServlet extends HttpServlet {

    private final RoomAssignmentService assignmentService
            = new RoomAssignmentService();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        if (!hasStaffPermission(session)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this page."
            );

            return;
        }

        moveFlashMessage(request, session);

        String bookingID
                = request.getParameter("bookingID");

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            List<Booking> bookings
                    = assignmentService
                            .getWaitingBookings();

            request.setAttribute(
                    "bookings",
                    bookings
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/staff-bookings.jsp"
            ).forward(request, response);

            return;
        }

        Booking booking
                = assignmentService.getBooking(
                        bookingID.trim()
                );

        if (booking == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "The requested booking was not found."
            );

            return;
        }

        List<BookingDetail> details
                = assignmentService
                        .getBookingDetails(
                                bookingID.trim()
                        );

        Map<Integer, List<Room>> availableRoomMap
                = new HashMap<>();

        for (BookingDetail detail : details) {
            List<Room> rooms
                    = assignmentService
                            .getAvailableRooms(
                                    detail.getBookingDetailID()
                            );

            availableRoomMap.put(
                    detail.getBookingDetailID(),
                    rooms
            );
        }

        request.setAttribute("booking", booking);
        request.setAttribute("details", details);

        request.setAttribute(
                "availableRoomMap",
                availableRoomMap
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/staff-booking-detail.jsp"
        ).forward(request, response);
    }

    private boolean hasStaffPermission(
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
                        "assignmentSuccess"
                );

        Object error
                = session.getAttribute(
                        "assignmentError"
                );

        if (success != null) {
            request.setAttribute(
                    "successMessage",
                    success.toString()
            );

            session.removeAttribute(
                    "assignmentSuccess"
            );
        }

        if (error != null) {
            request.setAttribute(
                    "errorMessage",
                    error.toString()
            );

            session.removeAttribute(
                    "assignmentError"
            );
        }
    }
}