/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.Customer;
import model.Nationality;

/**
 *
 * @author TAN LOI
 */
@WebServlet(name = "CustomerServlet", urlPatterns = {"/customer"})
public class CustomerServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        CustomerDAO daoCus = new CustomerDAO();
        NationalityDAO daoNat = new NationalityDAO();
        if (action.equalsIgnoreCase("list")) {
            List<Customer> list = daoCus.getAllCustomers();
            request.setAttribute("customers", list);
            request.getRequestDispatcher("customers.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("add")) {
            List<Nationality> listNat = daoNat.getAll();
            request.setAttribute("listNat", listNat);
            request.getRequestDispatcher("add-customer.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("update")) {
            String id = request.getParameter("id");
            Customer cus = daoCus.getCustomerById(id);
            request.setAttribute("customer", cus);
            List<Nationality> listNat = daoNat.getAll();
            request.setAttribute("listNat", listNat);
            request.getRequestDispatcher("update-customer-info.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("viewDetail")) {
            String id = request.getParameter("id");
            Customer cus = daoCus.getCustomerById(id);
            request.setAttribute("customer", cus);
            request.getRequestDispatcher("customer-detail.jsp").forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        CustomerDAO daoCus = new CustomerDAO();

        String name = request.getParameter("name");
        String phone = request.getParameter("phone");
        String email = request.getParameter("email");
        String address = request.getParameter("address");
        String cccd = request.getParameter("cccd");
        String passport = request.getParameter("passport");
        String nationalityId = request.getParameter("nation");

        if (action.equalsIgnoreCase("add")) {

            String createAccount = request.getParameter("createAccount");

            Integer userId = null;

            if (createAccount != null) {

                String username = request.getParameter("username");
                String password = request.getParameter("password");

                UserDAO userDAO = new UserDAO();

                // check if username exist
                if (userDAO.isUsernameExists(username)) {

                    request.setAttribute("error", "Username đã tồn tại");

                    NationalityDAO daoNat = new NationalityDAO();
                    request.setAttribute("listNat", daoNat.getAll());
                    
                    // display error message but need to load the nationality list again
                    request.getRequestDispatcher("add-customer.jsp")
                            .forward(request, response);
                    return;
                }

                userId = userDAO.insertUser(
                        username,
                        password,
                        "Customer"
                );
            }

            String newId = daoCus.generateCustomerID();

            daoCus.insertCustomer(
                    newId,
                    name,
                    phone,
                    email,
                    address,
                    cccd,
                    passport,
                    nationalityId,
                    userId
            );

            response.sendRedirect("customer?action=list");
        } else if (action.equalsIgnoreCase("update")) {
            String id = request.getParameter("id");

            Customer c = new Customer();
            c.setId(id);
            c.setFullname(name);
            c.setPhone(phone);
            c.setEmail(email);
            c.setAddress(address);
            c.setCccd(cccd);
            c.setPassportNumber(passport);
            c.setNationality(new Nationality(nationalityId, null));

            daoCus.updateCustomer(c);
            response.sendRedirect("customer?action=list");
        }
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
