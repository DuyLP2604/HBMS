package controller;

import dao.PasswordChangeDAO;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import util.EmployeeValidator;
import util.flash.Flash;
import util.security.CsrfTokens;
import util.security.LoginSession;

@WebServlet(name = "ChangePasswordServlet", urlPatterns = {"/change-password"})
public class ChangePasswordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users user = authenticatedUser(request, response);
        if (user != null) {
            showForm(request, response, user);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = authenticatedUser(request, response);
        if (user == null) {
            return;
        }
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN,
                    "The form expired. Reload the page and try again.");
            return;
        }
        String currentPassword = request.getParameter("currentPassword");
        String newPassword = request.getParameter("newPassword");
        String error = currentPassword == null || currentPassword.isEmpty()
                ? "Please enter your current password." : null;
        if (error == null) {
            error = "Staff".equals(user.getRole()) ? EmployeeValidator.validatePassword(newPassword)
                    : newPassword == null || newPassword.length() < 6
                            ? "Password must contain at least 6 characters." : null;
        }
        if (error == null && !newPassword.equals(request.getParameter("confirmPassword"))) {
            error = "Passwords do not match.";
        }
        if (error != null) {
            request.setAttribute("passwordError", error);
            showForm(request, response, user);
            return;
        }
        boolean changed;
        try {
            // Account identity and role come exclusively from the authenticated session.
            changed = createPasswordChangeDAO().changePassword(user.getUserID(), user.getRole(),
                    currentPassword, newPassword);
        } catch (RuntimeException exception) {
            getServletContext().log("Unable to change account password.", exception);
            request.setAttribute("passwordError", "Unable to save your password. Please try again later.");
            showForm(request, response, user);
            return;
        }
        if (!changed) {
            request.setAttribute("passwordError",
                    "The current password is incorrect or your account has changed. Please try again.");
            showForm(request, response, user);
            return;
        }
        // End this login and discard any OTP grants from the old credential.
        request.getSession(false).invalidate();
        Flash.success(request, "Password changed successfully. Please sign in with your new password.");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private Users authenticatedUser(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        if (LoginSession.pending(session) != null) {
            response.sendRedirect(request.getContextPath() + "/set-new-password");
            return null;
        }
        Object account = session == null ? null : session.getAttribute("user");
        if (!(account instanceof Users) || ((Users) account).getUserID() == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) account;
        if (!("Customer".equals(user.getRole()) || "Staff".equals(user.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        if ("Staff".equals(user.getRole()) && user.isMustChangePassword()) {
            session.invalidate();
            Flash.error(request, "Please sign in to finish your required password setup.");
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        return user;
    }

    private void showForm(HttpServletRequest request, HttpServletResponse response, Users user)
            throws ServletException, IOException {
        request.setAttribute("staffPassword", "Staff".equals(user.getRole()));
        request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/change-password.jsp").forward(request, response);
    }

    protected PasswordChangeDAO createPasswordChangeDAO() {
        return new PasswordChangeDAO();
    }
}
