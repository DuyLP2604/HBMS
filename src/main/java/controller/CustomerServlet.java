package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import entity.Customer;
import entity.Nationality;
import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import util.PersistenceManager;
import util.flash.Flash;

/** Handles staff access to customer records and account creation. */
@WebServlet(name = "CustomerServlet", urlPatterns = {"/customer"})
public class CustomerServlet extends HttpServlet {

    private Users getSessionUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object sessionUser = session.getAttribute("user");
        return sessionUser instanceof Users ? (Users) sessionUser : null;
    }

    private boolean isAdmin(HttpServletRequest request) {
        Users user = getSessionUser(request);
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    private boolean isWriteAction(String action) {
        return "add".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action);
    }

    /** Check whether the signed-in user can open customer management. */
    private boolean checkAccess(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Users user = getSessionUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Staff".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot access customer management.");
            return false;
        }
        return true;
    }

    private String trimToNull(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private int positiveIntOrDefault(String value, int defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        try {
            int number = Integer.parseInt(value.trim());
            return number > 0 ? number : defaultValue;
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    private int customerPageSize(String value) {
        int size = positiveIntOrDefault(value, 10);
        return size == 10 || size == 20 || size == 50 || size == 100 ? size : 10;
    }

    /** Show customer search results, a profile, or a customer form. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!checkAccess(request, response)) {
            return;
        }
        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }
        if (isWriteAction(action) && isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin cannot add or edit customers.");
            return;
        }
        boolean counter = "counter".equals(request.getParameter("flow"));
        if (counter && !new dao.EmployeeDAO().isReceptionistByUserId(getSessionUser(request).getUserID())) {
            response.sendError(403); return;
        }
        CustomerDAO daoCus = new CustomerDAO();
        NationalityDAO daoNat = new NationalityDAO();
        if ("list".equalsIgnoreCase(action)) {
            String keyword = trimToNull(request.getParameter("keyword"));
            int pageSize = customerPageSize(request.getParameter("pageSize"));
            int requestedPage = positiveIntOrDefault(request.getParameter("page"), 1);

            // Preserve the existing exact customer-ID search before name/phone matching.
            Customer exactMatch = keyword == null ? null : daoCus.getCustomerById(keyword);
            long totalCustomers = exactMatch == null ? daoCus.countCustomers(keyword) : 1;
            int totalPages = (int) Math.max(1L, (totalCustomers + pageSize - 1L) / pageSize);
            int currentPage = Math.min(requestedPage, totalPages);
            int offset = (currentPage - 1) * pageSize;
            List<Customer> customerList = exactMatch != null
                    ? List.of(exactMatch)
                    : totalCustomers == 0 ? List.of() : daoCus.getCustomersPage(keyword, offset, pageSize);

            request.setAttribute("customers", customerList);
            request.setAttribute("keyword", keyword);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalCustomers", totalCustomers);
            request.setAttribute("startIndex", customerList.isEmpty() ? 0 : offset + 1);
            request.setAttribute("endIndex", customerList.isEmpty() ? 0 : offset + customerList.size());
            request.setAttribute("pageStart", Math.max(1, currentPage - 2));
            request.setAttribute("pageEnd", Math.min(totalPages, currentPage + 2));
            request.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(request, response);
        } else if ("add".equalsIgnoreCase(action)) {
            request.setAttribute("listNat", daoNat.getAll());
            request.getRequestDispatcher("/WEB-INF/views/add-customer.jsp").forward(request, response);
        } else if ("update".equalsIgnoreCase(action)) {
            Customer customer = daoCus.getCustomerById(request.getParameter("id"));
            if (customer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found.");
                return;
            }
            request.setAttribute("customer", customer);
            request.setAttribute("listNat", daoNat.getAll());
            request.getRequestDispatcher("/WEB-INF/views/update-customer-info.jsp").forward(request, response);
        } else if ("success".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");

            Customer customer = daoCus.getCustomerById(id);

            if (customer == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Customer not found."
                );
                return;
            }

            request.setAttribute("customer", customer);

            request.getRequestDispatcher(
                    "/WEB-INF/views/customer-success.jsp"
            ).forward(request, response);
        } else if ("viewDetail".equalsIgnoreCase(action)) {
            Customer customer = daoCus.getCustomerById(request.getParameter("id"));
            if (customer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found.");
                return;
            }
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/WEB-INF/views/customer-detail.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    /** Save a new or updated customer record after checking staff access. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        if (!checkAccess(request, response)) {
            return;
        }
        String action = request.getParameter("action");
        if (!isWriteAction(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
            return;
        }
        if (isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin cannot add or edit customers.");
            return;
        }
        boolean counter = "counter".equals(request.getParameter("flow"));
        if (counter && !new dao.EmployeeDAO().isReceptionistByUserId(getSessionUser(request).getUserID())) {
            response.sendError(403); return;
        }
        CustomerDAO daoCus = new CustomerDAO();
        if (counter && (request.getSession().getAttribute("counterCsrf") == null
                || !java.util.Objects.equals(request.getSession().getAttribute("counterCsrf"), request.getParameter("csrfToken")))) {
            response.sendError(403); return;
        }
        String name = trimToNull(request.getParameter("name"));
        String phone = trimToNull(request.getParameter("phone"));
        String email = trimToNull(request.getParameter("email"));
        String address = trimToNull(request.getParameter("address"));
        String nationalityId = trimToNull(request.getParameter("nation"));
        Customer customer;
        if ("update".equalsIgnoreCase(action)) {
            customer = daoCus.getCustomerById(request.getParameter("id"));
            if (customer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found.");
                return;
            }
        } else {
            customer = new Customer();
        }
        String formUrl = "add".equalsIgnoreCase(action) ? "/customer?action=add" : "/customer?action=update&id=" + customer.getCustomerID();
        if (counter) {
            formUrl += "&flow=counter";
            java.util.Map<String, String> values = new java.util.HashMap<>();
            for (String key : new String[]{"name", "phone", "email", "address", "nation", "username"}) {
                values.put(key, request.getParameter(key));
            }
            request.getSession().setAttribute("counterRegistration", values);
            if (phone == null || !phone.matches("[+0-9 .-]{8,15}")) {
                Flash.error(request, "Enter a valid phone number.");
                response.sendRedirect(request.getContextPath() + formUrl); return;
            }
            if ("add".equalsIgnoreCase(action) && !daoCus.findByPhone(phone).isEmpty()) {
                Flash.error(request, "This phone already has a customer profile. Please search and select the customer.");
                response.sendRedirect(request.getContextPath() + "/staff/counter-booking?phone="
                        + java.net.URLEncoder.encode(phone, java.nio.charset.StandardCharsets.UTF_8)); return;
            }
        }
        if (name == null || nationalityId == null) {
            Flash.error(request, "Full name and nationality are required.");
            response.sendRedirect(request.getContextPath() + formUrl);
            return;
        }
        if (name.length() > 100 || (phone != null && phone.length() > 15) || (email != null && email.length() > 100) || (address != null && address.length() > 200)) {
            Flash.error(request, "Customer information exceeds the allowed length.");
            response.sendRedirect(request.getContextPath() + formUrl);
            return;
        }
        Nationality nationality = null;
        for (Nationality item : new NationalityDAO().getAll()) {
            if (nationalityId.equals(item.getNationalityID())) {
                nationality = item;
                break;
            }
        }
        if (nationality == null) {
            Flash.error(request, "Invalid nationality.");
            response.sendRedirect(request.getContextPath() + formUrl);
            return;
        }
        customer.setFullName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setNationalityID(nationality);
        if ("add".equalsIgnoreCase(action)) {
            String createAccount = request.getParameter("createAccount");
            String username = trimToNull(request.getParameter("username"));
            String password = request.getParameter("password");

            if (createAccount == null || username == null
                    || password == null || password.isBlank()) {
                Flash.error(request, "A customer login account is required. Please enter username and password.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }

            if (username.length() > 50) {
                Flash.error(request, "Username must not exceed 50 characters.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }

            if (new UserDAO().isUsernameExists(username)) {
                Flash.error(request, "Username already exists.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }

            customer.setCustomerID(daoCus.generateCustomerID());

            if (!daoCus.insertCustomerWithAccount(customer, username, password)) {
                Flash.error(request, "Failed to add customer. Please check the information and try again.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }

            if (counter) {
                request.getSession().removeAttribute("counterRegistration");
                response.sendRedirect(request.getContextPath() + "/staff/counter-booking?customerID=" + customer.getCustomerID());
                return;
            }
            // Customer created successfully
            Flash.success(request, "Customer added successfully.");

            // Redirect to the customer success page
            response.sendRedirect(
                    request.getContextPath()
                    + "/customer?action=success&id="
                    + customer.getCustomerID()
            );
            return;

        } else {
            if (!daoCus.updateCustomer(customer)) {
                Flash.error(request, "Failed to update customer information.");
                response.sendRedirect(
                        request.getContextPath() + formUrl
                );
                return;
            }

            Flash.success(request,
                    "Customer information updated successfully.");

            // Redirect to customer list after updating
            response.sendRedirect(
                    request.getContextPath() + "/customer?action=list"
            );
            return;
        }
    } // End doPost()

    @Override
    public String getServletInfo() {
        return "Customer management servlet";
    }
}
