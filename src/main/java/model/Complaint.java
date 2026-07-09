package model;

import java.sql.Timestamp;

public class Complaint {

    private int id;
    private String title;
    private String content;
    private Timestamp createdAt;
    private String status;
    private String customerName; // Dùng để hiển thị tên khách hàng thay vì chỉ hiển thị ID

    public Complaint() {
    }

    public Complaint(int id, String title, String content, Timestamp createdAt, String status, String customerName) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.status = status;
        this.customerName = customerName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }
}
