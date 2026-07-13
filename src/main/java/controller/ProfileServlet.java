/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Customer;
import model.Nationality;
import model.User;

/**
 *
 * @author default
 */
@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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
        CustomerDAO cdao = new CustomerDAO();
        UserDAO udao = new UserDAO();
        NationalityDAO ndao = new NationalityDAO();
        HttpSession session = request.getSession();

        User user = (User) session.getAttribute("user");
        if (action.equalsIgnoreCase("view")) {
            Customer customer = cdao.getCustomerByUserId(user.getId());
            request.setAttribute("customer", customer);
            request.getRequestDispatcher("profile.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("update")) {
            Customer customer = cdao.getCustomerByUserId(user.getId());
            request.setAttribute("customerUpdate", customer);
            List<Nationality> nationalities = ndao.getAll();
            request.setAttribute("nationalityUpdate", nationalities);
            request.getRequestDispatcher("updateProfile.jsp").forward(request, response);
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

        String action = request.getParameter("action");

        CustomerDAO cdao = new CustomerDAO();
        NationalityDAO ndao = new NationalityDAO();

        HttpSession session = request.getSession();
        User user = (User) session.getAttribute("user");

        if (action.equalsIgnoreCase("update")) {

            Customer customer = cdao.getCustomerByUserId(user.getId());

            customer.setFullname(request.getParameter("fullname"));
            customer.setPhone(request.getParameter("phone"));
            customer.setEmail(request.getParameter("email"));
            customer.setAddress(request.getParameter("address"));
            customer.setCccd(request.getParameter("cccd"));
            customer.setPassportNumber(request.getParameter("passportNumber"));

            String nationalityId = request.getParameter("nationalityId");
            customer.setNationality(ndao.getNationalityById(nationalityId));

            cdao.updateCustomer(customer);

            response.sendRedirect("profile?action=view");
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
