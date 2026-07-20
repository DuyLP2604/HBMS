/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Booking;
import model.Customer;
import model.Room;
import util.DBContext;

/**
 *
 * @author Lenovo
 */
public class BookingDAO extends DBContext {

    public List<Booking> getAll() {
        List<Booking> list = new ArrayList<>();
        CustomerDAO cusDao = new CustomerDAO();
        RoomDAO roomDao = new RoomDAO();
        String sql = "select * from BOOKING";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String bookingId = rs.getString("BookingID");
                String bookingDate = rs.getString("BookingDate").replace("-", "/");
                String checkInDate = rs.getString("CheckInDate").replace("-", "/");
                String checkOutDate = rs.getString("CheckOutDate").replace("-", "/");
                String bookingStatus = rs.getString("BookingStatus");

                String roomId = rs.getString("RoomID");
                Room room = roomDao.getById(roomId);

                String customerId = rs.getString("CustomerID");
                Customer customer = cusDao.getCustomerById(customerId);
                list.add(new Booking(bookingId, bookingDate, checkInDate, checkOutDate, bookingStatus, room, customer));
            }
        } catch (Exception e) {

        }
        return list;
    }

    public Booking getById(String id) {
        String sql = "select * from BOOKING where BookingID = ?";

        RoomDAO rDao = new RoomDAO();
        CustomerDAO cDao = new CustomerDAO();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String bookingDate = rs.getString("BookingDate");
                String checkInDate = rs.getString("CheckInDate");
                String checkOutDate = rs.getString("CheckOutDate");
                String status = rs.getString("BookingStatus");

                String roomId = rs.getString("RoomID");
                Room room = rDao.getById(roomId);

                String cusId = rs.getString("CustomerID");
                Customer customer = cDao.getCustomerById(cusId);

                return new Booking(id, bookingDate, checkInDate, checkOutDate, status, room, customer);
            }
        } catch (Exception e) {
        }
        return null;
    }

    public static void main(String[] args) {
        BookingDAO dao = new BookingDAO();
//        for (Booking b : dao.getAll()) {
//            System.out.println(b);
//        }

//        System.out.println(dao.getById("B01"));

          for (Booking b: dao.getByUserId("KH01")){
              System.out.println(b);
          }
    }

    public List<Booking> getByUserId(String userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "select * from BOOKING where CustomerID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            RoomDAO rDao = new RoomDAO();
            CustomerDAO cDao = new CustomerDAO();
            ps.setString(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String bookingId = rs.getString("BookingID");
                String bookingDate = rs.getString("BookingDate");
                String checkInDate = rs.getString("CheckInDate");
                String checkOutDate = rs.getString("CheckOutDate");
                String bookingStatus = rs.getString("BookingStatus");

                String roomId = rs.getString("RoomID");
                Room room = rDao.getById(roomId);
                
                String cusId = rs.getString("CustomerID");
                Customer customer = cDao.getCustomerById(cusId);
                
                list.add(new Booking(bookingId, bookingDate, checkInDate, checkOutDate, bookingStatus, room, customer));
            }
        } catch (Exception e) {
        }
        return list;
    }
}
