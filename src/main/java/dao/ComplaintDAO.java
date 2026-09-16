/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Complaint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author TAN LOI
 */
public class ComplaintDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Complaint> getAllComplaints() {
        EntityManager em = emf.createEntityManager();
        try {
            String spql = "SLECT cp FROM COMPLAINT cp";
            TypedQuery<Complaint> query = em.createQuery(spql, Complaint.class);
            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Complaint getComplaintById(int id) {
        EntityManager em = emf.createEntityManager();
        try {
            return em.find(Complaint.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    public void insertComplaint(Complaint cp) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(cp);
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

    public void updateStatus(int id, String status) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint cp = em.find(Complaint.class, id);
            cp.setStatus(status);
            em.merge(cp);
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
}
