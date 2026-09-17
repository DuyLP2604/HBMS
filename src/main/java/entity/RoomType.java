package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

@Entity
@Table(name = "ROOM_TYPE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
            name = "RoomType.findAll",
            query = "SELECT rt FROM RoomType rt"
    ),
    @NamedQuery(
            name = "RoomType.findByRoomTypeID",
            query = "SELECT rt FROM RoomType rt "
            + "WHERE rt.roomTypeID = :roomTypeID"
    ),
    @NamedQuery(
            name = "RoomType.findByTypeName",
            query = "SELECT rt FROM RoomType rt "
            + "WHERE rt.typeName = :typeName"
    )
})
public class RoomType implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(name = "RoomTypeID", length = 4)
    private String roomTypeID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(
            name = "TypeName",
            nullable = false,
            length = 50
    )
    private String typeName;

    @Basic(optional = false)
    @NotNull
    @Min(1)
    @Column(name = "Capacity", nullable = false)
    private Integer capacity;

    @Basic(optional = false)
    @NotNull
    @Column(
            name = "Price",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal price;

    @Size(max = 500)
    @Column(name = "Description", length = 500)
    private String description;

    @OneToMany(mappedBy = "roomTypeID")
    private Collection<Room> roomCollection;

    @OneToMany(mappedBy = "roomTypeID")
    private Collection<BookingDetail> bookingDetailCollection;

    public RoomType() {
    }

    public RoomType(String roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public RoomType(
            String roomTypeID,
            String typeName,
            Integer capacity,
            BigDecimal price) {

        this.roomTypeID = roomTypeID;
        this.typeName = typeName;
        this.capacity = capacity;
        this.price = price;
    }

    public String getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(String roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Collection<Room> getRoomCollection() {
        return roomCollection;
    }

    public void setRoomCollection(
            Collection<Room> roomCollection) {
        this.roomCollection = roomCollection;
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

    @Override
    public int hashCode() {
        return roomTypeID != null
                ? roomTypeID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RoomType)) {
            return false;
        }

        RoomType other = (RoomType) object;

        if (roomTypeID == null && other.roomTypeID != null) {
            return false;
        }

        return roomTypeID == null
                || roomTypeID.equals(other.roomTypeID);
    }

    @Override
    public String toString() {
        return "entity.RoomType[roomTypeID="
                + roomTypeID + "]";
    }
}
