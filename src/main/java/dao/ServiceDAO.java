/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Booking;
import entity.Room;
import entity.Service;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author ADMIN
 */
public class ServiceDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Service> getAllServices() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT new entity.Service(MIN(s.serviceID), s.serviceName, s.unitPrice) "
                    + "FROM Service s "
                    + "GROUP BY s.serviceName, s.unitPrice";
            TypedQuery<Service> query = em.createQuery(jpql, Service.class);
            return query.getResultList();
        }
    }

    public String generateServiceID() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT s.serviceID FROM Service s ORDER BY s.serviceID DESC";
            TypedQuery<String> query = em.createQuery(jpql, String.class);
            query.setMaxResults(1);

            List<String> results = query.getResultList();
            if (!results.isEmpty()) {
                String id = results.get(0);
                int number = Integer.parseInt(id.substring(1));
                return String.format("S%02d", number + 1);
            }
        } catch (Exception e) {
        }
        return "S01";
    }

    public void insertService(Service s) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(s);
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

    public void insertBookingService(String bookingID, String serviceID) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();

            Booking b = em.find(Booking.class, bookingID);
            Service s = em.find(Service.class, serviceID);

            if (b != null && s != null) {
                b.getServiceCollection().add(s);
                em.merge(b);
            }
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
