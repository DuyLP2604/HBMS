package controller;

import dao.EmployeeDAO;
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

    private final EmployeeDAO employeeDAO
            = new EmployeeDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session = request.getSession(false);

        Users user = session == null
                ? null
                : (Users) session.getAttribute("user");

        // User has not logged in
        if (user == null) {
            response.sendError(
                    HttpServletResponse.SC_UNAUTHORIZED,
                    "You must log in to perform this action."
            );
            return;
        }

        // Only receptionists can assign rooms
        if (!isReceptionist(user)) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only receptionists can assign rooms."
            );
            return;
        }

        String bookingID = trimParameter(
                request.getParameter("bookingID")
        );

        if (bookingID == null || bookingID.isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Booking ID is required."
            );
            return;
        }

        try {
            List<BookingDetail> details
                    = assignmentService.getBookingDetails(
                            bookingID
                    );

            if (details == null || details.isEmpty()) {
                throw new IllegalArgumentException(
                        "No booking details were found."
                );
            }

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

                List<String> roomValues;

                if (roomIDs == null) {
                    roomValues = new ArrayList<>();
                } else {
                    roomValues = new ArrayList<>(
                            Arrays.asList(roomIDs)
                    );
                }

                selectedRooms.put(
                        detail.getBookingDetailID(),
                        roomValues
                );
            }

            /*
             * The user ID is taken from the authenticated session.
             * It is not taken from a form parameter.
             */
            assignmentService.assignRooms(
                    bookingID,
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

        } catch (SecurityException ex) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    ex.getMessage()
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

    /**
     * Checks whether the logged-in user is a receptionist.
     */
    private boolean isReceptionist(Users user) {
        if (user == null) {
            return false;
        }

        if (!"Staff".equalsIgnoreCase(user.getRole())) {
            return false;
        }

        return employeeDAO.isReceptionistByUserId(
                user.getUserID()
        );
    }

    /**
     * Removes surrounding spaces from request parameters.
     */
    private String trimParameter(String value) {
        return value == null
                ? null
                : value.trim();
    }
}