/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import util.PersistenceManager;

/**
 *
 * @author Lenovo
 */
public class RoomDAO extends DAOFramework<Room> {

    public RoomDAO() {
        super(Room.class);
    }

    public String generateRoomID() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            // Lấy số lớn nhất từ chuỗi mã phòng (cắt từ vị trí số 2 vì ký tự đầu là 'R')
            String jpql = "SELECT MAX("
                    + "CAST(SUBSTRING(r.roomID, 2, LENGTH(r.roomID)) AS Integer)"
                    + ") FROM Room r";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            Integer maxId = query.getSingleResult();

            if (maxId != null) {
                return String.format("R%02d", maxId + 1); // Trả về R01, R02...
            }
        } catch (Exception e) {
        }
        return "R01"; // ID mặc định nếu bảng Room đang trống
    }

    public static void main(String[] args) {
        System.out.println(new RoomDAO().getAll());
    }
}
