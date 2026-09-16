/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
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

/**
 *
 * @author Asus
 */
@Entity
@Table(name = "Nationality")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Nationality.findAll", query = "SELECT n FROM Nationality n"),
    @NamedQuery(name = "Nationality.findByNationalityID", query = "SELECT n FROM Nationality n WHERE n.nationalityID = :nationalityID"),
    @NamedQuery(name = "Nationality.findByNationalityName", query = "SELECT n FROM Nationality n WHERE n.nationalityName = :nationalityName")})
public class Nationality implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "NationalityID")
    private String nationalityID;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(name = "NationalityName")
    private String nationalityName;
    @OneToMany(mappedBy = "nationalityID")
    private Collection<Customer> customerCollection;

    public Nationality() {
    }

    public Nationality(String nationalityID) {
        this.nationalityID = nationalityID;
    }

    public Nationality(String nationalityID, String nationalityName) {
        this.nationalityID = nationalityID;
        this.nationalityName = nationalityName;
    }

    public String getNationalityID() {
        return nationalityID;
    }

    public void setNationalityID(String nationalityID) {
        this.nationalityID = nationalityID;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    public void setNationalityName(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    @XmlTransient
    public Collection<Customer> getCustomerCollection() {
        return customerCollection;
    }

    public void setCustomerCollection(Collection<Customer> customerCollection) {
        this.customerCollection = customerCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (nationalityID != null ? nationalityID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Nationality)) {
            return false;
        }
        Nationality other = (Nationality) object;
        if ((this.nationalityID == null && other.nationalityID != null) || (this.nationalityID != null && !this.nationalityID.equals(other.nationalityID))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "entity.Nationality[ nationalityID=" + nationalityID + " ]";
    }

}
