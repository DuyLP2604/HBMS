/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import util.DBContext;
import java.sql.*;
import model.User;

/**
 *
 * @author TAN LOI
 */
public class UserDAO extends DBContext {

    public User checkLogin(String username, String password) {
        String sql = "SELECT * FROM USERS WHERE Username = ? AND Password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new User(rs.getInt("UserID"), rs.getString("Username"),
                        rs.getString("Password"), rs.getString("Role"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }
}
