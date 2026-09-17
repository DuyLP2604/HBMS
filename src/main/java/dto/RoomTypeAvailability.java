package dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class RoomTypeAvailability implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roomTypeID;
    private String typeName;
    private int capacity;
    private BigDecimal price;
    private long totalActiveRooms;
    private long reservedRooms;
    private long availableRooms;

    public RoomTypeAvailability() {
    }

    public RoomTypeAvailability(
            String roomTypeID,
            String typeName,
            int capacity,
            BigDecimal price,
            long totalActiveRooms,
            long reservedRooms,
            long availableRooms) {

        this.roomTypeID = roomTypeID;
        this.typeName = typeName;
        this.capacity = capacity;
        this.price = price;
        this.totalActiveRooms = totalActiveRooms;
        this.reservedRooms = reservedRooms;
        this.availableRooms = availableRooms;
    }

    public String getRoomTypeID() {
        return roomTypeID;
    }

    public void setRoomTypeID(String roomTypeID) {
        this.roomTypeID = roomTypeID;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public long getTotalActiveRooms() {
        return totalActiveRooms;
    }

    public void setTotalActiveRooms(long totalActiveRooms) {
        this.totalActiveRooms = totalActiveRooms;
    }

    public long getReservedRooms() {
        return reservedRooms;
    }

    public void setReservedRooms(long reservedRooms) {
        this.reservedRooms = reservedRooms;
    }

    public long getAvailableRooms() {
        return availableRooms;
    }

    public void setAvailableRooms(long availableRooms) {
        this.availableRooms = availableRooms;
    }

    public boolean isAvailable() {
        return availableRooms > 0;
    }

    @Override
    public String toString() {
        return "RoomTypeAvailability{"
                + "roomTypeID=" + roomTypeID
                + ", typeName=" + typeName
                + ", capacity=" + capacity
                + ", price=" + price
                + ", totalActiveRooms=" + totalActiveRooms
                + ", reservedRooms=" + reservedRooms
                + ", availableRooms=" + availableRooms
                + '}';
    }
}