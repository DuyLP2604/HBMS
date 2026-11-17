package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import entity.Customer;
import entity.Nationality;
import java.io.IOException;
import java.util.regex.Pattern;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.flash.Flash;

/** Validates registration details and creates a customer account. */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("[A-Za-z0-9][A-Za-z0-9._\\-]{3,49}");
    private static final Pattern NAME_PATTERN = Pattern.compile("\\p{L}[\\p{L}\\p{M} .'’·・\\-]*");
    private static final String EMAIL_ATOM = "[A-Za-z0-9!#$%&'*+\\-\\/=?^_\\x60\\{\\|\\}~]+";
    private static final String DOMAIN_LABEL = "[A-Za-z0-9](?:[A-Za-z0-9\\-]{0,61}[A-Za-z0-9])?";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "(?=[^@]{1,64}@)" + EMAIL_ATOM + "(?:\\." + EMAIL_ATOM + ")*@"
            + "(?:" + DOMAIN_LABEL + "\\.)+[A-Za-z]{2,63}");
    private static final Pattern VIETNAMESE_PHONE_PATTERN = Pattern.compile("(?:0|\\+84)[0-9]{9}");
    private static final Pattern INTERNATIONAL_PHONE_PATTERN = Pattern.compile("(?:[0-9]{8,15}|\\+[1-9][0-9]{7,13})");

    private static final String USERNAME_FORMAT = "Use 4 to 50 characters. Start with a letter or digit. Use ASCII letters, digits, dots, underscores or hyphens.";
    private static final String NAME_FORMAT = "Start with a letter and use only letters, spaces, periods, apostrophes, hyphens or middle dots.";
    private static final String EMAIL_FORMAT = "Enter a valid email address, such as name@example.com.";
    private static final String VIETNAMESE_PHONE_FORMAT = "Enter 0 followed by 9 digits, or +84 followed by 9 digits.";
    private static final String INTERNATIONAL_PHONE_FORMAT = "Enter 8 to 15 digits, or + followed by 8 to 14 digits starting with 1 to 9.";

    /** Load the registration form with the available nationalities. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if ("checkUsername".equals(request.getParameter("action"))) {
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("Cache-Control", "no-store");
            String username = trimParameter(request.getParameter("username"));
            if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("{\"error\":true}");
                return;
            }
            try {
                boolean exists = new UserDAO().isUsernameExists(username);
                response.getWriter().write("{\"exists\":" + exists + "}");
            } catch (Exception e) {
                getServletContext().log("Unable to check username.", e);
                response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
                response.getWriter().write("{\"error\":true}");
            }
            return;
        }
        forwardRegister(request, response, new NationalityDAO());
    }

    /** Validate the form and create the customer profile and user account. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        UserDAO userDAO = new UserDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        NationalityDAO nationalityDAO = new NationalityDAO();
        String username = trimParameter(request.getParameter("username"));
        String password = request.getParameter("password");
        String fullname = trimParameter(request.getParameter("fullname"));
        String phone = trimParameter(request.getParameter("phone"));
        String email = trimParameter(request.getParameter("email"));
        String address = trimParameter(request.getParameter("address"));
        String nationalityId = trimParameter(request.getParameter("nationalityID"));
        keepFormData(request, username, fullname, phone, email, address, nationalityId);
        if ("checkUsername".equals(request.getParameter("action"))) {
            if (username == null || !USERNAME_PATTERN.matcher(username).matches()) {
                forwardRegisterWithError(request, response, nationalityDAO, USERNAME_FORMAT);
                return;
            }
            try {
                if (userDAO.isUsernameExists(username)) {
                    forwardRegisterWithError(request, response, nationalityDAO, "Username already exists. Please choose another username.");
                    return;
                }
            } catch (Exception e) {
                getServletContext().log("Unable to check username.", e);
                forwardRegisterWithError(request, response, nationalityDAO, "Unable to check username. Please try again.");
                return;
            }
            forwardRegister(request, response, nationalityDAO);
            return;
        }
        if (isBlank(username) || isBlank(password) || isBlank(fullname) || isBlank(email) || isBlank(address) || isBlank(nationalityId)) {
            forwardRegisterWithError(request, response, nationalityDAO, "Please complete all required fields.");
            return;
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            forwardRegisterWithError(request, response, nationalityDAO, USERNAME_FORMAT);
            return;
        }
        String confirmPassword = request.getParameter("confirmPassword");
        if (password.length() < 6) {
            forwardRegisterWithError(request, response, nationalityDAO, "Password must contain at least 6 characters.");
            return;
        }
        if (confirmPassword == null || !password.equals(confirmPassword)) {
            forwardRegisterWithError(request, response, nationalityDAO, "Passwords do not match.");
            return;
        }
        if (fullname.length() > 100) {
            forwardRegisterWithError(request, response, nationalityDAO, "Full name must not exceed 100 characters.");
            return;
        }
        if (!NAME_PATTERN.matcher(fullname).matches()) {
            forwardRegisterWithError(request, response, nationalityDAO, NAME_FORMAT);
            return;
        }
        if (phone != null && phone.length() > 15) {
            forwardRegisterWithError(request, response, nationalityDAO, "Phone number must not exceed 15 characters.");
            return;
        }
        if (email.length() > 100) {
            forwardRegisterWithError(request, response, nationalityDAO, "Email must not exceed 100 characters.");
            return;
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            forwardRegisterWithError(request, response, nationalityDAO, EMAIL_FORMAT);
            return;
        }
        if (address.length() > 200) {
            forwardRegisterWithError(request, response, nationalityDAO, "Address must not exceed 200 characters.");
            return;
        }
        if (userDAO.isUsernameExists(username)) {
            forwardRegisterWithError(request, response, nationalityDAO, "Username already exists.");
            return;
        }
        Nationality selectedNationality = nationalityDAO.getNationalityById(nationalityId);
        if (selectedNationality == null) {
            forwardRegisterWithError(request, response, nationalityDAO, "Please select a valid nationality.");
            return;
        }
        boolean vietnamese = isVietnamese(selectedNationality);
        if (isBlank(phone)) {
            if (vietnamese) {
                forwardRegisterWithError(request, response, nationalityDAO, "Vietnamese customers must provide a phone number.");
                return;
            }
            phone = null;
        } else {
            Pattern phonePattern = vietnamese ? VIETNAMESE_PHONE_PATTERN : INTERNATIONAL_PHONE_PATTERN;
            if (!phonePattern.matcher(phone).matches()) {
                forwardRegisterWithError(request, response, nationalityDAO,
                        vietnamese ? VIETNAMESE_PHONE_FORMAT : INTERNATIONAL_PHONE_FORMAT);
                return;
            }
        }
        Customer customer = new Customer();
        customer.setCustomerID(customerDAO.generateCustomerID());
        customer.setFullName(fullname);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setNationalityID(selectedNationality);
        if (!customerDAO.insertCustomerWithAccount(customer, username, password)) {
            forwardRegisterWithError(request, response, nationalityDAO, "Unable to complete registration. Please check your information and try again.");
            return;
        }
        Flash.success(request, "Account created successfully. You can now log in.");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private boolean isVietnamese(Nationality nationality) {
        return nationality != null && "N01".equalsIgnoreCase(nationality.getNationalityID());
    }

    private String trimParameter(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private void keepFormData(HttpServletRequest request, String username, String fullname, String phone, String email, String address, String nationalityId) {
        request.setAttribute("username", username);
        request.setAttribute("fullname", fullname);
        request.setAttribute("phone", phone);
        request.setAttribute("email", email);
        request.setAttribute("address", address);
        request.setAttribute("nationalityID", nationalityId);
    }

    private void forwardRegisterWithError(HttpServletRequest request, HttpServletResponse response, NationalityDAO nationalityDAO, String message) throws ServletException, IOException {
        Flash.error(request, message);
        forwardRegister(request, response, nationalityDAO);
    }

    private void forwardRegister(HttpServletRequest request, HttpServletResponse response, NationalityDAO nationalityDAO) throws ServletException, IOException {
        request.setAttribute("usernamePattern", USERNAME_PATTERN.pattern());
        request.setAttribute("usernameFormat", USERNAME_FORMAT);
        request.setAttribute("namePattern", NAME_PATTERN.pattern());
        request.setAttribute("nameFormat", NAME_FORMAT);
        request.setAttribute("emailPattern", EMAIL_PATTERN.pattern());
        request.setAttribute("emailFormat", EMAIL_FORMAT);
        request.setAttribute("vietnamesePhonePattern", VIETNAMESE_PHONE_PATTERN.pattern());
        request.setAttribute("vietnamesePhoneFormat", VIETNAMESE_PHONE_FORMAT);
        request.setAttribute("internationalPhonePattern", INTERNATIONAL_PHONE_PATTERN.pattern());
        request.setAttribute("internationalPhoneFormat", INTERNATIONAL_PHONE_FORMAT);
        request.setAttribute("nationalities", nationalityDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/registerCustomer.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Customer registration servlet";
    }
}
