package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.OneToMany;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collection;

@Entity
@Table(name = "BOOKING_DETAIL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
            name = "BookingDetail.findAll",
            query = "SELECT bd FROM BookingDetail bd"
    ),
    @NamedQuery(
            name = "BookingDetail.findByBookingDetailID",
            query = "SELECT bd FROM BookingDetail bd "
            + "WHERE bd.bookingDetailID = :bookingDetailID"
    ),
    @NamedQuery(
            name = "BookingDetail.findByBooking",
            query = "SELECT bd FROM BookingDetail bd "
            + "WHERE bd.bookingID = :bookingID"
    ),
    @NamedQuery(
            name = "BookingDetail.findByRoomType",
            query = "SELECT bd FROM BookingDetail bd "
            + "WHERE bd.roomTypeID = :roomTypeID"
    )
})
public class BookingDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(
            name = "BookingDetailID",
            nullable = false
    )
    private Integer bookingDetailID;

    @Basic(optional = false)
    @NotNull
    @Min(1)
    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

    @Basic(optional = false)
    @NotNull
    @Min(1)
    @Column(name = "GuestCount", nullable = false)
    private Integer guestCount;

    @Basic(optional = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(
            name = "UnitPrice",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    @Basic(optional = false)
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    @Column(
            name = "Subtotal",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal subtotal;

    @JoinColumn(
            name = "BookingID",
            referencedColumnName = "BookingID",
            nullable = false
    )
    @ManyToOne(optional = false)
    private Booking bookingID;

    @JoinColumn(
            name = "RoomTypeID",
            referencedColumnName = "RoomTypeID",
            nullable = false
    )
    @ManyToOne(optional = false)
    private RoomType roomTypeID;

    @OneToMany(mappedBy = "bookingDetailID")
    private Collection<RoomAssignment> roomAssignmentCollection;

    public BookingDetail() {
    }

    public BookingDetail(Integer bookingDetailID) {
        this.bookingDetailID = bookingDetailID;
    }

    public BookingDetail(
            Integer quantity,
            Integer guestCount,
            BigDecimal unitPrice,
            BigDecimal subtotal,
            Booking bookingID,
            RoomType roomTypeID) {

        this.quantity = quantity;
        this.guestCount = guestCount;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
        this.bookingID = bookingID;
        this.roomTypeID = roomTypeID;
    }

    public Integer getBookingDetailID() {
        return bookingDetailID;
    }

    public void setBookingDetailID(Integer bookingDetailID) {
        this.bookingDetailID = bookingDetailID;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Integer getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(Integer guestCount) {
        this.guestCount = guestCount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Booking getBookingID() {
        return bookingID;
    }

    public void setBookingID(Booking bookingID) {
        this.bookingID = bookingID;
    }

    public RoomType getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(RoomType roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    @XmlTransient
    public Collection<RoomAssignment>
            getRoomAssignmentCollection() {

        return roomAssignmentCollection;
    }

    public void setRoomAssignmentCollection(
            Collection<RoomAssignment> roomAssignmentCollection) {

        this.roomAssignmentCollection
                = roomAssignmentCollection;
    }

    @Override
    public int hashCode() {
        return bookingDetailID != null
                ? bookingDetailID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BookingDetail)) {
            return false;
        }

        BookingDetail other = (BookingDetail) object;

        if (bookingDetailID == null
                && other.bookingDetailID != null) {
            return false;
        }

        return bookingDetailID == null
                || bookingDetailID.equals(
                        other.bookingDetailID
                );
    }

    @Override
    public String toString() {
        return "entity.BookingDetail[bookingDetailID="
                + bookingDetailID + "]";
    }
}
