package util.security;

import dao.PasswordResetDAO;
import dto.PasswordResetAccount;
import jakarta.servlet.http.HttpSession;

/** Binds OTP permission to the exact account and credentials that received the email. */
public final class PasswordResetSession {
    private PasswordResetSession() { }

    public static void bind(HttpSession session, PasswordResetAccount account) {
        session.setAttribute("resetUserId", account.getUserId());
        session.setAttribute("resetEmail", account.getEmail());
        session.setAttribute("resetAccountRole", account.getRole());
        session.setAttribute("resetPasswordHash", account.getPasswordHash());
        session.setAttribute("resetAccessVersion", account.getAccessVersion());
    }

    public static PasswordResetAccount account(HttpSession session) {
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
        Object version = session.getAttribute("resetAccessVersion");
        return new PasswordResetAccount((Integer) id, (String) role, (String) email, (String) hash,
                version instanceof Integer ? (Integer) version : 0);
    }

    /** Check that the session still points to an eligible recovery account. */
    public static boolean isEligible(HttpSession session, PasswordResetDAO dao) {
        PasswordResetAccount account = account(session);
        return account != null && dao.isEligible(account);
    }

    public static void clear(HttpSession session) {
        if (session == null) {
            return;
        }
        String[] attributes = {"resetUserId", "resetEmail", "resetAccountRole", "resetPasswordHash", "resetAccessVersion",
            "resetOtp", "resetOtpExpiry", "resetOtpAttempts", "resetOtpVerified", "resetVerifiedExpiry",
            "resetOtpResendCount", "resetOtpResendWindowStart"};
        for (String name : attributes) {
            session.removeAttribute(name);
        }
        // Keep cooldown and rate counters when a flow is cancelled, rejected or completed.
    }
}
