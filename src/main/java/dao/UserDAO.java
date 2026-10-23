package dao;

import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import util.PersistenceManager;

public class UserDAO {

    public String hashMD5(String password) {
        if (password == null) {
            throw new IllegalArgumentException("Password must not be null.");
        }
        StringBuilder hash = new StringBuilder();
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            for (byte b : bytes) {
                hash.append(String.format("%02x", b));
            }
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Cannot hash password.", e);
        }
        return hash.toString();
    }

    public Users login(String username, String password) {
        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            return null;
        }
        try (EntityManager em = createEntityManager()) {
            String jpql = "SELECT u FROM Users u WHERE u.username = :username AND u.password = :password";
            TypedQuery<Users> query = em.createQuery(jpql, Users.class);
            query.setParameter("username", username);
            query.setParameter("password", hashMD5(password));
            List<Users> users = query.setMaxResults(1).getResultList();
            return users.isEmpty() ? null : users.get(0);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public boolean isUsernameExists(String username) {
        if (username == null || username.isBlank()) {
            return false;
        }
        try (EntityManager em = createEntityManager()) {
            String jpql = "SELECT COUNT(u) FROM Users u WHERE u.username = :username";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("username", username.trim());
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            throw new IllegalStateException("Unable to check username.", e);
        }
    }

    public Users insertUser(EntityManager em, String username, String password, String role) {
        if (em == null || !em.isOpen() || !em.getTransaction().isActive()) {
            throw new IllegalStateException("An active transaction is required.");
        }
        if (username == null || username.isBlank() || username.length() > 50 || password == null || password.isBlank() || role == null || role.isBlank()) {
            throw new IllegalArgumentException("Invalid account information.");
        }
        String jpql = "SELECT COUNT(u) FROM Users u WHERE u.username = :username";
        Long count = em.createQuery(jpql, Long.class).setParameter("username", username).getSingleResult();
        if (count > 0) {
            throw new IllegalArgumentException("Username already exists.");
        }
        Users newUser = new Users();
        newUser.setUsername(username);
        newUser.setPassword(hashMD5(password));
        newUser.setRole(role);
        em.persist(newUser);
        em.flush();
        return newUser;
    }

    public int insertUser(String username, String password, String role) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            Users newUser = insertUser(em, username, password, role);
            em.getTransaction().commit();
            return newUser.getUserID();
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
        return -1;
    }

    public boolean deleteUser(int userId) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            Users user = em.find(Users.class, userId);
            if (user == null) {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(user);
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPassword) {
        if (newPassword == null || newPassword.isBlank()) {
            return false;
        }
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            Users user = em.find(Users.class, userId);
            if (user == null) {
                em.getTransaction().rollback();
                return false;
            }
            user.setPassword(hashMD5(newPassword));
            em.getTransaction().commit();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
        return false;
    }

    /* Conditional updates make a temporary credential single-use across sessions.
     * Bulk updates also evict EclipseLink's shared cache before the next login. */
    public Users completeFirstLogin(int userId, String expectedPasswordHash, String newPassword) {
        String error = util.EmployeeValidator.validatePassword(newPassword);
        if (error != null) {
            throw new IllegalArgumentException(error);
        }
        String newHash = hashMD5(newPassword);
        if (newHash.equals(expectedPasswordHash)) {
            throw new IllegalArgumentException("The new password must be different from the temporary password.");
        }
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            int changed = em.createQuery("UPDATE Users u SET u.password = :password, u.mustChangePassword = false WHERE u.userID = :id AND u.role = :role AND u.mustChangePassword = true AND u.password = :expected")
                    .setParameter("password", newHash).setParameter("id", userId).setParameter("role", "Staff").setParameter("expected", expectedPasswordHash).executeUpdate();
            if (changed != 1) {
                em.getTransaction().rollback();
                return null;
            }
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evict(Users.class, userId);
            em.clear();
            Users user = em.find(Users.class, userId);
            em.refresh(user);
            return user;
        } catch (RuntimeException exception) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
            throw exception;
        } finally {
            em.close();
        }
    }

    /* An administrator can resend credentials only while first-login setup is pending. */
    public boolean renewStaffTemporaryPassword(int userId, String temporaryPassword) {
        EntityManager em = createEntityManager();
        try {
            em.getTransaction().begin();
            int changed = em.createQuery("UPDATE Users u SET u.password = :password WHERE u.userID = :id AND u.role = :role AND u.mustChangePassword = true")
                    .setParameter("password", hashMD5(temporaryPassword)).setParameter("id", userId).setParameter("role", "Staff").executeUpdate();
            em.getTransaction().commit();
            em.getEntityManagerFactory().getCache().evict(Users.class, userId);
            return changed == 1;
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

    public static void main(String[] args) {
        System.out.println(new UserDAO().login("admin", "123456").toString());
    }
}
