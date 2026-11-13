package dto;

import java.math.BigDecimal;

/** A PayOS order and its payment status or front-desk QR details. */
public final class PayOsOrder {

    public long orderCode;
    public String bookingID;
    public String paymentType;
    public String status;
    public String reference;
    public String paymentID;
    public int userID;
    public BigDecimal amount;
    public BigDecimal walletAmount;
    public BigDecimal externalAmount;
    public long checkInExpiresAt;
    public String checkInQrCode;
    public String checkInCheckoutUrl;
}
