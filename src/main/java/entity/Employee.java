package entity;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlTransient;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;

@Entity
@Table(name = "EMPLOYEE")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Employee.findAll",
        query = "SELECT e FROM Employee e"
    ),
    @NamedQuery(
        name = "Employee.findByEmployeeID",
        query = "SELECT e FROM Employee e "
              + "WHERE e.employeeID = :employeeID"
    ),
    @NamedQuery(
        name = "Employee.findByFullName",
        query = "SELECT e FROM Employee e "
              + "WHERE e.fullName = :fullName"
    ),
    @NamedQuery(
        name = "Employee.findByPosition",
        query = "SELECT e FROM Employee e "
              + "WHERE e.position = :position"
    ),
    @NamedQuery(
        name = "Employee.findByShift",
        query = "SELECT e FROM Employee e "
              + "WHERE e.shift = :shift"
    ),
    @NamedQuery(
        name = "Employee.findByPhone",
        query = "SELECT e FROM Employee e "
              + "WHERE e.phone = :phone"
    )
})
public class Employee implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 6)
    @Column(
        name = "EmployeeID",
        nullable = false,
        length = 6
    )
    private String employeeID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 100)
    @Column(
        name = "FullName",
        nullable = false,
        length = 100
    )
    private String fullName;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 50)
    @Column(
        name = "Position",
        nullable = false,
        length = 50
    )
    private String position;

    @DecimalMin(value = "0.0", inclusive = true)
    @Column(
        name = "Salary",
        precision = 12,
        scale = 2
    )
    private BigDecimal salary;

    @Size(max = 20)
    @Column(name = "Shift", length = 20)
    private String shift;

    @Size(max = 200)
    @Column(name = "Address", length = 200)
    private String address;

    @Size(max = 20)
    @Column(
        name = "Phone",
        unique = true,
        length = 20
    )
    private String phone;

    /*
     * EMPLOYEE.UserID có UNIQUE constraint,
     * vì vậy đây là quan hệ một-một.
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
     * Không dùng CascadeType.ALL.
     * Xóa employee không được xóa lịch sử hóa đơn.
     */
    @OneToMany(mappedBy = "employeeID")
    private Collection<Invoice> invoiceCollection;

    /*
     * Một nhân viên có thể thực hiện nhiều lần phân phòng.
     */
    @OneToMany(mappedBy = "employeeID")
    private Collection<RoomAssignment> roomAssignmentCollection;

    public Employee() {
    }

    public Employee(String employeeID) {
        this.employeeID = employeeID;
    }

    public Employee(
            String employeeID,
            String fullName,
            String position,
            BigDecimal salary,
            String shift,
            String address,
            String phone,
            Users userID) {

        this.employeeID = employeeID;
        this.fullName = fullName;
        this.position = position;
        this.salary = salary;
        this.shift = shift;
        this.address = address;
        this.phone = phone;
        this.userID = userID;
    }

    public String getEmployeeID() {
        return employeeID;
    }

    public void setEmployeeID(String employeeID) {
        this.employeeID = employeeID;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public String getShift() {
        return shift;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Users getUserID() {
        return userID;
    }

    public void setUserID(Users userID) {
        this.userID = userID;
    }

    @XmlTransient
    public Collection<Invoice> getInvoiceCollection() {
        return invoiceCollection;
    }

    public void setInvoiceCollection(
            Collection<Invoice> invoiceCollection) {

        this.invoiceCollection = invoiceCollection;
    }

    @XmlTransient
    public Collection<RoomAssignment>
            getRoomAssignmentCollection() {

        return roomAssignmentCollection;
    }

    public void setRoomAssignmentCollection(
            Collection<RoomAssignment>
                    roomAssignmentCollection) {

        this.roomAssignmentCollection =
                roomAssignmentCollection;
    }

    @Override
    public int hashCode() {
        return employeeID != null
                ? employeeID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Employee)) {
            return false;
        }

        Employee other = (Employee) object;

        if (employeeID == null
                && other.employeeID != null) {
            return false;
        }

        return employeeID == null
                || employeeID.equals(other.employeeID);
    }

    @Override
    public String toString() {
        return "entity.Employee[employeeID="
                + employeeID + "]";
    }
}