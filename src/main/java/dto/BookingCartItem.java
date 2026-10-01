package dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class BookingCartItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roomTypeID;
    private String typeName;
    private int capacity;
    private BigDecimal unitPrice;
    private int quantity;
    private int guestCount;

    public BookingCartItem() {
    }

    public BookingCartItem(
            String roomTypeID,
            String typeName,
            int capacity,
            BigDecimal unitPrice,
            int quantity,
            int guestCount) {

        setRoomTypeID(roomTypeID);
        setTypeName(typeName);
        setCapacity(capacity);
        setUnitPrice(unitPrice);
        setQuantity(quantity);
        setGuestCount(guestCount);

        validateGuestCapacity();
    }

    public String getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(String roomTypeID) {
        if (roomTypeID == null
                || roomTypeID.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Room type ID is required."
            );
        }

        this.roomTypeID = roomTypeID.trim();
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        if (typeName == null
                || typeName.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Room type name is required."
            );
        }

        this.typeName = typeName.trim();
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Room capacity must be greater than zero."
            );
        }

        this.capacity = capacity;
    }

    public BigDecimal getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(BigDecimal unitPrice) {
        if (unitPrice == null
                || unitPrice.compareTo(BigDecimal.ZERO) < 0) {

            throw new IllegalArgumentException(
                    "Room price cannot be negative."
            );
        }

        this.unitPrice = unitPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Room quantity must be greater than zero."
            );
        }

        this.quantity = quantity;
    }

    public int getGuestCount() {
        return guestCount;
    }

    public void setGuestCount(int guestCount) {
        if (guestCount <= 0) {
            throw new IllegalArgumentException(
                    "Guest count must be greater than zero."
            );
        }

        this.guestCount = guestCount;
    }

    public int getMaximumGuests() {
        return capacity * quantity;
    }

    public void validateGuestCapacity() {
        if (guestCount > getMaximumGuests()) {
            throw new IllegalArgumentException(
                    "Guest count exceeds the selected room capacity."
            );
        }
    }

    public BigDecimal calculateSubtotal(long numberOfNights) {
        if (numberOfNights <= 0) {
            return BigDecimal.ZERO;
        }

        return unitPrice
                .multiply(BigDecimal.valueOf(quantity))
                .multiply(BigDecimal.valueOf(numberOfNights));
    }

    @Override
    public String toString() {
        return "BookingCartItem{"
                + "roomTypeID=" + roomTypeID
                + ", typeName=" + typeName
                + ", capacity=" + capacity
                + ", unitPrice=" + unitPrice
                + ", quantity=" + quantity
                + ", guestCount=" + guestCount
                + '}';
    }
}