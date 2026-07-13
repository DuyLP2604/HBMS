/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import util.DBContext;
import java.util.ArrayList;
import java.util.List;
import model.Customer;
import model.Nationality;
import model.User;

/**
 *
 * @author default
 */
public class CustomerDAO extends DBContext {

    // Câu SELECT dùng chung cho getAllCustomers() và getCustomerById().
    // Đặt alias rõ ràng để tránh trùng tên cột (CUSTOMER, Nationality, USERS đều có cột ID/Name giống nhau).
    private static final String SELECT_JOIN_SQL
            = "SELECT\n"
            + "    c.CustomerID, c.FullName, c.Phone, c.Email, c.Address, c.CCCD, c.PassportNumber,\n"
            + "    n.NationalityID AS NatID, n.NationalityName AS NatName,\n"
            + "    u.UserID AS UID, u.Username, u.Password, u.Role\n"
            + "FROM CUSTOMER c\n"
            + "LEFT JOIN Nationality n ON c.NationalityID = n.NationalityID\n"
            + "LEFT JOIN USERS u ON c.UserID = u.UserID";

    // Map 1 dòng ResultSet -> Customer (kèm Nationality và User lồng bên trong)
    private Customer mapRow(ResultSet rs) throws SQLException {
        Nationality nationality = new Nationality(rs.getString("NatID"), rs.getString("NatName"));

        User user = null;
        // UserID có thể NULL (khách hàng chưa có tài khoản đăng nhập)
        if (rs.getObject("UID") != null) {
            user = new User(rs.getInt("UID"), rs.getString("Username"),
                    rs.getString("Password"), rs.getString("Role"));
        }

        return new Customer(
                rs.getString("CustomerID"),
                rs.getString("FullName"),
                rs.getString("Phone"),
                rs.getString("Email"),
                rs.getString("Address"),
                rs.getString("CCCD"),
                rs.getString("PassportNumber"),
                user,
                nationality
        );
    }

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
            Integer userId) {

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
            if (userId == null) {
                ps.setNull(9, Types.INTEGER);
            } else {
                ps.setInt(9, userId);
            }

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.out.println("Error code: " + e.getErrorCode());
            System.out.println("SQL state: " + e.getSQLState());
            System.out.println("Message: " + e.getMessage());
            e.printStackTrace();
        }

        return false;
    }

    public List<Customer> getAllCustomers() {
        List<Customer> list = new ArrayList<>();
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

    public Customer getCustomerById(String id) {
        String sql = SELECT_JOIN_SQL + " WHERE c.CustomerID = ?";
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

    public Customer getCustomerByUserId(int userId) {
        String sql = "SELECT c.*,\n"
                + "       n.NationalityID,\n"
                + "       n.NationalityName\n"
                + "FROM Customer c\n"
                + "LEFT JOIN Nationality n\n"
                + "    ON c.NationalityID = n.NationalityID\n"
                + "WHERE c.UserID = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                Nationality nationality = new Nationality(
                        rs.getString("NationalityID"),
                        rs.getString("NationalityName")
                );

                User user = new User();
                user.setId(rs.getInt("UserID"));

                Customer c = new Customer();

                c.setId(rs.getString("CustomerID"));
                c.setFullname(rs.getString("FullName"));
                c.setPhone(rs.getString("Phone"));
                c.setEmail(rs.getString("Email"));
                c.setAddress(rs.getString("Address"));
                c.setCccd(rs.getString("CCCD"));
                c.setPassportNumber(rs.getString("PassportNumber"));
                c.setNationality(nationality);
                c.setUser(user);

                return c;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
