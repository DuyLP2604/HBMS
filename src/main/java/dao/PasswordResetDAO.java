package dao;

import dto.PasswordResetAccount;
import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import util.PersistenceManager;

/** Finds accounts for OTP recovery and writes verified password resets. */
public class PasswordResetDAO {
    /** Find one eligible customer or staff account by email or phone. */
    public PasswordResetAccount findAccount(String identifier, String role) {
        String table = accountTable(role);
        if (table == null || identifier == null || identifier.isBlank()) {
            return null;
        }
        String value = identifier.trim();
        try (EntityManager em = createEntityManager()) {
            Query query = em.createNativeQuery(
                    "SELECT u.UserID, o.Email, u.Password FROM USERS u JOIN " + table
                    + " o ON o.UserID = u.UserID WHERE u.Role = ?"
                    + " AND (LOWER(LTRIM(RTRIM(o.Email))) = LOWER(?) OR o.Phone = ?)"
                    + " AND o.Email IS NOT NULL AND LTRIM(RTRIM(o.Email)) <> ''"
                    + staffCondition(role, "u."));
            query.setParameter(1, role);
            query.setParameter(2, value);
            query.setParameter(3, value);
            List<?> matches = query.setMaxResults(2).getResultList();
            // Never pick an arbitrary account when a phone number matches several users.
            if (matches.size() != 1) {
                return null;
            }
            Object[] row = (Object[]) matches.get(0);
            return new PasswordResetAccount(((Number) row[0]).intValue(), role,
                    ((String) row[1]).trim(), (String) row[2]);
        }
    }

    /** Check that the verified account still has the same email and password. */
    public boolean isEligible(PasswordResetAccount account) {
        String table = account == null ? null : accountTable(account.getRole());
        if (table == null) {
            return false;
        }
        try (EntityManager em = createEntityManager()) {
            Query query = em.createNativeQuery(
                    "SELECT COUNT(*) FROM USERS u JOIN " + table
                    + " o ON o.UserID = u.UserID WHERE u.UserID = ? AND u.Role = ?"
                    + " AND u.Password = ? AND LOWER(LTRIM(RTRIM(o.Email))) = LOWER(?)"
                    + staffCondition(account.getRole(), "u."));
            bindAccount(query, account, 1);
            return ((Number) query.getSingleResult()).intValue() == 1;
        }
    }

    /** Update the password only if the verified account details still match. */
    public boolean resetPassword(PasswordResetAccount account, String password) {
        String table = account == null ? null : accountTable(account.getRole());
        if (table == null || password == null || password.isBlank()) {
            return false;
        }
        String hash = new UserDAO().hashMD5(password);
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            // The UPDATE checks that the account details still match the verified reset request.
            Query query = em.createNativeQuery(
                    "UPDATE USERS SET Password = ? WHERE UserID = ? AND Role = ?"
                    + " AND Password = ?" + staffCondition(account.getRole(), "")
                    + " AND EXISTS (SELECT 1 FROM " + table
                    + " o WHERE o.UserID = USERS.UserID"
                    + " AND LOWER(LTRIM(RTRIM(o.Email))) = LOWER(?))");
            query.setParameter(1, hash);
            bindAccount(query, account, 2);
            if (query.executeUpdate() != 1) {
                em.getTransaction().rollback();
                return false;
            }
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evict(Users.class, account.getUserId());
            return true;
        } catch (RuntimeException exception) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw exception;
        } finally {
            em.close();
        }
    }

    private void bindAccount(Query query, PasswordResetAccount account, int first) {
        query.setParameter(first, account.getUserId());
        query.setParameter(first + 1, account.getRole());
        query.setParameter(first + 2, account.getPasswordHash());
        query.setParameter(first + 3, account.getEmail());
    }

    private String accountTable(String role) {
        if ("Customer".equals(role)) {
            return "CUSTOMER";
        }
        if ("Staff".equals(role)) {
            return "EMPLOYEE";
        }
        return null;
    }

    // Staff must have an active account and finish first-login setup before password recovery.
    private String staffCondition(String role, String prefix) {
        return "Staff".equals(role) ? " AND " + prefix + "MustChangePassword = 0 AND " + prefix + "IsActive = 1" : "";
    }

    protected EntityManager createEntityManager() {
        return PersistenceManager.createEntityManager();
    }
}
