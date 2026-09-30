/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "BOOKING_SERVICE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BookingService.findAll", query = "SELECT b FROM BookingService b"),
    @NamedQuery(name = "BookingService.findByBookingID", query = "SELECT b FROM BookingService b WHERE b.bookingServicePK.bookingID = :bookingID"),
    @NamedQuery(name = "BookingService.findByServiceID", query = "SELECT b FROM BookingService b WHERE b.bookingServicePK.serviceID = :serviceID"),
    @NamedQuery(name = "BookingService.findByQuantity", query = "SELECT b FROM BookingService b WHERE b.quantity = :quantity"),
    @NamedQuery(name = "BookingService.findByUnitPrice", query = "SELECT b FROM BookingService b WHERE b.unitPrice = :unitPrice"),
    @NamedQuery(name = "BookingService.findBySubtotal", query = "SELECT b FROM BookingService b WHERE b.subtotal = :subtotal")})
public class BookingService implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected BookingServicePK bookingServicePK;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Quantity")
    private int quantity;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "UnitPrice")
    private BigDecimal unitPrice;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Subtotal")
    private BigDecimal subtotal;
    @JoinColumn(name = "BookingID", referencedColumnName = "BookingID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Booking booking;
    @JoinColumn(name = "ServiceID", referencedColumnName = "ServiceID", insertable = false, updatable = false)
    @ManyToOne(optional = false)
    private Service service;

    public BookingService() {
    }

    public BookingService(BookingServicePK bookingServicePK) {
        this.bookingServicePK = bookingServicePK;
    }

    public BookingService(BookingServicePK bookingServicePK, int quantity, BigDecimal unitPrice, BigDecimal subtotal) {
        this.bookingServicePK = bookingServicePK;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = subtotal;
    }

    public BookingService(String bookingID, String serviceID) {
        this.bookingServicePK = new BookingServicePK(bookingID, serviceID);
    }

    public BookingServicePK getBookingServicePK() {
        return bookingServicePK;
    }

    public void setBookingServicePK(BookingServicePK bookingServicePK) {
        this.bookingServicePK = bookingServicePK;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
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

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Service getService() {
        return service;
    }

    public void setService(Service service) {
        this.service = service;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (bookingServicePK != null ? bookingServicePK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookingService)) {
            return false;
        }
        BookingService other = (BookingService) object;
        if ((this.bookingServicePK == null && other.bookingServicePK != null) || (this.bookingServicePK != null && !this.bookingServicePK.equals(other.bookingServicePK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.BookingService[ bookingServicePK=" + bookingServicePK + " ]";
    }

}
