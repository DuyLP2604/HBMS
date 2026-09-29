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

@WebServlet(name = "StaffProfileServlet", urlPatterns = {"/staff/profile"})
public class StaffProfileServlet extends HttpServlet {

    private final EmployeeDAO employeeDAO = new EmployeeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null
                || !(session.getAttribute("user") instanceof Users)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Users user = (Users) session.getAttribute("user");

        if (!"Staff".equalsIgnoreCase(user.getRole())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only staff can view this page."
            );
            return;
        }

        try {
            Employee employee = employeeDAO.getByUserId(user.getUserID());

            if (employee == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "No employee profile is linked to this account."
                );
                return;
            }

            request.setAttribute("employee", employee);

            request.getRequestDispatcher(
                    "/WEB-INF/views/staff-profile.jsp"
            ).forward(request, response);

        } catch (RuntimeException exception) {
            throw new ServletException(
                    "Unable to load the staff profile.",
                    exception
            );
        }
    }
}