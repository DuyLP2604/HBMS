/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Service;
import util.DBContext;

/**
 *
 * @author ADMIN
 */
public class ServiceDAO extends DBContext {

    public List<Service> getAllServices() {
        List<Service> list = new ArrayList<>();
        String sql = "SELECT\n"
                + "    MIN(ServiceID) AS ServiceID,\n"
                + "    ServiceName,\n"
                + "    UnitPrice\n"
                + "FROM SERVICE\n"
                + "GROUP BY ServiceName, UnitPrice";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String serviceId = rs.getString("ServiceID");
                String serviceName = rs.getString("ServiceName");
                Double unitPrice = rs.getDouble("UnitPrice");
                list.add(new Service(serviceId, serviceName, unitPrice, null, null));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public String generateServiceID() {
        String sql = "SELECT TOP 1 ServiceID FROM SERVICE ORDER BY ServiceID DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String id = rs.getString("ServiceID");
                int number = Integer.parseInt(id.substring(2));
                return String.format("S%02d", number + 1);
            }
        } catch (Exception e) {
        }
        return "S01";
    }

    public String getRoomID(String bookingID) {
        String sql = "SELECT RoomID FROM BOOKING WHERE BookingID=?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, bookingID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("RoomID");
            }
        } catch (Exception e) {
        }
        return null;
    }

    public String getHotelID(String roomID) {
        String sql = "SELECT HotelID FROM ROOM WHERE RoomID=?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, roomID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getString("HotelID");
            }
        } catch (Exception e) {
        }

        return null;
    }

    public void insertService(Service s) {
        String sql = "INSERT INTO SERVICE VALUES(?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, s.getServiceID());
            ps.setString(2, s.getServiceName());
            ps.setDouble(3, s.getUnitPrice());
            ps.setString(4, s.getHotelID());
            ps.setString(5, s.getRoomID());
            ps.executeUpdate();
        } catch (Exception e) {
        }
    }

    public void insertBookingService(String bookingID, String serviceID) {
        String sql = "INSERT INTO BOOKING_SERVICE VALUES(?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, bookingID);
            ps.setString(2, serviceID);
            ps.executeUpdate();

        } catch (Exception e) {
        }
    }

    public String getBookingIDByUser(int userID) {
        String sql = "SELECT TOP 1 b.BookingID "
                + "FROM BOOKING b "
                + "JOIN CUSTOMER c ON b.CustomerID = c.CustomerID "
                + "WHERE c.UserID = ? "
                + "ORDER BY b.BookingDate DESC";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userID);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return rs.getString("BookingID");
            }
        } catch (Exception e) {
        }
        return null;
    }
}
