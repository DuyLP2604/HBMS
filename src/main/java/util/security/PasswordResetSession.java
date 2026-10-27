package util.security;

import dao.PasswordResetDAO;
import dao.PasswordResetDAO.Account;
import jakarta.servlet.http.HttpSession;

/** Binds OTP permission to the exact account and credentials that received the email. */
public final class PasswordResetSession {
    private PasswordResetSession() { }

    public static void bind(HttpSession session, Account account) {
        session.setAttribute("resetUserId", account.getUserId());
        session.setAttribute("resetEmail", account.getEmail());
        session.setAttribute("resetAccountRole", account.getRole());
        session.setAttribute("resetPasswordHash", account.getPasswordHash());
    }

    public static Account account(HttpSession session) {
        if (session == null) {
            return null;
        }
        Object id = session.getAttribute("resetUserId");
        Object email = session.getAttribute("resetEmail");
        Object role = session.getAttribute("resetAccountRole");
        Object hash = session.getAttribute("resetPasswordHash");
        if (!(id instanceof Integer) || !(email instanceof String) || !(role instanceof String)
                || !(hash instanceof String) || ((String) email).isBlank()
                || ((String) hash).isBlank()) {
            return null;
        }
        return new Account((Integer) id, (String) role, (String) email, (String) hash);
    }

    public static boolean isEligible(HttpSession session, PasswordResetDAO dao) {
        Account account = account(session);
        return account != null && dao.isEligible(account);
    }

    public static void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        String[] attributes = {"resetUserId", "resetEmail", "resetAccountRole", "resetPasswordHash",
            "resetOtp", "resetOtpExpiry", "resetOtpAttempts", "resetOtpVerified", "resetVerifiedExpiry",
            "resetOtpResendCount", "resetOtpResendWindowStart"};
        for (String name : attributes) {
            session.removeAttribute(name);
        }
        // Keep cooldown and rate counters when a flow is cancelled, rejected or completed.
    }
}
