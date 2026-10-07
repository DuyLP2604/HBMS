package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import entity.Customer;
import entity.Nationality;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import util.flash.Flash;

@WebServlet(name = "CustomerServlet", urlPatterns = {"/customer"})
public class CustomerServlet extends HttpServlet {

    private boolean isAdmin(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return false;
        }

        Object sessionUser = session.getAttribute("user");
        if (!(sessionUser instanceof Users)) {
            return false;
        }

        Users user = (Users) sessionUser;
        return "Admin".equalsIgnoreCase(user.getRole());
    }

    private boolean isWriteAction(String action) {
        return "add".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String action = request.getParameter("action");
        if (action == null || action.isBlank()) {
            action = "list";
        }

        if (isWriteAction(action) && isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin cannot add or edit customers.");
            return;
        }

        CustomerDAO daoCus = new CustomerDAO();
        NationalityDAO daoNat = new NationalityDAO();

        if ("list".equalsIgnoreCase(action)) {
            String keyword = request.getParameter("keyword");
            List<Customer> customerList;
            // tim theo name hoac sdt
            if (keyword != null && !keyword.trim().isEmpty()) {
                customerList = daoCus.searchCustomerByKeyword(keyword.trim());
            } else {
                customerList = daoCus.getAllCustomers();
            }
            request.setAttribute("customers", customerList);
            request.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(request, response);

        } else if ("add".equalsIgnoreCase(action)) {
            List<Nationality> listNat = daoNat.getAll();
            request.setAttribute("listNat", listNat);

            request.getRequestDispatcher("/WEB-INF/views/add-customer.jsp")
                    .forward(request, response);

        } else if ("update".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");
            Customer customer = daoCus.getCustomerById(id);

            if (customer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            request.setAttribute("customer", customer);
            request.setAttribute("listNat", daoNat.getAll());

            request.getRequestDispatcher("/WEB-INF/views/update-customer-info.jsp")
                    .forward(request, response);

        } else if ("viewDetail".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");
            Customer customer = daoCus.getCustomerById(id);

            if (customer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            request.setAttribute("customer", customer);

            request.getRequestDispatcher("/WEB-INF/views/customer-detail.jsp")
                    .forward(request, response);

        } else {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");

        if (!isWriteAction(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // Kiểm tra trước khi đọc dữ liệu và ghi vào database.
        if (isAdmin(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "Admin cannot add or edit customers.");
            return;
        }

        CustomerDAO daoCus = new CustomerDAO();

        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String cccd = request.getParameter("cccd");
        String passport = request.getParameter("passport");
        String nationalityId = request.getParameter("nation");

        if ("add".equalsIgnoreCase(action)) {
            String createAccount = request.getParameter("createAccount");
            Users newUser = null;

            if (createAccount != null) {
                String username = request.getParameter("username");
                String password = request.getParameter("password");
                UserDAO userDAO = new UserDAO();

                if (userDAO.isUsernameExists(username)) {
                    Flash.error(request, "Username already exists.");
                    response.sendRedirect(request.getContextPath() + "/customer?action=add");
                    return;
                }

                int userId = userDAO.insertUser(username, password, "Customer");
                if (userId > 0) {
                    newUser = new Users();
                    newUser.setUserID(userId);
                }
            }

            Customer customer = new Customer();
            customer.setCustomerID(daoCus.generateCustomerID());
            customer.setFullName(name);
            customer.setPhone(phone);
            customer.setEmail(email);
            customer.setAddress(address);
            customer.setCccd(cccd);
            customer.setPassportNumber(passport);
            customer.setNationalityID(new Nationality(nationalityId, null));
            customer.setUserID(newUser); // THÊM DÒNG NÀY ĐỂ GÁN USER CHO CUSTOMER

            daoCus.insertCustomer(customer);

            Flash.success(request, "Customer added successfully.");
            response.sendRedirect(request.getContextPath() + "/customer?action=list");
        } else if ("update".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");

            // Lấy thông tin khách hàng cũ từ DB để giữ lại userID hiện tại (tránh bị đè null)
            Customer existingCustomer = daoCus.getCustomerById(id);

            if (existingCustomer == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }

            Customer customer = new Customer();

            // Giữ ID hiện tại, không tạo CustomerID mới khi cập nhật.
            customer.setCustomerID(id);
            customer.setFullName(name);
            customer.setPhone(phone);
            customer.setEmail(email);
            customer.setAddress(address);
            customer.setCccd(cccd);
            customer.setPassportNumber(passport);
            customer.setNationalityID(
                    new Nationality(nationalityId, null));

            // Giữ lại userID cũ của khách hàng (nếu có)
            customer.setUserID(existingCustomer.getUserID());

            daoCus.updateCustomer(customer);

            Flash.success(request, "Customer information updated successfully.");
            response.sendRedirect(
                    request.getContextPath() + "/customer?action=list");
        }
    }

    @Override
    public String getServletInfo() {
        return "Customer management servlet";
    }
}
