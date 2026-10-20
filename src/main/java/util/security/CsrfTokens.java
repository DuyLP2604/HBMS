package util.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public final class CsrfTokens {
    private static final String KEY = "staffAccountCsrf";
    private static final SecureRandom RANDOM = new SecureRandom();

    private CsrfTokens() {
    }

    public static String getOrCreate(HttpSession session) {
        synchronized (session) {
            String token = (String) session.getAttribute(KEY);
            if (token == null) {
                byte[] bytes = new byte[32];
                RANDOM.nextBytes(bytes);
                token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
                session.setAttribute(KEY, token);
            }
            return token;
        }
    }

    public static boolean isValid(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        String sent = request.getParameter("csrfToken");
        Object expected = session == null ? null : session.getAttribute(KEY);
        return expected instanceof String && sent != null && MessageDigest.isEqual(((String) expected).getBytes(StandardCharsets.UTF_8), sent.getBytes(StandardCharsets.UTF_8));
    }
}
