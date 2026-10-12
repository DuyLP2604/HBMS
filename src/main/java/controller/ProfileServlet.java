package controller;
import dao.CustomerDAO;
import dao.NationalityDAO;
import entity.Customer;
import entity.Nationality;
import entity.Users;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.flash.Flash;
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet
{
    private Users checkAccess(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute("user");
        if (!(sessionUser instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) sessionUser;
        if (!"Customer".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can access this profile.");
            return null;
        }
        return user;
    }

    private String trimToNull(String value)
    {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private void forwardUpdateWithError(HttpServletRequest request, HttpServletResponse response, Customer customer, NationalityDAO nationalityDAO, String message) throws ServletException, IOException
    {
        Flash.error(request, message);
        request.setAttribute("customerUpdate", customer);
        request.setAttribute("nationalityUpdate", nationalityDAO.getAll());
        request.getRequestDispatcher("/WEB-INF/views/updateProfile.jsp").forward(request, response);
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = checkAccess(request, response);
        if (user == null)
        {
            return;
        }
        String action = trimToNull(request.getParameter("action"));
        if (action == null)
        {
            action = "view";
        }
        if (!"view".equalsIgnoreCase(action) && !"update".equalsIgnoreCase(action))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Invalid profile action.");
            return;
        }
        CustomerDAO customerDAO = new CustomerDAO();
        Customer customer = customerDAO.getCustomerByUserId(user.getUserID());
        if (customer == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer profile not found.");
            return;
        }
        if ("view".equalsIgnoreCase(action))
        {
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("/WEB-INF/views/profile.jsp").forward(request, response);
        }
        else
        {
            NationalityDAO nationalityDAO = new NationalityDAO();
            request.setAttribute("customerUpdate", customer);
            request.setAttribute("nationalityUpdate", nationalityDAO.getAll());
            request.getRequestDispatcher("/WEB-INF/views/updateProfile.jsp").forward(request, response);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Users user = checkAccess(request, response);
        if (user == null)
        {
            return;
        }
        String action = trimToNull(request.getParameter("action"));
        if (!"update".equalsIgnoreCase(action))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid profile action.");
            return;
        }
        CustomerDAO customerDAO = new CustomerDAO();
        NationalityDAO nationalityDAO = new NationalityDAO();
        Customer customer = customerDAO.getCustomerByUserId(user.getUserID());
        if (customer == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer profile not found.");
            return;
        }
        String fullname = trimToNull(request.getParameter("fullname"));
        String phone = trimToNull(request.getParameter("phone"));
        String email = trimToNull(request.getParameter("email"));
        String address = trimToNull(request.getParameter("address"));
        String nationalityId = trimToNull(request.getParameter("nationalityId"));
        customer.setFullName(fullname);
        customer.setPhone(phone);
        customer.setEmail(email);
        customer.setAddress(address);
        if (fullname == null || nationalityId == null)
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Full name and nationality are required.");
            return;
        }
        if (fullname.length() > 100)
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Full name must not exceed 100 characters.");
            return;
        }
        if (phone != null && phone.length() > 15)
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Phone number must not exceed 15 characters.");
            return;
        }
        if (email != null && email.length() > 100)
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Email must not exceed 100 characters.");
            return;
        }
        if (address != null && address.length() > 200)
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Address must not exceed 200 characters.");
            return;
        }
        Nationality selectedNationality = nationalityDAO.getNationalityById(nationalityId);
        if (selectedNationality == null)
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Please select a valid nationality.");
            return;
        }
        customer.setNationalityID(selectedNationality);
        if (!customerDAO.updateCustomer(customer))
        {
            forwardUpdateWithError(request, response, customer, nationalityDAO, "Unable to update your profile. Please check your information and try again.");
            return;
        }
        Flash.success(request, "Profile updated successfully.");
        response.sendRedirect(request.getContextPath() + "/profile?action=view");
    }

    @Override
    public String getServletInfo()
    {
        return "Customer profile servlet";
    }
}
