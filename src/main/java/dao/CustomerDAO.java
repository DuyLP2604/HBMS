/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DBContext;
import java.util.ArrayList;
import java.util.List;
import model.Customer;

/**
 *
 * @author default
 */
public class CustomerDAO extends DBContext {

    public String generateCustomerID() {
        String sql
                = "SELECT MAX(CAST(SUBSTRING(CustomerID, 3, LEN(CustomerID)) AS INT)) AS MaxID "
                + "FROM CUSTOMER";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                int number = rs.getInt("MaxID");
                return String.format("KH%02d", number + 1);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return "KH01"; // if the table was first create
    }

    public boolean insertCustomer(String customerId,
            String fullname,
            String phone,
            String email,
            String address,
            String cccd,
            String passportNumber,
            String nationalityId,
            int userId) {

        String sql = "INSERT INTO CUSTOMER\n"
                + "                 (\n"
                + "                    CustomerID,\n"
                + "                    FullName,\n"
                + "                    Phone,\n"
                + "                    Email,\n"
                + "                    Address,\n"
                + "                    CCCD,\n"
                + "                    PassportNumber,\n"
                + "                    NationalityID,\n"
                + "                    UserID\n"
                + "                 )\n"
                + "                 VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, customerId);
            ps.setString(2, fullname);
            ps.setString(3, phone);
            ps.setString(4, email);
            ps.setString(5, address);
            ps.setString(6, cccd);
            ps.setString(7, passportNumber);
            ps.setString(8, nationalityId);
            ps.setInt(9, userId);

            System.out.println("customerId = " + customerId);
            System.out.println("fullname = " + fullname);
            System.out.println("phone = " + phone);
            System.out.println("email = " + email);
            System.out.println("address = " + address);
            System.out.println("cccd = " + cccd);
            System.out.println("passportNumber = " + passportNumber);
            System.out.println("nationalityId = " + nationalityId);
            System.out.println("userInsertedID = " + userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }
    
    // missing UserID
    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
        String sql = "SELECT * FROM CUSTOMER";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Customer(rs.getString("CustomerID"), rs.getString("FullName"),
                        rs.getString("Phone"), rs.getString("Email"), rs.getString("Address"),
                        rs.getString("CCCD"), rs.getString("PassportNumber"), rs.getString("NationalityID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // missing userId
    public Customer getCustomerById(String id) {
        String sql = "SELECT * FROM CUSTOMER WHERE CustomerID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Customer(rs.getString("CustomerID"), rs.getString("FullName"),
                        rs.getString("Phone"), rs.getString("Email"), rs.getString("Address"),
                        rs.getString("CCCD"), rs.getString("PassportNumber"), rs.getString("NationalityID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    public void updateCustomer(Customer c) {
        String sql = "UPDATE CUSTOMER SET FullName = ?, Phone = ?, Email = ?, Address = ?, CCCD = ?, PassportNumber = ?, NationalityID = ? WHERE CustomerID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getFullname());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, (c.getCccd() == null || c.getCccd().isEmpty()) ? null : c.getCccd());
            ps.setString(6, (c.getPassportNumber() == null || c.getPassportNumber().isEmpty()) ? null : c.getPassportNumber());
            ps.setString(7, c.getNationality().getId());
            ps.setString(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
