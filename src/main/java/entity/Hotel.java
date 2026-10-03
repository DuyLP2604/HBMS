/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * The single hotel managed by the system.
 *
 * The hotel name is fixed (see CK_HOTEL_SingleHotel in the database), so it is
 * also the primary key. Use {@link #FIXED_NAME} to load the one hotel row.
 */
@Entity
@Table(name = "HOTEL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Hotel.findAll", query = "SELECT h FROM Hotel h"),
    @NamedQuery(name = "Hotel.findByHotelName", query = "SELECT h FROM Hotel h WHERE h.hotelName = :hotelName"),
    @NamedQuery(name = "Hotel.findByHotelImage", query = "SELECT h FROM Hotel h WHERE h.hotelImage = :hotelImage"),
    @NamedQuery(name = "Hotel.findByAddress", query = "SELECT h FROM Hotel h WHERE h.address = :address")})
public class Hotel implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The only hotel name the database accepts. It is the primary key of the
     * single HOTEL row.
     */
    public static final String FIXED_NAME = "Simple Bear Hotel";

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "HotelName")
    private String hotelName;

    /** Background image at the top of the home page. */
    @Size(max = 100)
    @Column(name = "HotelImage")
    private String hotelImage;

    /** "Luxury Rooms" image in the Overview section. */
    @Size(max = 100)
    @Column(name = "RoomImage")
    private String roomImage;

    /** "Swimming Pool" image in the Overview section. */
    @Size(max = 100)
    @Column(name = "PoolImage")
    private String poolImage;

    /** "Restaurant" image in the Overview section. */
    @Size(max = 100)
    @Column(name = "RestaurantImage")
    private String restaurantImage;

    /** One address per line. */
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 500)
    @Column(name = "Address")
    private String address;

    public Hotel() {
    }

    public Hotel(String hotelName) {
        this.hotelName = hotelName;
    }

    public Hotel(String hotelName, String address) {
        this.hotelName = hotelName;
        this.address = address;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getHotelImage() {
        return hotelImage;
    }

    public void setHotelImage(String hotelImage) {
        this.hotelImage = hotelImage;
    }

    public String getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }

    public String getPoolImage() {
        return poolImage;
    }

    public void setPoolImage(String poolImage) {
        this.poolImage = poolImage;
    }

    public String getRestaurantImage() {
        return restaurantImage;
    }

    public void setRestaurantImage(String restaurantImage) {
        this.restaurantImage = restaurantImage;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (hotelName != null ? hotelName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hotel)) {
            return false;
        }
        Hotel other = (Hotel) object;
        if ((this.hotelName == null && other.hotelName != null) || (this.hotelName != null && !this.hotelName.equals(other.hotelName))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Hotel[ hotelName=" + hotelName + " ]";
    }

}
