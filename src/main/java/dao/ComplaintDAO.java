package dao;

import entity.Complaint;
import jakarta.persistence.EntityManager;
import java.util.List;
import util.PersistenceManager;

/**
 *
 * @author TAN LOI
 */
public class ComplaintDAO extends DAOFramework<Complaint> {

    public ComplaintDAO() {
        super(Complaint.class);
    }

    public Complaint getComplaintById(int id) {
        return new DAOFramework<>(Complaint.class).getById("" + id);
    }

    public void updateStatus(int id, String status) {
        EntityManager em = PersistenceManager.createEntityManager();
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

    public static void main(String[] args) {
        System.out.println(new ComplaintDAO().getAll());
    }
}
