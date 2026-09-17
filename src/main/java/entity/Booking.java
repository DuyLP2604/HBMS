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
import jakarta.persistence.OneToMany;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collection;
import jakarta.persistence.Cacheable;

@Entity
@Cacheable(false)
@Table(name = "BOOKING")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
            name = "Booking.findAll",
            query = "SELECT b FROM Booking b"
    ),
    @NamedQuery(
            name = "Booking.findByBookingID",
            query = "SELECT b FROM Booking b "
            + "WHERE b.bookingID = :bookingID"
    ),
    @NamedQuery(
            name = "Booking.findByBookingDate",
            query = "SELECT b FROM Booking b "
            + "WHERE b.bookingDate = :bookingDate"
    ),
    @NamedQuery(
            name = "Booking.findByCheckInDate",
            query = "SELECT b FROM Booking b "
            + "WHERE b.checkInDate = :checkInDate"
    ),
    @NamedQuery(
            name = "Booking.findByCheckOutDate",
            query = "SELECT b FROM Booking b "
            + "WHERE b.checkOutDate = :checkOutDate"
    ),
    @NamedQuery(
            name = "Booking.findByBookingStatus",
            query = "SELECT b FROM Booking b "
            + "WHERE b.bookingStatus = :bookingStatus"
    ),
    @NamedQuery(
            name = "Booking.findByCustomer",
            query = "SELECT b FROM Booking b "
            + "WHERE b.customerID = :customerID "
            + "ORDER BY b.bookingDate DESC"
    )
})
public class Booking implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(
            name = "BookingID",
            nullable = false,
            length = 6
    )
    private String bookingID;

    @Basic(optional = false)
    @NotNull
    @Column(name = "BookingDate", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingDate;

    @Basic(optional = false)
    @NotNull
    @Column(name = "CheckInDate", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date checkInDate;

    @Basic(optional = false)
    @NotNull
    @Column(name = "CheckOutDate", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date checkOutDate;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(
            name = "BookingStatus",
            nullable = false,
            length = 30
    )
    private String bookingStatus;

    @Basic(optional = false)
    @NotNull
    @Column(
            name = "TotalAmount",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal totalAmount;

    @JoinColumn(
            name = "CustomerID",
            referencedColumnName = "CustomerID",
            nullable = false
    )
    @ManyToOne(optional = false)
    private Customer customerID;

    @OneToOne(mappedBy = "bookingID")
    private Invoice invoice;

    @OneToMany(mappedBy = "bookingID")
    private Collection<BookingDetail> bookingDetailCollection;

    @OneToMany(mappedBy = "bookingID")
    private Collection<BookingService> bookingServiceCollection;

    @OneToMany(mappedBy = "bookingID")
    private Collection<Payment> paymentCollection;

    @Column(name = "PaymentDeadline")
    @Temporal(TemporalType.TIMESTAMP)
    private Date paymentDeadline;

    public Booking() {
    }

    public Booking(String bookingID) {
        this.bookingID = bookingID;
    }

    public Booking(
            String bookingID,
            Date bookingDate,
            Date checkInDate,
            Date checkOutDate,
            String bookingStatus) {

        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.totalAmount = BigDecimal.ZERO;
    }

    public Booking(
            String bookingID,
            Date bookingDate,
            Date checkInDate,
            Date checkOutDate,
            String bookingStatus,
            BigDecimal totalAmount,
            Customer customerID) {

        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.totalAmount = totalAmount;
        this.customerID = customerID;
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

    public Customer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Customer customerID) {
        this.customerID = customerID;
    }

    public Invoice getInvoice() {
        return invoice;
    }

    public void setInvoice(Invoice invoice) {
        this.invoice = invoice;
    }

    public Date getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(
            Date paymentDeadline) {

        this.paymentDeadline = paymentDeadline;
    }

    @XmlTransient
    public Collection<BookingDetail>
            getBookingDetailCollection() {

        return bookingDetailCollection;
    }

    public void setBookingDetailCollection(
            Collection<BookingDetail> bookingDetailCollection) {

        this.bookingDetailCollection
                = bookingDetailCollection;
    }

    @XmlTransient
    public Collection<BookingService>
            getBookingServiceCollection() {

        return bookingServiceCollection;
    }

    public void setBookingServiceCollection(
            Collection<BookingService> bookingServiceCollection) {

        this.bookingServiceCollection
                = bookingServiceCollection;
    }

    @XmlTransient
    public Collection<Payment>
            getPaymentCollection() {

        return paymentCollection;
    }

    public void setPaymentCollection(
            Collection<Payment> paymentCollection) {

        this.paymentCollection = paymentCollection;
    }

    @Override
    public int hashCode() {
        return bookingID != null
                ? bookingID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Booking)) {
            return false;
        }

        Booking other = (Booking) object;

        if (bookingID == null && other.bookingID != null) {
            return false;
        }

        return bookingID == null
                || bookingID.equals(other.bookingID);
    }

    @Override
    public String toString() {
        return "entity.Booking[bookingID="
                + bookingID + "]";
    }
}
