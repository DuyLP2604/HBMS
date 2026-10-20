package filter;

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

/* Mapped first in web.xml. A pending login has no user/role/userId attributes. */
public class StaffFirstLoginFilter implements Filter {
    @Override
    public void doFilter(ServletRequest input, ServletResponse output, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) input;
        HttpServletResponse response = (HttpServletResponse) output;
        HttpSession session = request.getSession(false);
        Users user = session == null ? null : (Users) session.getAttribute("user");
        if (user != null && "Staff".equalsIgnoreCase(user.getRole()) && user.isMustChangePassword()) {
            LoginSession.beginPasswordSetup(request, user);
            session = request.getSession(false);
        }
        boolean hasPending = session != null && session.getAttribute(LoginSession.PENDING_STAFF) != null;
        if (!hasPending) {
            chain.doFilter(input, output);
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        String path = request.getServletPath();
        boolean staticAsset = ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod()))
                && (path.startsWith("/css/") || path.startsWith("/js/") || path.startsWith("/fontawesome/") || path.startsWith("/images/") || "/favicon.ico".equals(path));
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
}
