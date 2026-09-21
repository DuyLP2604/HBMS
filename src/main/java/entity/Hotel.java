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

@Entity
@Table(name = "HOTEL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Hotel.findAll",
        query = "SELECT h FROM Hotel h"
    ),
    @NamedQuery(
        name = "Hotel.findByHotelID",
        query = "SELECT h FROM Hotel h "
              + "WHERE h.hotelID = :hotelID"
    ),
    @NamedQuery(
        name = "Hotel.findByHotelName",
        query = "SELECT h FROM Hotel h "
              + "WHERE h.hotelName = :hotelName"
    ),
    @NamedQuery(
        name = "Hotel.findByAddress",
        query = "SELECT h FROM Hotel h "
              + "WHERE h.address = :address"
    )
})
public class Hotel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(
        name = "HotelID",
        nullable = false,
        length = 3
    )
    private String hotelID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(
        name = "HotelName",
        nullable = false,
        length = 50
    )
    private String hotelName;

    /*
     * HotelImage cho phép NULL trong database.
     */
    @Size(max = 100)
    @Column(name = "HotelImage", length = 100)
    private String hotelImage;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(
        name = "Address",
        nullable = false,
        length = 200
    )
    private String address;

    public Hotel() {
    }

    public Hotel(String hotelID) {
        this.hotelID = hotelID;
    }

    public Hotel(
            String hotelID,
            String hotelName,
            String hotelImage,
            String address) {

        this.hotelID = hotelID;
        this.hotelName = hotelName;
        this.hotelImage = hotelImage;
        this.address = address;
    }

    public String getHotelID() {
        return hotelID;
    }

    public void setHotelID(String hotelID) {
        this.hotelID = hotelID;
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

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    @Override
    public int hashCode() {
        return hotelID != null
                ? hotelID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Hotel)) {
            return false;
        }

        Hotel other = (Hotel) object;

        if (hotelID == null && other.hotelID != null) {
            return false;
        }

        return hotelID == null
                || hotelID.equals(other.hotelID);
    }

    @Override
    public String toString() {
        return "entity.Hotel[hotelID="
                + hotelID + "]";
    }
}