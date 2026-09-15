package controller;

import dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.User;
import util.flash.Flash;

@WebServlet(name = "LoginServlet", urlPatterns = {"/login"})
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request
                .getRequestDispatcher("/WEB-INF/views/login.jsp")
                .forward(request, response);
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        String user =
                request.getParameter("username");

        String pass =
                request.getParameter("password");

        UserDAO udao =
                new UserDAO();

        User u =
                udao.login(user, pass);


        if (u == null || u.getId() == 0) {

            Flash.error(
                    request,
                    "Invalid username or password."
            );

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }


        HttpSession session =
                request.getSession();

        session.setAttribute("user", u);
        session.setAttribute("role", u.getRole());
        session.setAttribute("userId", u.getId());


        Flash.success(
                request,
                "Login successful. Welcome back!"
        );


        response.sendRedirect(
                request.getContextPath() + "/home"
        );
    }


    @Override
    public String getServletInfo() {
        return "Handles user authentication.";
    }
}

