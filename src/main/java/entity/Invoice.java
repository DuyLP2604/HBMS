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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Entity
@Table(name = "INVOICE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Invoice.findAll",
        query = "SELECT i FROM Invoice i"
    ),
    @NamedQuery(
        name = "Invoice.findByInvoiceID",
        query = "SELECT i FROM Invoice i "
              + "WHERE i.invoiceID = :invoiceID"
    ),
    @NamedQuery(
        name = "Invoice.findByInvoiceType",
        query = "SELECT i FROM Invoice i "
              + "WHERE i.invoiceType = :invoiceType"
    ),
    @NamedQuery(
        name = "Invoice.findByInvoiceDate",
        query = "SELECT i FROM Invoice i "
              + "WHERE i.invoiceDate = :invoiceDate"
    ),
    @NamedQuery(
        name = "Invoice.findByTotalAmount",
        query = "SELECT i FROM Invoice i "
              + "WHERE i.totalAmount = :totalAmount"
    ),
    @NamedQuery(
        name = "Invoice.findByBooking",
        query = "SELECT i FROM Invoice i "
              + "WHERE i.bookingID = :bookingID"
    )
})
public class Invoice implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(
        name = "InvoiceID",
        nullable = false,
        length = 6
    )
    private String invoiceID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(
        name = "InvoiceType",
        nullable = false,
        length = 30
    )
    private String invoiceType;

    @Basic(optional = false)
    @NotNull
    @Column(name = "InvoiceDate", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date invoiceDate;

    @Basic(optional = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(
        name = "TotalAmount",
        nullable = false,
        precision = 12,
        scale = 2
    )
    private BigDecimal totalAmount;

    /*
     * Database quy định mỗi Booking chỉ có một Invoice.
     */
    @JoinColumn(
        name = "BookingID",
        referencedColumnName = "BookingID",
        nullable = false,
        unique = true
    )
    @OneToOne(optional = false)
    private Booking bookingID;

    /*
     * EmployeeID cho phép NULL vì invoice có thể
     * được hệ thống tạo tự động sau khi thanh toán online.
     */
    @JoinColumn(
        name = "EmployeeID",
        referencedColumnName = "EmployeeID",
        nullable = true
    )
    @ManyToOne(optional = true)
    private Employee employeeID;

    public Invoice() {
    }

    public Invoice(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public Invoice(
            String invoiceID,
            String invoiceType,
            Date invoiceDate,
            BigDecimal totalAmount,
            Booking bookingID,
            Employee employeeID) {

        this.invoiceID = invoiceID;
        this.invoiceType = invoiceType;
        this.invoiceDate = invoiceDate;
        this.totalAmount = totalAmount;
        this.bookingID = bookingID;
        this.employeeID = employeeID;
    }

    @PrePersist
    private void prePersist() {
        if (invoiceDate == null) {
            invoiceDate = new Date();
        }

        if (totalAmount == null && bookingID != null) {
            totalAmount = bookingID.getTotalAmount();
        }
    }

    public String getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(String invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getInvoiceType() {
        return invoiceType;
    }

    public void setInvoiceType(String invoiceType) {
        this.invoiceType = invoiceType;
    }

    public Date getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(Date invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Booking getBookingID() {
        return bookingID;
    }

    public void setBookingID(Booking bookingID) {
        this.bookingID = bookingID;
    }

    public Employee getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Employee employeeID) {
        this.employeeID = employeeID;
    }

    @Override
    public int hashCode() {
        return invoiceID != null
                ? invoiceID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Invoice)) {
            return false;
        }

        Invoice other = (Invoice) object;

        if (invoiceID == null
                && other.invoiceID != null) {
            return false;
        }

        return invoiceID == null
                || invoiceID.equals(other.invoiceID);
    }

    @Override
    public String toString() {
        return "entity.Invoice[invoiceID="
                + invoiceID + "]";
    }
}