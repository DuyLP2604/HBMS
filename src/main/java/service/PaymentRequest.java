package service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Booking payment amounts and the identifiers used to retry the same payment. */
public final class PaymentRequest implements Serializable {

    private static final long serialVersionUID = 2L;
    private final String bookingID;
    private final int userID;
    private final String paymentType;
    private final BigDecimal amount;
    private final BigDecimal walletBalance;

    // PaymentService updates this state while holding the request lock.
    String transactionCode = "PAYMENT-" + UUID.randomUUID();
    int actorUserID;
    Integer walletAuthorizedBy;
    String paymentID;
    String transactionID;
    private final String transferReference = "HBMS" + UUID.randomUUID().toString().replace("-", "").substring(0, 20);
    String methodID;
    BigDecimal walletAmount;

    PaymentRequest(String bookingID, int userID, String paymentType, BigDecimal amount, BigDecimal walletBalance) {
        this.bookingID = bookingID;
        this.userID = userID;
        this.paymentType = paymentType;
        this.amount = amount;
        this.walletBalance = walletBalance;
    }

    public String getTransferReference() {
        return transferReference;
    }

    public String getBookingID() {
        return bookingID;
    }

    public int getUserID() {
        return userID;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public BigDecimal getWalletBalance() {
        return walletBalance;
    }

    public BigDecimal getOfferedWalletAmount() {
        // Limit the wallet contribution to the amount due.
        return walletBalance == null ? BigDecimal.ZERO : walletBalance.max(BigDecimal.ZERO).min(amount);
    }
}
