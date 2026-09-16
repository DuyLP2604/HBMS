/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Hotel;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author TAN LOI
 */
public class HotelDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Hotel> getAllHotels() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT h FROM HOTEL h";
            TypedQuery<Hotel> query = em.createQuery(jpql, Hotel.class);
            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Hotel getHotelById(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Hotel.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    public void insertHotel(Hotel h) {
        EntityManager em = emf.createEntityManager();
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
        EntityManager em = emf.createEntityManager();
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
