package controller;

import dao.EmployeeDAO;
import entity.Employee;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(
        name = "StaffDashboardServlet",
        urlPatterns = {"/staff/dashboard"}
)
public class StaffDashboardServlet extends HttpServlet {

    private final EmployeeDAO employeeDAO
            = new EmployeeDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        Users user = session == null
                ? null
                : (Users) session.getAttribute("user");

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if (!"Staff".equalsIgnoreCase(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only staff members can access this page."
            );
            return;
        }

        Employee employee;

        try {
            employee = employeeDAO.getByUserId(
                    user.getUserID()
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load employee information."
            );
            return;
        }

        if (employee == null) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "No employee information is associated with this account."
            );
            return;
        }

        boolean isReceptionist =
                "Receptionist".equalsIgnoreCase(
                        employee.getPosition()
                );

        request.setAttribute("employee", employee);
        request.setAttribute(
                "isReceptionist",
                isReceptionist
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/staff-dashboard.jsp"
        ).forward(request, response);
    }
}