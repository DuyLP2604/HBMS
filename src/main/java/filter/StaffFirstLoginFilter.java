package filter;

import dao.UserDAO;
import entity.Users;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import util.flash.Flash;
import util.security.LoginSession;
import util.security.PendingStaffLogin;

/* Mapped first in web.xml. A pending login has no user/role/userId attributes. */
/** Checks staff account access and enforces first-login password setup. */
public class StaffFirstLoginFilter implements Filter {
    /** Redirect pending staff sessions to the password setup page. */
    @Override
    public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) input;
        HttpServletResponse response = (HttpServletResponse) output;
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        String path = request.getServletPath();
        boolean staticAsset = ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod()))
                && (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/fontawesome/")
                || path.startsWith("/assets/") || path.startsWith("/images/") || "/favicon.ico".equals(path));
        boolean staff = user != null && "Staff".equalsIgnoreCase(user.getRole());
        PendingStaffLogin pending = LoginSession.pending(session);
        if ((staff || pending != null) && !staticAsset && !"/logout".equals(path)) {
            response.setHeader("Cache-Control", "no-store");
            int userId = staff ? user.getUserID() : pending.getUserId();
            int accessVersion = staff ? user.getAccessVersion() : pending.getAccessVersion();
            boolean valid;
            try {
                valid = createUserDAO().isStaffSessionValid(userId, accessVersion);
            } catch (RuntimeException exception) {
                request.getServletContext().log("Unable to check staff account status.", exception);
                response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE, "Unable to check your account status. Please try again.");
                return;
            }
            if (!valid) {
                session.invalidate();
                Flash.error(request, "Your staff session has ended. Sign in again or contact an administrator.");
                response.sendRedirect(request.getContextPath() + "/login");
                return;
            }
        }
        if (staff && user.isMustChangePassword()) {
            LoginSession.beginPasswordSetup(request, user);
            session = request.getSession(false);
        }
        boolean hasPending = session != null && session.getAttribute(LoginSession.PENDING_STAFF) != null;
        if (!hasPending) {
            chain.doFilter(input, output);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        if (LoginSession.pending(session) == null) {
            session.invalidate();
            Flash.error(request, "Your password setup session expired. Please log in again.");
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        if ("/set-new-password".equals(path) || "/logout".equals(path) || staticAsset) {
            chain.doFilter(input, output);
            return;
        }
        response.sendRedirect(request.getContextPath() + "/set-new-password");
    }

    protected UserDAO createUserDAO() {
        return new UserDAO();
    }
}
