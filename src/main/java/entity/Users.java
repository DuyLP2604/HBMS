package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

@Entity
@Table(name = "USERS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Users.findAll",
        query = "SELECT u FROM Users u"
    ),
    @NamedQuery(
        name = "Users.findByUserID",
        query = "SELECT u FROM Users u "
              + "WHERE u.userID = :userID"
    ),
    @NamedQuery(
        name = "Users.findByUsername",
        query = "SELECT u FROM Users u "
              + "WHERE u.username = :username"
    ),
    @NamedQuery(
        name = "Users.findByRole",
        query = "SELECT u FROM Users u "
              + "WHERE u.role = :role"
    )
})
public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(
        name = "UserID",
        nullable = false
    )
    private Integer userID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(
        name = "Username",
        nullable = false,
        unique = true,
        length = 50
    )
    private String username;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 255)
    @Column(
        name = "Password",
        nullable = false,
        length = 255
    )
    private String password;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 20)
    @Column(
        name = "Role",
        nullable = false,
        length = 20
    )
    private String role;

    /*
     * mappedBy trỏ tới thuộc tính userID
     * trong Employee.java.
     */
    @OneToOne(mappedBy = "userID")
    private Employee employee;

    /*
     * mappedBy trỏ tới thuộc tính userID
     * trong Customer.java.
     */
    @OneToOne(mappedBy = "userID")
    private Customer customer;

    public Users() {
    }

    public Users(Integer userID) {
        this.userID = userID;
    }

    public Users(
            String username,
            String password,
            String role) {

        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Users(
            Integer userID,
            String username,
            String password,
            String role) {

        this.userID = userID;
        this.username = username;
        this.password = password;
        this.role = role;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    @Override
    public int hashCode() {
        return userID != null ? userID.hashCode() : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Users)) {
            return false;
        }

        Users other = (Users) object;

        if (userID == null && other.userID != null) {
            return false;
        }

        return userID == null
                || userID.equals(other.userID);
    }

    @Override
    public String toString() {
        return "entity.Users[userID="
                + userID + "]";
    }
}