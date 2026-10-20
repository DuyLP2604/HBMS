package controller;

import dao.UserDAO;
import entity.Users;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.flash.Flash;
import util.security.LoginSession;

@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/login"}
)
public class LoginServlet extends HttpServlet {

    protected UserDAO createUserDAO() {
        return new UserDAO();
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (LoginSession.pending(session) != null) {
            response.sendRedirect(request.getContextPath() + "/set-new-password");
            return;
        }

        if (session != null) {
            Users loggedInUser
                    = (Users) session.getAttribute("user");

            if (loggedInUser != null) {
                redirectByRole(
                        loggedInUser,
                        request,
                        response
                );
                return;
            }
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/login.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = trimParameter(
                request.getParameter("username")
        );

        String password
                = request.getParameter("password");

        if (username == null
                || username.isEmpty()
                || password == null
                || password.isEmpty()) {

            Flash.error(
                    request,
                    "Username and password are required."
            );

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        Users user;

        try {
            user = createUserDAO().login(
                    username,
                    password
            );
        } catch (Exception ex) {
            ex.printStackTrace();

            Flash.error(
                    request,
                    "Unable to log in. Please try again."
            );

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if (user == null || user.getUserID() == 0) {
            Flash.error(
                    request,
                    "Invalid username or password."
            );

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if ("Staff".equalsIgnoreCase(user.getRole()) && user.isMustChangePassword()) {
            LoginSession.beginPasswordSetup(request, user);
            response.sendRedirect(request.getContextPath() + "/set-new-password");
            return;
        }

        LoginSession.authenticate(request, user);

        Flash.success(
                request,
                "Login successful. Welcome back!"
        );

        redirectByRole(
                user,
                request,
                response
        );
    }

    /**
     * Redirects the logged-in user according to their role.
     */
    private void redirectByRole(Users user, HttpServletRequest request, HttpServletResponse response) throws IOException {
        String contextPath = request.getContextPath();
        String role = user.getRole();
        if ("Customer".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/home");
            return;
        }

        if ("Staff".equalsIgnoreCase(role)) {
            /*
             * Receptionist and other staff first enter
             * the same staff dashboard.
             *
             * StaffDashboardServlet will load Employee
             * and display menus based on Position.
             */
            response.sendRedirect(contextPath + "/staff/dashboard");
            return;
        }

        if ("Admin".equalsIgnoreCase(role)) {
            response.sendRedirect(contextPath + "/dashboard");
            return;
        }

        /*
         * Reject accounts with an unsupported role.
         */
        HttpSession session = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(contextPath + "/login");
    }

    private String trimParameter(String value) {
        return value == null ? null : value.trim();
    }

    @Override
    public String getServletInfo() {
        return "Authenticates users and redirects them by role.";
    }
}
