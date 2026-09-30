/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "ROOM_ASSIGNMENT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "RoomAssignment.findAll", query = "SELECT r FROM RoomAssignment r"),
    @NamedQuery(name = "RoomAssignment.findByAssignmentID", query = "SELECT r FROM RoomAssignment r WHERE r.assignmentID = :assignmentID"),
    @NamedQuery(name = "RoomAssignment.findByAssignedAt", query = "SELECT r FROM RoomAssignment r WHERE r.assignedAt = :assignedAt")})
public class RoomAssignment implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "AssignmentID")
    private Integer assignmentID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "AssignedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;
    @JoinColumn(name = "BookingDetailID", referencedColumnName = "BookingDetailID")
    @ManyToOne(optional = false)
    private BookingDetail bookingDetailID;
    @JoinColumn(name = "EmployeeID", referencedColumnName = "EmployeeID")
    @ManyToOne
    private Employee employeeID;
    @JoinColumn(name = "RoomID", referencedColumnName = "RoomID")
    @ManyToOne(optional = false)
    private Room roomID;

    public RoomAssignment() {
    }

    public RoomAssignment(Integer assignmentID) {
        this.assignmentID = assignmentID;
    }

    public RoomAssignment(Integer assignmentID, Date assignedAt) {
        this.assignmentID = assignmentID;
        this.assignedAt = assignedAt;
    }

    public Integer getAssignmentID() {
        return assignmentID;
    }

    public void setAssignmentID(Integer assignmentID) {
        this.assignmentID = assignmentID;
    }

    public Date getAssignedAt() {
        return assignedAt;
    }

    public void setAssignedAt(Date assignedAt) {
        this.assignedAt = assignedAt;
    }

    public BookingDetail getBookingDetailID() {
        return bookingDetailID;
    }

    public void setBookingDetailID(BookingDetail bookingDetailID) {
        this.bookingDetailID = bookingDetailID;
    }

    public Employee getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Employee employeeID) {
        this.employeeID = employeeID;
    }

    public Room getRoomID() {
        return roomID;
    }

    public void setRoomID(Room roomID) {
        this.roomID = roomID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (assignmentID != null ? assignmentID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof RoomAssignment)) {
            return false;
        }
        RoomAssignment other = (RoomAssignment) object;
        if ((this.assignmentID == null && other.assignmentID != null) || (this.assignmentID != null && !this.assignmentID.equals(other.assignmentID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.RoomAssignment[ assignmentID=" + assignmentID + " ]";
    }

}
