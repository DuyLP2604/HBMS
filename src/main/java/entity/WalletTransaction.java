/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
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
 *
 * @author Asus
 */
@Entity
@Table(name = "WALLET_TRANSACTION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "WalletTransaction.findAll", query = "SELECT w FROM WalletTransaction w"),
    @NamedQuery(name = "WalletTransaction.findByWalletTransactionID", query = "SELECT w FROM WalletTransaction w WHERE w.walletTransactionID = :walletTransactionID"),
    @NamedQuery(name = "WalletTransaction.findByTransactionType", query = "SELECT w FROM WalletTransaction w WHERE w.transactionType = :transactionType"),
    @NamedQuery(name = "WalletTransaction.findByAmount", query = "SELECT w FROM WalletTransaction w WHERE w.amount = :amount"),
    @NamedQuery(name = "WalletTransaction.findByTransactionTime", query = "SELECT w FROM WalletTransaction w WHERE w.transactionTime = :transactionTime")})
public class WalletTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "WalletTransactionID")
    private Long walletTransactionID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "TransactionType")
    private String transactionType;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "Amount")
    private BigDecimal amount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "TransactionTime")
    @Temporal(TemporalType.TIMESTAMP)
    private Date transactionTime;
    @JoinColumns({
        @JoinColumn(name = "WalletID", referencedColumnName = "WalletID"),
        @JoinColumn(name = "RefundID", referencedColumnName = "RefundID")})
    @ManyToOne(optional = false)
    private BookingRefund bookingRefund;
    @JoinColumns({
        @JoinColumn(name = "WalletID", referencedColumnName = "WalletID", insertable = false, updatable = false),
        @JoinColumn(name = "RefundID", referencedColumnName = "RefundID", insertable = false, updatable = false),
        @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID", insertable = false, updatable = false)
    })
    @ManyToOne(optional = false)
    private BookingRefund bookingRefund1;

    @JoinColumn(name = "WalletID", referencedColumnName = "WalletID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private CustomerWallet walletID;

    @JoinColumn(name = "PaymentID", referencedColumnName = "PaymentID", insertable = false, updatable = false)
    @OneToOne
    private Payment paymentID;

    @JoinColumns({
        @JoinColumn(name = "PaymentID", referencedColumnName = "PaymentID", insertable = false, updatable = false),
        @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID", insertable = false, updatable = false)
    })
    @ManyToOne(optional = false)
    private Payment payment;

    @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Transaction transactionID;

    public WalletTransaction() {
    }

    public WalletTransaction(Long walletTransactionID) {
        this.walletTransactionID = walletTransactionID;
    }

    public WalletTransaction(Long walletTransactionID, String transactionType, BigDecimal amount, Date transactionTime) {
        this.walletTransactionID = walletTransactionID;
        this.transactionType = transactionType;
        this.amount = amount;
        this.transactionTime = transactionTime;
    }

    public Long getWalletTransactionID() {
        return walletTransactionID;
    }

    public void setWalletTransactionID(Long walletTransactionID) {
        this.walletTransactionID = walletTransactionID;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Date getTransactionTime() {
        return transactionTime;
    }

    public void setTransactionTime(Date transactionTime) {
        this.transactionTime = transactionTime;
    }

    public BookingRefund getBookingRefund() {
        return bookingRefund;
    }

    public void setBookingRefund(BookingRefund bookingRefund) {
        this.bookingRefund = bookingRefund;
    }

    public BookingRefund getBookingRefund1() {
        return bookingRefund1;
    }

    public void setBookingRefund1(BookingRefund bookingRefund1) {
        this.bookingRefund1 = bookingRefund1;
    }

    public CustomerWallet getWalletID() {
        return walletID;
    }

    public void setWalletID(CustomerWallet walletID) {
        this.walletID = walletID;
    }

    public Payment getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(Payment paymentID) {
        this.paymentID = paymentID;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Transaction getTransactionID() {
        return transactionID;
    }

    public void setTransactionID(Transaction transactionID) {
        this.transactionID = transactionID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (walletTransactionID != null ? walletTransactionID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof WalletTransaction)) {
            return false;
        }
        WalletTransaction other = (WalletTransaction) object;
        if ((this.walletTransactionID == null && other.walletTransactionID != null) || (this.walletTransactionID != null && !this.walletTransactionID.equals(other.walletTransactionID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.WalletTransaction[ walletTransactionID=" + walletTransactionID + " ]";
    }

}
