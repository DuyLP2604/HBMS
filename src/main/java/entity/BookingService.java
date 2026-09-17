package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.math.BigDecimal;

@Entity
@Table(name = "BOOKING_SERVICE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "BookingService.findAll",
        query = "SELECT bs FROM BookingService bs"
    ),
    @NamedQuery(
        name = "BookingService.findByBooking",
        query = "SELECT bs FROM BookingService bs "
              + "WHERE bs.bookingID = :bookingID"
    ),
    @NamedQuery(
        name = "BookingService.findByService",
        query = "SELECT bs FROM BookingService bs "
              + "WHERE bs.serviceID = :serviceID"
    ),
    @NamedQuery(
        name = "BookingService.findByBookingAndService",
        query = "SELECT bs FROM BookingService bs "
              + "WHERE bs.bookingServicePK.bookingID = :bookingID "
              + "AND bs.bookingServicePK.serviceID = :serviceID"
    )
})
public class BookingService implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected BookingServicePK bookingServicePK;

    @Basic(optional = false)
    @NotNull
    @Min(1)
    @Column(name = "Quantity", nullable = false)
    private Integer quantity;

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

    /*
     * BookingID và ServiceID đã được ánh xạ trong EmbeddedId.
     * Vì vậy hai quan hệ dưới đây phải dùng
     * insertable = false, updatable = false.
     */
    @JoinColumn(
        name = "BookingID",
        referencedColumnName = "BookingID",
        nullable = false,
        insertable = false,
        updatable = false
    )
    @ManyToOne(optional = false)
    private Booking bookingID;

    @JoinColumn(
        name = "ServiceID",
        referencedColumnName = "ServiceID",
        nullable = false,
        insertable = false,
        updatable = false
    )
    @ManyToOne(optional = false)
    private Service serviceID;

    public BookingService() {
    }

    public BookingService(
            BookingServicePK bookingServicePK) {

        this.bookingServicePK = bookingServicePK;
    }

    public BookingService(
            Booking bookingID,
            Service serviceID,
            Integer quantity,
            BigDecimal unitPrice) {

        this.bookingID = bookingID;
        this.serviceID = serviceID;
        this.quantity = quantity;
        this.unitPrice = unitPrice;

        createPrimaryKey();
        calculateSubtotal();
    }

    @PrePersist
    @PreUpdate
    private void prepareData() {
        createPrimaryKey();
        calculateSubtotal();
    }

    private void createPrimaryKey() {
        if (bookingID != null
                && serviceID != null
                && bookingID.getBookingID() != null
                && serviceID.getServiceID() != null) {

            bookingServicePK = new BookingServicePK(
                    bookingID.getBookingID(),
                    serviceID.getServiceID()
            );
        }
    }

    private void calculateSubtotal() {
        if (quantity != null && unitPrice != null) {
            subtotal = unitPrice.multiply(
                    BigDecimal.valueOf(quantity)
            );
        }
    }

    public BookingServicePK getBookingServicePK() {
        return bookingServicePK;
    }

    public void setBookingServicePK(
            BookingServicePK bookingServicePK) {

        this.bookingServicePK = bookingServicePK;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
        calculateSubtotal();
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
        calculateSubtotal();
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    /*
     * Setter vẫn được giữ cho JPA và khi đọc dữ liệu.
     * Trước persist/update, subtotal sẽ được tính lại.
     */
    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public Booking getBookingID() {
        return bookingID;
    }

    public void setBookingID(Booking bookingID) {
        this.bookingID = bookingID;
        createPrimaryKey();
    }

    public Service getServiceID() {
        return serviceID;
    }

    public void setServiceID(Service serviceID) {
        this.serviceID = serviceID;
        createPrimaryKey();
    }

    @Override
    public int hashCode() {
        return bookingServicePK != null
                ? bookingServicePK.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BookingService)) {
            return false;
        }

        BookingService other =
                (BookingService) object;

        if (bookingServicePK == null
                && other.bookingServicePK != null) {
            return false;
        }

        return bookingServicePK == null
                || bookingServicePK.equals(
                        other.bookingServicePK
                );
    }

    @Override
    public String toString() {
        return "entity.BookingService["
                + bookingServicePK + "]";
    }
}