/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package util;

import entity.Users;
import jakarta.servlet.http.HttpSession;

/**
 *
 * @author Asus
 */
public class RoleManager {

    public static boolean requireAdmin(HttpSession session) {
        if (session == null) {
            return false;
        }
        Users u = (Users) session.getAttribute("user");
        return u != null && "admin".equalsIgnoreCase(u.getRole());
    }
}
