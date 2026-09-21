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
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "COMPLAINT")
@XmlRootElement
@NamedQueries({
    @NamedQuery(
        name = "Complaint.findAll",
        query = "SELECT c FROM Complaint c"
    ),
    @NamedQuery(
        name = "Complaint.findByComplaintID",
        query = "SELECT c FROM Complaint c "
              + "WHERE c.complaintID = :complaintID"
    ),
    @NamedQuery(
        name = "Complaint.findByTitle",
        query = "SELECT c FROM Complaint c "
              + "WHERE c.title = :title"
    ),
    @NamedQuery(
        name = "Complaint.findByCreatedAt",
        query = "SELECT c FROM Complaint c "
              + "WHERE c.createdAt = :createdAt"
    ),
    @NamedQuery(
        name = "Complaint.findByStatus",
        query = "SELECT c FROM Complaint c "
              + "WHERE c.status = :status"
    ),
    @NamedQuery(
        name = "Complaint.findByCustomer",
        query = "SELECT c FROM Complaint c "
              + "WHERE c.customerID = :customerID "
              + "ORDER BY c.createdAt DESC"
    )
})
public class Complaint implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(
        name = "ComplaintID",
        nullable = false
    )
    private Integer complaintID;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 200)
    @Column(
        name = "Title",
        nullable = false,
        length = 200
    )
    private String title;

    /*
     * Database sử dụng NVARCHAR(MAX).
     * Không dùng @Size(max = 2147483647).
     */
    @Basic(optional = false)
    @NotNull
    @Column(
        name = "Content",
        nullable = false,
        columnDefinition = "NVARCHAR(MAX)"
    )
    private String content;

    @Basic(optional = false)
    @NotNull
    @Column(name = "CreatedAt", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date createdAt;

    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 30)
    @Column(
        name = "Status",
        nullable = false,
        length = 30
    )
    private String status = "Chưa xử lý";

    /*
     * CustomerID cho phép NULL trong database.
     */
    @JoinColumn(
        name = "CustomerID",
        referencedColumnName = "CustomerID",
        nullable = true
    )
    @ManyToOne(optional = true)
    private Customer customerID;

    public Complaint() {
    }

    public Complaint(Integer complaintID) {
        this.complaintID = complaintID;
    }

    public Complaint(
            String title,
            String content,
            Customer customerID) {

        this.title = title;
        this.content = content;
        this.customerID = customerID;
        this.status = "Chưa xử lý";
        this.createdAt = new Date();
    }

    @PrePersist
    private void prePersist() {
        if (createdAt == null) {
            createdAt = new Date();
        }

        if (status == null || status.isBlank()) {
            status = "Chưa xử lý";
        }
    }

    public Integer getComplaintID() {
        return complaintID;
    }

    public void setComplaintID(Integer complaintID) {
        this.complaintID = complaintID;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Customer getCustomerID() {
        return customerID;
    }

    public void setCustomerID(Customer customerID) {
        this.customerID = customerID;
    }

    @Override
    public int hashCode() {
        return complaintID != null
                ? complaintID.hashCode()
                : 0;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof Complaint)) {
            return false;
        }

        Complaint other = (Complaint) object;

        if (complaintID == null
                && other.complaintID != null) {
            return false;
        }

        return complaintID == null
                || complaintID.equals(
                        other.complaintID
                );
    }

    @Override
    public String toString() {
        return "entity.Complaint[complaintID="
                + complaintID + "]";
    }
}