/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

/**
 *
 * @author Admin
 */
public class invoice {
    private String invoidId; 
    private String invoiceDate;
    private double totalAmount;
    private String hotelID;
    private String hotelName;
    private String customerName;
    private String customerId; 
    private String roomNumber;
    private String bookingId;
    private String employeeId;
    private String serviceName;
    private String nationalityName;
    public invoice() {
        
    }

    public invoice(String invoidId, String invoiceDate, double totalAmount, String hotelID, String hotelName, String customerName, String customerId, String roomNumber, String bookingId, String employeeId, String serviceName, String nationalityName) {
        this.invoidId = invoidId;
        this.invoiceDate = invoiceDate;
        this.totalAmount = totalAmount;
        this.hotelID = hotelID;
        this.hotelName = hotelName;
        this.customerName = customerName;
        this.customerId = customerId;
        this.roomNumber = roomNumber;
        this.bookingId = bookingId;
        this.employeeId = employeeId;
        this.serviceName = serviceName;
        this.nationalityName = nationalityName;
    }

    public String getInvoidId() {
        return invoidId;
    }

    public void setInvoidId(String invoidId) {
        this.invoidId = invoidId;
    }


    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public String getHotelID() {
        return hotelID;
    }

    public void setHotelID(String hotelID) {
        this.hotelID = hotelID;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getBookingId() {
        return bookingId;
    }

    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }

    public String getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(String employeeId) {
        this.employeeId = employeeId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getNationalityName() {
        return nationalityName;
    }

    public void setNationalityName(String nationalityName) {
        this.nationalityName = nationalityName;
    }

    @Override
    public String toString() {
        return "invoice{" + "invoidId=" + invoidId + ", invoiceDate=" + invoiceDate + ", totalAmount=" + totalAmount + ", hotelID=" + hotelID + ", customerName=" + customerName + ", customerId=" + customerId + ", bookingId=" + bookingId + ", employeeId=" + employeeId + ", serviceName=" + serviceName + ", nationalityName=" + nationalityName + '}';
    }
    
}
