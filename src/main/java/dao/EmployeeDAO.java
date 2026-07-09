/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import util.DBContext;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.Employee;

/**
 *
 * @author TAN LOI
 */
public class EmployeeDAO extends DBContext {

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT * FROM EMPLOYEE";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Employee(rs.getString("EmployeeID"), rs.getString("FullName"),
                        rs.getString("Position"), rs.getBigDecimal("Salary"), rs.getString("Shift"),
                        rs.getString("Address"), rs.getString("Phone"), rs.getString("HotelID")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public void insertEmployee(Employee e) {
        String sql = "INSERT INTO EMPLOYEE (EmployeeID, FullName, Position, Salary, Shift, Address, Phone, HotelID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getId());
            ps.setString(2, e.getName());
            ps.setString(3, e.getPosition());
            ps.setBigDecimal(4, e.getSalary());
            ps.setString(5, e.getShift());
            ps.setString(6, e.getAddress());
            ps.setString(7, e.getPhone());
            ps.setString(8, e.getHotelId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public Employee getEmployeeById(String id) {
        String sql = "SELECT * FROM EMPLOYEE WHERE EmployeeID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Employee(rs.getString("EmployeeID"), rs.getString("FullName"),
                        rs.getString("Position"), rs.getBigDecimal("Salary"), rs.getString("Shift"),
                        rs.getString("Address"), rs.getString("Phone"), rs.getString("HotelID"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateEmployee(Employee e) {
        String sql = "UPDATE EMPLOYEE SET FullName = ?, Position = ?, Salary = ?, Shift = ?, Address = ?, Phone = ?, HotelID = ? WHERE EmployeeID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getName());
            ps.setString(2, e.getPosition());
            ps.setBigDecimal(3, e.getSalary());
            ps.setString(4, e.getShift());
            ps.setString(5, e.getAddress());
            ps.setString(6, e.getPhone());
            ps.setString(7, e.getHotelId());
            ps.setString(8, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
}
