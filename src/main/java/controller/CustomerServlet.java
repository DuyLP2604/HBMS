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
@WebServlet(name = "CustomerServlet", urlPatterns = {"/customer"})
public class CustomerServlet extends HttpServlet
{
    private Users getSessionUser(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        if (session == null)
        {
            return null;
        }
        Object sessionUser = session.getAttribute("user");
        return sessionUser instanceof Users ? (Users) sessionUser : null;
    }

    private boolean isAdmin(HttpServletRequest request)
    {
        Users user = getSessionUser(request);
        return user != null && "Admin".equalsIgnoreCase(user.getRole());
    }

    private boolean isWriteAction(String action)
    {
        return "add".equalsIgnoreCase(action) || "update".equalsIgnoreCase(action);
    }

    private boolean checkAccess(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        Users user = getSessionUser(request);
        if (user == null)
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return false;
        }
        if (!"Admin".equalsIgnoreCase(user.getRole()) && !"Staff".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot access customer management.");
            return false;
        }
        return true;
    }

    private String trimToNull(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (!checkAccess(request, response))
        {
            return;
        }
        String action = request.getParameter("action");
        if (action == null || action.isBlank())
        {
            action = "list";
        }
        if (isWriteAction(action) && isAdmin(request))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin cannot add or edit customers.");
            return;
        }
        CustomerDAO daoCus = new CustomerDAO();
        NationalityDAO daoNat = new NationalityDAO();
        if ("list".equalsIgnoreCase(action))
        {
            String keyword = trimToNull(request.getParameter("keyword"));
            List<Customer> customerList = keyword == null ? daoCus.getAllCustomers() : daoCus.searchCustomerByKeyword(keyword);
            request.setAttribute("customers", customerList);
            request.getRequestDispatcher("/WEB-INF/views/customers.jsp").forward(request, response);
        }
        else if ("add".equalsIgnoreCase(action))
        {
            request.setAttribute("listNat", daoNat.getAll());
            request.getRequestDispatcher("/WEB-INF/views/add-customer.jsp").forward(request, response);
        }
        else if ("update".equalsIgnoreCase(action))
        {
            Customer customer = daoCus.getCustomerById(request.getParameter("id"));
            if (customer == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found.");
                return;
            }
            request.setAttribute("customer", customer);
            request.setAttribute("listNat", daoNat.getAll());
            request.getRequestDispatcher("/WEB-INF/views/update-customer-info.jsp").forward(request, response);
        }
        else if ("viewDetail".equalsIgnoreCase(action))
        {
            Customer customer = daoCus.getCustomerById(request.getParameter("id"));
            if (customer == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found.");
                return;
            }
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/WEB-INF/views/customer-detail.jsp").forward(request, response);
        }
        else
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        if (!checkAccess(request, response))
        {
            return;
        }
        String action = request.getParameter("action");
        if (!isWriteAction(action))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
            return;
        }
        if (isAdmin(request))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin cannot add or edit customers.");
            return;
        }
        CustomerDAO daoCus = new CustomerDAO();
        String name = trimToNull(request.getParameter("name"));
        String phone = trimToNull(request.getParameter("phone"));
        String email = trimToNull(request.getParameter("email"));
        String address = trimToNull(request.getParameter("address"));
        String nationalityId = trimToNull(request.getParameter("nation"));
        Customer customer;
        if ("update".equalsIgnoreCase(action))
        {
            customer = daoCus.getCustomerById(request.getParameter("id"));
            if (customer == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer not found.");
                return;
            }
        }
        else
        {
            customer = new Customer();
        }
        String formUrl = "add".equalsIgnoreCase(action) ? "/customer?action=add" : "/customer?action=update&id=" + customer.getCustomerID();
        if (name == null || nationalityId == null)
        {
            Flash.error(request, "Full name and nationality are required.");
            response.sendRedirect(request.getContextPath() + formUrl);
            return;
        }
        if (name.length() > 100 || (phone != null && phone.length() > 15) || (email != null && email.length() > 100) || (address != null && address.length() > 200))
        {
            Flash.error(request, "Customer information exceeds the allowed length.");
            response.sendRedirect(request.getContextPath() + formUrl);
            return;
        }
        Nationality nationality = null;
        for (Nationality item : new NationalityDAO().getAll())
        {
            if (nationalityId.equals(item.getNationalityID()))
            {
                nationality = item;
                break;
            }
        }
        if (nationality == null)
        {
            Flash.error(request, "Invalid nationality.");
            response.sendRedirect(request.getContextPath() + formUrl);
            return;
        }
        customer.setFullName(name);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setNationalityID(nationality);
        if ("add".equalsIgnoreCase(action))
        {
            String createAccount = request.getParameter("createAccount");
            String username = trimToNull(request.getParameter("username"));
            String password = request.getParameter("password");
            if (createAccount == null || username == null || password == null || password.isBlank())
            {
                Flash.error(request, "A customer login account is required. Please enter username and password.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }
            if (username.length() > 50)
            {
                Flash.error(request, "Username must not exceed 50 characters.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }
            if (new UserDAO().isUsernameExists(username))
            {
                Flash.error(request, "Username already exists.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }
            customer.setCustomerID(daoCus.generateCustomerID());
            if (!daoCus.insertCustomerWithAccount(customer, username, password))
            {
                Flash.error(request, "Failed to add customer. Please check the information and try again.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }
            Flash.success(request, "Customer added successfully.");
        }
        else
        {
            if (!daoCus.updateCustomer(customer))
            {
                Flash.error(request, "Failed to update customer information.");
                response.sendRedirect(request.getContextPath() + formUrl);
                return;
            }
            Flash.success(request, "Customer information updated successfully.");
        }
        response.sendRedirect(request.getContextPath() + "/customer?action=list");
    }

    @Override
    public String getServletInfo()
    {
        return "Customer management servlet";
    }
}