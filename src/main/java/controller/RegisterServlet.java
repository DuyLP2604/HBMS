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
public class RegisterServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        NationalityDAO nationalityDAO = new NationalityDAO();
        List<Nationality> nationalities = nationalityDAO.getAll();
        request.setAttribute("nationalities", nationalities);
        request.getRequestDispatcher("/WEB-INF/views/registerCustomer.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
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
        if (isBlank(username) || isBlank(password) || isBlank(fullname) || isBlank(email) || isBlank(address) || isBlank(nationalityId))
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Please complete all required fields.");
            return;
        }
        if (username.length() > 50)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Username must not exceed 50 characters.");
            return;
        }
        String confirmPassword = request.getParameter("confirmPassword");
        if (password.length() < 6)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Password must contain at least 6 characters.");
            return;
        }
        if (confirmPassword == null || !password.equals(confirmPassword))
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Passwords do not match.");
            return;
        }
        if (fullname.length() > 100)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Full name must not exceed 100 characters.");
            return;
        }
        if (phone != null && phone.length() > 15)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Phone number must not exceed 15 characters.");
            return;
        }
        if (email.length() > 100)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Email must not exceed 100 characters.");
            return;
        }
        if (address.length() > 200)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Address must not exceed 200 characters.");
            return;
        }
        if (userDAO.isUsernameExists(username))
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Username already exists.");
            return;
        }
        Nationality selectedNationality = nationalityDAO.getNationalityById(nationalityId);
        if (selectedNationality == null)
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Please select a valid nationality.");
            return;
        }
        if (isVietnamese(selectedNationality) && isBlank(phone))
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Vietnamese customers must provide a phone number.");
            return;
        }
        if (isBlank(phone))
        {
            phone = null;
        }
        Customer customer = new Customer();
        customer.setCustomerID(customerDAO.generateCustomerID());
        customer.setFullName(fullname);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        customer.setNationalityID(selectedNationality);
        if (!customerDAO.insertCustomerWithAccount(customer, username, password))
        {
            forwardRegisterWithError(request, response, nationalityDAO, "Unable to complete registration. Please check your information and try again.");
            return;
        }
        Flash.success(request, "Account created successfully. You can now log in.");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private boolean isVietnamese(Nationality nationality)
    {
        return nationality != null && "N01".equalsIgnoreCase(nationality.getNationalityID());
    }

    private String trimParameter(String value)
    {
        return value == null ? null : value.trim();
    }

    private boolean isBlank(String value)
    {
        return value == null || value.isBlank();
    }

    private void keepFormData(HttpServletRequest request, String username, String fullname, String phone, String email, String address, String nationalityId)
    {
        request.setAttribute("username", username);
        request.setAttribute("fullname", fullname);
        request.setAttribute("phone", phone);
        request.setAttribute("email", email);
        request.setAttribute("address", address);
        request.setAttribute("nationalityID", nationalityId);
    }

    private void forwardRegisterWithError(HttpServletRequest request, HttpServletResponse response, NationalityDAO nationalityDAO, String message) throws ServletException, IOException
    {
        Flash.error(request, message);
        request.setAttribute("nationalities", nationalityDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/registerCustomer.jsp").forward(request, response);
    }

    @Override
    public String getServletInfo()
    {
        return "Customer registration servlet";
    }
}