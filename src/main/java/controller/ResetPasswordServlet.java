package controller;

import dao.PasswordResetDAO;
import dao.PasswordResetDAO.Account;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import util.EmployeeValidator;
import util.flash.Flash;
import util.security.PasswordResetSession;

@WebServlet(name = "ResetPasswordServlet", urlPatterns = {"/reset-password"})
public class ResetPasswordServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isResetAllowed(session)) {
            rejectReset(request, response, session);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        request.getRequestDispatcher("/WEB-INF/views/reset-password.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        if (session == null) {
            rejectReset(request, response, null);
            return;
        }
        // Consume a verified grant once, including concurrent submissions in the same session.
        synchronized (session) {
            if (!isResetAllowed(session)) {
                rejectReset(request, response, session);
                return;
            }
            Account account = PasswordResetSession.account(session);
            String password = request.getParameter("password");
            String confirmPassword = request.getParameter("confirmPassword");
            String error = "Staff".equals(account.getRole())
                    ? EmployeeValidator.validatePassword(password)
                    : password == null || password.length() < 6
                            ? "Password must contain at least 6 characters." : null;
            if (error == null && !password.equals(confirmPassword)) {
                error = "Passwords do not match.";
            }
            if (error != null) {
                Flash.error(request, error);
                response.sendRedirect(request.getContextPath() + "/reset-password");
                return;
            }
            boolean updated;
            try {
                updated = createPasswordResetDAO().resetPassword(account, password);
            } catch (RuntimeException exception) {
                getServletContext().log("Unable to reset account password.", exception);
                Flash.error(request, "Unable to reset the password. Please try again later.");
                response.sendRedirect(request.getContextPath() + "/reset-password");
                return;
            }
            if (!updated) {
                rejectReset(request, response, session);
                return;
            }
            PasswordResetSession.clear(session);
            // Recovery may start from Change password while the account is signed in.
            session.invalidate();
        }
        Flash.success(request, "Password reset successfully. Please log in.");
        response.sendRedirect(request.getContextPath() + "/login");
    }

    private boolean isResetAllowed(HttpSession session) {
        if (session == null || !Boolean.TRUE.equals(session.getAttribute("resetOtpVerified"))) {
            return false;
        }
        Object expiry = session.getAttribute("resetVerifiedExpiry");
        return expiry instanceof Long && System.currentTimeMillis() <= (Long) expiry
                && PasswordResetSession.isEligible(session, createPasswordResetDAO());
    }

    private void rejectReset(HttpServletRequest request, HttpServletResponse response,
            HttpSession session) throws IOException {
        PasswordResetSession.clear(session);
        Flash.error(request, "This password reset request is no longer valid. Please start again.");
        response.sendRedirect(request.getContextPath() + "/forgot-password");
    }

    protected PasswordResetDAO createPasswordResetDAO() {
        return new PasswordResetDAO();
    }

    @Override
    public String getServletInfo() {
        return "Reset Password Servlet";
    }
}
