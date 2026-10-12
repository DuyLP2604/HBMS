package dto;

import java.math.BigDecimal;
import java.util.Date;

public final class WalletSummary
{
    private final long walletID;
    private final String customerID;
    private final BigDecimal balance;
    private final Date createdAt;
    public WalletSummary(long walletID, String customerID, BigDecimal balance, Date createdAt)
    {
        this.walletID = walletID;
        this.customerID = customerID;
        this.balance = balance;
        this.createdAt = createdAt == null ? null : new Date(createdAt.getTime());
    }

    public long getWalletID()
    {
        return walletID;
    }

    public String getCustomerID()
    {
        return customerID;
    }

    public BigDecimal getBalance()
    {
        return balance;
    }

    public Date getCreatedAt()
    {
        return createdAt == null ? null : new Date(createdAt.getTime());
    }

}
