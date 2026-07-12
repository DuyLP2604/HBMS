/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.security.MessageDigest;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import model.User;
import util.DBContext;

/**
 *
 * @author default
 */
public class UserDAO extends DBContext {

    public String hashMD5(String password) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] bytes = md.digest(password.getBytes());
            for (byte b : bytes) {
                hash += String.format("%02x", b);
            }
        } catch (Exception e) {
        }
        return hash;
    }

    public User login(String username, String password) {
        User user = new User();
        String sql = "SELECT * FROM USERS WHERE username = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, hashMD5(password));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                user.setId(rs.getInt("UserID"));
                user.setUsername(rs.getString("Username"));
                user.setPassword(rs.getString("Password"));
                user.setRole(rs.getString("Role"));
            }
        } catch (Exception e) {
        }
        return user;
    }

    public boolean isUsernameExists(String username) {
        String sql = "SELECT * FROM USERS WHERE Username = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);

            ps.setString(1, username);

            ResultSet rs = ps.executeQuery();

            return rs.next();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public int insertUser(String username,
            String password,
            String role) {

        String sql = "INSERT INTO USERS\n"
                + "                 (Username, Password, Role)\n"
                + "                 VALUES (?, ?, ?)";

        try {
            PreparedStatement ps = conn.prepareStatement(
                    sql,
                    Statement.RETURN_GENERATED_KEYS);

            ps.setString(1, username);
            ps.setString(2, hashMD5(password));
            ps.setString(3, role);

            int affectedRows = ps.executeUpdate();

            if (affectedRows > 0) {

                ResultSet rs = ps.getGeneratedKeys();

                if (rs.next()) {
                    return rs.getInt(1); // UserID vừa tạo
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return -1;
    }

 
    public boolean deleteUser(int userId) {
        String sql = "DELETE FROM USERS WHERE UserID = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setInt(1, userId);

            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }
}
