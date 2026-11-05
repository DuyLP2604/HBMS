/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

/**
 * Maps a booking payment and its linked transaction and payment method.
 *
 * @author Asus
 */
@Entity
@Table(name = "PAYMENT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Payment.findAll", query = "SELECT p FROM Payment p"),
    @NamedQuery(name = "Payment.findByPaymentID", query = "SELECT p FROM Payment p WHERE p.paymentID = :paymentID"),
    @NamedQuery(name = "Payment.findByPaymentType", query = "SELECT p FROM Payment p WHERE p.paymentType = :paymentType"),
    @NamedQuery(name = "Payment.findByPaymentTime", query = "SELECT p FROM Payment p WHERE p.paymentTime = :paymentTime"),
    @NamedQuery(name = "Payment.findByAmount", query = "SELECT p FROM Payment p WHERE p.amount = :amount"),
    @NamedQuery(name = "Payment.findByStatus", query = "SELECT p FROM Payment p WHERE p.status = :status"),
    @NamedQuery(name = "Payment.findByWalletAmount", query = "SELECT p FROM Payment p WHERE p.walletAmount = :walletAmount"),
    @NamedQuery(name = "Payment.findByExternalAmount", query = "SELECT p FROM Payment p WHERE p.externalAmount = :externalAmount")})
public class Payment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "PaymentID")
    private String paymentID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Column(name = "PaymentType")
    private String paymentType;
    @Column(name = "PaymentTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentTime;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "Amount")
    private BigDecimal amount;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "Status")
    private String status;
    @Basic(optional = false)
    @NotNull
    @Column(name = "WalletAmount")
    private BigDecimal walletAmount;
    @Column(name = "ExternalAmount")
    private BigDecimal externalAmount;
    @JoinColumn(name = "BookingID", referencedColumnName = "BookingID")
    @ManyToOne(optional = false)
    private Booking bookingID;
    @JoinColumn(name = "MethodID", referencedColumnName = "MethodID")
    @ManyToOne
    private Paymentmethod methodID;
    @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID")
    @OneToOne
    private Transaction transactionID;
    @JoinColumn(name = "CollectedByUserID", referencedColumnName = "UserID")
    @ManyToOne
    private Users collectedByUserID;
    @JoinColumn(name = "WalletAuthorizedByUserID", referencedColumnName = "UserID")
    @ManyToOne
    private Users walletAuthorizedByUserID;

    public Payment() {
    }

    public Payment(String paymentID) {
        this.paymentID = paymentID;
    }

    public Payment(String paymentID, String paymentType, BigDecimal amount, String status, BigDecimal walletAmount) {
        this.paymentID = paymentID;
        this.paymentType = paymentType;
        this.amount = amount;
        this.status = status;
        this.walletAmount = walletAmount;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
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

    public BigDecimal getWalletAmount() {
        return walletAmount;
    }

    public void setWalletAmount(BigDecimal walletAmount) {
        this.walletAmount = walletAmount;
    }

    public BigDecimal getExternalAmount() {
        return externalAmount;
    }

    public void setExternalAmount(BigDecimal externalAmount) {
        this.externalAmount = externalAmount;
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

    public void setMethodID(Paymentmethod methodID) {
        this.methodID = methodID;
    }

    public Transaction getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Transaction transactionID) {
        this.transactionID = transactionID;
    }

    public Users getCollectedByUserID() {
        return collectedByUserID;
    }

    public void setCollectedByUserID(Users collectedByUserID) {
        this.collectedByUserID = collectedByUserID;
    }

    public Users getWalletAuthorizedByUserID() {
        return walletAuthorizedByUserID;
    }

    public void setWalletAuthorizedByUserID(Users walletAuthorizedByUserID) {
        this.walletAuthorizedByUserID = walletAuthorizedByUserID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (paymentID != null ? paymentID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Payment)) {
            return false;
        }
        Payment other = (Payment) object;
        if ((this.paymentID == null && other.paymentID != null) || (this.paymentID != null && !this.paymentID.equals(other.paymentID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Payment[ paymentID=" + paymentID + " ]";
    }

}
