/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ComplaintDAO;
import dao.CustomerDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Complaint;
import model.Customer;
import model.User;

/**
 *
 * @author TAN LOI
 */
@WebServlet(name = "ComplaintServlet", urlPatterns = {"/complaint"})
public class ComplaintServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if (action == null) {
            action = "list";
        }
        ComplaintDAO daoCp = new ComplaintDAO();

        if (action.equalsIgnoreCase("list")) {
            // Chỉ Admin/Staff mới được xem danh sách khiếu nại
            HttpSession session = request.getSession();
            String role = (String) session.getAttribute("role");
            if (role == null || (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("Staff"))) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập!");
                return;
            }
            List<Complaint> list = daoCp.getAllComplaints();
            request.setAttribute("complaints", list);
            request.getRequestDispatcher("complaints.jsp").forward(request, response);

        } else if (action.equalsIgnoreCase("add")) {
            request.getRequestDispatcher("add-complaint.jsp").forward(request, response);

        } else if (action.equalsIgnoreCase("viewDetail")) {
            String id = request.getParameter("id");
            Complaint cp = daoCp.getComplaintById(Integer.parseInt(id));
            request.setAttribute("complaint", cp);
            request.getRequestDispatcher("complaint-detail.jsp").forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        ComplaintDAO daoCp = new ComplaintDAO();
        HttpSession session = request.getSession();
        CustomerDAO cdao = new CustomerDAO();
        
        User user = (User) session.getAttribute("user");
        Customer customer = cdao.getCustomerByUserId(user.getId());
        if ("add".equalsIgnoreCase(action)) {
            String title = request.getParameter("title");
            String content = request.getParameter("content");
            String customerId = customer.getId();

            Complaint cp = new Complaint();
            cp.setTitle(title);
            cp.setContent(content);

            daoCp.insertComplaint(cp, customerId);
            response.sendRedirect("complaint?action=add&success=1");

        } else if ("updateStatus".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");
            String status = request.getParameter("status");
            daoCp.updateStatus(Integer.parseInt(id), status);
            response.sendRedirect("complaint?action=viewDetail&id=" + id);
        }
    }

    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
