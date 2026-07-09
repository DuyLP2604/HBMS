/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import util.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Customer;

/**
 *
 * @author TAN LOI
 */
public class CustomerDAO extends DBContext {

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

    public void insertCustomer(Customer c) {
        String sql = "INSERT INTO CUSTOMER (CustomerID, FullName, Phone, Email, Address, CCCD, PassportNumber, NationalityID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, c.getId());
            ps.setString(2, c.getName());
            ps.setString(3, c.getPhone());
            ps.setString(4, c.getEmail());
            ps.setString(5, c.getAddress());
            ps.setString(6, (c.getCccd() == null || c.getCccd().isEmpty()) ? null : c.getCccd());
            ps.setString(7, (c.getPassport() == null || c.getPassport().isEmpty()) ? null : c.getPassport());
            ps.setString(8, c.getNation());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

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
            ps.setString(1, c.getName());
            ps.setString(2, c.getPhone());
            ps.setString(3, c.getEmail());
            ps.setString(4, c.getAddress());
            ps.setString(5, (c.getCccd() == null || c.getCccd().isEmpty()) ? null : c.getCccd());
            ps.setString(6, (c.getPassport() == null || c.getPassport().isEmpty()) ? null : c.getPassport());
            ps.setString(7, c.getNation());
            ps.setString(8, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
