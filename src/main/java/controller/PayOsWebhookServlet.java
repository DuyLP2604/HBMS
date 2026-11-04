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
    private final PayOsService payOsService = new PayOsService();
    private final PaymentService paymentService = new PaymentService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 1. Parse JSON from request body
        JsonObject root = parseRequestBody(req);
        if (root == null || !isValidSignature(root)) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST); // 400
            return;
        }
        // 2. Check signature
        resp.setStatus(HttpServletResponse.SC_OK); // 200
        // 3. Check payload for success payment
        if (!isSuccessfulPaymentEvent(root)) {
            return;
        }
        // 4. Write to DB
        processPaymentWebhook(root.getAsJsonObject("data"));
    }

    /**
     * Đọc và parse JSON từ Request Input Stream.
     */
    private JsonObject parseRequestBody(HttpServletRequest req) throws IOException {
        String body = new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
        try {
            return JsonParser.parseString(body).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            LOG.warning("PayOS webhook error: Invalid JSON format");
            return null;
        }
    }

    /**
     * Xác thực chữ ký HMAC từ PayOS.
     */
    private boolean isValidSignature(JsonObject root) {
        boolean valid = payOsService.verifyWebhook(root);
        if (!valid) {
            LOG.warning("PayOS webhook rejected: Bad signature");
        }
        return valid;
    }

    /**
     * Kiểm tra sự kiện thanh toán có thành công (code = "00", success = true).
     */
    private boolean isSuccessfulPaymentEvent(JsonObject root) {
        return root.has("success") && root.get("success").getAsBoolean()
                && root.has("code") && "00".equals(root.get("code").getAsString())
                && root.has("data") && !root.get("data").isJsonNull();
    }

    /**
     * Luồng xử lý dữ liệu thanh toán từ Webhook PayOS.
     */
    private void processPaymentWebhook(JsonObject data) {
        long orderCode = data.get("orderCode").getAsLong();
        PayOsOrderDAO dao = new PayOsOrderDAO();

        try {
            PayOsOrderDAO.Order order = dao.find(orderCode);
            if (order == null) {
                return; // Payload thử nghiệm (Test Webhook) khi cài đặt URL trên PayOS
            }

            BigDecimal paidAmount = data.get("amount").getAsBigDecimal();
            if (!isAmountMatching(dao, orderCode, paidAmount, order.externalAmount)) {
                return;
            }

            if (!dao.claim(orderCode)) {
                return; // Đơn hàng đã được thread/process khác xử lý trước đó
            }

            String reference = extractReference(data, orderCode);
            executePaymentTransaction(dao, order, reference);

        } catch (SQLException e) {
            LOG.log(Level.SEVERE, "Database error processing PayOS webhook", e);
        }
    }

    /**
     * Kiểm tra số tiền chuyển khớp với số tiền chờ thanh toán.
     */
    private boolean isAmountMatching(PayOsOrderDAO dao, long orderCode, BigDecimal paid, BigDecimal expected) throws SQLException {
        if (paid.compareTo(expected) != 0) {
            //TODO: nếu dư thì cho vào ví, nếu thiếu dưới 30% thì nuốt tiền, nếu trên thì coi như book 30%
            dao.markFailed(orderCode, "Amount mismatch: paid " + paid + ", expected " + expected);
            return false;
        }
        return true;
    }

    /**
     * Trích xuất mã giao dịch ngân hàng (reference).
     */
    private String extractReference(JsonObject data, long orderCode) {
        return data.has("reference") && !data.get("reference").isJsonNull()
                ? data.get("reference").getAsString()
                : String.valueOf(orderCode);
    }

    /**
     * Thực thi Stored Procedure ghi nhận thanh toán và cập nhật trạng thái đơn
     * PayOS.
     */
    private void executePaymentTransaction(PayOsOrderDAO dao, PayOsOrderDAO.Order order, String reference) throws SQLException {
        try {
            paymentService.confirmGatewayPayment(
                    order.bookingID, order.userID, order.paymentType,
                    order.amount, order.walletAmount, reference
            );
            dao.markPaid(order.orderCode, reference);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Paid on PayOS but failed to record in DB, orderCode=" + order.orderCode, ex);
            dao.markFailed(order.orderCode, ex.getMessage());
        }
    }
}
