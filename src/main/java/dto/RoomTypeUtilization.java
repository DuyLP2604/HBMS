package dto;

import java.io.Serializable;

/**
 * Room usage of one room type in the selected report period.
 */
public class RoomTypeUtilization implements Serializable {

    private static final long serialVersionUID = 1L;

    private String roomTypeID;
    private String typeName;
    private int roomCount;
    private long usedRoomDays;
    private long availableRoomDays;
    private double occupancyRate;

    public RoomTypeUtilization() {
    }

    public RoomTypeUtilization(String roomTypeID, String typeName, int roomCount,
            long usedRoomDays, long availableRoomDays, double occupancyRate) {
        this.roomTypeID = roomTypeID;
        this.typeName = typeName;
        this.roomCount = roomCount;
        this.usedRoomDays = usedRoomDays;
        this.availableRoomDays = availableRoomDays;
        this.occupancyRate = occupancyRate;
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

    /**
     * Number of ACTIVE rooms of this type.
     */
    public int getRoomCount() {
        return roomCount;
    }

    public void setRoomCount(int roomCount) {
        this.roomCount = roomCount;
    }

    /**
     * Room-days sold in the period (one room occupied for one night = 1).
     */
    public long getUsedRoomDays() {
        return usedRoomDays;
    }

    public void setUsedRoomDays(long usedRoomDays) {
        this.usedRoomDays = usedRoomDays;
    }

    /**
     * Room-days that could have been sold: active rooms x days in the period.
     */
    public long getAvailableRoomDays() {
        return availableRoomDays;
    }

    public void setAvailableRoomDays(long availableRoomDays) {
        this.availableRoomDays = availableRoomDays;
    }

    /**
     * Occupancy in percent (0-100, one decimal).
     */
    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    @Override
    public String toString() {
        return "RoomTypeUtilization[typeName=" + typeName
                + ", roomCount=" + roomCount
                + ", usedRoomDays=" + usedRoomDays
                + ", availableRoomDays=" + availableRoomDays
                + ", occupancyRate=" + occupancyRate + "]";
    }
}
