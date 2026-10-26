package controller;

import dao.BookingDAO;
import dao.EmployeeDAO;
import entity.Booking;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import util.BookingTimePolicy;
import util.PersistenceManager;

@WebServlet(name = "ProcessCheckInServlet", urlPatterns = {"/process-checkin"})
public class ProcessCheckInServlet extends HttpServlet {

    private Users requireReceptionist(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users)) {
            res.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) value;
        if (!"Staff".equalsIgnoreCase(user.getRole()) || user.getUserID() == null
                || !new EmployeeDAO().isReceptionistByUserId(user.getUserID())) {
            res.sendError(403);
            return null;
        }
        res.setHeader("Cache-Control", "no-store");
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws ServletException, IOException {

        if (requireReceptionist(req, res) == null) {
            return;
        }

        String bookingID = req.getParameter("bookingID");

        if (bookingID == null
                || !bookingID.trim().matches("BK[0-9]{4}")) {
            res.sendError(HttpServletResponse.SC_BAD_REQUEST,
                    "Invalid booking ID.");
            return;
        }

        Booking booking = new BookingDAO()
                .getByIdWithDetails(bookingID.trim());

        if (booking == null) {
            res.sendError(HttpServletResponse.SC_NOT_FOUND,
                    "Booking not found.");
            return;
        }

        HttpSession session = req.getSession(false);

        if (!"ASSIGNED".equals(booking.getBookingStatus())) {
            session.setAttribute("lifecycleError",
                    "Please assign all rooms before check-in.");
            res.sendRedirect(req.getContextPath() + "/staff/stays");
            return;
        }

        synchronized (session) {
            if (session.getAttribute("checkInCsrf") == null) {
                session.setAttribute(
                        "checkInCsrf",
                        UUID.randomUUID().toString()
                );
            }
        }

        req.setAttribute("booking", booking);
        req.getRequestDispatcher("/WEB-INF/views/checkin-form.jsp")
                .forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Users actor = requireReceptionist(req, res);
        if (actor == null) {
            return;
        }
        HttpSession session = req.getSession(false);
        if (session.getAttribute("checkInCsrf") == null
                || !Objects.equals(session.getAttribute("checkInCsrf"), req.getParameter("csrfToken"))) {
            res.sendError(403);
            return;
        }
        String id = req.getParameter("bookingID");
        if (id == null || !id.trim().matches("BK[0-9]{4}")) {
            res.sendError(400);
            return;
        }
        id = id.trim();
        try {
            if (!"confirmCheckIn".equals(req.getParameter("action")) || !"true".equals(req.getParameter("roomsReady"))) {
                throw new IllegalArgumentException("Confirm that the assigned rooms are clean and ready.");
            }
            String[] documents = req.getParameterValues("cccd");
            if (documents == null || documents.length == 0 || documents.length > 100) {
                throw new IllegalArgumentException("Enter valid guest identity documents.");
            }
            List<String> values = new ArrayList<>();
            for (String value : documents) {
                if (value == null || !value.trim().matches("[A-Za-z0-9]{6,20}")) {
                    throw new IllegalArgumentException("Use 6–20 letters or digits for a CCCD/passport number.");
                }
                if (values.contains(value.trim())) {
                    throw new IllegalArgumentException("Guest documents must not be duplicated.");
                }
                values.add(value.trim());
            }
            String joined = String.join(", ", values);
            if (joined.length() > 1000) {
                throw new IllegalArgumentException("Guest documents are too long.");
            }
            try (Connection connection = PersistenceManager.openConnection(); PreparedStatement statement = connection.prepareStatement("EXEC dbo.SP_COUNTER_CHECKIN @BookingID=?, @ActorUserID=?, @GuestDocuments=?")) {
                statement.setString(1, id);
                statement.setInt(2, actor.getUserID());
                statement.setString(3, joined);
                boolean result = statement.execute();
                while (true) {
                    if (result) {
                        try (ResultSet rows = statement.getResultSet()) {
                            while (rows.next()) {
                            }
                        }
                    } else if (statement.getUpdateCount() == -1) {
                        break;
                    }
                    result = statement.getMoreResults(Statement.CLOSE_CURRENT_RESULT);
                }
            }
            PersistenceManager.getEMF().getCache().evictAll();
            session.removeAttribute("errorMessage");
            session.setAttribute("successMessage", "Guest checked in successfully.");
            res.sendRedirect(req.getContextPath() + "/checkin");
            return;
        } catch (IllegalArgumentException ex) {
            session.setAttribute("counterMessage", ex.getMessage());
        } catch (Exception ex) {
            log("Unable to check in " + id, ex);
            session.setAttribute("counterMessage", "Check-in could not be confirmed. Review the booking and assigned rooms before retrying.");
        }
        res.sendRedirect(req.getContextPath() + "/staff/counter-booking?bookingID=" + id);
    }
}
