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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Locale;

@WebServlet(name = "EmployeeServlet", urlPatterns = {"/employee"})
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!hasPermission(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this page.");
            return;
        }
        EmployeeDAO employeeDAO = new EmployeeDAO();
        String action = normalizeOptional(request.getParameter("action"));
        if (action == null) {
            action = "list";
        }
        switch (action) {
            case "list":
                request.setAttribute("employees", employeeDAO.getAllEmployees());
                request.setAttribute("positions", employeeDAO.getAllPositions());
                request.setAttribute("shifts", employeeDAO.getAllShifts());
                request.getRequestDispatcher("/WEB-INF/views/employees.jsp").forward(request, response);
                break;
            case "add":
                request.setAttribute("positions", employeeDAO.getAllPositions());
                request.getRequestDispatcher("/WEB-INF/views/add-employee.jsp").forward(request, response);
                break;
            case "update":
                showUpdatePage(request, response, employeeDAO);
                break;
            case "viewDetail":
                showEmployeeDetail(request, response, employeeDAO);
                break;
            default:
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee action.");
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!hasPermission(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to perform this action.");
            return;
        }
        String action = normalizeOptional(request.getParameter("action"));
        if (action == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Employee action is required.");
            return;
        }
        if (!"update".equalsIgnoreCase(action) && !"add".equalsIgnoreCase(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee action.");
            return;
        }
        try {
            if ("update".equalsIgnoreCase(action)) {
                updateEmployee(request);
            } else {
                addEmployee(request);
            }
            response.sendRedirect(request.getContextPath() + "/employee?action=list");
        } catch (IllegalArgumentException exception) {
            forwardFormError(request, response, action, exception.getMessage());
        } catch (Exception exception) {
            getServletContext().log("Unable to process employee information.", exception);
            forwardFormError(request, response, action, "Unable to process employee information.");
        }
    }

    private void showUpdatePage(HttpServletRequest request, HttpServletResponse response, EmployeeDAO employeeDAO) throws ServletException, IOException {
        String employeeID = requireParameter(request, "id", "Employee ID is required.");
        Employee employee = employeeDAO.getEmployeeById(employeeID);
        if (employee == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found.");
            return;
        }
        request.setAttribute("employee", employee);
        request.setAttribute("positions", employeeDAO.getAllPositions());
        request.getRequestDispatcher("/WEB-INF/views/update-employee-info.jsp").forward(request, response);
    }

    private void showEmployeeDetail(HttpServletRequest request, HttpServletResponse response, EmployeeDAO employeeDAO) throws ServletException, IOException {
        String employeeID = requireParameter(request, "id", "Employee ID is required.");
        Employee employee = employeeDAO.getEmployeeById(employeeID);
        if (employee == null) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Employee not found.");
            return;
        }
        request.setAttribute("employee", employee);
        request.getRequestDispatcher("/WEB-INF/views/employee-detail.jsp").forward(request, response);
    }

    private void updateEmployee(HttpServletRequest request) {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        String employeeID = requireParameter(request, "id", "Employee ID is required.");
        Employee employee = employeeDAO.getEmployeeById(employeeID);
        if (employee == null) {
            throw new IllegalArgumentException("Employee not found.");
        }
        request.setAttribute("employee", employee);
        fillEmployeeInformation(request, employee);
        if (employeeDAO.isEmailExists(employee.getEmail(), employeeID)) {
            throw new IllegalArgumentException("Email is already used by another employee.");
        }
        employeeDAO.updateEmployee(employee);
    }

    private void addEmployee(HttpServletRequest request) {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        Employee employee = new Employee();
        employee.setEmployeeID(employeeDAO.generateEmployeeID());
        request.setAttribute("employee", employee);
        fillEmployeeInformation(request, employee);
        String username = requireParameter(request, "username", "Username is required.");
        String password = requireParameter(request, "password", "Password is required.");
        if (employeeDAO.isEmailExists(employee.getEmail())) {
            throw new IllegalArgumentException("Email is already used by another employee.");
        }
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(hashPassword(password));
        user.setRole("Staff");
        employeeDAO.insertEmployeeWithUser(employee, user);
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
        } catch (NoSuchAlgorithmException exception) {
            return password;
        }
    }

    private void fillEmployeeInformation(HttpServletRequest request, Employee employee) {
        employee.setFullName(normalizeOptional(request.getParameter("name")));
        employee.setPosition(normalizeOptional(request.getParameter("position")));
        employee.setShift(normalizeOptional(request.getParameter("shift")));
        employee.setAddress(normalizeOptional(request.getParameter("address")));
        employee.setPhone(normalizeOptional(request.getParameter("phone")));
        String emailParameter = request.getParameter("email");
        if (emailParameter != null) {
            String email = normalizeOptional(emailParameter);
            employee.setEmail(email == null ? null : email.toLowerCase(Locale.ROOT));
        }
        requireParameter(request, "name", "Employee name is required.");
        requireParameter(request, "position", "Employee position is required.");
        employee.setSalary(parseSalary(request.getParameter("salary")));
        String email = employee.getEmail();
        if (email != null) {
            if (email.length() > 100) {
                throw new IllegalArgumentException("Email must not exceed 100 characters.");
            }
            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Invalid email address.");
            }
        }
    }

    private void forwardFormError(HttpServletRequest request, HttpServletResponse response, String action, String message) throws ServletException, IOException {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        request.setAttribute("positions", employeeDAO.getAllPositions());
        request.setAttribute("flashError", message);
        String targetJsp = "update".equalsIgnoreCase(action) ? "/WEB-INF/views/update-employee-info.jsp" : "/WEB-INF/views/add-employee.jsp";
        request.getRequestDispatcher(targetJsp).forward(request, response);
    }

    private BigDecimal parseSalary(String value) {
        if (value == null || value.trim().isEmpty()) {
            return null;
        }
        try {
            BigDecimal salary = new BigDecimal(value.trim());
            if (salary.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("Salary must not be negative.");
            }
            return salary;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Salary must be a valid number.");
        }
    }

    private String requireParameter(HttpServletRequest request, String parameterName, String errorMessage) {
        String value = request.getParameter(parameterName);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(errorMessage);
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
        return role != null && ("Admin".equalsIgnoreCase(role) || "Staff".equalsIgnoreCase(role));
    }

    @Override
    public String getServletInfo() {
        return "Handles employee management.";
    }
}
