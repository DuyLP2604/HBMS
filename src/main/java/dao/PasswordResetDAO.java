package dao;

import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import java.util.List;
import util.PersistenceManager;

public class PasswordResetDAO {
    public Account findAccount(String identifier, String role) {
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
            return new Account(((Number) row[0]).intValue(), role,
                    ((String) row[1]).trim(), (String) row[2]);
        }
    }

    public boolean isEligible(Account account) {
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

    public boolean resetPassword(Account account, String password) {
        String table = account == null ? null : accountTable(account.getRole());
        if (table == null || password == null || password.isBlank()) {
            return false;
        }
        String hash = new UserDAO().hashMD5(password);
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            // Check the flag and original credentials in the UPDATE itself to close races.
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

    private void bindAccount(Query query, Account account, int first) {
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

    private String staffCondition(String role, String prefix) {
        return "Staff".equals(role) ? " AND " + prefix + "MustChangePassword = 0" : "";
    }

    protected EntityManager createEntityManager() {
        return PersistenceManager.createEntityManager();
    }

    public static final class Account {
        private final int userId;
        private final String role;
        private final String email;
        private final String passwordHash;

        public Account(int userId, String role, String email, String passwordHash) {
            this.userId = userId;
            this.role = role;
            this.email = email;
            this.passwordHash = passwordHash;
        }

        public int getUserId() { return userId; }
        public String getRole() { return role; }
        public String getEmail() { return email; }
        public String getPasswordHash() { return passwordHash; }
    }
}
