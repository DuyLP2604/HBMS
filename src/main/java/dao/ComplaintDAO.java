/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;
import util.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Complaint;

/**
 *
 * @author TAN LOI
 */
public class ComplaintDAO extends DBContext {
    public List<Complaint> getAllComplaints() {
        List<Complaint> list = new ArrayList<>();
        String sql = "SELECT c.*, cust.FullName FROM COMPLAINT c LEFT JOIN CUSTOMER cust ON c.CustomerID = cust.CustomerID ORDER BY c.CreatedAt DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Complaint(rs.getInt("ComplaintID"), rs.getString("Title"), 
                    rs.getString("Content"), rs.getTimestamp("CreatedAt"), 
                    rs.getString("Status"), rs.getString("FullName")));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return list;
    }

    public void insertComplaint(Complaint cp, String customerId) {
        String sql = "INSERT INTO COMPLAINT (Title, Content, CustomerID) VALUES (?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, cp.getTitle());
            ps.setString(2, cp.getContent());
            ps.setString(3, (customerId == null || customerId.isEmpty()) ? null : customerId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
}
