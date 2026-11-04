package controller;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import dao.PayOsOrderDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import service.PayOsService;
import util.PersistenceManager;

@WebServlet("/payos-webhook")
public class PayOsWebhookServlet extends HttpServlet {
    private static final int MAX_BODY_BYTES = 32 * 1024;
    private final PayOsService payOsService = new PayOsService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        byte[] body = request.getInputStream().readNBytes(MAX_BODY_BYTES + 1);
        if (body.length > MAX_BODY_BYTES) {
            response.setStatus(HttpServletResponse.SC_REQUEST_ENTITY_TOO_LARGE);
            return;
        }

        JsonObject root;
        try {
            root = JsonParser.parseString(new String(body, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (JsonParseException | IllegalStateException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            if (!payOsService.verifyWebhook(root)) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (IllegalStateException exception) {
            log("PayOS webhook verification is unavailable.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        // PayOS signs the data object.
        JsonObject data = root.getAsJsonObject("data");
        if (!"00".equals(value(data, "code"))) {
            response.setStatus(HttpServletResponse.SC_NO_CONTENT);
            return;
        }

        try {
            long orderCode = new BigDecimal(value(data, "orderCode")).longValueExact();
            BigDecimal amount = new BigDecimal(value(data, "amount"));
            String linkID = value(data, "paymentLinkId");
            String reference = value(data, "reference");
            if (orderCode <= 0 || amount.signum() <= 0 || amount.stripTrailingZeros().scale() > 0
                    || amount.compareTo(new BigDecimal("9999999999")) > 0 || !"VND".equals(value(data, "currency"))
                    || !linkID.matches("[A-Za-z0-9_-]{1,100}") || reference.isEmpty() || reference.length() > 100) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                return;
            }
            new PayOsOrderDAO().confirm(orderCode, linkID, amount, reference);
        } catch (ArithmeticException | IllegalArgumentException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } catch (SQLException | IllegalStateException exception) {
            // Let PayOS retry if the database write fails.
            log("Unable to record the PayOS webhook.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return;
        }

        try {
            PersistenceManager.getEMF().getCache().evictAll();
        } catch (RuntimeException exception) {
            log("Unable to refresh the booking cache after PayOS confirmation.", exception);
        }
        response.setStatus(HttpServletResponse.SC_OK);
    }

    private String value(JsonObject object, String key) {
        JsonElement value = object.get(key);
        return value != null && value.isJsonPrimitive() ? value.getAsString().trim() : "";
    }
}
