package entity;
import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
@Entity
@Table(name = "PAYMENT")
@XmlRootElement
@NamedQueries({@NamedQuery(name = "Payment.findAll", query = "SELECT p FROM Payment p"), @NamedQuery(name = "Payment.findByPaymentID", query = "SELECT p FROM Payment p WHERE p.paymentID = :paymentID"), @NamedQuery(name = "Payment.findByPaymentTime", query = "SELECT p FROM Payment p WHERE p.paymentTime = :paymentTime"), @NamedQuery(name = "Payment.findByAmount", query = "SELECT p FROM Payment p WHERE p.amount = :amount"), @NamedQuery(name = "Payment.findByStatus", query = "SELECT p FROM Payment p WHERE p.status = :status"), @NamedQuery(name = "Payment.findByTransactionCode", query = "SELECT p FROM Payment p WHERE p.transactionCode = :transactionCode"), @NamedQuery(name = "Payment.findByPaymentType", query = "SELECT p FROM Payment p WHERE p.paymentType = :paymentType"), @NamedQuery(name = "Payment.findByBookingID", query = "SELECT p FROM Payment p WHERE p.bookingID.bookingID = :bookingID")})
public class Payment implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id @Basic(optional = false) @NotNull @Size(min = 1, max = 6) @Column(name = "PaymentID")
    private String paymentID;
    @Basic(optional = false) @NotNull @Size(min = 1, max = 10) @Pattern(regexp = "DEPOSIT|FULL|BALANCE") @Column(name = "PaymentType", updatable = false)
    private String paymentType = "FULL";
    @Column(name = "PaymentTime") @Temporal(TemporalType.TIMESTAMP)
    private Date paymentTime;
    @Basic(optional = false) @NotNull @DecimalMin(value = "0", inclusive = false) @Digits(integer = 10, fraction = 2) @Column(name = "Amount", precision = 12, scale = 2)
    private BigDecimal amount;
    @Basic(optional = false) @NotNull @Size(min = 1, max = 30) @Pattern(regexp = "PENDING|PAID|FAILED|REFUNDED") @Column(name = "Status")
    private String status;
    @Size(max = 100) @Column(name = "TransactionCode")
    private String transactionCode;
    @JoinColumn(name = "BookingID", referencedColumnName = "BookingID") @ManyToOne(optional = false)
    private Booking bookingID;
    @JoinColumn(name = "MethodID", referencedColumnName = "MethodID") @ManyToOne
    private Paymentmethod methodID;
    public Payment()
    {
    }

    public Payment(String paymentID)
    {
        this.paymentID = paymentID;
    }

    public Payment(String paymentID, BigDecimal amount, String status)
    {
        this.paymentID = paymentID;
        this.amount = amount;
        this.status = status;
    }

    public Payment(String paymentID, BigDecimal amount, String status, String paymentType)
    {
        this(paymentID, amount, status);
        this.paymentType = paymentType;
    }

    public String getPaymentID()
    {
        return paymentID;
    }

    public void setPaymentID(String paymentID)
    {
        this.paymentID = paymentID;
    }

    public String getPaymentType()
    {
        return paymentType;
    }

    public void setPaymentType(String paymentType)
    {
        this.paymentType = paymentType;
    }

    public Date getPaymentTime()
    {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime)
    {
        this.paymentTime = paymentTime;
    }

    public BigDecimal getAmount()
    {
        return amount;
    }

    public void setAmount(BigDecimal amount)
    {
        this.amount = amount;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public String getTransactionCode()
    {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode)
    {
        this.transactionCode = transactionCode;
    }

    public Booking getBookingID()
    {
        return bookingID;
    }

    public void setBookingID(Booking bookingID)
    {
        this.bookingID = bookingID;
    }

    public Paymentmethod getMethodID()
    {
        return methodID;
    }

    public void setMethodID(Paymentmethod methodID)
    {
        this.methodID = methodID;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (paymentID != null ? paymentID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof Payment))
        {
            return false;
        }
        Payment other = (Payment) object;
        if ((this.paymentID == null && other.paymentID != null) || (this.paymentID != null && !this.paymentID.equals(other.paymentID)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "entity.Payment[ paymentID=" + paymentID + " ]";
    }
}