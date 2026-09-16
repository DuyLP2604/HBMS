/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.List;

/**
 *
 * @author default
 */
public class CustomerDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public String generateCustomerID() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT MAX(CAST(SUBSTRING(c.customerID, 3, LENGTH(c.customerID)) AS Integer)) FROM Customer c";

            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            Integer maxId = query.getSingleResult();
            if (maxId != null) {
                return String.format("KH%02d", maxId + 1);
            }
        } catch (Exception e) {
        }
        return "KH01"; // if the table was first create
    }

    public boolean insertCustomer(Customer c) {

        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    public List<Customer> getAllCustomers() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT c FROM CUSTOMER c";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Customer getCustomerById(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Customer.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    public void updateCustomer(Customer c) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public Customer getCustomerByUserId(int userId) {
        EntityManager em = emf.createEntityManager();
        String jpql = "SELECT c FROM Customer c WHERE c.userID.userID = :userId";

        try {
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("userId", userId);
            List<Customer> customers = query.getResultList();

            if (!customers.isEmpty()) {
                return customers.get(0);
            }
        } catch (Exception e) {
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }

        return null;
    }
}
