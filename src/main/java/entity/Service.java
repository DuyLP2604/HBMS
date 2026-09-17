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
import java.math.BigDecimal;
import jakarta.persistence.OneToMany;
import jakarta.xml.bind.annotation.XmlTransient;
import java.util.Collection;

@Entity
@Table(name = "SERVICE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
            name = "Service.findAll",
            query = "SELECT s FROM Service s"
    ),
    @NamedQuery(
            name = "Service.findByServiceID",
            query = "SELECT s FROM Service s "
            + "WHERE s.serviceID = :serviceID"
    ),
    @NamedQuery(
            name = "Service.findByServiceName",
            query = "SELECT s FROM Service s "
            + "WHERE s.serviceName = :serviceName"
    ),
    @NamedQuery(
            name = "Service.findByUnitPrice",
            query = "SELECT s FROM Service s "
            + "WHERE s.unitPrice = :unitPrice"
    )
})
public class Service implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 4)
    @Column(
            name = "ServiceID",
            nullable = false,
            length = 4
    )
    private String serviceID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 60)
    @Column(
            name = "ServiceName",
            nullable = false,
            unique = true,
            length = 60
    )
    private String serviceName;

    @Basic(optional = false)
    @NotNull
    @Column(
            name = "UnitPrice",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal unitPrice;

    @OneToMany(mappedBy = "serviceID")
    private Collection<BookingService> bookingServiceCollection;

    public Service() {
    }

    public Service(String serviceID) {
        this.serviceID = serviceID;
    }

    public Service(
            String serviceID,
            String serviceName,
            BigDecimal unitPrice) {

        this.serviceID = serviceID;
        this.serviceName = serviceName;
        this.unitPrice = unitPrice;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        this.unitPrice = unitPrice;
    }

    @XmlTransient
    public Collection<BookingService>
            getBookingServiceCollection() {

        return bookingServiceCollection;
    }

    public void setBookingServiceCollection(
            Collection<BookingService> bookingServiceCollection) {

        this.bookingServiceCollection
                = bookingServiceCollection;
    }

    @Override
    public int hashCode() {
        return serviceID != null
                ? serviceID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Service)) {
            return false;
        }

        Service other = (Service) object;

        if (serviceID == null && other.serviceID != null) {
            return false;
        }

        return serviceID == null
                || serviceID.equals(other.serviceID);
    }

    @Override
    public String toString() {
        return "entity.Service[serviceID="
                + serviceID + "]";
    }
}
