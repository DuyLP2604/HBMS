/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

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
import java.util.List;
import model.Nationality;

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
        request.getRequestDispatcher("registerCustomer.jsp").forward(request, response);
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
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        String fullname = request.getParameter("fullname");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");

        String cccd = request.getParameter("cccd");
        String passportNumber = request.getParameter("passportNumber");
        String nationalityId = request.getParameter("nationalityID");

        if (udao.isUsernameExists(username)) {
            request.setAttribute("error",
                    "Username already exists");

            request.getRequestDispatcher(
                    "registerCustomer.jsp")
                    .forward(request, response);

            return;
        }

        if ((cccd == null || cccd.trim().isEmpty())
                && (passportNumber == null || passportNumber.trim().isEmpty())) {

            request.setAttribute("error",
                    "Please provide either Identity Number or Passport Number.");

            request.getRequestDispatcher("registerCustomer.jsp")
                    .forward(request, response);

            return;
        }

        int userInsertedID = udao.insertUser(username, password, "Customer");
        if (userInsertedID == -1) {
            response.sendRedirect("error.jsp");
            return;
        }

        String customerId = cdao.generateCustomerID();
        boolean customerInserted = cdao.insertCustomer(customerId, fullname, phone, email, address, cccd, passportNumber, nationalityId, userInsertedID);
        if (!customerInserted) {
            response.sendRedirect("error.jsp");
            return;
        }
        else{
            response.sendRedirect("login.jsp");
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
