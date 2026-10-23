/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
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
@Table(name = "USER_BOOKING_CONTROL")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserBookingControl.findAll", query = "SELECT u FROM UserBookingControl u"),
    @NamedQuery(name = "UserBookingControl.findByUserID", query = "SELECT u FROM UserBookingControl u WHERE u.userID = :userID"),
    @NamedQuery(name = "UserBookingControl.findByWindowStartedAt", query = "SELECT u FROM UserBookingControl u WHERE u.windowStartedAt = :windowStartedAt"),
    @NamedQuery(name = "UserBookingControl.findByConsecutiveCancellationCount", query = "SELECT u FROM UserBookingControl u WHERE u.consecutiveCancellationCount = :consecutiveCancellationCount"),
    @NamedQuery(name = "UserBookingControl.findByCancellationCountInWindow", query = "SELECT u FROM UserBookingControl u WHERE u.cancellationCountInWindow = :cancellationCountInWindow"),
    @NamedQuery(name = "UserBookingControl.findByPenaltyLevel", query = "SELECT u FROM UserBookingControl u WHERE u.penaltyLevel = :penaltyLevel"),
    @NamedQuery(name = "UserBookingControl.findByBookingLockedUntil", query = "SELECT u FROM UserBookingControl u WHERE u.bookingLockedUntil = :bookingLockedUntil"),
    @NamedQuery(name = "UserBookingControl.findByUpdatedAt", query = "SELECT u FROM UserBookingControl u WHERE u.updatedAt = :updatedAt")})
public class UserBookingControl implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Column(name = "UserID")
    private Integer userID;
    @Column(name = "WindowStartedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date windowStartedAt;
    @Basic(optional = false)
    @NotNull
    @Column(name = "ConsecutiveCancellationCount")
    private int consecutiveCancellationCount;
    @Basic(optional = false)
    @NotNull
    @Column(name = "CancellationCountInWindow")
    private int cancellationCountInWindow;
    @Basic(optional = false)
    @NotNull
    @Column(name = "PenaltyLevel")
    private short penaltyLevel;
    @Column(name = "BookingLockedUntil")
    @Temporal(TemporalType.TIMESTAMP)
    private Date bookingLockedUntil;
    @Basic(optional = false)
    @NotNull
    @Column(name = "UpdatedAt")
    @Temporal(TemporalType.TIMESTAMP)
    private Date updatedAt;
    @JoinColumn(name = "UserID", referencedColumnName = "UserID", insertable = false, updatable = false)
    @OneToOne(optional = false)
    private Users users;

    public UserBookingControl() {
    }

    public UserBookingControl(Integer userID) {
        this.userID = userID;
    }

    public UserBookingControl(Integer userID, int consecutiveCancellationCount, int cancellationCountInWindow, short penaltyLevel, Date updatedAt) {
        this.userID = userID;
        this.consecutiveCancellationCount = consecutiveCancellationCount;
        this.cancellationCountInWindow = cancellationCountInWindow;
        this.penaltyLevel = penaltyLevel;
        this.updatedAt = updatedAt;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Date getWindowStartedAt() {
        return windowStartedAt;
    }

    public void setWindowStartedAt(Date windowStartedAt) {
        this.windowStartedAt = windowStartedAt;
    }

    public int getConsecutiveCancellationCount() {
        return consecutiveCancellationCount;
    }

    public void setConsecutiveCancellationCount(int consecutiveCancellationCount) {
        this.consecutiveCancellationCount = consecutiveCancellationCount;
    }

    public int getCancellationCountInWindow() {
        return cancellationCountInWindow;
    }

    public void setCancellationCountInWindow(int cancellationCountInWindow) {
        this.cancellationCountInWindow = cancellationCountInWindow;
    }

    public short getPenaltyLevel() {
        return penaltyLevel;
    }

    public void setPenaltyLevel(short penaltyLevel) {
        this.penaltyLevel = penaltyLevel;
    }

    public Date getBookingLockedUntil() {
        return bookingLockedUntil;
    }

    public void setBookingLockedUntil(Date bookingLockedUntil) {
        this.bookingLockedUntil = bookingLockedUntil;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Users getUsers() {
        return users;
    }

    public void setUsers(Users users) {
        this.users = users;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userID != null ? userID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserBookingControl)) {
            return false;
        }
        UserBookingControl other = (UserBookingControl) object;
        if ((this.userID == null && other.userID != null) || (this.userID != null && !this.userID.equals(other.userID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.UserBookingControl[ userID=" + userID + " ]";
    }

}
