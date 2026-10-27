package dao;

import jakarta.persistence.EntityManager;
import entity.Users;
import util.EmployeeValidator;
import util.PersistenceManager;

/** Changes only the signed-in account, using its current password as a write guard. */
public class PasswordChangeDAO {
    public boolean changePassword(int userId, String role, String currentPassword, String newPassword) {
        if (userId <= 0 || !("Customer".equals(role) || "Staff".equals(role))
                || currentPassword == null || currentPassword.isEmpty()) {
            return false;
        }
        String error = "Staff".equals(role) ? EmployeeValidator.validatePassword(newPassword)
                : newPassword == null || newPassword.length() < 6
                        ? "Password must contain at least 6 characters." : null;
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        UserDAO passwords = new UserDAO();
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            // Check the role, old hash and first-login flag in the UPDATE itself.
            // A concurrent reset or flag change cannot be overwritten by this form.
            String sql = "UPDATE USERS SET Password = ? WHERE UserID = ? AND Role = ? AND Password = ?"
                    + ("Staff".equals(role) ? " AND MustChangePassword = 0" : "");
            int changed = em.createNativeQuery(sql)
                    .setParameter(1, passwords.hashMD5(newPassword))
                    .setParameter(2, userId)
                    .setParameter(3, role)
                    .setParameter(4, passwords.hashMD5(currentPassword))
                    .executeUpdate();
            if (changed != 1) {
                em.getTransaction().rollback();
                return false;
            }
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evict(Users.class, userId);
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

    protected EntityManager createEntityManager() {
        return PersistenceManager.createEntityManager();
    }
}
