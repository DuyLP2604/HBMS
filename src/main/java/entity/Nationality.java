package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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

@Entity
@Table(name = "NATIONALITY")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Nationality.findAll",
        query = "SELECT n FROM Nationality n"
    ),
    @NamedQuery(
        name = "Nationality.findByNationalityID",
        query = "SELECT n FROM Nationality n "
              + "WHERE n.nationalityID = :nationalityID"
    ),
    @NamedQuery(
        name = "Nationality.findByNationalityName",
        query = "SELECT n FROM Nationality n "
              + "WHERE n.nationalityName = :nationalityName"
    )
})
public class Nationality implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(
        name = "NationalityID",
        nullable = false,
        length = 50
    )
    private String nationalityID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(
        name = "NationalityName",
        nullable = false,
        unique = true,
        length = 100
    )
    private String nationalityName;

    @OneToMany(mappedBy = "nationalityID")
    private Collection<Customer> customerCollection;

    public Nationality() {
    }

    public Nationality(String nationalityID) {
        this.nationalityID = nationalityID;
    }

    public Nationality(
            String nationalityID,
            String nationalityName) {

        this.nationalityID = nationalityID;
        this.nationalityName = nationalityName;
    }

    public String getNationalityID() {
        return nationalityID;
    }

    public void setNationalityID(
            String nationalityID) {

        this.nationalityID = nationalityID;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    public void setNationalityName(
            String nationalityName) {

        this.nationalityName = nationalityName;
    }

    @XmlTransient
    public Collection<Customer> getCustomerCollection() {
        return customerCollection;
    }

    public void setCustomerCollection(
            Collection<Customer> customerCollection) {

        this.customerCollection = customerCollection;
    }

    @Override
    public int hashCode() {
        return nationalityID != null
                ? nationalityID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Nationality)) {
            return false;
        }

        Nationality other = (Nationality) object;

        if (nationalityID == null
                && other.nationalityID != null) {
            return false;
        }

        return nationalityID == null
                || nationalityID.equals(
                        other.nationalityID
                );
    }

    @Override
    public String toString() {
        return "entity.Nationality[nationalityID="
                + nationalityID + "]";
    }
}