package controller;

import dao.CustomerDAO;
import dao.NationalityDAO;
import java.io.IOException;
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
import util.flash.Flash;

@WebServlet(name = "ProfileServlet", urlPatterns = {"/profile"})
public class ProfileServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String action =
                request.getParameter("action");

        CustomerDAO cdao =
                new CustomerDAO();

        NationalityDAO ndao =
                new NationalityDAO();

        HttpSession session =
                request.getSession();

        User user =
                (User) session.getAttribute("user");


        if (action.equalsIgnoreCase("view")) {

            Customer customer =
                    cdao.getCustomerByUserId(
                            user.getId()
                    );

            request.setAttribute(
                    "customer",
                    customer
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/profile.jsp"
                    )
                    .forward(request, response);


        } else if (action.equalsIgnoreCase("update")) {

            Customer customer =
                    cdao.getCustomerByUserId(
                            user.getId()
                    );

            request.setAttribute(
                    "customerUpdate",
                    customer
            );

            List<Nationality> nationalities =
                    ndao.getAll();

            request.setAttribute(
                    "nationalityUpdate",
                    nationalities
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/updateProfile.jsp"
                    )
                    .forward(request, response);
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action =
                request.getParameter("action");

        CustomerDAO cdao =
                new CustomerDAO();

        NationalityDAO ndao =
                new NationalityDAO();

        HttpSession session =
                request.getSession();

        User user =
                (User) session.getAttribute("user");


        if (action.equalsIgnoreCase("update")) {

            Customer customer =
                    cdao.getCustomerByUserId(
                            user.getId()
                    );


            customer.setFullname(
                    request.getParameter("fullname")
            );

            customer.setPhone(
                    request.getParameter("phone")
            );

            customer.setEmail(
                    request.getParameter("email")
            );

            customer.setAddress(
                    request.getParameter("address")
            );

            customer.setCccd(
                    request.getParameter("cccd")
            );

            customer.setPassportNumber(
                    request.getParameter("passportNumber")
            );


            String nationalityId =
                    request.getParameter("nationalityId");

            customer.setNationality(
                    ndao.getNationalityById(
                            nationalityId
                    )
            );


            cdao.updateCustomer(customer);


            Flash.success(
                    request,
                    "Profile updated successfully."
            );


            response.sendRedirect(
                    request.getContextPath()
                    + "/profile?action=view"
            );
        }
    }


    @Override
    public String getServletInfo() {
        return "Handles customer profile management.";
    }
}

