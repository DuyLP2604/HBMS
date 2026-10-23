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
import jakarta.persistence.OneToOne;
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
@Table(name = "BOOKING_LOCK_HISTORY")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "BookingLockHistory.findAll", query = "SELECT b FROM BookingLockHistory b"),
    @NamedQuery(name = "BookingLockHistory.findByLockHistoryID", query = "SELECT b FROM BookingLockHistory b WHERE b.lockHistoryID = :lockHistoryID"),
    @NamedQuery(name = "BookingLockHistory.findByPenaltyLevel", query = "SELECT b FROM BookingLockHistory b WHERE b.penaltyLevel = :penaltyLevel"),
    @NamedQuery(name = "BookingLockHistory.findByLockedAt", query = "SELECT b FROM BookingLockHistory b WHERE b.lockedAt = :lockedAt"),
    @NamedQuery(name = "BookingLockHistory.findByLockedUntil", query = "SELECT b FROM BookingLockHistory b WHERE b.lockedUntil = :lockedUntil"),
    @NamedQuery(name = "BookingLockHistory.findByDurationMinutes", query = "SELECT b FROM BookingLockHistory b WHERE b.durationMinutes = :durationMinutes")})
public class BookingLockHistory implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "LockHistoryID")
    private Long lockHistoryID;
    @Basic(optional = false)
    @NotNull
    @Column(name = "PenaltyLevel")
    private short penaltyLevel;
    @Basic(optional = false)
    @NotNull
    @Column(name = "LockedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "LockedUntil")
    @Temporal(TemporalType.TIMESTAMP)
    private Date lockedUntil;
    @Basic(optional = false)
    @NotNull
    @Column(name = "DurationMinutes")
    private int durationMinutes;
    @JoinColumn(name = "TriggerBookingID", referencedColumnName = "BookingID")
    @OneToOne(optional = false)
    private Booking triggerBookingID;
    @JoinColumn(name = "UserID", referencedColumnName = "UserID")
    @ManyToOne(optional = false)
    private Users userID;

    public BookingLockHistory() {
    }

    public BookingLockHistory(Long lockHistoryID) {
        this.lockHistoryID = lockHistoryID;
    }

    public BookingLockHistory(Long lockHistoryID, short penaltyLevel, Date lockedAt, Date lockedUntil, int durationMinutes) {
        this.lockHistoryID = lockHistoryID;
        this.penaltyLevel = penaltyLevel;
        this.lockedAt = lockedAt;
        this.lockedUntil = lockedUntil;
        this.durationMinutes = durationMinutes;
    }

    public Long getLockHistoryID() {
        return lockHistoryID;
    }

    public void setLockHistoryID(Long lockHistoryID) {
        this.lockHistoryID = lockHistoryID;
    }

    public short getPenaltyLevel() {
        return penaltyLevel;
    }

    public void setPenaltyLevel(short penaltyLevel) {
        this.penaltyLevel = penaltyLevel;
    }

    public Date getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(Date lockedAt) {
        this.lockedAt = lockedAt;
    }

    public Date getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(Date lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public int getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(int durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Booking getTriggerBookingID() {
        return triggerBookingID;
    }

    public void setTriggerBookingID(Booking triggerBookingID) {
        this.triggerBookingID = triggerBookingID;
    }

    public Users getUserID() {
        return userID;
    }

    public void setUserID(Users userID) {
        this.userID = userID;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (lockHistoryID != null ? lockHistoryID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof BookingLockHistory)) {
            return false;
        }
        BookingLockHistory other = (BookingLockHistory) object;
        if ((this.lockHistoryID == null && other.lockHistoryID != null) || (this.lockHistoryID != null && !this.lockHistoryID.equals(other.lockHistoryID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.BookingLockHistory[ lockHistoryID=" + lockHistoryID + " ]";
    }

}
