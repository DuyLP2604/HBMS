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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.util.Collection;
@Entity
@Table(name = "CUSTOMER")
@XmlRootElement
@NamedQueries({@NamedQuery(name = "Customer.findAll", query = "SELECT c FROM Customer c"), @NamedQuery(name = "Customer.findByCustomerID", query = "SELECT c FROM Customer c WHERE c.customerID = :customerID"), @NamedQuery(name = "Customer.findByFullName", query = "SELECT c FROM Customer c WHERE c.fullName = :fullName"), @NamedQuery(name = "Customer.findByPhone", query = "SELECT c FROM Customer c WHERE c.phone = :phone"), @NamedQuery(name = "Customer.findByEmail", query = "SELECT c FROM Customer c WHERE c.email = :email"), @NamedQuery(name = "Customer.findByAddress", query = "SELECT c FROM Customer c WHERE c.address = :address")})
public class Customer implements Serializable
{
    private static final long serialVersionUID = 1L;
    @Id @Basic(optional = false) @NotNull @Size(min = 1, max = 6) @Column(name = "CustomerID")
    private String customerID;
    @Basic(optional = false) @NotNull @Size(min = 1, max = 100) @Column(name = "FullName")
    private String fullName;
    @Size(max = 15) @Column(name = "Phone")
    private String phone;
    @Size(max = 100) @Column(name = "Email")
    private String email;
    @Size(max = 200) @Column(name = "Address")
    private String address;
    @JoinColumn(name = "NationalityID", referencedColumnName = "NationalityID") @ManyToOne
    private Nationality nationalityID;
    @JoinColumn(name = "UserID", referencedColumnName = "UserID") @OneToOne(optional = false)
    private Users userID;
    @OneToMany(mappedBy = "customerID")
    private Collection<Complaint> complaintCollection;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "customerID")
    private Collection<Booking> bookingCollection;
    public Customer()
    {
    }

    public Customer(String customerID)
    {
        this.customerID = customerID;
    }

    public Customer(String customerID, String fullName)
    {
        this.customerID = customerID;
        this.fullName = fullName;
    }

    public String getCustomerID()
    {
        return customerID;
    }

    public void setCustomerID(String customerID)
    {
        this.customerID = customerID;
    }

    public String getFullName()
    {
        return fullName;
    }

    public void setFullName(String fullName)
    {
        this.fullName = fullName;
    }

    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getEmail()
    {
        return email;
    }

    public void setEmail(String email)
    {
        this.email = email;
    }

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String address)
    {
        this.address = address;
    }

    public Nationality getNationalityID()
    {
        return nationalityID;
    }

    public void setNationalityID(Nationality nationalityID)
    {
        this.nationalityID = nationalityID;
    }

    public Users getUserID()
    {
        return userID;
    }

    public void setUserID(Users userID)
    {
        this.userID = userID;
    }

    @XmlTransient
    public Collection<Complaint> getComplaintCollection()
    {
        return complaintCollection;
    }

    public void setComplaintCollection(Collection<Complaint> complaintCollection)
    {
        this.complaintCollection = complaintCollection;
    }

    @XmlTransient
    public Collection<Booking> getBookingCollection()
    {
        return bookingCollection;
    }

    public void setBookingCollection(Collection<Booking> bookingCollection)
    {
        this.bookingCollection = bookingCollection;
    }

    @Override
    public int hashCode()
    {
        int hash = 0;
        hash += (customerID != null ? customerID.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        if (!(object instanceof Customer))
        {
            return false;
        }
        Customer other = (Customer) object;
        if ((this.customerID == null && other.customerID != null) || (this.customerID != null && !this.customerID.equals(other.customerID)))
        {
            return false;
        }
        return true;
    }

    @Override
    public String toString()
    {
        return "entity.Customer[ customerID=" + customerID + " ]";
    }
}