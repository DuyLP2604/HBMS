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
import java.math.BigDecimal;
import util.flash.Flash;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

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
                request.setAttribute("employees", employeeDAO.getAllEmployees());
                request.setAttribute("positions", employeeDAO.getAllPositions());
                request.setAttribute("shifts", employeeDAO.getAllShifts());

                request.getRequestDispatcher(
                        "/WEB-INF/views/employees.jsp"
                ).forward(request, response);
                break;

            case "add":
                request.setAttribute("positions", employeeDAO.getAllPositions());
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
            request.setAttribute("positions", new EmployeeDAO().getAllPositions());
            request.setAttribute("flashError", exception.getMessage());
            String targetJsp = "update".equalsIgnoreCase(action) ? "/WEB-INF/views/update-employee-info.jsp" : "/WEB-INF/views/add-employee.jsp";
            request.getRequestDispatcher(targetJsp).forward(request, response);
        } catch (Exception exception) {
            request.setAttribute("positions", new EmployeeDAO().getAllPositions());
            request.setAttribute("flashError", "Unable to process employee information.");
            String targetJsp = "update".equalsIgnoreCase(action) ? "/WEB-INF/views/update-employee-info.jsp" : "/WEB-INF/views/add-employee.jsp";
            request.getRequestDispatcher(targetJsp).forward(request, response);
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
        request.setAttribute("positions", employeeDAO.getAllPositions());

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

        fillEmployeeInformation(request, employee);

        employeeDAO.updateEmployee(employee);
    }

    private void addEmployee(
            HttpServletRequest request) {

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

        Users user = new Users();
        user.setUsername(username);
        user.setPassword(hashPassword(password)); // <-- Đã mã hóa mật khẩu ở đây
        user.setRole("Staff");

        Employee employee = new Employee();
        employee.setEmployeeID(employeeDAO.generateEmployeeID());

        fillEmployeeInformation(
                request,
                employee
        );

        employeeDAO.insertEmployeeWithUser(
                employee,
                user
        );
    }
    private String hashPassword(String password) {
    try {
        MessageDigest md = MessageDigest.getInstance("MD5");
        byte[] array = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : array) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    } catch (NoSuchAlgorithmException e) {
        return password; // Fallback nếu lỗi
    }
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