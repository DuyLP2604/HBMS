package dto;

import java.math.BigDecimal;
import java.util.Date;

public final class WalletTransactionItem
{
    private final long walletTransactionID;
    private final String transactionType;
    private final BigDecimal amount;
    private final Date transactionTime;
    private final String bookingID;
    private final String paymentID;
    public WalletTransactionItem(long walletTransactionID, String transactionType, BigDecimal amount, Date transactionTime, String bookingID, String paymentID)
    {
        this.walletTransactionID = walletTransactionID;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionTime = transactionTime == null ? null : new Date(transactionTime.getTime());
        this.bookingID = bookingID;
        this.paymentID = paymentID;
    }

    public long getWalletTransactionID()
    {
        return walletTransactionID;
    }

    public String getTransactionType()
    {
        return transactionType;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public Date getTransactionTime()
    {
        return transactionTime == null ? null : new Date(transactionTime.getTime());
    }

    public String getBookingID()
    {
        return bookingID;
    }

    public String getPaymentID()
    {
        return paymentID;
    }

    public boolean isCredit()
    {
        return "REFUND_CREDIT".equals(transactionType);
    }

}
