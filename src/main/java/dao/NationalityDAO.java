/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import model.Nationality;
import util.DBContext;

/**
 *
 * @author default
 */
public class NationalityDAO extends DBContext {

    public List<Nationality> getAll() {
        List<Nationality> list = new ArrayList<>();
        String sql = "SELECT * FROM Nationality";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String id = rs.getString("NationalityID");
                String name = rs.getString("NationalityName");
                list.add(new Nationality(id, name));
            }
        } catch (Exception e) {
        }
        return list;
    }

    public Nationality getNationalityById(String nationalityId) {
        String sql = "SELECT * FROM Nationality WHERE NationalityID = ?";

        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, nationalityId);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Nationality(
                        rs.getString("NationalityID"),
                        rs.getString("NationalityName")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }
}
