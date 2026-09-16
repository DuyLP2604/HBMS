/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Room;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Lenovo
 */
public class RoomDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Room> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT r FROM ROOM r";
            TypedQuery<Room> query = em.createQuery(jpql, Room.class);
            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Room getById(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Room.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    public static void main(String[] args) {
        RoomDAO dao = new RoomDAO();
        for (Room r : dao.getAll()) {
            System.out.println(r);
        }
    }
}
