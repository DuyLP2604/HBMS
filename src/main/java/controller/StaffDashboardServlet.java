package controller;

import dao.EmployeeDAO;
import dto.ReceptionDashboard;
import entity.Employee;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import service.BookingLifecycleService;
import util.BookingTimePolicy;

@WebServlet(
        name = "StaffDashboardServlet",
        urlPatterns = {"/staff/dashboard"}
)
public class StaffDashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Users user = session == null ? null : (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        if (!"Staff".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff members can access this page.");
            return;
        }

        Employee employee;

        try {
            employee = createEmployeeDAO().getByUserId(user.getUserID());
        } catch (Exception ex) {
            ex.printStackTrace();

            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Unable to load employee information.");
            return;
        }

        if (employee == null) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "No employee information is associated with this account.");
            return;
        }

        boolean isReceptionist = "Receptionist".equalsIgnoreCase(employee.getPosition());
        request.setAttribute("employee", employee);
        request.setAttribute("isReceptionist", isReceptionist);
        response.setHeader("Cache-Control", "no-store");
        if (isReceptionist) {
            LocalDateTime now = hotelNow();
            request.setAttribute("dashboardDate", now.format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy", Locale.ENGLISH)));
            request.setAttribute("dashboardTime", now.format(DateTimeFormatter.ofPattern("HH:mm")));
            try {
                // Reuse the existing operational query; the dashboard performs no booking updates.
                request.setAttribute("receptionDashboard", ReceptionDashboard.from(
                        createLifecycleService().getOperationalBookings(), now, request.getParameter("focus")));
            } catch (RuntimeException exception) {
                getServletContext().log("Unable to load the front-desk overview.", exception);
                request.setAttribute("dashboardUnavailable", true);
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/staff-dashboard.jsp").forward(request, response);
    }

    protected EmployeeDAO createEmployeeDAO() { return new EmployeeDAO(); }
    protected BookingLifecycleService createLifecycleService() { return new BookingLifecycleService(); }
    protected LocalDateTime hotelNow() { return BookingTimePolicy.now(); }
}
