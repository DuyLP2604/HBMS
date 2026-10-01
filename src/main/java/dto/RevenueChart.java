package dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class RevenueChart implements Serializable {

    private static final long serialVersionUID = 1L;

    private String day;
    private BigDecimal revenue;

    public RevenueChart() {
    }

    public RevenueChart(
            String day,
            BigDecimal revenue) {

        this.day = day;
        this.revenue = revenue;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }

    public void setRevenue(BigDecimal revenue) {
        this.revenue = revenue;
    }

    @Override
    public String toString() {
        return "RevenueChart[day="
                + day
                + ", revenue="
                + revenue
                + "]";
    }
}