package controller;

import dao.UserDAO;
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
import util.security.LoginSession.PendingStaffLogin;

@WebServlet(name = "SetNewPasswordServlet", urlPatterns = {"/set-new-password"})
public class SetNewPasswordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PendingStaffLogin pending = LoginSession.pending(request.getSession(false));
        if (pending == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        request.setAttribute("setupUsername", pending.getUsername());
        request.setAttribute("csrfToken", CsrfTokens.getOrCreate(request.getSession()));
        request.getRequestDispatcher("/WEB-INF/views/set-new-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        PendingStaffLogin pending = LoginSession.pending(session);
        if (pending == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The form expired. Reload the page and try again.");
            return;
        }
        String password = request.getParameter("password");
        String error = EmployeeValidator.validatePassword(password);
        if (error == null && !password.equals(request.getParameter("confirmPassword"))) {
            error = "Passwords do not match.";
        }
        if (error != null) {
            Flash.error(request, error);
            response.sendRedirect(request.getContextPath() + "/set-new-password");
            return;
        }
        Users user;
        try {
            user = createUserDAO().completeFirstLogin(pending.getUserId(), pending.getPasswordHash(), password);
        } catch (IllegalArgumentException exception) {
            Flash.error(request, exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/set-new-password");
            return;
        } catch (RuntimeException exception) {
            getServletContext().log("Unable to finish staff password setup.", exception);
            Flash.error(request, "Unable to save the new password. Please try again.");
            response.sendRedirect(request.getContextPath() + "/set-new-password");
            return;
        }
        if (user == null) {
            session.invalidate();
            Flash.error(request, "These temporary credentials are no longer valid. Please log in using your current password or the latest login email.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        LoginSession.authenticate(request, user);
        Flash.success(request, "Password set successfully. Welcome to Simple Bear Hotel!");
        response.sendRedirect(request.getContextPath() + LoginSession.destination(user));
    }

    protected UserDAO createUserDAO() {
        return new UserDAO();
    }
}
