package service;

import com.google.gson.*;
import java.math.BigDecimal;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
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
            String returnUrl, String cancelUrl) throws Exception {
        long amt = amount.setScale(0, java.math.RoundingMode.UNNECESSARY).longValueExact();
        String toSign = "amount=" + amt + "&cancelUrl=" + cancelUrl + "&description=" + description
                + "&orderCode=" + orderCode + "&returnUrl=" + returnUrl;
        JsonObject body = new JsonObject();
        body.addProperty("orderCode", orderCode);
        body.addProperty("amount", amt);
        body.addProperty("description", description);          // max 25 chars
        body.addProperty("returnUrl", returnUrl);
        body.addProperty("cancelUrl", cancelUrl);
        body.addProperty("expiredAt", System.currentTimeMillis() / 1000 + 15 * 60);
        body.addProperty("signature", hmac(cfg("PAYOS_CHECKSUM_KEY"), toSign));
        // NOTE: if PayOS rejects expiredAt in the signature check, remove it from the body.

        HttpRequest req = HttpRequest.newBuilder(URI.create(API))
                .header("Content-Type", "application/json")
                .header("x-client-id", cfg("PAYOS_CLIENT_ID"))
                .header("x-api-key", cfg("PAYOS_API_KEY"))
                .POST(HttpRequest.BodyPublishers.ofString(body.toString())).build();
        HttpResponse<String> res = HTTP.send(req, HttpResponse.BodyHandlers.ofString());
        JsonObject json = JsonParser.parseString(res.body()).getAsJsonObject();
        if (!"00".equals(json.get("code").getAsString()) || json.get("data").isJsonNull()) {
            throw new IllegalStateException("PayOS error: " + json.get("desc").getAsString());
        }
        JsonObject d = json.getAsJsonObject("data");

        Link l = new Link();
        l.paymentLinkId = d.get("paymentLinkId").getAsString();
        l.bin = d.get("bin").getAsString();
        l.accountNumber = d.get("accountNumber").getAsString();
        l.accountName = d.get("accountName").getAsString();
        l.checkoutUrl = d.get("checkoutUrl").getAsString();
        l.qrCode = d.get("qrCode").getAsString();
        l.description = d.get("description").getAsString();

        return l;
    }

    /**
     * Image URL for the QR generated from the PayOS link (same image host you
     * already use).
     *
     * @param l
     * @param amount
     * @return
     */
    public String qrImageUrl(Link l, BigDecimal amount) {
        return "https://img.vietqr.io/image/" + l.bin + "-" + l.accountNumber + "-compact2.png?amount="
                + amount.stripTrailingZeros().toPlainString()
                + "&addInfo=" + java.net.URLEncoder.encode(l.description, StandardCharsets.UTF_8)
                + "&accountName=" + java.net.URLEncoder.encode(l.accountName, StandardCharsets.UTF_8);
    }

    /**
     * Verify the webhook signature: sort data keys A-Z, join key=value with &,
     * HMAC-SHA256.
     *
     * @param root
     * @return
     */
    public boolean verifyWebhook(JsonObject root) {
        if (!root.has("data") || !root.has("signature") || !root.get("data").isJsonObject()) {
            return false;
        }
        JsonObject data = root.getAsJsonObject("data");
        TreeMap<String, String> sorted = new TreeMap<>();
        for (Map.Entry<String, JsonElement> e : data.entrySet()) {
            JsonElement v = e.getValue();
            sorted.put(e.getKey(), v.isJsonNull() ? "" : v.getAsString());
        }
        StringJoiner sj = new StringJoiner("&");
        sorted.forEach((k, v) -> sj.add(k + "=" + v));
        String expected = hmac(cfg("PAYOS_CHECKSUM_KEY"), sj.toString());
        return java.security.MessageDigest.isEqual(
                expected.getBytes(StandardCharsets.UTF_8),
                root.get("signature").getAsString().getBytes(StandardCharsets.UTF_8));
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
            throw new IllegalStateException(e);
        }
    }

    private static String cfg(String k) {
        String v = System.getProperty(k);
        if (v == null || v.isBlank()) {
            v = System.getenv(k);
        }
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing config: " + k);
        }
        return v.trim();
    }
}
