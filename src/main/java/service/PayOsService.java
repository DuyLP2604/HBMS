package service;

import com.google.gson.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PayOsService {

    private static final HttpClient HTTP = HttpClient.newHttpClient();
    private static final String API = "https://api-merchant.payos.vn/v2/payment-requests";

    public static final class Link {

        public String paymentLinkId, bin, accountNumber, accountName, checkoutUrl, qrCode, description;
    }

    public Link createPaymentLink(long orderCode, BigDecimal amount, String description,
            String returnUrl, String cancelUrl, long expiredAtEpochSeconds) throws Exception {

        long amt = amount.setScale(0, java.math.RoundingMode.UNNECESSARY).longValueExact();

        // 1. Đóng gói Request Body kèm chữ ký HMAC
        JsonObject body = buildRequestBody(orderCode, amt, description, returnUrl, cancelUrl, expiredAtEpochSeconds);

        // 2. Gửi HTTP Request tới cổng PayOS
        HttpRequest req = HttpRequest.newBuilder(URI.create(API))
                .header("Content-Type", "application/json")
                .header("x-client-id", cfg("PAYOS_CLIENT_ID"))
                .header("x-api-key", cfg("PAYOS_API_KEY"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

        // 3. Trích xuất và parse kết quả trả về
        return parsePaymentLinkResponse(res.body());
    }

    /**
     * Overload khởi tạo link PayOS không truyền thời hạn custom (mặc định = 0).
     *
     * @param orderCode
     * @param amount
     * @param description
     * @param returnUrl
     * @param cancelUrl
     * @return
     * @throws java.lang.Exception
     */
    public Link createPaymentLink(long orderCode, BigDecimal amount, String description,
            String returnUrl, String cancelUrl) throws Exception {
        return createPaymentLink(orderCode, amount, description, returnUrl, cancelUrl, 0);
    }

    /**
     * Tạo URL ảnh VietQR tĩnh từ thông tin Link PayOS trả về.
     *
     * @param link
     * @param amount
     * @return
     */
    public String qrImageUrl(Link link, BigDecimal amount) {
        String cleanAmount = amount.stripTrailingZeros().toPlainString();
        String encodedDesc = java.net.URLEncoder.encode(link.description, StandardCharsets.UTF_8);
        String encodedName = java.net.URLEncoder.encode(link.accountName, StandardCharsets.UTF_8);

        return String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                link.bin, link.accountNumber, cleanAmount, encodedDesc, encodedName);
    }

    /**
     * Xác thực chữ ký Webhook HMAC-SHA256 từ PayOS gửi về.
     *
     * @param root
     * @return
     */
    public boolean verifyWebhook(JsonObject root) {
        if (!isValidWebhookPayload(root)) {
            return false;
        }

        JsonObject data = root.getAsJsonObject("data");
        String sortedDataString = buildSortedDataString(data);
        String expectedSignature = hmac(cfg("PAYOS_CHECKSUM_KEY"), sortedDataString);
        String receivedSignature = root.get("signature").getAsString();

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Tạo JsonObject request body và tính chữ ký HMAC theo thứ tự tham số PayOS
     * quy định.
     */
    private JsonObject buildRequestBody(long orderCode, long amt, String description,
            String returnUrl, String cancelUrl, long expiredAtEpochSeconds) {

        String toSign = "amount=" + amt
                + "&cancelUrl=" + cancelUrl
                + "&description=" + description
                + "&orderCode=" + orderCode
                + "&returnUrl=" + returnUrl;

        JsonObject body = new JsonObject();
        body.addProperty("orderCode", orderCode);
        body.addProperty("amount", amt);
        body.addProperty("description", description);
        body.addProperty("returnUrl", returnUrl);
        body.addProperty("cancelUrl", cancelUrl);
        if (expiredAtEpochSeconds > 0) {
            body.addProperty("expiredAt", expiredAtEpochSeconds);
        }
        body.addProperty("signature", hmac(cfg("PAYOS_CHECKSUM_KEY"), toSign));

        return body;
    }

    /**
     * Parse kết quả trả về từ API PayOS thành đối tượng Link.
     */
    private Link parsePaymentLinkResponse(String responseBody) {
        JsonObject json = JsonParser.parseString(responseBody).getAsJsonObject();

        if (!json.has("code") || !"00".equals(json.get("code").getAsString()) || !json.has("data") || json.get("data").isJsonNull()) {
            String desc = json.has("desc") ? json.get("desc").getAsString() : "Unknown error";
            throw new IllegalStateException("PayOS error: " + desc);
        }

        JsonObject data = json.getAsJsonObject("data");
        Link link = new Link();
        link.paymentLinkId = data.get("paymentLinkId").getAsString();
        link.bin = data.get("bin").getAsString();
        link.accountNumber = data.get("accountNumber").getAsString();
        link.accountName = data.get("accountName").getAsString();
        link.checkoutUrl = data.get("checkoutUrl").getAsString();
        link.qrCode = data.get("qrCode").getAsString();
        link.description = data.get("description").getAsString();

        return link;
    }

    /**
     * Kiểm tra cấu trúc Webhook payload từ PayOS có đầy đủ thuộc tính bắt buộc
     * không.
     */
    private boolean isValidWebhookPayload(JsonObject root) {
        return root != null
                && root.has("data")
                && root.get("data").isJsonObject()
                && root.has("signature")
                && !root.get("signature").isJsonNull();
    }

    /**
     * Sắp xếp các key trong JsonObject `data` theo thứ tự Alphabet và ghép
     * thành chuỗi `k1=v1&k2=v2...`
     */
    private String buildSortedDataString(JsonObject data) {
        TreeMap<String, String> sortedMap = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            JsonElement val = entry.getValue();
            sortedMap.put(entry.getKey(), val.isJsonNull() ? "" : val.getAsString());
        }

        StringJoiner sj = new StringJoiner("&");
        sortedMap.forEach((key, value) -> sj.add(key + "=" + value));
        return sj.toString();
    }

    /**
     * Tính toán mã HMAC-SHA256 dạng chuỗi Hex lower-case.
     */
    public static String hmac(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            StringBuilder sb = new StringBuilder();
            for (byte b : mac.doFinal(data.getBytes(StandardCharsets.UTF_8))) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (IllegalStateException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw new IllegalStateException("Failed to calculate HMAC-SHA256", e);
        }
    }

    /**
     * Đọc giá trị cấu hình từ System Property hoặc Environment Variable.
     */
    private static String cfg(String key) {
        String val = System.getProperty(key);
        if (val == null || val.isBlank()) {
            val = System.getenv(key);
        }
        if (val == null || val.isBlank()) {
            throw new IllegalStateException("Missing config: " + key);
        }
        return val.trim();
    }
}
