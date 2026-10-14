package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Room-usage report for a date range: totals plus a breakdown by room type.
 */
public class RoomUtilizationReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private String periodLabel;
    private long totalDays;
    private int activeRooms;
    private long usedRoomDays;
    private long availableRoomDays;
    private double occupancyRate;
    private List<RoomTypeUtilization> byRoomType = new ArrayList<>();

    public RoomUtilizationReport() {
    }

    /**
     * Text such as "01/10/2026 - 13/10/2026" (the period actually measured).
     */
    public String getPeriodLabel() {
        return periodLabel;
    }

    public void setPeriodLabel(String periodLabel) {
        this.periodLabel = periodLabel;
    }

    /**
     * Number of days in the measured period (inclusive).
     */
    public long getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(long totalDays) {
        this.totalDays = totalDays;
    }

    public int getActiveRooms() {
        return activeRooms;
    }

    public void setActiveRooms(int activeRooms) {
        this.activeRooms = activeRooms;
    }

    public long getUsedRoomDays() {
        return usedRoomDays;
    }

    public void setUsedRoomDays(long usedRoomDays) {
        this.usedRoomDays = usedRoomDays;
    }

    public long getAvailableRoomDays() {
        return availableRoomDays;
    }

    public void setAvailableRoomDays(long availableRoomDays) {
        this.availableRoomDays = availableRoomDays;
    }

    public double getOccupancyRate() {
        return occupancyRate;
    }

    public void setOccupancyRate(double occupancyRate) {
        this.occupancyRate = occupancyRate;
    }

    public List<RoomTypeUtilization> getByRoomType() {
        return byRoomType;
    }

    public void setByRoomType(List<RoomTypeUtilization> byRoomType) {
        this.byRoomType = byRoomType;
    }

    @Override
    public String toString() {
        return "RoomUtilizationReport[period=" + periodLabel
                + ", totalDays=" + totalDays
                + ", usedRoomDays=" + usedRoomDays
                + ", availableRoomDays=" + availableRoomDays
                + ", occupancyRate=" + occupancyRate + "]";
    }
}
