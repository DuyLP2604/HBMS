/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "BOOKING")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Booking.findAll", query = "SELECT b FROM Booking b"),
    @NamedQuery(name = "Booking.findByBookingID", query = "SELECT b FROM Booking b WHERE b.bookingID = :bookingID"),
    @NamedQuery(name = "Booking.findByBookingDate", query = "SELECT b FROM Booking b WHERE b.bookingDate = :bookingDate"),
    @NamedQuery(name = "Booking.findByPaymentDeadline", query = "SELECT b FROM Booking b WHERE b.paymentDeadline = :paymentDeadline"),
    @NamedQuery(name = "Booking.findByCheckInDate", query = "SELECT b FROM Booking b WHERE b.checkInDate = :checkInDate"),
    @NamedQuery(name = "Booking.findByCheckOutDate", query = "SELECT b FROM Booking b WHERE b.checkOutDate = :checkOutDate"),
    @NamedQuery(name = "Booking.findByBookingStatus", query = "SELECT b FROM Booking b WHERE b.bookingStatus = :bookingStatus"),
    @NamedQuery(name = "Booking.findByTotalAmount", query = "SELECT b FROM Booking b WHERE b.totalAmount = :totalAmount")})
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(name = "BookingID")
    private String bookingID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "BookingDate")
    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;
    @Column(name = "PaymentDeadline")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDeadline;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CheckInDate")
    @Temporal(TemporalType.DATE)
    private Date checkInDate;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CheckOutDate")
    @Temporal(TemporalType.DATE)
    private Date checkOutDate;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(name = "BookingStatus")
    private String bookingStatus;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "TotalAmount")
    private BigDecimal totalAmount;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "booking")
    private Collection<BookingService> bookingServiceCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bookingID")
    private Collection<Payment> paymentCollection;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "bookingID")
    private Invoice invoice;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bookingID")
    private Collection<BookingDetail> bookingDetailCollection;
    @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID")
    @ManyToOne(optional = false)
    private Customer customerID;

    public Booking() {
    }

    public Booking(String bookingID) {
        this.bookingID = bookingID;
    }

    public Booking(String bookingID, Date bookingDate, Date checkInDate, Date checkOutDate, String bookingStatus, BigDecimal totalAmount) {
        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.totalAmount = totalAmount;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(Date paymentDeadline) {
        this.paymentDeadline = paymentDeadline;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    @XmlTransient
    public Collection<BookingService> getBookingServiceCollection() {
        return bookingServiceCollection;
    }

    public void setBookingServiceCollection(Collection<BookingService> bookingServiceCollection) {
        this.bookingServiceCollection = bookingServiceCollection;
    }

    @XmlTransient
    public Collection<Payment> getPaymentCollection() {
        return paymentCollection;
    }

    public void setPaymentCollection(Collection<Payment> paymentCollection) {
        this.paymentCollection = paymentCollection;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    @XmlTransient
    public Collection<BookingDetail> getBookingDetailCollection() {
        return bookingDetailCollection;
    }

    public void setBookingDetailCollection(Collection<BookingDetail> bookingDetailCollection) {
        this.bookingDetailCollection = bookingDetailCollection;
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
        hash += (bookingID != null ? bookingID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Booking)) {
            return false;
        }
        Booking other = (Booking) object;
        if ((this.bookingID == null && other.bookingID != null) || (this.bookingID != null && !this.bookingID.equals(other.bookingID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Booking[ bookingID=" + bookingID + " ]";
    }

}
