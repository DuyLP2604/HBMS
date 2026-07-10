/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import util.DBContext;

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
}
