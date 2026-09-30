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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "ROOM")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Room.findAll", query = "SELECT r FROM Room r"),
    @NamedQuery(name = "Room.findByRoomID", query = "SELECT r FROM Room r WHERE r.roomID = :roomID"),
    @NamedQuery(name = "Room.findByRoomNumber", query = "SELECT r FROM Room r WHERE r.roomNumber = :roomNumber"),
    @NamedQuery(name = "Room.findByRoomImage", query = "SELECT r FROM Room r WHERE r.roomImage = :roomImage"),
    @NamedQuery(name = "Room.findByStatus", query = "SELECT r FROM Room r WHERE r.status = :status")})
public class Room implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 3)
    @Column(name = "RoomID")
    private String roomID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "RoomNumber")
    private String roomNumber;
    @Size(max = 100)
    @Column(name = "RoomImage")
    private String roomImage;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(name = "Status")
    private String status;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "roomID")
    private Collection<RoomAssignment> roomAssignmentCollection;
    @JoinColumn(name = "RoomTypeID", referencedColumnName = "RoomTypeID")
    @ManyToOne(optional = false)
    private RoomType roomTypeID;

    public Room() {
    }

    public Room(String roomID) {
        this.roomID = roomID;
    }

    public Room(String roomID, String roomNumber, String status) {
        this.roomID = roomID;
        this.roomNumber = roomNumber;
        this.status = status;
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

    @XmlTransient
    public Collection<RoomAssignment> getRoomAssignmentCollection() {
        return roomAssignmentCollection;
    }

    public void setRoomAssignmentCollection(Collection<RoomAssignment> roomAssignmentCollection) {
        this.roomAssignmentCollection = roomAssignmentCollection;
    }

    public RoomType getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(RoomType roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (roomID != null ? roomID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Room)) {
            return false;
        }
        Room other = (Room) object;
        if ((this.roomID == null && other.roomID != null) || (this.roomID != null && !this.roomID.equals(other.roomID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Room[ roomID=" + roomID + " ]";
    }

}
