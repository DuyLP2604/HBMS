/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Booking;
import entity.Room;
import jakarta.persistence.EntityManager;
import java.util.List;
import util.PersistenceManager;

/**
 *
 * @author Asus
 * @param <entity>
 */
public class DAOFramework<entity> {

    private Class<entity> e;

    public DAOFramework(Class<entity> entityClassType) {
        this.e = entityClassType;
    }

    public List<entity> getAll() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            String jpql = "SELECT x FROM " + e.getName() + " x";
            return em.createQuery(jpql, this.e).getResultList();
        }
    }

    public entity getById(String id) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return em.find(this.e, id);
        } catch (Exception ex) {
            return null;
        }
    }

    public boolean insert(entity e) {
        return process(e, "c");
    }

    public boolean update(entity e) {
        return process(e, "u");
    }

    public boolean deleteById(String id) {

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            entity obj = em.find(this.e, id);
            if (obj != null) {
                em.remove(obj);
            } else {
                em.getTransaction().rollback();
                return false;
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            em.getTransaction().rollback();
            return false;
        } finally {
            em.close();
        }
    }

    private boolean process(entity e, String task) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            switch (task.toLowerCase()) {
                case "c":
                    em.persist(e);
                    break;
                case "u":
                    em.merge(e);
                    break;
            }
            em.getTransaction().commit();
            return true;
        } catch (Exception ex) {
            em.getTransaction().rollback();
        } finally {
            em.close();
        }
        return false;
    }

    public static void main(String[] args) {
        System.out.println(new DAOFramework<>(Booking.class).getAll());
        System.out.println(new DAOFramework<>(Room.class).getAll());
    }
}
