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
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
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
@Table(name = "ROOM_TYPE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RoomType.findAll", query = "SELECT r FROM RoomType r"),
    @NamedQuery(name = "RoomType.findByRoomTypeID", query = "SELECT r FROM RoomType r WHERE r.roomTypeID = :roomTypeID"),
    @NamedQuery(name = "RoomType.findByTypeName", query = "SELECT r FROM RoomType r WHERE r.typeName = :typeName"),
    @NamedQuery(name = "RoomType.findByCapacity", query = "SELECT r FROM RoomType r WHERE r.capacity = :capacity"),
    @NamedQuery(name = "RoomType.findByPrice", query = "SELECT r FROM RoomType r WHERE r.price = :price"),
    @NamedQuery(name = "RoomType.findByRoomTypeImage", query = "SELECT r FROM RoomType r WHERE r.roomTypeImage = :roomTypeImage"),
    @NamedQuery(name = "RoomType.findByDescription", query = "SELECT r FROM RoomType r WHERE r.description = :description")})
public class RoomType implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(name = "RoomTypeID")
    private String roomTypeID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "TypeName")
    private String typeName;
    @Basic(optional = false)
    @NotNull
    @Column(name = "Capacity")
    private int capacity;
    // @Max(value=?)  @Min(value=?)//if you know range of your decimal fields consider using these annotations to enforce field validation
    @Basic(optional = false)
    @NotNull
    @Column(name = "Price")
    private BigDecimal price;
    @Size(max = 255)
    @Column(name = "RoomTypeImage")
    private String roomTypeImage;
    @Size(max = 500)
    @Column(name = "Description")
    private String description;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "roomTypeID")
    private Collection<BookingDetail> bookingDetailCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "roomTypeID")
    private Collection<Room> roomCollection;

    public RoomType() {
    }

    public RoomType(String roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public RoomType(String roomTypeID, String typeName, int capacity, BigDecimal price) {
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

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getRoomTypeImage() {
        return roomTypeImage;
    }

    public void setRoomTypeImage(String roomTypeImage) {
        this.roomTypeImage = roomTypeImage;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @XmlTransient
    public Collection<BookingDetail> getBookingDetailCollection() {
        return bookingDetailCollection;
    }

    public void setBookingDetailCollection(Collection<BookingDetail> bookingDetailCollection) {
        this.bookingDetailCollection = bookingDetailCollection;
    }

    @XmlTransient
    public Collection<Room> getRoomCollection() {
        return roomCollection;
    }

    public void setRoomCollection(Collection<Room> roomCollection) {
        this.roomCollection = roomCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (roomTypeID != null ? roomTypeID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RoomType)) {
            return false;
        }
        RoomType other = (RoomType) object;
        if ((this.roomTypeID == null && other.roomTypeID != null) || (this.roomTypeID != null && !this.roomTypeID.equals(other.roomTypeID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.RoomType[ roomTypeID=" + roomTypeID + " ]";
    }

}
