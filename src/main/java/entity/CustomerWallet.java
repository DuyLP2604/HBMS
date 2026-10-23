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
import java.util.Collection;
import java.util.Date;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "CUSTOMER_WALLET")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "CustomerWallet.findAll", query = "SELECT c FROM CustomerWallet c"),
    @NamedQuery(name = "CustomerWallet.findByWalletID", query = "SELECT c FROM CustomerWallet c WHERE c.walletID = :walletID"),
    @NamedQuery(name = "CustomerWallet.findByCreatedAt", query = "SELECT c FROM CustomerWallet c WHERE c.createdAt = :createdAt")})
public class CustomerWallet implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "WalletID")
    private Long walletID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CreatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerWallet")
    private Collection<BookingRefund> bookingRefundCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "walletID")
    private Collection<WalletTransaction> walletTransactionCollection;
    @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID")
    @OneToOne(optional = false)
    private Customer customerID;

    public CustomerWallet() {
    }

    public CustomerWallet(Long walletID) {
        this.walletID = walletID;
    }

    public CustomerWallet(Long walletID, Date createdAt) {
        this.walletID = walletID;
        this.createdAt = createdAt;
    }

    public Long getWalletID() {
        return walletID;
    }

    public void setWalletID(Long walletID) {
        this.walletID = walletID;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @XmlTransient
    public Collection<BookingRefund> getBookingRefundCollection() {
        return bookingRefundCollection;
    }

    public void setBookingRefundCollection(Collection<BookingRefund> bookingRefundCollection) {
        this.bookingRefundCollection = bookingRefundCollection;
    }

    @XmlTransient
    public Collection<WalletTransaction> getWalletTransactionCollection() {
        return walletTransactionCollection;
    }

    public void setWalletTransactionCollection(Collection<WalletTransaction> walletTransactionCollection) {
        this.walletTransactionCollection = walletTransactionCollection;
    }

    public Customer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Customer customerID) {
        this.customerID = customerID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (walletID != null ? walletID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof CustomerWallet)) {
            return false;
        }
        CustomerWallet other = (CustomerWallet) object;
        if ((this.walletID == null && other.walletID != null) || (this.walletID != null && !this.walletID.equals(other.walletID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.CustomerWallet[ walletID=" + walletID + " ]";
    }

}
