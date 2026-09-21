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

@WebServlet(
        name = "LoginServlet",
        urlPatterns = {"/login"}
)
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

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
            user = userDAO.login(
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

        /*
         * Delete the old session to prevent session fixation.
         */
        HttpSession oldSession
                = request.getSession(false);

        if (oldSession != null) {
            oldSession.invalidate();
        }

        /*
         * Create a new authenticated session.
         */
        HttpSession session
                = request.getSession(true);

        session.setAttribute("user", user);
        session.setAttribute(
                "role",
                user.getRole()
        );
        session.setAttribute(
                "userId",
                user.getUserID()
        );

        /*
         * Session expires after 30 minutes of inactivity.
         * Unit: seconds.
         */
        session.setMaxInactiveInterval(30 * 60);

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
    private void redirectByRole(
            Users user,
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        String contextPath
                = request.getContextPath();

        String role = user.getRole();

        if ("Customer".equalsIgnoreCase(role)) {
            response.sendRedirect(
                    contextPath + "/home"
            );
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
            response.sendRedirect(
                    contextPath + "/staff/dashboard"
            );
            return;
        }

        if ("Admin".equalsIgnoreCase(role)) {
            response.sendRedirect(
                    contextPath + "/dashboard"
            );
            return;
        }

        /*
         * Reject accounts with an unsupported role.
         */
        HttpSession session
                = request.getSession(false);

        if (session != null) {
            session.invalidate();
        }

        response.sendRedirect(
                contextPath + "/login"
        );
    }

    private String trimParameter(String value) {
        return value == null
                ? null
                : value.trim();
    }

    @Override
    public String getServletInfo() {
        return "Authenticates users and redirects them by role.";
    }
}