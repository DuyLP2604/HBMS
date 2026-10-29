package service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Builds the existing VietQR image URL. It does not confirm bank transfers. */
public class VietQrService {
    public String createUrl(BigDecimal amount, String reference) {
        if (amount == null || amount.signum() <= 0 || reference == null || reference.isBlank()) {
            throw new IllegalArgumentException("A positive amount and transfer reference are required.");
        }
        String bank = setting("HBMS_VIETQR_BANK_ID", "BIDV");
        String account = setting("HBMS_VIETQR_ACCOUNT_NO", "8803748420");
        String name = setting("HBMS_VIETQR_ACCOUNT_NAME", "SIMPLE BEAR HOTEL");
        return "https://img.vietqr.io/image/" + encode(bank) + "-" + encode(account)
                + "-compact2.png?amount=" + amount.stripTrailingZeros().toPlainString()
                + "&addInfo=" + encode(reference) + "&accountName=" + encode(name);
    }

    private String setting(String key, String fallback) {
        String value = System.getProperty(key);
        if (value == null || value.isBlank()) value = System.getenv(key);
        return value == null || value.isBlank() ? fallback : value.trim();
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
