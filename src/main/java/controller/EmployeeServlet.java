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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import util.EmployeeIdGenerator;
import util.EmployeeValidator;
import service.EmailService;
import util.flash.Flash;
import util.security.CsrfTokens;
import util.security.TemporaryPasswordGenerator;

@WebServlet(name = "EmployeeServlet", urlPatterns = {"/employee"})
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!hasPermission(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this page.");
            return;
        }
        EmployeeDAO employeeDAO = createEmployeeDAO();
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
                if (!isAdmin(request)) {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can create staff accounts.");
                    return;
                }
                request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession()));
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
        if (!"update".equalsIgnoreCase(action) && !"add".equalsIgnoreCase(action) && !"resendCredentials".equalsIgnoreCase(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid employee action.");
            return;
        }
        boolean provisioning = "add".equalsIgnoreCase(action) || "resendCredentials".equalsIgnoreCase(action);
        if (provisioning && !isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only administrators can create or resend staff credentials.");
            return;
        }
        if (provisioning && !CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form expired. Reload the page and try again.");
            return;
        }
        try {
            if ("update".equalsIgnoreCase(action)) {
                updateEmployee(request);
                Flash.success(request, "Employee information updated.");
            } else if ("add".equalsIgnoreCase(action)) {
                boolean sent = addEmployee(request);
                if (sent) {
                    Flash.success(request, "Staff account created. The username and temporary password were sent by email.");
                } else {
                    Flash.warning(request, "The staff account was created, but email delivery failed. Open View Details and use Resend login email. Do not create the account again.");
                }
            } else {
                boolean sent = resendCredentials(request);
                if (sent) {
                    Flash.success(request, "A new temporary password was sent. Earlier temporary credentials are no longer valid.");
                } else {
                    Flash.warning(request, "The temporary password was renewed, but email delivery failed. Please try Resend login email again.");
                }
            }
            response.sendRedirect(request.getContextPath() + "/employee?action=list");
        } catch (ValidationException exception) {
            forwardFormError(request, response, action, "Please correct the highlighted fields.", exception.getErrors());
        } catch (IllegalArgumentException exception) {
            handleFormFailure(request, response, action, exception.getMessage());
        } catch (Exception exception) {
            getServletContext().log("Unable to process employee information.", exception);
            handleFormFailure(request, response, action, "Unable to process employee information.");
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
        request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/employee-detail.jsp").forward(request, response);
    }

    private void updateEmployee(HttpServletRequest request) {
        EmployeeDAO employeeDAO = createEmployeeDAO();
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

    private boolean addEmployee(HttpServletRequest request) {
        EmployeeDAO employeeDAO = createEmployeeDAO();
        Employee employee = new Employee();
        String employeeID = employeeDAO.generateEmployeeID();
        employee.setEmployeeID(employeeID);
        request.setAttribute("employee", employee);
        Map<String, String> errors = fillEmployeeInformation(request, employee, employeeDAO, null);
        if (EmployeeValidator.normalizeEmail(request.getParameter("email")) == null) {
            errors.put("email", "Email is required to send the staff login credentials.");
        }
        String username = EmployeeIdGenerator.usernameOf(employeeID);
        String usernameError = EmployeeValidator.validateUsername(username);
        if (usernameError != null) {
            errors.put("username", usernameError);
        } else if (createUserDAO().isUsernameExists(username)) {
            errors.put("username", "Username is already taken.");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
        EmailService emailService = configuredEmailService();
        String password = TemporaryPasswordGenerator.generate();
        Users user = new Users();
        user.setUsername(username);
        user.setPassword(createUserDAO().hashMD5(password));
        user.setRole("Staff");
        user.setMustChangePassword(true);
        // Commit both records before sending email: SMTP failures do not leave partial records.
        employeeDAO.insertEmployeeWithUser(employee, user);
        return deliverCredentials(emailService, employee, user, password);
    }

    private boolean resendCredentials(HttpServletRequest request) {
        String employeeID = requireParameter(request, "id", "Employee ID is required.");
        Employee employee = createEmployeeDAO().getEmployeeById(employeeID);
        Users user = employee == null ? null : employee.getUserID();
        if (user == null || !"Staff".equalsIgnoreCase(user.getRole()) || !user.isMustChangePassword()) {
            throw new IllegalArgumentException("Only staff accounts awaiting their first password change can receive temporary credentials.");
        }
        String email = EmployeeValidator.normalizeEmail(employee.getEmail());
        if (email == null || EmployeeValidator.validateEmail(email) != null) {
            throw new IllegalArgumentException("Update this employee with a valid email address first.");
        }
        EmailService emailService = configuredEmailService();
        String password = TemporaryPasswordGenerator.generate();
        if (!createUserDAO().renewStaffTemporaryPassword(user.getUserID(), password)) {
            throw new IllegalArgumentException("This account has already completed password setup. Its password was not changed.");
        }
        return deliverCredentials(emailService, employee, user, password);
    }

    private boolean deliverCredentials(EmailService emailService, Employee employee, Users user, String password) {
        try {
            return emailService.sendStaffCredentials(employee.getEmail(), employee.getFullName(), user.getUsername(), password);
        } catch (RuntimeException exception) {
            // The account is already committed; report delivery failure without suggesting recreation.
            return false;
        }
    }

    private EmailService configuredEmailService() {
        EmailService service = createEmailService();
        if (!service.isConfigured()) {
            throw new IllegalArgumentException("Staff email delivery is not configured. Please contact the system administrator before creating or resending an account.");
        }
        return service;
    }

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    private void handleFormFailure(HttpServletRequest request, HttpServletResponse response, String action, String message) throws ServletException, IOException {
        if ("resendCredentials".equalsIgnoreCase(action)) {
            Flash.error(request, message);
            response.sendRedirect(request.getContextPath() + "/employee?action=list");
        } else {
            forwardFormError(request, response, action, message, Collections.emptyMap());
        }
    }

    protected EmployeeDAO createEmployeeDAO() {
        return new EmployeeDAO();
    }

    protected UserDAO createUserDAO() {
        return new UserDAO();
    }

    protected EmailService createEmailService() {
        return new EmailService();
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
        EmployeeDAO employeeDAO = createEmployeeDAO();
        request.setAttribute("positions", employeeDAO.getAllPositions());
        request.setAttribute("flashError", message);
        request.setAttribute("fieldErrors", fieldErrors);
        request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession()));
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
