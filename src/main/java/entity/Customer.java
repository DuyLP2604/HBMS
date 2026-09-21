package entity;

import jakarta.persistence.Basic;
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
@NamedQueries({
    @NamedQuery(
        name = "Customer.findAll",
        query = "SELECT c FROM Customer c"
    ),
    @NamedQuery(
        name = "Customer.findByCustomerID",
        query = "SELECT c FROM Customer c "
              + "WHERE c.customerID = :customerID"
    ),
    @NamedQuery(
        name = "Customer.findByFullName",
        query = "SELECT c FROM Customer c "
              + "WHERE c.fullName = :fullName"
    ),
    @NamedQuery(
        name = "Customer.findByPhone",
        query = "SELECT c FROM Customer c "
              + "WHERE c.phone = :phone"
    ),
    @NamedQuery(
        name = "Customer.findByEmail",
        query = "SELECT c FROM Customer c "
              + "WHERE c.email = :email"
    ),
    @NamedQuery(
        name = "Customer.findByCccd",
        query = "SELECT c FROM Customer c "
              + "WHERE c.cccd = :cccd"
    ),
    @NamedQuery(
        name = "Customer.findByPassportNumber",
        query = "SELECT c FROM Customer c "
              + "WHERE c.passportNumber = :passportNumber"
    )
})
public class Customer implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(
        name = "CustomerID",
        nullable = false,
        length = 6
    )
    private String customerID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(
        name = "FullName",
        nullable = false,
        length = 100
    )
    private String fullName;

    @Size(max = 15)
    @Column(name = "Phone", length = 15)
    private String phone;

    @Size(max = 100)
    @Column(
        name = "Email",
        unique = true,
        length = 100
    )
    private String email;

    @Size(max = 200)
    @Column(name = "Address", length = 200)
    private String address;

    @Size(max = 20)
    @Column(name = "CCCD", length = 20)
    private String cccd;

    @Size(max = 30)
    @Column(
        name = "PassportNumber",
        length = 30
    )
    private String passportNumber;

    @JoinColumn(
        name = "NationalityID",
        referencedColumnName = "NationalityID",
        nullable = true
    )
    @ManyToOne(optional = true)
    private Nationality nationalityID;

    /*
     * CUSTOMER.UserID có UNIQUE và NOT NULL,
     * do đó đây là quan hệ một-một.
     */
    @JoinColumn(
        name = "UserID",
        referencedColumnName = "UserID",
        nullable = false,
        unique = true
    )
    @OneToOne(optional = false)
    private Users userID;

    /*
     * Không dùng cascade delete để giữ lịch sử nghiệp vụ.
     */
    @OneToMany(mappedBy = "customerID")
    private Collection<Complaint> complaintCollection;

    @OneToMany(mappedBy = "customerID")
    private Collection<Booking> bookingCollection;

    public Customer() {
    }

    public Customer(String customerID) {
        this.customerID = customerID;
    }

    public Customer(
            String customerID,
            String fullName,
            String phone,
            String email,
            String address,
            String cccd,
            String passportNumber,
            Nationality nationalityID,
            Users userID) {

        this.customerID = customerID;
        this.fullName = fullName;
        this.phone = phone;
        this.email = email;
        this.address = address;
        this.cccd = cccd;
        this.passportNumber = passportNumber;
        this.nationalityID = nationalityID;
        this.userID = userID;
    }

    public String getCustomerID() {
        return customerID;
    }

    public void setCustomerID(String customerID) {
        this.customerID = customerID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCccd() {
        return cccd;
    }

    public void setCccd(String cccd) {
        this.cccd = cccd;
    }

    public String getPassportNumber() {
        return passportNumber;
    }

    public void setPassportNumber(
            String passportNumber) {

        this.passportNumber = passportNumber;
    }

    public Nationality getNationalityID() {
        return nationalityID;
    }

    public void setNationalityID(
            Nationality nationalityID) {

        this.nationalityID = nationalityID;
    }

    public Users getUserID() {
        return userID;
    }

    public void setUserID(Users userID) {
        this.userID = userID;
    }

    @XmlTransient
    public Collection<Complaint>
            getComplaintCollection() {

        return complaintCollection;
    }

    public void setComplaintCollection(
            Collection<Complaint> complaintCollection) {

        this.complaintCollection = complaintCollection;
    }

    @XmlTransient
    public Collection<Booking> getBookingCollection() {
        return bookingCollection;
    }

    public void setBookingCollection(
            Collection<Booking> bookingCollection) {

        this.bookingCollection = bookingCollection;
    }

    @Override
    public int hashCode() {
        return customerID != null
                ? customerID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Customer)) {
            return false;
        }

        Customer other = (Customer) object;

        if (customerID == null
                && other.customerID != null) {
            return false;
        }

        return customerID == null
                || customerID.equals(other.customerID);
    }

    @Override
    public String toString() {
        return "entity.Customer[customerID="
                + customerID + "]";
    }
}