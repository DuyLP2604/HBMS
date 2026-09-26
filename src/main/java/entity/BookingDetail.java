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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "BOOKING_DETAIL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BookingDetail.findAll", query = "SELECT b FROM BookingDetail b"),
    @NamedQuery(name = "BookingDetail.findByBookingDetailID", query = "SELECT b FROM BookingDetail b WHERE b.bookingDetailID = :bookingDetailID"),
    @NamedQuery(name = "BookingDetail.findByQuantity", query = "SELECT b FROM BookingDetail b WHERE b.quantity = :quantity"),
    @NamedQuery(name = "BookingDetail.findByGuestCount", query = "SELECT b FROM BookingDetail b WHERE b.guestCount = :guestCount"),
    @NamedQuery(name = "BookingDetail.findByUnitPrice", query = "SELECT b FROM BookingDetail b WHERE b.unitPrice = :unitPrice"),
    @NamedQuery(name = "BookingDetail.findBySubtotal", query = "SELECT b FROM BookingDetail b WHERE b.subtotal = :subtotal")})
public class BookingDetail implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "BookingDetailID")
    private Integer bookingDetailID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Quantity")
    private int quantity;
    @Basic(optional = false)
    @NotNull
    @Column(name = "GuestCount")
    private int guestCount;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Subtotal")
    private BigDecimal subtotal;
    @JoinColumn(name = "BookingID", referencedColumnName = "BookingID")
    @ManyToOne(optional = false)
    private Booking bookingID;
    @JoinColumn(name = "RoomTypeID", referencedColumnName = "RoomTypeID")
    @ManyToOne(optional = false)
    private RoomType roomTypeID;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "bookingDetailID")
    private Collection<RoomAssignment> roomAssignmentCollection;

    public BookingDetail() {
    }

    public BookingDetail(Integer bookingDetailID) {
        this.bookingDetailID = bookingDetailID;
    }

    public BookingDetail(Integer bookingDetailID, int quantity, int guestCount, BigDecimal unitPrice, BigDecimal subtotal) {
        this.bookingDetailID = bookingDetailID;
        this.quantity = quantity;
        this.guestCount = guestCount;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public Integer getBookingDetailID() {
        return bookingDetailID;
    }

    public void setBookingDetailID(Integer bookingDetailID) {
        this.bookingDetailID = bookingDetailID;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
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
    public Collection<RoomAssignment> getRoomAssignmentCollection() {
        return roomAssignmentCollection;
    }

    public void setRoomAssignmentCollection(Collection<RoomAssignment> roomAssignmentCollection) {
        this.roomAssignmentCollection = roomAssignmentCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bookingDetailID != null ? bookingDetailID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookingDetail)) {
            return false;
        }
        BookingDetail other = (BookingDetail) object;
        if ((this.bookingDetailID == null && other.bookingDetailID != null) || (this.bookingDetailID != null && !this.bookingDetailID.equals(other.bookingDetailID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.BookingDetail[ bookingDetailID=" + bookingDetailID + " ]";
    }

}
