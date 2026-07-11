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

    // Câu SELECT dùng chung, JOIN với HOTEL và USERS (bắt buộc mỗi Employee phải có Hotel + User)
    private static final String SELECT_JOIN_SQL
            = "SELECT\n"
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

    // Map 1 dòng ResultSet -> Employee (kèm Hotel và User lồng bên trong)
    private Employee mapRow(ResultSet rs) throws SQLException {
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

        return new Employee(
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
    }

    public List<Employee> getAllEmployees() {
        List<Employee> list = new ArrayList<>();
        try {
            PreparedStatement ps = conn.prepareStatement(SELECT_JOIN_SQL);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    public Employee getEmployeeById(String id) {
        String sql = SELECT_JOIN_SQL + " WHERE e.EmployeeID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapRow(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Thêm nhân viên mới. Bắt buộc phải có HotelID và UserID (tài khoản đăng
     * nhập) vì EMPLOYEE JOIN USERS là INNER JOIN.
     */
    public void insertEmployee(Employee e) {
        String sql = "INSERT INTO EMPLOYEE (EmployeeID, FullName, Position, Salary, Shift, Address, Phone, HotelID, UserID) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
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
            ps.setInt(9, e.getUser().getId());
            ps.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
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
}