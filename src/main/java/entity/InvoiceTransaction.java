/*
     * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
     * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinColumns;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "INVOICE_TRANSACTION")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "InvoiceTransaction.findAll", query = "SELECT i FROM InvoiceTransaction i"),
    @NamedQuery(name = "InvoiceTransaction.findByInvoiceID", query = "SELECT i FROM InvoiceTransaction i WHERE i.invoiceTransactionPK.invoiceID = :invoiceID"),
    @NamedQuery(name = "InvoiceTransaction.findByTransactionID", query = "SELECT i FROM InvoiceTransaction i WHERE i.invoiceTransactionPK.transactionID = :transactionID")})
public class InvoiceTransaction implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected InvoiceTransactionPK invoiceTransactionPK;
    @JoinColumns({
        @JoinColumn(name = "BookingID", referencedColumnName = "BookingID"),
        @JoinColumn(name = "InvoiceID", referencedColumnName = "InvoiceID", insertable = false, updatable = false)})
    @ManyToOne(optional = false)
    private Invoice invoice;
    @JoinColumns({
        @JoinColumn(name = "BookingID", referencedColumnName = "BookingID", insertable = false, updatable = false),
        @JoinColumn(name = "PaymentID", referencedColumnName = "PaymentID", insertable = false, updatable = false),
        @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID", insertable = false, updatable = false)
    })
    @ManyToOne(optional = false)
    private Payment payment;
    @JoinColumn(name = "TransactionID", referencedColumnName = "TransactionID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Transaction transaction;
    @ManyToOne
    @JoinColumn(name = "BookingID", referencedColumnName = "BookingID", insertable = false, updatable = false)
    private Booking booking;

    public InvoiceTransaction() {
    }

    public InvoiceTransaction(InvoiceTransactionPK invoiceTransactionPK) {
        this.invoiceTransactionPK = invoiceTransactionPK;
    }

    public InvoiceTransaction(String invoiceID, String transactionID) {
        this.invoiceTransactionPK = new InvoiceTransactionPK(invoiceID, transactionID);
    }

    public InvoiceTransactionPK getInvoiceTransactionPK() {
        return invoiceTransactionPK;
    }

    public void setInvoiceTransactionPK(InvoiceTransactionPK invoiceTransactionPK) {
        this.invoiceTransactionPK = invoiceTransactionPK;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Payment getPayment() {
        return payment;
    }

    public void setPayment(Payment payment) {
        this.payment = payment;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (invoiceTransactionPK != null ? invoiceTransactionPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof InvoiceTransaction)) {
            return false;
        }
        InvoiceTransaction other = (InvoiceTransaction) object;
        if ((this.invoiceTransactionPK == null && other.invoiceTransactionPK != null) || (this.invoiceTransactionPK != null && !this.invoiceTransactionPK.equals(other.invoiceTransactionPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.InvoiceTransaction[ invoiceTransactionPK=" + invoiceTransactionPK + " ]";
    }

}
