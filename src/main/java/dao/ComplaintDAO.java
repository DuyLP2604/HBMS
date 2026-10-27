package dao;

import entity.Complaint;
import entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import util.PersistenceManager;

public class ComplaintDAO extends DAOFramework<Complaint> {

    public ComplaintDAO() {
        super(Complaint.class);
    }

    protected EntityManager createEntityManager() {
        return PersistenceManager.createEntityManager();
    }

    public Complaint getComplaintById(int id) {
        EntityManager em = createEntityManager();
        try {
            return em.find(Complaint.class, id);
        } finally {
            em.close();
        }
    }

    public void updateStatus(int id, String status) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            Complaint complaint = em.find(Complaint.class, id);
            if (complaint == null) {
                throw new IllegalArgumentException("Complaint not found: " + id);
            }
            complaint.setStatus(status);
            em.getTransaction().commit();
        } catch (IllegalArgumentException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw new IllegalStateException("Unable to update complaint status.", ex);
        } finally {
            em.close();
        }
    }

    /** Save the latest reply and its author together; actorUserId must come from the login session. */
    public void updateReply(int id, String replyMessage, String status, int actorUserId) {
        if (id <= 0 || actorUserId <= 0) {
            throw new IllegalArgumentException("Invalid complaint or account ID.");
        }
        if (replyMessage == null || replyMessage.trim().isEmpty()) {
            throw new IllegalArgumentException("Please enter a reply message.");
        }
        String nextStatus = status == null ? "" : status.trim();
        if (!("Chưa xử lý".equals(nextStatus) || "Đang xử lý".equals(nextStatus) || "Đã xử lý".equals(nextStatus))) {
            throw new IllegalArgumentException("Please select a valid complaint status.");
        }
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            // Read the current role and Employee relationship rather than a detached session object.
            List<Object[]> actors = em.createQuery(
                    "SELECT u.role, e.employeeID FROM Users u LEFT JOIN u.employee e WHERE u.userID = :userId",
                    Object[].class).setParameter("userId", actorUserId).getResultList();
            if (actors.size() != 1) {
                throw new SecurityException("This account can no longer reply to complaints.");
            }
            Object[] actor = actors.get(0);
            boolean admin = "Admin".equalsIgnoreCase((String) actor[0]);
            if (!admin && !"Staff".equalsIgnoreCase((String) actor[0])) {
                throw new SecurityException("This account can no longer reply to complaints.");
            }
            String employeeId = (String) actor[1];
            if (!admin && employeeId == null) {
                throw new IllegalArgumentException("Your staff account has no employee profile. Please contact an administrator.");
            }
            Complaint complaint = em.find(Complaint.class, id);
            if (complaint == null) {
                throw new EntityNotFoundException("Complaint not found.");
            }
            complaint.setReplyMessage(replyMessage.trim());
            complaint.setStatus(nextStatus);
            // An Admin without an employee profile is valid and must not inherit a previous author.
            complaint.setEmployeeID(employeeId == null ? null : em.getReference(Employee.class, employeeId));
            em.getTransaction().commit();
        } catch (RuntimeException ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw ex;
        } finally {
            em.close();
        }
    }

    public static void main(String[] args) {
        System.out.println(new ComplaintDAO().getAll());
    }
}
