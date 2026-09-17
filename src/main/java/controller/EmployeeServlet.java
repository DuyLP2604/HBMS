package controller;

import dao.EmployeeDAO;
import dao.UserDAO;
import entity.Employee;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import util.flash.Flash;

@WebServlet(
        name = "EmployeeServlet",
        urlPatterns = {"/employee"}
)
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        if (!hasPermission(request.getSession())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission "
                    + "to access this page."
            );
            return;
        }

        EmployeeDAO employeeDAO = new EmployeeDAO();

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }

        switch (action) {
            case "list":
                request.setAttribute(
                        "employees",
                        employeeDAO.getAllEmployees()
                );

                request.getRequestDispatcher(
                        "/WEB-INF/views/employees.jsp"
                ).forward(request, response);
                break;

            case "add":
                request.getRequestDispatcher(
                        "/WEB-INF/views/add-employee.jsp"
                ).forward(request, response);
                break;

            case "update":
                showUpdatePage(
                        request,
                        response,
                        employeeDAO
                );
                break;

            case "viewDetail":
                showEmployeeDetail(
                        request,
                        response,
                        employeeDAO
                );
                break;

            default:
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid employee action."
                );
                break;
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        if (!hasPermission(request.getSession())) {
            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission "
                    + "to perform this action."
            );
            return;
        }

        String action = request.getParameter("action");

        if (action == null || action.trim().isEmpty()) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Employee action is required."
            );
            return;
        }

        try {
            if ("update".equalsIgnoreCase(action)) {
                updateEmployee(request);
            } else if ("add".equalsIgnoreCase(action)) {
                addEmployee(request);
            } else {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid employee action."
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/employee?action=list"
            );
        } catch (IllegalArgumentException exception) {
            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    exception.getMessage()
            );
        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to process employee information.",
                    exception
            );
        }
    }

    private void showUpdatePage(
            HttpServletRequest request,
            HttpServletResponse response,
            EmployeeDAO employeeDAO)
            throws ServletException, IOException {

        String employeeID = requireParameter(
                request,
                "id",
                "Employee ID is required."
        );

        Employee employee
                = employeeDAO.getEmployeeById(employeeID);

        if (employee == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Employee not found."
            );
            return;
        }

        request.setAttribute("employee", employee);

        request.getRequestDispatcher(
                "/WEB-INF/views/update-employee-info.jsp"
        ).forward(request, response);
    }

    private void showEmployeeDetail(
            HttpServletRequest request,
            HttpServletResponse response,
            EmployeeDAO employeeDAO)
            throws ServletException, IOException {

        String employeeID = requireParameter(
                request,
                "id",
                "Employee ID is required."
        );

        Employee employee
                = employeeDAO.getEmployeeById(employeeID);

        if (employee == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "Employee not found."
            );
            return;
        }

        request.setAttribute("employee", employee);

        request.getRequestDispatcher(
                "/WEB-INF/views/employee-detail.jsp"
        ).forward(request, response);
    }

    private void updateEmployee(
            HttpServletRequest request) {

        EmployeeDAO employeeDAO = new EmployeeDAO();

        String employeeID = requireParameter(
                request,
                "id",
                "Employee ID is required."
        );

        Employee employee
                = employeeDAO.getEmployeeById(employeeID);

        if (employee == null) {
            throw new IllegalArgumentException(
                    "Employee not found."
            );
        }

        /*
         * Update the existing employee instead of creating
         * a new object. This preserves the linked UserID.
         */
        fillEmployeeInformation(request, employee);

        employeeDAO.updateEmployee(employee);

        Flash.success(
                request,
                "Employee information updated successfully."
        );
    }

    private void addEmployee(
            HttpServletRequest request) {

        String employeeID = requireParameter(
                request,
                "id",
                "Employee ID is required."
        );

        String username = requireParameter(
                request,
                "username",
                "Username is required."
        );

        String password = requireParameter(
                request,
                "password",
                "Password is required."
        );

        EmployeeDAO employeeDAO = new EmployeeDAO();

        if (employeeDAO.getEmployeeById(employeeID) != null) {
            throw new IllegalArgumentException(
                    "Employee ID already exists."
            );
        }

        Users user = new Users();
        user.setUsername(username);

        user.setPassword(password);
        user.setRole("Staff");

        Employee employee = new Employee();
        employee.setEmployeeID(employeeID);

        fillEmployeeInformation(
                request,
                employee
        );

        employeeDAO.insertEmployeeWithUser(
                employee,
                user
        );

        Flash.success(
                request,
                "Employee added successfully."
        );
    }

    private void fillEmployeeInformation(
            HttpServletRequest request,
            Employee employee) {

        String fullName = requireParameter(
                request,
                "name",
                "Employee name is required."
        );

        String position = requireParameter(
                request,
                "position",
                "Employee position is required."
        );

        BigDecimal salary = parseSalary(
                request.getParameter("salary")
        );

        String shift = normalizeOptional(
                request.getParameter("shift")
        );

        String address = normalizeOptional(
                request.getParameter("address")
        );

        String phone = normalizeOptional(
                request.getParameter("phone")
        );

        employee.setFullName(fullName);
        employee.setPosition(position);
        employee.setSalary(salary);
        employee.setShift(shift);
        employee.setAddress(address);
        employee.setPhone(phone);
    }

    private BigDecimal parseSalary(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        try {
            BigDecimal salary = new BigDecimal(
                    value.trim()
            );

            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException(
                        "Salary must not be negative."
                );
            }

            return salary;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Salary must be a valid number."
            );
        }
    }

    private String requireParameter(
            HttpServletRequest request,
            String parameterName,
            String errorMessage) {

        String value = request.getParameter(
                parameterName
        );

        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    errorMessage
            );
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }

        return value.trim();
    }

    private boolean hasPermission(HttpSession session) {
        String role = (String) session.getAttribute("role");

        return role != null
                && ("Admin".equalsIgnoreCase(role)
                || "Staff".equalsIgnoreCase(role));
    }

    @Override
    public String getServletInfo() {
        return "Handles employee management.";
    }
}
