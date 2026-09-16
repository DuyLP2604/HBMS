/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import entity.Customer;
import entity.Nationality;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import util.flash.Flash;

/**
 *
 * @author default
 */
@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

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
        NationalityDAO ndao = new NationalityDAO();
        List<Nationality> list = ndao.getAll();
        request.setAttribute("nationalities", list);
        request.getRequestDispatcher("/WEB-INF/views/registerCustomer.jsp").forward(request, response);
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

        UserDAO udao = new UserDAO();
        CustomerDAO cdao = new CustomerDAO();
        NationalityDAO ndao = new NationalityDAO();

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String fullname = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");

        String cccd = request.getParameter("cccd");
        String passportNumber = request.getParameter("passportNumber");
        String nationalityId = request.getParameter("nationalityID");

        // get the information if error occur to avoid typing again
        request.setAttribute("username", username);
        request.setAttribute("fullname", fullname);
        request.setAttribute("phone", phone);
        request.setAttribute("email", email);
        request.setAttribute("address", address);
        request.setAttribute("cccd", cccd);
        request.setAttribute("passportNumber", passportNumber);
        request.setAttribute("nationalityID", nationalityId);

        if (udao.isUsernameExists(username)) {
            Flash.error(request, "Username already exists.");
            response.sendRedirect(request.getContextPath() + "/register");
            //reload nationality lists
            request.setAttribute("nationalities", ndao.getAll());

            request.getRequestDispatcher("registerCustomer.jsp").forward(request, response);
            return;
        }

        if ((cccd == null || cccd.trim().isEmpty()) && (passportNumber == null || passportNumber.trim().isEmpty())) {
            Flash.error(request, "Please provide either an Identity Number or Passport Number.");
            response.sendRedirect(request.getContextPath() + "/register");
            request.setAttribute("nationalities", ndao.getAll());
            request.getRequestDispatcher("registerCustomer.jsp").forward(request, response);
            return;
        }

        int userInsertedID = udao.insertUser(username, password, "Customer");

        if (userInsertedID == -1) {
            Flash.error(request, "Unable to create your account. Please try again.");

            response.sendRedirect(request.getContextPath() + "/register");
        }

        String customerId = cdao.generateCustomerID();
        Customer c = new Customer();
        c.setCustomerID(customerId);
        c.setFullName(fullname);
        c.setPhone(phone);
        c.setEmail(email);
        c.setAddress(address);
        c.setCccd(cccd);
        c.setPassportNumber(passportNumber);
        c.setNationalityID(ndao.getNationalityById(nationalityId));
        c.setUserID(udao.login(username, password));

        boolean customerInserted = cdao.insertCustomer(c);

        if (!customerInserted) {

            // rollback
            Flash.error(request, "Unable to complete registration. Please try again.");

            response.sendRedirect(request.getContextPath() + "/register");
            response.sendRedirect("error.jsp");
            return;
        }
        Flash.success(request, "Account created successfully. You can now log in.");
        response.sendRedirect(request.getContextPath() + "/login");

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
