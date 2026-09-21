package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import jakarta.persistence.OneToMany;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collection;

@Entity
@Table(name = "ROOM")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
            name = "Room.findAll",
            query = "SELECT r FROM Room r"
    ),
    @NamedQuery(
            name = "Room.findByRoomID",
            query = "SELECT r FROM Room r "
            + "WHERE r.roomID = :roomID"
    ),
    @NamedQuery(
            name = "Room.findByRoomNumber",
            query = "SELECT r FROM Room r "
            + "WHERE r.roomNumber = :roomNumber"
    ),
    @NamedQuery(
            name = "Room.findByStatus",
            query = "SELECT r FROM Room r "
            + "WHERE r.status = :status"
    ),
    @NamedQuery(
            name = "Room.findByRoomType",
            query = "SELECT r FROM Room r "
            + "WHERE r.roomTypeID = :roomTypeID"
    ),
    @NamedQuery(
            name = "Room.findActiveByRoomType",
            query = "SELECT r FROM Room r "
            + "WHERE r.roomTypeID = :roomTypeID "
            + "AND r.status = 'ACTIVE'"
    )
})
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(
            name = "RoomID",
            nullable = false,
            length = 3
    )
    private String roomID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(
            name = "RoomNumber",
            nullable = false,
            length = 20
    )
    private String roomNumber;

    @Size(max = 100)
    @Column(name = "RoomImage", length = 100)
    private String roomImage;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(
            name = "Status",
            nullable = false,
            length = 20
    )
    private String status = "ACTIVE";

    @JoinColumn(
            name = "RoomTypeID",
            referencedColumnName = "RoomTypeID",
            nullable = false
    )
    @ManyToOne(optional = false)
    private RoomType roomTypeID;

    @OneToMany(mappedBy = "roomID")
    private Collection<RoomAssignment> roomAssignmentCollection;

    public Room() {
    }

    public Room(String roomID) {
        this.roomID = roomID;
    }

    public Room(
            String roomID,
            String roomNumber,
            String roomImage,
            String status,
            RoomType roomTypeID) {

        this.roomID = roomID;
        this.roomNumber = roomNumber;
        this.roomImage = roomImage;
        this.status = status;
        this.roomTypeID = roomTypeID;
    }

    public String getRoomID() {
        return roomID;
    }

    public void setRoomID(String roomID) {
        this.roomID = roomID;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getRoomImage() {
        return roomImage;
    }

    public void setRoomImage(String roomImage) {
        this.roomImage = roomImage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
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
        return roomID != null ? roomID.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Room)) {
            return false;
        }

        Room other = (Room) object;

        if (roomID == null && other.roomID != null) {
            return false;
        }

        return roomID == null
                || roomID.equals(other.roomID);
    }

    @Override
    public String toString() {
        return "entity.Room[roomID="
                + roomID + "]";
    }
}
