package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.io.Serializable;

@Embeddable
public class BookingServicePK implements Serializable {

    private static final long serialVersionUID = 1L;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(
        name = "BookingID",
        nullable = false,
        length = 6
    )
    private String bookingID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(
        name = "ServiceID",
        nullable = false,
        length = 4
    )
    private String serviceID;

    public BookingServicePK() {
    }

    public BookingServicePK(
            String bookingID,
            String serviceID) {

        this.bookingID = bookingID;
        this.serviceID = serviceID;
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    @Override
    public int hashCode() {
        int hash = 0;

        hash += bookingID != null
                ? bookingID.hashCode()
                : 0;

        hash += serviceID != null
                ? serviceID.hashCode()
                : 0;

        return hash;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof BookingServicePK)) {
            return false;
        }

        BookingServicePK other =
                (BookingServicePK) object;

        if (bookingID == null
                && other.bookingID != null) {
            return false;
        }

        if (bookingID != null
                && !bookingID.equals(other.bookingID)) {
            return false;
        }

        if (serviceID == null
                && other.serviceID != null) {
            return false;
        }

        return serviceID == null
                || serviceID.equals(other.serviceID);
    }

    @Override
    public String toString() {
        return "BookingServicePK[bookingID="
                + bookingID
                + ", serviceID="
                + serviceID
                + "]";
    }
}