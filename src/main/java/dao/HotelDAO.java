/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import util.DBContext;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Hotel;

/**
 *
 * @author TAN LOI
 */
public class HotelDAO extends DBContext {

    public List<Hotel> getAllHotels() {
        List<Hotel> list = new ArrayList<>();
        String sql = "SELECT * FROM HOTEL";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Hotel h = new Hotel();
                h.setId(rs.getString("HotelID"));
                h.setName(rs.getString("HotelName"));
                h.setAddress(rs.getString("Address"));
                list.add(h);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Hotel getHotelById(String id) {
        String sql = "SELECT * FROM HOTEL WHERE HotelID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                Hotel h = new Hotel();
                h.setId(rs.getString("HotelID"));
                h.setName(rs.getString("HotelName"));
                h.setAddress(rs.getString("Address"));
                return h;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void insertHotel(Hotel h) {
        String sql = "INSERT INTO HOTEL (HotelID, HotelName, Address) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, h.getId());
            ps.setString(2, h.getName());
            ps.setString(3, h.getAddress());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateHotel(Hotel h) {
        String sql = "UPDATE HOTEL SET HotelName = ?, Address = ? WHERE HotelID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, h.getName());
            ps.setString(2, h.getAddress());
            ps.setString(3, h.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}