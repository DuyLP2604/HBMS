package controller;

import com.google.gson.*;
import dao.PayOsOrderDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.logging.*;
import service.PayOsService;
import service.PaymentService;

@WebServlet("/payos-webhook")
public class PayOsWebhookServlet extends HttpServlet {

    private static final Logger LOG = Logger.getLogger(PayOsWebhookServlet.class.getName());

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        JsonObject root;
        try {
            root = JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            resp.setStatus(400);
            return;
        }

        if (!new PayOsService().verifyWebhook(root)) {
            LOG.warning("PayOS webhook rejected: bad signature");
            resp.setStatus(400);
            return;
        }
        resp.setStatus(200); // after a valid signature always answer 200, otherwise PayOS keeps retrying

        JsonObject data = root.getAsJsonObject("data");
        boolean ok = root.has("success") && root.get("success").getAsBoolean()
                && root.has("code") && "00".equals(root.get("code").getAsString());

        if (!ok || !root.has("data") || root.get("data").isJsonNull()) {
            return;
        }

        PayOsOrderDAO dao = new PayOsOrderDAO();
        long orderCode = data.get("orderCode").getAsLong();
        try {
            PayOsOrderDAO.Order o = dao.find(orderCode);
            if (o == null) {
                return; // e.g. the sample payload sent when confirming the webhook URL
            }
            BigDecimal paid = data.get("amount").getAsBigDecimal();
            if (paid.compareTo(o.externalAmount) != 0) {
                dao.markFailed(orderCode, "Amount mismatch: paid " + paid + ", expected " + o.externalAmount);
                return;
            }
            if (!dao.claim(orderCode)) {
                return; // already processed
            }
            String reference = data.has("reference") && !data.get("reference").isJsonNull()
                    ? data.get("reference").getAsString()
                    : String.valueOf(orderCode);
            try {
                new PaymentService().confirmGatewayPayment(o.bookingID, o.userID, o.paymentType,
                        o.amount, o.walletAmount, reference);
                dao.markPaid(orderCode, reference);
            } catch (Exception ex) {
                // Money was received but not recorded -> needs manual review.
                LOG.log(Level.SEVERE, "Paid but not recorded, orderCode=" + orderCode, ex);
                dao.markFailed(orderCode, ex.getMessage());
            }
        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Webhook error", e);
        }
    }
}
