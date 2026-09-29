/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Complaint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import util.PersistenceManager;

/**
 *
 * @author TAN LOI
 */
public class ComplaintDAO {

    public List<Complaint> getAllComplaints() {
        return new DAOFramework<>(Complaint.class).getAll();
    }

    public Complaint getComplaintById(int id) {
        return new DAOFramework<>(Complaint.class).findById("" + id);
    }

    public void insertComplaint(Complaint cp) {
        new DAOFramework<>(Complaint.class).insert(cp);
    }

    public void updateStatus(int id, String status) {
        EntityManager em = PersistenceManager.createEntityManager();
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

    public static void main(String[] args) {
        System.out.println(new ComplaintDAO().getAllComplaints());
    }
}
