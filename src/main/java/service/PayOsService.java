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
import java.util.concurrent.ThreadLocalRandom;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class PayOsService {

    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(java.time.Duration.ofSeconds(5)).build();
    private static final String API = "https://api-merchant.payos.vn/v2/payment-requests";

    public static long newOrderCode() {
        return ThreadLocalRandom.current().nextLong(1_000_000_000_000L, 10_000_000_000_000L);
    }

    public static String publicBaseUrl(String localBaseUrl) {
        String configured = System.getProperty("PAYOS_PUBLIC_BASE_URL");
        if (configured == null || configured.isBlank()) {
            configured = System.getenv("PAYOS_PUBLIC_BASE_URL");
        }
        if (configured == null || configured.isBlank()) {
            return localBaseUrl;
        }
        URI base = URI.create(configured.trim());
        if (!"https".equalsIgnoreCase(base.getScheme()) || base.getHost() == null
                || base.getRawUserInfo() != null || base.getRawQuery() != null || base.getRawFragment() != null) {
            throw new IllegalStateException("PAYOS_PUBLIC_BASE_URL must be an HTTPS application URL.");
        }
        return base.toString().replaceFirst("/+$", "");
    }

    public static final class Link {

        public String paymentLinkId, bin, accountNumber, accountName, checkoutUrl, qrCode, description;
    }

    public Link createPaymentLink(long orderCode, BigDecimal amount, String description,
            String returnUrl, String cancelUrl, long expiredAtEpochSeconds) throws Exception {

        long amt = amount.setScale(0, java.math.RoundingMode.UNNECESSARY).longValueExact();

        JsonObject body = buildRequestBody(orderCode, amt, description, returnUrl, cancelUrl, expiredAtEpochSeconds);

        HttpRequest req = HttpRequest.newBuilder(URI.create(API))
                .timeout(java.time.Duration.ofSeconds(12))
                .header("Content-Type", "application/json")
                .header("x-client-id", cfg("PAYOS_CLIENT_ID"))
                .header("x-api-key", cfg("PAYOS_API_KEY"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString()))
                .build();

        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());

        if (res.statusCode() < 200 || res.statusCode() >= 300) {
            throw new IllegalStateException("PayOS HTTP error " + res.statusCode());
        }
        return parsePaymentLinkResponse(res.body());
    }

    public Link createPaymentLink(long orderCode, BigDecimal amount, String description,
            String returnUrl, String cancelUrl) throws Exception {
        return createPaymentLink(orderCode, amount, description, returnUrl, cancelUrl, 0);
    }

    public String qrImageUrl(Link link, BigDecimal amount) {
        String cleanAmount = amount.stripTrailingZeros().toPlainString();
        String encodedDesc = java.net.URLEncoder.encode(link.description, StandardCharsets.UTF_8);
        String encodedName = java.net.URLEncoder.encode(link.accountName, StandardCharsets.UTF_8);

        return String.format("https://img.vietqr.io/image/%s-%s-compact2.png?amount=%s&addInfo=%s&accountName=%s",
                link.bin, link.accountNumber, cleanAmount, encodedDesc, encodedName);
    }

    public boolean verifyWebhook(JsonObject root) {
        if (!isValidWebhookPayload(root)) {
            return false;
        }

        JsonObject data = root.getAsJsonObject("data");
        if (data.entrySet().stream().anyMatch(field -> field.getValue().isJsonObject()
                || field.getValue().isJsonArray())) {
            return false;
        }
        String receivedSignature = root.get("signature").getAsString();
        if (!receivedSignature.matches("[0-9a-fA-F]{64}")) {
            return false;
        }
        String expectedSignature = hmac(cfg("PAYOS_CHECKSUM_KEY"), buildSortedDataString(data));

        return MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.UTF_8),
                receivedSignature.toLowerCase(Locale.ROOT).getBytes(StandardCharsets.UTF_8)
        );
    }

    private JsonObject buildRequestBody(long orderCode, long amt, String description,
            String returnUrl, String cancelUrl, long expiredAtEpochSeconds) {

        // PayOS signs these fields in this order.
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

    private boolean isValidWebhookPayload(JsonObject root) {
        return root != null
                && root.has("data")
                && root.get("data").isJsonObject()
                && root.has("signature")
                && root.get("signature").isJsonPrimitive();
    }

    private String buildSortedDataString(JsonObject data) {
        TreeMap<String, String> sortedMap = new TreeMap<>();
        for (Map.Entry<String, JsonElement> entry : data.entrySet()) {
            JsonElement val = entry.getValue();
            String value = val.isJsonNull() ? "" : val.getAsString();
            sortedMap.put(entry.getKey(), "null".equals(value) || "undefined".equals(value) ? "" : value);
        }

        StringJoiner sj = new StringJoiner("&");
        sortedMap.forEach((key, value) -> sj.add(key + "=" + value));
        return sj.toString();
    }

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
