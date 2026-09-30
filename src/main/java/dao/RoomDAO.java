/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Room;
import java.util.List;

/**
 *
 * @author Lenovo
 */
public class RoomDAO extends DAOFramework<Room> {

    public RoomDAO() {
        super(Room.class);
    }

    public static void main(String[] args) {
        System.out.println(new RoomDAO().getAll());
    }
}
