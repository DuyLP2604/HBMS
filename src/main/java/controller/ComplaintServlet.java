package controller;

import dao.ComplaintDAO;
import dao.CustomerDAO;
import entity.Complaint;
import entity.Customer;
import entity.Users;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import util.flash.Flash;
import util.security.CsrfTokens;

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
        action = (action == null || action.trim().isEmpty()) ? "list" : action;
        ComplaintDAO daoCp = createComplaintDAO();

        if ("list".equalsIgnoreCase(action)) {
            if (!canManageComplaints(user)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }
            request.setAttribute("complaints", daoCp.getAll());
            request.getRequestDispatcher("/WEB-INF/views/complaints.jsp").forward(request, response);

        } else if ("add".equalsIgnoreCase(action)) {
            if (!"Customer".equalsIgnoreCase(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can submit complaints.");
                return;
            }
            Customer customer = new CustomerDAO().getCustomerByUserId(user.getUserID());
            if (customer != null) {
                List<Complaint> myComplaints = daoCp.getAll().stream()
                        .filter(c -> c.getCustomerID() != null && c.getCustomerID().getCustomerID().equals(customer.getCustomerID()))
                        .collect(Collectors.toList());
                request.setAttribute("myComplaints", myComplaints);
            }
            request.getRequestDispatcher("/WEB-INF/views/add-complaint.jsp").forward(request, response);

        } else if ("viewDetail".equalsIgnoreCase(action)) {
            Integer id = readComplaintId(request, response);
            if (id == null) {
                return;
            }
            Complaint complaint = daoCp.getComplaintById(id);

            if (complaint == null || !canViewComplaint(user, complaint)) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Complaint not found or access denied.");
                return;
            }

            request.setAttribute("complaint", complaint);
            request.setAttribute("canManageComplaints", canManageComplaints(user));
            if (canManageComplaints(user)) {
                request.setAttribute("complaintCsrfToken", CsrfTokens.getOrCreate(request.getSession()));
            }
            request.getRequestDispatcher("/WEB-INF/views/complaint-detail.jsp").forward(request, response);
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
        ComplaintDAO daoCp = createComplaintDAO();

        if ("add".equalsIgnoreCase(action)) {
            if (!"Customer".equalsIgnoreCase(user.getRole())) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can submit complaints.");
                return;
            }
            Customer customer = new CustomerDAO().getCustomerByUserId(user.getUserID());
            String title = request.getParameter("title");
            String content = request.getParameter("content");

            if (title == null || title.trim().isEmpty() || content == null || content.trim().isEmpty() || title.length() > 200) {
                Flash.error(request, "Title (max 200 chars) and details are required.");
                response.sendRedirect(request.getContextPath() + "/complaint?action=add");
                return;
            }

            Complaint complaint = new Complaint();
            complaint.setTitle(title.trim());
            complaint.setContent(content.trim());
            complaint.setCustomerID(customer);
            complaint.setCreatedAt(new Date());
            complaint.setStatus("Chưa xử lý");

            if (daoCp.insert(complaint)) {
                Flash.success(request, "Complaint added successfully.");
            } else {
                Flash.error(request, "System error. Please try again.");
            }
            response.sendRedirect(request.getContextPath() + "/complaint?action=add");

        } else if ("reply".equalsIgnoreCase(action)) {
            if (!canManageComplaints(user)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }

            if (!CsrfTokens.isValid(request)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form expired. Reload the page and try again.");
                return;
            }
            if (user.getUserID() == null || user.getUserID() <= 0) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Please sign in again.");
                return;
            }
            Integer id = readComplaintId(request, response);
            if (id == null) {
                return;
            }
            try {
                daoCp.updateReply(id, request.getParameter("replyMessage"), request.getParameter("status"), user.getUserID());
                Flash.success(request, "Reply saved successfully.");
            } catch (EntityNotFoundException ex) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Complaint not found.");
                return;
            } catch (SecurityException ex) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Your account can no longer reply to complaints. Please sign in again.");
                return;
            } catch (IllegalArgumentException ex) {
                Flash.error(request, ex.getMessage());
            } catch (RuntimeException ex) {
                getServletContext().log("Unable to save complaint reply for complaint " + id, ex);
                Flash.error(request, "Unable to save your reply. Please try again later.");
            }
            response.sendRedirect(request.getContextPath() + "/complaint?action=viewDetail&id=" + id);
        }
    }

    protected ComplaintDAO createComplaintDAO() {
        return new ComplaintDAO();
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
        return "Handles complaint submission, viewing, status updates and replies.";
    }
}
