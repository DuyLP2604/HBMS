/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Hotel;
import jakarta.persistence.EntityManager;
import util.PersistenceManager;

/**
 *
 * @author TAN LOI
 */
public class HotelDAO {

    /**
     * The project manages exactly one hotel.
     *
     * @return the hotel, or null if the row is missing or cannot be read
     */
    public Hotel getHotel() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return em.find(Hotel.class, Hotel.FIXED_NAME);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * @param h
     * @return true if the hotel was saved, false if the update failed.
     */
    public boolean updateHotel(Hotel h) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(h);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            return false;
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
