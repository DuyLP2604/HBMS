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
import model.Nationality;
import util.flash.Flash;

@WebServlet(name = "RegisterServlet", urlPatterns = {"/register"})
public class RegisterServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        NationalityDAO ndao =
                new NationalityDAO();

        List<Nationality> list =
                ndao.getAll();

        request.setAttribute(
                "nationalities",
                list
        );

        request
                .getRequestDispatcher(
                        "/WEB-INF/views/registerCustomer.jsp"
                )
                .forward(request, response);
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        UserDAO udao =
                new UserDAO();

        CustomerDAO cdao =
                new CustomerDAO();


        String username =
                request.getParameter("username");

        String password =
                request.getParameter("password");

        String fullname =
                request.getParameter("fullname");

        String phone =
                request.getParameter("phone");

        String email =
                request.getParameter("email");

        String address =
                request.getParameter("address");

        String cccd =
                request.getParameter("cccd");

        String passportNumber =
                request.getParameter("passportNumber");

        String nationalityId =
                request.getParameter("nationalityID");


        /*
         * Check username
         */
        if (udao.isUsernameExists(username)) {

            Flash.error(
                    request,
                    "Username already exists."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/register"
            );

            return;
        }


        /*
         * Customer must provide either
         * Citizen ID or Passport Number
         */
        if ((cccd == null || cccd.trim().isEmpty())
                && (passportNumber == null
                || passportNumber.trim().isEmpty())) {

            Flash.error(
                    request,
                    "Please provide either an Identity Number or Passport Number."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/register"
            );

            return;
        }


        /*
         * Create user account
         */
        int userInsertedID =
                udao.insertUser(
                        username,
                        password,
                        "Customer"
                );


        if (userInsertedID == -1) {

            Flash.error(
                    request,
                    "Unable to create your account. Please try again."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/register"
            );

            return;
        }


        /*
         * Create customer information
         */
        String customerId =
                cdao.generateCustomerID();


        boolean customerInserted =
                cdao.insertCustomer(
                        customerId,
                        fullname,
                        phone,
                        email,
                        address,
                        cccd,
                        passportNumber,
                        nationalityId,
                        userInsertedID
                );


        if (!customerInserted) {

            // Roll back the created user account
            udao.deleteUser(userInsertedID);

            Flash.error(
                    request,
                    "Unable to complete registration. Please try again."
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/register"
            );

            return;
        }


        Flash.success(
                request,
                "Account created successfully. You can now log in."
        );


        response.sendRedirect(
                request.getContextPath()
                + "/login"
        );
    }


    @Override
    public String getServletInfo() {
        return "Handles customer registration.";
    }
}

