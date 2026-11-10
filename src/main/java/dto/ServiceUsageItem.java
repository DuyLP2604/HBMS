package dto;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * Usage of one hotel service in the selected report period.
 *
 * "Used" counts service lines of bookings that were not cancelled (confirmed,
 * assigned, checked-in, checked-out). "Cancelled" counts service lines of
 * bookings that were cancelled.
 */
public class ServiceUsageItem implements Serializable {

    private static final long serialVersionUID = 1L;

    private String serviceID;
    private String serviceName;
    private long usedQuantity;
    private long usedBookings;
    private BigDecimal usedRevenue = BigDecimal.ZERO;
    private long cancelledQuantity;
    private long cancelledBookings;
    private BigDecimal cancelledRevenue = BigDecimal.ZERO;

    public ServiceUsageItem() {
    }

    public ServiceUsageItem(String serviceID, String serviceName) {
        this.serviceID = serviceID;
        this.serviceName = serviceName;
    }

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    /** Total quantity ordered in bookings that were not cancelled. */
    public long getUsedQuantity() {
        return usedQuantity;
    }

    public void setUsedQuantity(long usedQuantity) {
        this.usedQuantity = usedQuantity;
    }

    /** Number of non-cancelled bookings that ordered this service. */
    public long getUsedBookings() {
        return usedBookings;
    }

    public void setUsedBookings(long usedBookings) {
        this.usedBookings = usedBookings;
    }

    /** Revenue (subtotal) of the non-cancelled orders. */
    public BigDecimal getUsedRevenue() {
        return usedRevenue;
    }

    public void setUsedRevenue(BigDecimal usedRevenue) {
        this.usedRevenue = usedRevenue;
    }

    /** Total quantity ordered in bookings that were cancelled. */
    public long getCancelledQuantity() {
        return cancelledQuantity;
    }

    public void setCancelledQuantity(long cancelledQuantity) {
        this.cancelledQuantity = cancelledQuantity;
    }

    /** Number of cancelled bookings that ordered this service. */
    public long getCancelledBookings() {
        return cancelledBookings;
    }

    public void setCancelledBookings(long cancelledBookings) {
        this.cancelledBookings = cancelledBookings;
    }

    /** Value of the orders lost to cancellation. */
    public BigDecimal getCancelledRevenue() {
        return cancelledRevenue;
    }

    public void setCancelledRevenue(BigDecimal cancelledRevenue) {
        this.cancelledRevenue = cancelledRevenue;
    }

    /** Cancelled share of the ordered quantity of this service, in percent (0-100, one decimal). */
    public double getCancelRate() {
        long total = usedQuantity + cancelledQuantity;
        if (total <= 0) {
            return 0.0;
        }
        return Math.round(cancelledQuantity * 1000.0 / total) / 10.0;
    }

    @Override
    public String toString() {
        return "ServiceUsageItem[serviceID=" + serviceID
                + ", serviceName=" + serviceName
                + ", usedQuantity=" + usedQuantity
                + ", cancelledQuantity=" + cancelledQuantity + "]";
    }
}
