package dto;

/** Payment link and transfer details returned by PayOS. */
public final class PayOsLink {

    public String paymentLinkId;
    public String bin;
    public String accountNumber;
    public String accountName;
    public String checkoutUrl;
    public String qrCode;
    public String description;
}
