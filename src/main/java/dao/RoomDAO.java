/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import model.Hotel;
import model.Room;
import util.DBContext;

/**
 *
 * @author Lenovo
 */
public class RoomDAO extends DBContext {

    public List<Room> getAll(){
        List<Room> list = new ArrayList<>();
        String sql = "select * from ROOM";
        
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            HotelDAO hotelDao = new HotelDAO();
            while (rs.next()){
                String roomId = rs.getString("RoomID");
                String roomNumber = rs.getString("RoomNumber");
                String roomImage = rs.getString("RoomImage");
                double price = rs.getDouble("Price");
                String status = rs.getString("Status");
                String hotelId = rs.getString("HotelID");

                Hotel hotel = hotelDao.getHotelById(hotelId);
                
                list.add(new Room(roomId, roomNumber, roomImage, price, status, hotel));
            }
            
        } catch (Exception e) {
        }
        return list;
    }
    
    public Room getById(String id) {
        String sql = "select * from ROOM where RoomID = ?";
        HotelDAO hotelDao = new HotelDAO();
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                String roomId = rs.getString("RoomID");
                String roomNumber = rs.getString("RoomNumber");
                String roomImage = rs.getString("RoomImage");
                double price = rs.getDouble("Price");
                String status = rs.getString("Status");
                String hotelId = rs.getString("HotelID");

                Hotel hotel = hotelDao.getHotelById(hotelId);
                
                return new Room(roomId, roomNumber, roomImage, price, status, hotel);
            }
        } catch (Exception e) {
        }
        return null;
    }
    
    public static void main(String[] args) {
        RoomDAO dao = new RoomDAO();
//        System.out.print(dao.getById("R01"));

        for (Room r : dao.getAll()) {
            System.out.println(r);
        }
    }
}
