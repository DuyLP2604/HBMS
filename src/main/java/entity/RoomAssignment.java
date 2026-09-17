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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "ROOM_ASSIGNMENT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "RoomAssignment.findAll",
        query = "SELECT ra FROM RoomAssignment ra"
    ),
    @NamedQuery(
        name = "RoomAssignment.findByAssignmentID",
        query = "SELECT ra FROM RoomAssignment ra "
              + "WHERE ra.assignmentID = :assignmentID"
    ),
    @NamedQuery(
        name = "RoomAssignment.findByBookingDetail",
        query = "SELECT ra FROM RoomAssignment ra "
              + "WHERE ra.bookingDetailID = :bookingDetailID"
    ),
    @NamedQuery(
        name = "RoomAssignment.findByRoom",
        query = "SELECT ra FROM RoomAssignment ra "
              + "WHERE ra.roomID = :roomID"
    ),
    @NamedQuery(
        name = "RoomAssignment.findByEmployee",
        query = "SELECT ra FROM RoomAssignment ra "
              + "WHERE ra.employeeID = :employeeID"
    )
})
public class RoomAssignment implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(
        name = "AssignmentID",
        nullable = false
    )
    private Integer assignmentID;

    @Basic(optional = false)
    @NotNull
    @Column(name = "AssignedAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date assignedAt;

    @JoinColumn(
        name = "BookingDetailID",
        referencedColumnName = "BookingDetailID",
        nullable = false
    )
    @ManyToOne(optional = false)
    private BookingDetail bookingDetailID;

    @JoinColumn(
        name = "RoomID",
        referencedColumnName = "RoomID",
        nullable = false
    )
    @ManyToOne(optional = false)
    private Room roomID;

    @JoinColumn(
        name = "EmployeeID",
        referencedColumnName = "EmployeeID",
        nullable = true
    )
    @ManyToOne(optional = true)
    private Employee employeeID;

    public RoomAssignment() {
    }

    public RoomAssignment(Integer assignmentID) {
        this.assignmentID = assignmentID;
    }

    public RoomAssignment(
            BookingDetail bookingDetailID,
            Room roomID,
            Employee employeeID) {

        this.bookingDetailID = bookingDetailID;
        this.roomID = roomID;
        this.employeeID = employeeID;
        this.assignedAt = new Date();
    }

    @PrePersist
    private void prePersist() {
        if (assignedAt == null) {
            assignedAt = new Date();
        }
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

    public void setBookingDetailID(
            BookingDetail bookingDetailID) {

        this.bookingDetailID = bookingDetailID;
    }

    public Room getRoomID() {
        return roomID;
    }

    public void setRoomID(Room roomID) {
        this.roomID = roomID;
    }

    public Employee getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(Employee employeeID) {
        this.employeeID = employeeID;
    }

    @Override
    public int hashCode() {
        return assignmentID != null
                ? assignmentID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof RoomAssignment)) {
            return false;
        }

        RoomAssignment other = (RoomAssignment) object;

        if (assignmentID == null
                && other.assignmentID != null) {
            return false;
        }

        return assignmentID == null
                || assignmentID.equals(other.assignmentID);
    }

    @Override
    public String toString() {
        return "entity.RoomAssignment[assignmentID="
                + assignmentID + "]";
    }
}