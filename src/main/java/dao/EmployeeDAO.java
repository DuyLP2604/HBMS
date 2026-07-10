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
import model.Hotel;
import model.User;

/**
 *
 * @author TAN LOI
 */
public class EmployeeDAO extends DBContext {

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        String sql = "SELECT\n"
                + "    e.EmployeeID,\n"
                + "    e.FullName,\n"
                + "    e.Position,\n"
                + "    e.Salary,\n"
                + "    e.Shift,\n"
                + "    e.Address AS EmployeeAddress,\n"
                + "    e.Phone,\n"
                + "\n"
                + "    h.HotelID,\n"
                + "    h.HotelName,\n"
                + "    h.Address AS HotelAddress,\n"
                + "\n"
                + "    u.UserID,\n"
                + "    u.Username,\n"
                + "    u.Password,\n"
                + "    u.Role\n"
                + "FROM EMPLOYEE e\n"
                + "JOIN HOTEL h ON e.HotelID = h.HotelID\n"
                + "JOIN USERS u ON e.UserID = u.UserID";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {

                Hotel hotel = new Hotel(
                        rs.getString("HotelID"),
                        rs.getString("HotelName"),
                        rs.getString("HotelAddress")
                );

                User user = new User(
                        rs.getInt("UserID"),
                        rs.getString("Username"),
                        rs.getString("Password"),
                        rs.getString("Role")
                );

                Employee emp = new Employee(
                        rs.getString("EmployeeID"),
                        rs.getString("FullName"),
                        rs.getString("Position"),
                        rs.getDouble("Salary"),
                        rs.getString("Shift"),
                        rs.getString("EmployeeAddress"),
                        rs.getString("Phone"),
                        hotel,
                        user
                );

                list.add(emp);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
    
    // missing userId
    public void insertEmployee(Employee e) {
        String sql = "INSERT INTO EMPLOYEE (EmployeeID, FullName, Position, Salary, Shift, Address, Phone, HotelID) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, e.getId());
            ps.setString(2, e.getFullname());
            ps.setString(3, e.getPosition());
            ps.setDouble(4, e.getSalary());
            ps.setString(5, e.getShift());
            ps.setString(6, e.getAddress());
            ps.setString(7, e.getPhone());
            ps.setString(8, e.getHotel().getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    
    // missing userId
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
            ps.setString(1, e.getFullname());
            ps.setString(2, e.getPosition());
            ps.setDouble(3, e.getSalary());
            ps.setString(4, e.getShift());
            ps.setString(5, e.getAddress());
            ps.setString(6, e.getPhone());
            ps.setString(7, e.getHotel().getId());
            ps.setString(8, e.getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }
    >>>>>>> CUS
    -and
    -EMP
}
