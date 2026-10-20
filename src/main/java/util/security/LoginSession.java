package util.security;

import entity.Users;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.Serializable;

public final class LoginSession {
    public static final String PENDING_STAFF = "pendingStaffLogin";
    public static final int TIMEOUT_SECONDS = 30 * 60;

    private LoginSession() {
    }

    public static void beginPasswordSetup(HttpServletRequest request, Users user) {
        HttpSession session = replaceSession(request);
        session.setAttribute(PENDING_STAFF, new PendingStaffLogin(user, System.currentTimeMillis() + TIMEOUT_SECONDS * 1000L));
        CsrfTokens.getOrCreate(session);
    }

    public static void authenticate(HttpServletRequest request, Users user) {
        HttpSession session = replaceSession(request);
        session.setAttribute("user", user);
        session.setAttribute("role", user.getRole());
        session.setAttribute("userId", user.getUserID());
    }

    private static HttpSession replaceSession(HttpServletRequest request) {
        HttpSession old = request.getSession(false);
        if (old != null) {
            old.invalidate();
        }
        HttpSession session = request.getSession(true);
        session.setMaxInactiveInterval(TIMEOUT_SECONDS);
        return session;
    }

    public static PendingStaffLogin pending(HttpSession session) {
        Object value = session == null ? null : session.getAttribute(PENDING_STAFF);
        if (!(value instanceof PendingStaffLogin)) {
            return null;
        }
        PendingStaffLogin pending = (PendingStaffLogin) value;
        return pending.getExpiresAt() > System.currentTimeMillis() ? pending : null;
    }

    public static String destination(Users user) {
        if ("Customer".equalsIgnoreCase(user.getRole())) {
            return "/home";
        }
        if ("Staff".equalsIgnoreCase(user.getRole())) {
            return "/staff/dashboard";
        }
        return "Admin".equalsIgnoreCase(user.getRole()) ? "/dashboard" : "/login";
    }

    public static final class PendingStaffLogin implements Serializable {
        private static final long serialVersionUID = 1L;
        private final int userId;
        private final String username;
        private final String passwordHash;
        private final long expiresAt;

        public PendingStaffLogin(Users user, long expiresAt) {
            this.userId = user.getUserID();
            this.username = user.getUsername();
            this.passwordHash = user.getPassword();
            this.expiresAt = expiresAt;
        }

        public int getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getPasswordHash() { return passwordHash; }
        public long getExpiresAt() { return expiresAt; }
    }
}
