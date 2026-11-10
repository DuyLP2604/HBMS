package dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/** Service usage and cancellation report shown on the admin dashboard. */
public class ServiceUsageReport implements Serializable {

    private static final long serialVersionUID = 1L;

    private List<ServiceUsageItem> items = new ArrayList<>();
    private ServiceUsageItem mostUsed;
    private ServiceUsageItem mostCancelled;
    private long totalUsedQuantity;
    private long totalCancelledQuantity;

    public List<ServiceUsageItem> getItems() {
        return items;
    }

    public void setItems(List<ServiceUsageItem> items) {
        this.items = items;
    }

    /** Service ordered the most in non-cancelled bookings, or null when none was ordered. */
    public ServiceUsageItem getMostUsed() {
        return mostUsed;
    }

    public void setMostUsed(ServiceUsageItem mostUsed) {
        this.mostUsed = mostUsed;
    }

    /** Service that appears the most in cancelled bookings, or null when none was cancelled. */
    public ServiceUsageItem getMostCancelled() {
        return mostCancelled;
    }

    public void setMostCancelled(ServiceUsageItem mostCancelled) {
        this.mostCancelled = mostCancelled;
    }

    public long getTotalUsedQuantity() {
        return totalUsedQuantity;
    }

    public void setTotalUsedQuantity(long totalUsedQuantity) {
        this.totalUsedQuantity = totalUsedQuantity;
    }

    public long getTotalCancelledQuantity() {
        return totalCancelledQuantity;
    }

    public void setTotalCancelledQuantity(long totalCancelledQuantity) {
        this.totalCancelledQuantity = totalCancelledQuantity;
    }
}
