package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

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
    @NamedQuery(name = "Booking.findByTotalAmount", query = "SELECT b FROM Booking b WHERE b.totalAmount = :totalAmount"),
    @NamedQuery(name = "Booking.findByPaymentOption", query = "SELECT b FROM Booking b WHERE b.paymentOption = :paymentOption"),
    @NamedQuery(name = "Booking.findByCancelledAt", query = "SELECT b FROM Booking b WHERE b.cancelledAt = :cancelledAt")
})
public class Booking implements Serializable
{
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
    @Basic(optional = false)
    @NotNull
    @Column(name = "TotalAmount", precision = 12, scale = 2)
    private BigDecimal totalAmount;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 10)
    @Pattern(regexp = "DEPOSIT|FULL")
    @Column(name = "PaymentOption", updatable = false)
    private String paymentOption = "FULL";
    @Column(name = "FirstPaidAt", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date firstPaidAt;
    @Column(name = "BookingAmountAtFirstPayment", precision = 12, scale = 2, insertable = false, updatable = false)
    private BigDecimal bookingAmountAtFirstPayment;
    @Column(name = "RefundDeadline", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date refundDeadline;
    @Column(name = "CancelledAt", insertable = false, updatable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date cancelledAt;
    @Size(max = 30)
    @Column(name = "CancellationReason", insertable = false, updatable = false)
    private String cancellationReason;
    @JoinColumn(name = "CancelledByUserID", referencedColumnName = "UserID", insertable = false, updatable = false)
    @ManyToOne
    private Users cancelledByUserID;
    @JoinColumn(name = "CustomerID", referencedColumnName = "CustomerID")
    @ManyToOne(optional = false)
    private Customer customerID;
    @OneToMany(mappedBy = "bookingID")
    private Collection<BookingDetail> bookingDetailCollection = new ArrayList<>();

    public Booking()
    {

    }

    public Booking(String bookingID)
    {
        this.bookingID = bookingID;
    }

    public Booking(String bookingID, Date bookingDate, Date checkInDate, Date checkOutDate, String bookingStatus, BigDecimal totalAmount)
    {
        this.bookingID = bookingID;
        this.bookingDate = bookingDate;
        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
        this.bookingStatus = bookingStatus;
        this.totalAmount = totalAmount;
    }

    public Booking(String bookingID, Date bookingDate, Date checkInDate, Date checkOutDate, String bookingStatus, BigDecimal totalAmount, String paymentOption)
    {
        this(bookingID, bookingDate, checkInDate, checkOutDate, bookingStatus, totalAmount);
        this.paymentOption = paymentOption;
    }

    public String getBookingID()
    {
        return bookingID;
    }

    public void setBookingID(String bookingID)
    {
        this.bookingID = bookingID;
    }

    public Date getBookingDate()
    {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate)
    {
        this.bookingDate = bookingDate;
    }

    public Date getPaymentDeadline()
    {
        return paymentDeadline;
    }

    public void setPaymentDeadline(Date paymentDeadline)
    {
        this.paymentDeadline = paymentDeadline;
    }

    public Date getCheckInDate()
    {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate)
    {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate()
    {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate)
    {
        this.checkOutDate = checkOutDate;
    }

    public String getBookingStatus()
    {
        return bookingStatus;
    }

    public void setBookingStatus(String bookingStatus)
    {
        this.bookingStatus = bookingStatus;
    }

    public BigDecimal getTotalAmount()
    {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount)
    {
        this.totalAmount = totalAmount;
    }

    public String getPaymentOption()
    {
        return paymentOption;
    }

    public void setPaymentOption(String paymentOption)
    {
        this.paymentOption = paymentOption;
    }

    public Date getFirstPaidAt()
    {
        return firstPaidAt;
    }

    public BigDecimal getBookingAmountAtFirstPayment()
    {
        return bookingAmountAtFirstPayment;
    }

    public Date getRefundDeadline()
    {
        return refundDeadline;
    }

    public Date getCancelledAt()
    {
        return cancelledAt;
    }

    public String getCancellationReason()
    {
        return cancellationReason;
    }

    public Users getCancelledByUserID()
    {
        return cancelledByUserID;
    }

    public Customer getCustomerID()
    {
        return customerID;
    }

    public void setCustomerID(Customer customerID)
    {
        this.customerID = customerID;
    }

    @XmlTransient
    public Collection<BookingDetail> getBookingDetailCollection()
    {
        return bookingDetailCollection;
    }

    public void setBookingDetailCollection(Collection<BookingDetail> bookingDetailCollection)
    {
        this.bookingDetailCollection = bookingDetailCollection;
    }

    @Override
    public int hashCode()
    {
        return bookingID != null ? bookingID.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof Booking))
        {
            return false;
        }
        Booking other = (Booking) object;
        if ((this.bookingID == null && other.bookingID != null) || (this.bookingID != null && !this.bookingID.equals(other.bookingID)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "entity.Booking[ bookingID=" + bookingID + " ]";
    }
}
