package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CustomerBookingItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer bookingDetailID;
    private String roomTypeID;
    private String roomTypeName;
    private int quantity;
    private int guestCount;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
    private List<String> roomNumbers = new ArrayList<>();

    public CustomerBookingItem() {
    }

    public Integer getBookingDetailID() {
        return bookingDetailID;
    }

    public void setBookingDetailID(
            Integer bookingDetailID) {

        this.bookingDetailID = bookingDetailID;
    }

    public String getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(String roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(
            String roomTypeName) {

        this.roomTypeName = roomTypeName;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        this.guestCount = guestCount;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(
            BigDecimal unitPrice) {

        this.unitPrice = unitPrice;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(
            BigDecimal subtotal) {

        this.subtotal = subtotal;
    }

    public List<String> getRoomNumbers() {
        return roomNumbers;
    }

    public void setRoomNumbers(
            List<String> roomNumbers) {

        this.roomNumbers = roomNumbers;
    }

    public boolean isFullyAssigned() {
        return roomNumbers != null
                && roomNumbers.size() == quantity;
    }
}