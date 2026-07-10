/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import model.CustomerSource;
import model.RevenueChart;
import util.DBContext;

/**
 *
 * @author ADMIN
 */
public class HomeDAO extends DBContext{
    public long getTodayRevenue() {
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) AS TodayRevenue\n"
                + "FROM INVOICE\n"
                + "WHERE CAST(InvoiceDate AS DATE) = CAST(GETDATE() AS DATE);";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("TodayRevenue");
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public long getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT ISNULL(SUM(TotalAmount), 0) AS Revenue "
                + "FROM INVOICE "
                + "WHERE InvoiceDate BETWEEN ? AND ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setDate(1, java.sql.Date.valueOf(startDate));
            ps.setDate(2, java.sql.Date.valueOf(endDate));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getLong("Revenue");
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public int getOccupiedRoomsCount(LocalDate startDate, LocalDate endDate) {
        String sql = "SELECT COUNT(DISTINCT RoomID) AS OccupiedRooms\n"
                + "FROM BOOKING\n"
                + "WHERE CheckInDate <= ?"
                + "AND CheckOutDate >= ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setDate(1, java.sql.Date.valueOf(endDate));
            ps.setDate(2, java.sql.Date.valueOf(startDate));

            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("OccupiedRooms");
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public int getTotalRoomsCount() {
        String sql = "SELECT COUNT(*) as count FROM ROOM";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getInt("count");
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public int getTotalCustomers() {
        String sql = "SELECT COUNT(*) AS TotalCustomers FROM CUSTOMER";

        try (PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                return rs.getInt("TotalCustomers");
            }

        } catch (Exception e) {
        }

        return 0;
    }

    public List<RevenueChart> getRevenueLastTenDays() {
        List<RevenueChart> list = new ArrayList<>();

        String sql = "Select convert(varchar,InvoiceDate,103) Day, SUM(TotalAmount) Revenue\n"
                + "From INVOICE\n"
                + "Group by InvoiceDate\n"
                + "Order by InvoiceDate DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(
                        new RevenueChart(
                                rs.getString("Day"),
                                rs.getLong("Revenue")
                        )
                );
            }
        } catch (Exception e) {
        }
        Collections.reverse(list);
        return list;
    }

    public List<CustomerSource> getCustomerSource() {
        List<CustomerSource> list = new ArrayList<>();
        String sql
                = "SELECT "
                + "n.NationalityName,"
                + "COUNT(*) Total "
                + "FROM CUSTOMER c "
                + "JOIN Nationality n "
                + "ON c.NationalityID=n.NationalityID "
                + "GROUP BY n.NationalityName";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(
                        new CustomerSource(
                                rs.getString("NationalityName"),
                                rs.getInt("Total")
                        )
                );
            }
        } catch (Exception e) {
        }
        return list;
    }
}
