package dao;
import entity.Complaint;
import jakarta.persistence.EntityManager;
import util.PersistenceManager;
public class ComplaintDAO extends DAOFramework<Complaint> {

    public ComplaintDAO() {
        super(Complaint.class);
    }

    public Complaint getComplaintById(int id) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.find(Complaint.class, id);
        } finally {
            em.close();
        }
    }

    public void updateStatus(int id, String status) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint complaint = em.find(Complaint.class, id);
            if (complaint == null) {
                throw new IllegalArgumentException("Complaint not found: " + id);
            }
            complaint.setStatus(status);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException("Unable to update complaint status.", ex);
        } finally {
            em.close();
        }
    }

    public void updateReply(int id, String replyMessage, String status) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint complaint = em.find(Complaint.class, id);
            if (complaint == null) {
                throw new IllegalArgumentException("Complaint not found: " + id);
            }
            complaint.setReplyMessage(replyMessage);
            complaint.setStatus(status);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException("Unable to update complaint reply.", ex);
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) {
        System.out.println(new ComplaintDAO().getAll());
    }
}