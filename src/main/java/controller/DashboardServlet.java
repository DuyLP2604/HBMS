package controller;

import dao.DashboardDAO;
import dto.CustomerSource;
import dto.RevenueChart;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

@WebServlet(name = "DashboardServlet", urlPatterns = {"/dashboard"})
public class DashboardServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!hasPermission(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access the dashboard.");
            return;
        }

        try {
            LocalDate[] dateRange = parseDateRange(request);
            LocalDate startDate = dateRange[0];
            LocalDate endDate = dateRange[1];

            DashboardDAO dashboardDAO = new DashboardDAO();

            long revenue = dashboardDAO.getRevenueByDateRange(startDate, endDate);
            int occupiedRooms = dashboardDAO.getOccupiedRoomsCount(startDate, endDate);
            int totalRooms = dashboardDAO.getTotalRoomsCount();
            int totalCustomers = dashboardDAO.getTotalCustomers(startDate, endDate); // Đã fix logic
            List<RevenueChart> revenueChart = dashboardDAO.getRevenueChartData(startDate, endDate); // Đã fix logic
            List<CustomerSource> customerSource = dashboardDAO.getCustomerSource();

            request.setAttribute("todayRevenue", revenue);
            request.setAttribute("selectedRevenue", revenue);
            request.setAttribute("occupiedRooms", occupiedRooms);
            request.setAttribute("totalRooms", totalRooms);
            request.setAttribute("totalCustomers", totalCustomers);
            request.setAttribute("revenueChart", revenueChart);
            request.setAttribute("customerSource", customerSource);
            request.setAttribute("from", startDate.toString());
            request.setAttribute("to", endDate.toString());
            request.setAttribute("revenueLabel", createRevenueLabel(startDate, endDate));

            request.getRequestDispatcher("/WEB-INF/views/dashboard.jsp").forward(request, response);
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
        } catch (ServletException | IOException exception) {
            throw new ServletException("Unable to load dashboard data.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    private LocalDate[] parseDateRange(HttpServletRequest request) {
        String fromValue = request.getParameter("from");
        String toValue = request.getParameter("to");

        boolean fromEmpty = fromValue == null || fromValue.trim().isEmpty();
        boolean toEmpty = toValue == null || toValue.trim().isEmpty();

        // ĐÃ SỬA: Load lần đầu tiên sẽ tự động load All Time
        if (fromEmpty && toEmpty) {
            return new LocalDate[]{LocalDate.of(2010, 1, 1), LocalDate.now()};
        }

        if (fromEmpty || toEmpty) {
            throw new IllegalArgumentException("Both start date and end date are required.");
        }

        try {
            LocalDate startDate = LocalDate.parse(fromValue.trim());
            LocalDate endDate = LocalDate.parse(toValue.trim());

            if (endDate.isBefore(startDate)) {
                throw new IllegalArgumentException("End date must not be before start date.");
            }
            return new LocalDate[]{startDate, endDate};
        } catch (DateTimeParseException exception) {
            throw new IllegalArgumentException("Invalid date format. Please use YYYY-MM-DD.");
        }
    }

    private String createRevenueLabel(LocalDate startDate, LocalDate endDate) {
        if (startDate.equals(endDate)) {
            return "Revenue for " + startDate;
        }
        return "Revenue from " + startDate + " to " + endDate;
    }

    private boolean hasPermission(HttpSession session) {
        String role = (String) session.getAttribute("role");
        if (role == null) {
            Users user = (Users) session.getAttribute("user");
            if (user != null) {
                role = user.getRole();
            }
        }
        return "Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role);
    }

    @Override
    public String getServletInfo() {
        return "Displays hotel dashboard statistics.";
    }
}
