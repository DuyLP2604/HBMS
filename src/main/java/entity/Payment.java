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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "PAYMENT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Payment.findAll",
        query = "SELECT p FROM Payment p"
    ),
    @NamedQuery(
        name = "Payment.findByPaymentID",
        query = "SELECT p FROM Payment p "
              + "WHERE p.paymentID = :paymentID"
    ),
    @NamedQuery(
        name = "Payment.findByBooking",
        query = "SELECT p FROM Payment p "
              + "WHERE p.bookingID = :booking"
    ),
    @NamedQuery(
        name = "Payment.findByStatus",
        query = "SELECT p FROM Payment p "
              + "WHERE p.status = :status"
    )
})
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(
        name = "PaymentID",
        nullable = false,
        length = 6
    )
    private String paymentID;

    @Basic(optional = false)
    @NotNull
    @Column(
        name = "PaymentTime",
        nullable = false
    )
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentTime;

    @Basic(optional = false)
    @NotNull
    @Column(
        name = "Amount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal amount;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(
        name = "Status",
        nullable = false,
        length = 30
    )
    private String status;

    @Size(max = 100)
    @Column(
        name = "TransactionCode",
        length = 100
    )
    private String transactionCode;

    @JoinColumn(
        name = "BookingID",
        referencedColumnName = "BookingID",
        nullable = false
    )
    @ManyToOne(optional = false)
    private Booking bookingID;

    @JoinColumn(
        name = "MethodID",
        referencedColumnName = "MethodID",
        nullable = false
    )
    @ManyToOne(optional = false)
    private Paymentmethod methodID;

    public Payment() {
    }

    public Payment(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public Date getPaymentTime() {
        return paymentTime;
    }

    public void setPaymentTime(Date paymentTime) {
        this.paymentTime = paymentTime;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(
            String transactionCode) {

        this.transactionCode = transactionCode;
    }

    public Booking getBookingID() {
        return bookingID;
    }

    public void setBookingID(Booking bookingID) {
        this.bookingID = bookingID;
    }

    public Paymentmethod getMethodID() {
        return methodID;
    }

    public void setMethodID(
            Paymentmethod methodID) {

        this.methodID = methodID;
    }

    @Override
    public int hashCode() {
        return paymentID != null
                ? paymentID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Payment)) {
            return false;
        }

        Payment other = (Payment) object;

        if (paymentID == null) {
            return other.paymentID == null;
        }

        return paymentID.equals(other.paymentID);
    }

    @Override
    public String toString() {
        return "entity.Payment[paymentID="
                + paymentID + "]";
    }
}