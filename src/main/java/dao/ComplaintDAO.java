package dao;

import entity.Complaint;
import entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import util.PersistenceManager;

public class ComplaintDAO extends DAOFramework<Complaint> {

    public ComplaintDAO() {
        super(Complaint.class);
    }

    protected EntityManager createEntityManager() {
        return PersistenceManager.createEntityManager();
    }

    @Override
    public List<Complaint> getAll() {
        EntityManager em = createEntityManager();
        try {
            String jpql = "SELECT c FROM Complaint c ORDER BY c.createdAt DESC, c.complaintID DESC";
            TypedQuery<Complaint> query = em.createQuery(jpql, Complaint.class);
            return query.getResultList();
        } catch (Exception e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public List<Complaint> getComplaintsPaged(int offset, int pageSize) {
        EntityManager em = createEntityManager();
        try {
            String jpql = "SELECT c FROM Complaint c ORDER BY c.createdAt DESC, c.complaintID DESC";
            TypedQuery<Complaint> query = em.createQuery(jpql, Complaint.class);
            query.setFirstResult(offset);
            query.setMaxResults(pageSize);
            return query.getResultList();
        } catch (Exception e) {
            return Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public long countComplaints() {
        EntityManager em = createEntityManager();
        try {
            String jpql = "SELECT COUNT(c) FROM Complaint c";
            return em.createQuery(jpql, Long.class).getSingleResult();
        } catch (Exception e) {
            return 0L;
        } finally {
            em.close();
        }
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
            if (!admin) {
                Employee emp = em.find(Employee.class, employeeId);
                if (emp == null || emp.getPosition() == null || !emp.getPosition().toLowerCase().contains("customer service")) {
                    throw new SecurityException("Only Customer Service staff can reply to complaints.");
                }
            }
            Complaint complaint = em.find(Complaint.class, id);
            if (complaint == null) {
                throw new EntityNotFoundException("Complaint not found.");
            }
            complaint.setReplyMessage(replyMessage.trim());
            complaint.setStatus(nextStatus);
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
}
