/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Booking;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author Lenovo
 */
public class BookingDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Booking> getAll() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT b FROM BOOKING b";
            TypedQuery<Booking> query = em.createQuery(jpql, Booking.class);

            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Booking getById(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Booking.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    public List<Booking> getByUserId(String customerId) {
        EntityManager em = emf.createEntityManager();
        // Entity: Booking
        // b.customerID: point toCustomer Object
        // b.customerID.customerID: point to CustomerID
        String jpql = "SELECT b FROM Booking b WHERE b.customerID.customerID = :customerId";
        try {
            TypedQuery<Booking> query = em.createQuery(jpql, Booking.class);
            query.setParameter("customerId", customerId);
            return query.getResultList();
        } catch (Exception e) {
        } finally {
            em.close();
        }
        return null;
    }

    public static void main(String[] args) {
        BookingDAO dao = new BookingDAO();
        for (Booking b : dao.getAll()) {
            System.out.println(b);
        }
        System.out.println(dao.getById("B01"));
        for (Booking b : dao.getByUserId("KH01")) {
            System.out.println(b);
        }
    }
}
