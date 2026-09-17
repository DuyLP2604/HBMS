package controller;

import entity.BookingDetail;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import service.RoomAssignmentService;

@WebServlet(
        name = "RoomAssignmentServlet",
        urlPatterns = {"/staff/room-assignments"}
)
public class RoomAssignmentServlet extends HttpServlet {

    private final RoomAssignmentService assignmentService
            = new RoomAssignmentService();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session
                = request.getSession(false);

        Users user = session == null
                ? null
                : (Users) session.getAttribute("user");

        if (user == null
                || !hasStaffPermission(session, user)) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to perform this action."
            );

            return;
        }

        String bookingID
                = request.getParameter("bookingID");

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Booking ID is required."
            );

            return;
        }

        try {
            List<BookingDetail> details
                    = assignmentService
                            .getBookingDetails(
                                    bookingID.trim()
                            );

            Map<Integer, List<String>> selectedRooms
                    = new HashMap<>();

            for (BookingDetail detail : details) {
                String parameterName
                        = "rooms_"
                        + detail.getBookingDetailID();

                String[] roomIDs
                        = request.getParameterValues(
                                parameterName
                        );

                List<String> values
                        = roomIDs == null
                        ? new ArrayList<>()
                        : Arrays.asList(roomIDs);

                selectedRooms.put(
                        detail.getBookingDetailID(),
                        values
                );
            }

            assignmentService.assignRooms(
                    bookingID.trim(),
                    selectedRooms,
                    user.getUserID()
            );

            session.setAttribute(
                    "assignmentSuccess",
                    "Rooms were assigned successfully."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/bookings"
            );
        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            session.setAttribute(
                    "assignmentError",
                    ex.getMessage()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/bookings?bookingID="
                    + bookingID
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            session.setAttribute(
                    "assignmentError",
                    "Room assignment could not be completed."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/staff/bookings?bookingID="
                    + bookingID
            );
        }
    }

    private boolean hasStaffPermission(
            HttpSession session,
            Users user) {

        String role
                = (String) session.getAttribute("role");

        if (role == null) {
            role = user.getRole();
        }

        return "Staff".equalsIgnoreCase(role)
                || "Admin".equalsIgnoreCase(role);
    }
}