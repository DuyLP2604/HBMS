package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import entity.Customer;
import entity.Nationality;
import java.io.IOException;
import java.util.List;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.flash.Flash;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        NationalityDAO nationalityDAO = new NationalityDAO();
        List<Nationality> nationalities = nationalityDAO.getAll();

        request.setAttribute("nationalities", nationalities);

        request.getRequestDispatcher(
                "/WEB-INF/views/registerCustomer.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        UserDAO userDAO = new UserDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        NationalityDAO nationalityDAO = new NationalityDAO();

        // Account information
        String username = trimParameter(
                request.getParameter("username")
        );

        String password = request.getParameter("password");

        // Personal information
        String fullname = trimParameter(
                request.getParameter("fullname")
        );

        String phone = trimParameter(
                request.getParameter("phone")
        );

        String email = trimParameter(
                request.getParameter("email")
        );

        String address = trimParameter(
                request.getParameter("address")
        );

        String cccd = trimParameter(
                request.getParameter("cccd")
        );

        String passportNumber = trimParameter(
                request.getParameter("passportNumber")
        );

        String nationalityId = trimParameter(
                request.getParameter("nationalityID")
        );

        // Keep entered information when an error occurs
        keepFormData(
                request,
                username,
                fullname,
                phone,
                email,
                address,
                cccd,
                passportNumber,
                nationalityId
        );

        // Basic validation
        if (isBlank(username)
                || isBlank(password)
                || isBlank(fullname)
                || isBlank(email)
                || isBlank(address)
                || isBlank(nationalityId)) {

            forwardRegisterWithError(
                    request,
                    response,
                    nationalityDAO,
                    "Please complete all required fields."
            );
            return;
        }

        // Check username
        if (userDAO.isUsernameExists(username)) {
            forwardRegisterWithError(
                    request,
                    response,
                    nationalityDAO,
                    "Username already exists."
            );
            return;
        }

        // Get selected nationality
        Nationality selectedNationality
                = nationalityDAO.getNationalityById(nationalityId);

        if (selectedNationality == null) {
            forwardRegisterWithError(
                    request,
                    response,
                    nationalityDAO,
                    "Please select a valid nationality."
            );
            return;
        }

        boolean isVietnamese = isVietnamese(selectedNationality);

        if (isVietnamese) {
            // Vietnamese customer must enter phone and CCCD
            if (isBlank(phone)) {
                forwardRegisterWithError(
                        request,
                        response,
                        nationalityDAO,
                        "Vietnamese customers must provide a phone number."
                );
                return;
            }

            if (isBlank(cccd)) {
                forwardRegisterWithError(
                        request,
                        response,
                        nationalityDAO,
                        "Vietnamese customers must provide an Identity Number."
                );
                return;
            }

            // Vietnamese customer does not use passport
            passportNumber = null;

        } else {
            // Foreign customer must enter passport number
            if (isBlank(passportNumber)) {
                forwardRegisterWithError(
                        request,
                        response,
                        nationalityDAO,
                        "Foreign customers must provide a passport number."
                );
                return;
            }

            // Foreign customer does not use CCCD
            cccd = null;

            // Phone is optional for foreign customers
            if (isBlank(phone)) {
                phone = null;
            }
        }

        // Insert user account
        int userInsertedID = userDAO.insertUser(
                username,
                password,
                "Customer"
        );

        if (userInsertedID == -1) {
            forwardRegisterWithError(
                    request,
                    response,
                    nationalityDAO,
                    "Unable to create your account. Please try again."
            );
            return;
        }

        // Create customer
        String customerId = customerDAO.generateCustomerID();

        Customer customer = new Customer();

        customer.setCustomerID(customerId);
        customer.setFullName(fullname);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setCccd(cccd);
        customer.setPassportNumber(passportNumber);
        customer.setNationalityID(selectedNationality);
        customer.setUserID(userDAO.login(username, password));

        boolean customerInserted
                = customerDAO.insertCustomer(customer);

        if (!customerInserted) {
            Flash.error(
                    request,
                    "Unable to complete registration. Please try again."
            );

            response.sendRedirect(
                    request.getContextPath() + "/register"
            );
            return;
        }

        Flash.success(
                request,
                "Account created successfully. You can now log in."
        );

        response.sendRedirect(
                request.getContextPath() + "/login"
        );
    }

    /**
     * Check whether the selected nationality is Vietnamese.
     */
    private boolean isVietnamese(Nationality nationality) {
        return nationality != null
                && "N01".equalsIgnoreCase(
                        nationality.getNationalityID()
                );
    }

    /**
     * Return null if the input is null; otherwise remove surrounding spaces.
     */
    private String trimParameter(String value) {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    /**
     * Store submitted information so JSP can display it again.
     */
    private void keepFormData(
            HttpServletRequest request,
            String username,
            String fullname,
            String phone,
            String email,
            String address,
            String cccd,
            String passportNumber,
            String nationalityId
    ) {
        request.setAttribute("username", username);
        request.setAttribute("fullname", fullname);
        request.setAttribute("phone", phone);
        request.setAttribute("email", email);
        request.setAttribute("address", address);
        request.setAttribute("cccd", cccd);
        request.setAttribute("passportNumber", passportNumber);
        request.setAttribute("nationalityID", nationalityId);
    }

    /**
     * Display the registration page again when validation fails.
     */
    private void forwardRegisterWithError(
            HttpServletRequest request,
            HttpServletResponse response,
            NationalityDAO nationalityDAO,
            String message
    ) throws ServletException, IOException {

        Flash.error(request, message);

        request.setAttribute(
                "nationalities",
                nationalityDAO.getAll()
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/registerCustomer.jsp"
        ).forward(request, response);
    }

    @Override
    public String getServletInfo() {
        return "Customer registration servlet";
    }
}
