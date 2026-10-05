package controller;
import dao.ComplaintDAO;
import dao.CustomerDAO;
import entity.Complaint;
import entity.Customer;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import util.flash.Flash;
@WebServlet(name = "ComplaintServlet", urlPatterns = {"/complaint"})
public class ComplaintServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "list";
        }
        ComplaintDAO daoCp = new ComplaintDAO();
        if ("list".equalsIgnoreCase(action)) {
            if (!canManageComplaints(user)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this page!");
                return;
            }
            request.setAttribute("complaints", daoCp.getAll());
            request.getRequestDispatcher("/WEB-INF/views/complaints.jsp").forward(request, response);
        } else if ("add".equalsIgnoreCase(action)) {
            if (!"Customer".equalsIgnoreCase(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can submit complaints.");
                return;
            }
            request.getRequestDispatcher("/WEB-INF/views/add-complaint.jsp").forward(request, response);
        } else if ("viewDetail".equalsIgnoreCase(action)) {
            Integer id = readComplaintId(request, response);
            if (id == null) {
                return;
            }
            Complaint complaint = daoCp.getComplaintById(id);
            if (complaint == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Complaint not found.");
                return;
            }
            if (!canViewComplaint(user, complaint)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to view this complaint.");
                return;
            }
            request.setAttribute("complaint", complaint);
            request.getRequestDispatcher("/WEB-INF/views/complaint-detail.jsp").forward(request, response);
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = getLoggedInUser(request);
        if (user == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        String action = request.getParameter("action");
        ComplaintDAO daoCp = new ComplaintDAO();
        if ("add".equalsIgnoreCase(action)) {
            if (!"Customer".equalsIgnoreCase(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can submit complaints.");
                return;
            }
            Customer customer = new CustomerDAO().getCustomerByUserId(user.getUserID());
            if (customer == null) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "No customer profile is linked to this account.");
                return;
            }
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty()) {
                request.setAttribute("errorMessage", "Title and complaint details are required.");
                request.getRequestDispatcher("/WEB-INF/views/add-complaint.jsp").forward(request, response);
                return;
            }
            if (title.trim().length() > 200) {
                request.setAttribute("errorMessage", "Title must not exceed 200 characters.");
                request.getRequestDispatcher("/WEB-INF/views/add-complaint.jsp").forward(request, response);
                return;
            }
            Complaint complaint = new Complaint();
            complaint.setTitle(title.trim());
            complaint.setContent(content.trim());
            complaint.setCustomerID(customer);
            complaint.setCreatedAt(new Date());
            complaint.setStatus("Chưa xử lý");
            if (!daoCp.insert(complaint)) {
                request.setAttribute("errorMessage", "Unable to submit your complaint. Please try again.");
                request.getRequestDispatcher("/WEB-INF/views/add-complaint.jsp").forward(request, response);
                return;
            }
            Flash.success(request, "Complaint added successfully.");
            response.sendRedirect(request.getContextPath() + "/complaint?action=add");
        } else if ("updateStatus".equalsIgnoreCase(action)) {
            if (!canManageComplaints(user)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to update complaint status.");
                return;
            }
            Integer id = readComplaintId(request, response);
            if (id == null) {
                return;
            }
            String status = request.getParameter("status");
            if (status == null || status.trim().isEmpty() || status.trim().length() > 30) {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Status is required and must not exceed 30 characters.");
                return;
            }
            if (daoCp.getComplaintById(id) == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Complaint not found.");
                return;
            }
            try {
                daoCp.updateStatus(id, status.trim());
            } catch (IllegalStateException ex) {
                throw new ServletException("Unable to update complaint status.", ex);
            }
            Flash.success(request, "Complaint status updated successfully.");
            response.sendRedirect(request.getContextPath() + "/complaint?action=list");
        } else {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid action.");
        }
    }

    private Users getLoggedInUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object user = session.getAttribute("user");
        return user instanceof Users ? (Users) user : null;
    }

    private boolean canManageComplaints(Users user) {
        return "Admin".equalsIgnoreCase(user.getRole()) || "Staff".equalsIgnoreCase(user.getRole());
    }

    private boolean canViewComplaint(Users user, Complaint complaint) {
        if (canManageComplaints(user)) {
            return true;
        }
        if (!"Customer".equalsIgnoreCase(user.getRole()) || complaint.getCustomerID() == null) {
            return false;
        }
        Customer customer = new CustomerDAO().getCustomerByUserId(user.getUserID());
        return customer != null && customer.getCustomerID() != null && Objects.equals(customer.getCustomerID(), complaint.getCustomerID().getCustomerID());
    }

    private Integer readComplaintId(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String value = request.getParameter("id");
        try {
            int id = Integer.parseInt(value == null ? "" : value.trim());
            if (id <= 0) {
                throw new NumberFormatException();
            }
            return id;
        } catch (NumberFormatException ex) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid complaint ID.");
            return null;
        }
    }

    @Override
    public String getServletInfo() {
        return "Handles complaint submission, viewing and status updates.";
    }
}