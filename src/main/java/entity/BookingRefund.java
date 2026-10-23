/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "BOOKING_REFUND")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BookingRefund.findAll", query = "SELECT b FROM BookingRefund b"),
    @NamedQuery(name = "BookingRefund.findByRefundID", query = "SELECT b FROM BookingRefund b WHERE b.refundID = :refundID"),
    @NamedQuery(name = "BookingRefund.findByAmount", query = "SELECT b FROM BookingRefund b WHERE b.amount = :amount"),
    @NamedQuery(name = "BookingRefund.findByRefundedAt", query = "SELECT b FROM BookingRefund b WHERE b.refundedAt = :refundedAt")})
public class BookingRefund implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "RefundID")
    private Long refundID;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "Amount")
    private BigDecimal amount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "RefundedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date refundedAt;
    @ManyToMany(mappedBy = "bookingRefundCollection")
    private Collection<Payment> paymentCollection;
    @JoinColumns({
        @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID"),
        @JoinColumn(name = "BookingID", referencedColumnName = "BookingID", insertable = false, updatable = false)
    })
    @ManyToOne(optional = false)
    private Booking booking;
    @JoinColumns({
        @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID", insertable = false, updatable = false),
        @JoinColumn(name = "WalletID", referencedColumnName = "WalletID")
    })
    @ManyToOne(optional = false)
    private CustomerWallet customerWallet;
    @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID")
    @OneToOne(optional = false)
    private Transaction transactionID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bookingRefund")
    private Collection<WalletTransaction> walletTransactionCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bookingRefund1")
    private Collection<WalletTransaction> walletTransactionCollection1;

    public BookingRefund() {
    }

    public BookingRefund(Long refundID) {
        this.refundID = refundID;
    }

    public BookingRefund(Long refundID, BigDecimal amount, Date refundedAt) {
        this.refundID = refundID;
        this.amount = amount;
        this.refundedAt = refundedAt;
    }

    public Long getRefundID() {
        return refundID;
    }

    public void setRefundID(Long refundID) {
        this.refundID = refundID;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getRefundedAt() {
        return refundedAt;
    }

    public void setRefundedAt(Date refundedAt) {
        this.refundedAt = refundedAt;
    }

    @XmlTransient
    public Collection<Payment> getPaymentCollection() {
        return paymentCollection;
    }

    public void setPaymentCollection(Collection<Payment> paymentCollection) {
        this.paymentCollection = paymentCollection;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public CustomerWallet getCustomerWallet() {
        return customerWallet;
    }

    public void setCustomerWallet(CustomerWallet customerWallet) {
        this.customerWallet = customerWallet;
    }

    public Transaction getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Transaction transactionID) {
        this.transactionID = transactionID;
    }

    @XmlTransient
    public Collection<WalletTransaction> getWalletTransactionCollection() {
        return walletTransactionCollection;
    }

    public void setWalletTransactionCollection(Collection<WalletTransaction> walletTransactionCollection) {
        this.walletTransactionCollection = walletTransactionCollection;
    }

    @XmlTransient
    public Collection<WalletTransaction> getWalletTransactionCollection1() {
        return walletTransactionCollection1;
    }

    public void setWalletTransactionCollection1(Collection<WalletTransaction> walletTransactionCollection1) {
        this.walletTransactionCollection1 = walletTransactionCollection1;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (refundID != null ? refundID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookingRefund)) {
            return false;
        }
        BookingRefund other = (BookingRefund) object;
        if ((this.refundID == null && other.refundID != null) || (this.refundID != null && !this.refundID.equals(other.refundID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.BookingRefund[ refundID=" + refundID + " ]";
    }

}
