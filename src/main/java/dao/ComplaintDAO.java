package dao;

import entity.Complaint;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

public class ComplaintDAO {

    private static final EntityManagerFactory EMF =
            Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Complaint> getAllComplaints() {
        EntityManager em = EMF.createEntityManager();

        try {
            TypedQuery<Complaint> query = em.createQuery(
                    "SELECT cp FROM Complaint cp "
                    + "LEFT JOIN FETCH cp.customerID "
                    + "ORDER BY cp.complaintID DESC",
                    Complaint.class
            );

            return query.getResultList();
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to load complaints.", ex
            );
        } finally {
            em.close();
        }
    }

    public Complaint getComplaintById(int id) {
        EntityManager em = EMF.createEntityManager();

        try {
            return em.find(Complaint.class, id);
        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to find complaint " + id + ".", ex
            );
        } finally {
            em.close();
        }
    }

    public void insertComplaint(Complaint complaint) {
        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();
            em.persist(complaint);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException(
                    "Unable to add complaint.", ex
            );
        } finally {
            em.close();
        }
    }

    public void updateStatus(int id, String status) {
        EntityManager em = EMF.createEntityManager();

        try {
            em.getTransaction().begin();

            Complaint complaint = em.find(Complaint.class, id);
            if (complaint == null) {
                throw new IllegalArgumentException(
                        "Complaint not found: " + id
                );
            }

            complaint.setStatus(status);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException(
                    "Unable to update complaint status.", ex
            );
        } finally {
            em.close();
        }
    }
}