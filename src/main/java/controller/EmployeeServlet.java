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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import util.EmployeeIdGenerator;
import util.EmployeeValidator;

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
                setNextAccount(request, employeeDAO.generateEmployeeID());
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
        } catch (ValidationException exception) {
            forwardFormError(request, response, action, "Please correct the highlighted fields.", exception.getErrors());
        } catch (IllegalArgumentException exception) {
            forwardFormError(request, response, action, exception.getMessage(), Collections.emptyMap());
        } catch (Exception exception) {
            getServletContext().log("Unable to process employee information.", exception);
            forwardFormError(request, response, action, "Unable to process employee information.", Collections.emptyMap());
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
        Map<String, String> errors = fillEmployeeInformation(request, employee, employeeDAO, employeeID);
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        employeeDAO.updateEmployee(employee);
    }

    private void addEmployee(HttpServletRequest request) {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        Employee employee = new Employee();
        // ID and username are always generated by the server, whatever the browser sent.
        String employeeID = employeeDAO.generateEmployeeID();
        employee.setEmployeeID(employeeID);
        request.setAttribute("employee", employee);

        // Collect every problem first so the admin sees all of them at once.
        Map<String, String> errors = fillEmployeeInformation(request, employee, employeeDAO, null);

        String username = EmployeeIdGenerator.usernameOf(employeeID);
        String password = request.getParameter("password");
        errors.putAll(EmployeeValidator.validateAccount(username, password));
        if (!errors.containsKey("username") && new UserDAO().isUsernameExists(username)) {
            errors.put("username", "Username is already taken.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
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

    /*
     * Validates the employee fields of the form. When everything is valid the
     * normalised values are copied into the employee; otherwise the employee is
     * left untouched. Returns field name -> message (empty when valid).
     */
    private Map<String, String> fillEmployeeInformation(HttpServletRequest request, Employee employee,
            EmployeeDAO employeeDAO, String excludeEmployeeID) {
        String name = EmployeeValidator.normalizeName(request.getParameter("name"));
        String phone = EmployeeValidator.normalizePhone(request.getParameter("phone"));
        String email = EmployeeValidator.normalizeEmail(request.getParameter("email"));
        String address = EmployeeValidator.normalizeOptional(request.getParameter("address"));
        String position = EmployeeValidator.normalizeOptional(request.getParameter("position"));
        String shift = EmployeeValidator.normalizeOptional(request.getParameter("shift"));
        String salary = request.getParameter("salary");

        Map<String, String> errors = new LinkedHashMap<>(EmployeeValidator.validateEmployee(
                name, phone, email, address, position, shift, salary, employeeDAO.getAllPositions()));

        // Uniqueness is only worth checking for values that are well formed.
        if (!errors.containsKey("phone") && employeeDAO.isPhoneExists(phone, excludeEmployeeID)) {
            errors.put("phone", "Phone number is already used by another employee.");
        }
        if (!errors.containsKey("email") && email != null && employeeDAO.isEmailExists(email, excludeEmployeeID)) {
            errors.put("email", "Email is already used by another employee.");
        }

        if (errors.isEmpty()) {
            employee.setFullName(name);
            employee.setPhone(phone);
            employee.setEmail(email);
            employee.setAddress(address);
            employee.setPosition(position);
            employee.setShift(shift);
            employee.setSalary(EmployeeValidator.parseSalary(salary));
        }
        return errors;
    }

    // Values shown (read-only) in the add form.
    private void setNextAccount(HttpServletRequest request, String employeeID) {
        request.setAttribute("nextEmployeeID", employeeID);
        request.setAttribute("nextUsername", EmployeeIdGenerator.usernameOf(employeeID));
    }

    private void forwardFormError(HttpServletRequest request, HttpServletResponse response, String action,
            String message, Map<String, String> fieldErrors) throws ServletException, IOException {
        EmployeeDAO employeeDAO = new EmployeeDAO();
        request.setAttribute("positions", employeeDAO.getAllPositions());
        request.setAttribute("flashError", message);
        request.setAttribute("fieldErrors", fieldErrors);
        if (!"update".equalsIgnoreCase(action)) {
            setNextAccount(request, employeeDAO.generateEmployeeID());
        }
        String targetJsp = "update".equalsIgnoreCase(action) ? "/WEB-INF/views/update-employee-info.jsp" : "/WEB-INF/views/add-employee.jsp";
        request.getRequestDispatcher(targetJsp).forward(request, response);
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

    /*
     * Carries one message per invalid form field back to doPost.
     */
    private static class ValidationException extends IllegalArgumentException {

        private static final long serialVersionUID = 1L;

        private final transient Map<String, String> errors;

        ValidationException(Map<String, String> errors) {
            super("Invalid employee information.");
            this.errors = errors;
        }

        Map<String, String> getErrors() {
            return errors;
        }
    }

    @Override
    public String getServletInfo() {
        return "Handles employee management.";
    }
}
