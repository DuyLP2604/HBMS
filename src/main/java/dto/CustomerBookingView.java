package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class CustomerBookingView implements Serializable {

    private static final long serialVersionUID = 1L;

    private String bookingID;
    private Date bookingDate;
    private Date checkInDate;
    private Date checkOutDate;
    private long numberOfNights;
    private String bookingStatus;
    private String paymentStatus;
    private BigDecimal totalAmount;
    private Date paymentDeadline;

    private List<CustomerBookingItem> items
            = new ArrayList<>();

    public CustomerBookingView() {
    }

    public String getBookingID() {
        return bookingID;
    }

    public void setBookingID(String bookingID) {
        this.bookingID = bookingID;
    }

    public Date getBookingDate() {
        return bookingDate;
    }

    public void setBookingDate(Date bookingDate) {
        this.bookingDate = bookingDate;
    }

    public Date getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(Date checkInDate) {
        this.checkInDate = checkInDate;
    }

    public Date getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(Date checkOutDate) {
        this.checkOutDate = checkOutDate;
    }

    public long getNumberOfNights() {
        return numberOfNights;
    }

    public void setNumberOfNights(
            long numberOfNights) {

        this.numberOfNights = numberOfNights;
    }

    public String getBookingStatus() {
        return bookingStatus;
    }

    public void setBookingStatus(
            String bookingStatus) {

        this.bookingStatus = bookingStatus;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(
            String paymentStatus) {

        this.paymentStatus = paymentStatus;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(
            BigDecimal totalAmount) {

        this.totalAmount = totalAmount;
    }

    public List<CustomerBookingItem> getItems() {
        return items;
    }

    public void setItems(
            List<CustomerBookingItem> items) {

        this.items = items;
    }

    public Date getPaymentDeadline() {
        return paymentDeadline;
    }

    public void setPaymentDeadline(
            Date paymentDeadline) {

        this.paymentDeadline = paymentDeadline;
    }
}
