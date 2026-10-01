/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Hotel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import util.PersistenceManager;

/**
 *
 * @author TAN LOI
 */
public class HotelDAO {

    public List<Hotel> getAllHotels() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            String jpql = "SELECT h FROM HOTEL h";
            TypedQuery<Hotel> query = em.createQuery(jpql, Hotel.class);
            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Hotel getHotelById(String id) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return em.find(Hotel.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    public void insertHotel(Hotel h) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(h);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public void updateHotel(Hotel h) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(h);
            em.getTransaction().commit();
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
