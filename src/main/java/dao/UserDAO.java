package dao;
import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import util.PersistenceManager;
public class UserDAO
{
    public String hashMD5(String password)
    {
        if (password == null)
        {
            throw new IllegalArgumentException("Password must not be null.");
        }
        StringBuilder hash = new StringBuilder();
        try
        {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(password.getBytes());
            for (byte b : bytes)
            {
                hash.append(String.format("%02x", b));
            }
        }
        catch (NoSuchAlgorithmException e)
        {
            throw new IllegalStateException("Cannot hash password.", e);
        }
        return hash.toString();
    }

    public Users login(String username, String password)
    {
        if (username == null || username.isBlank() || password == null || password.isBlank())
        {
            return null;
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT u FROM Users u WHERE u.username = :username AND u.password = :password";
            TypedQuery<Users> query = em.createQuery(jpql, Users.class);
            query.setParameter("username", username);
            query.setParameter("password", hashMD5(password));
            List<Users> users = query.setMaxResults(1).getResultList();
            return users.isEmpty() ? null : users.get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isUsernameExists(String username)
    {
        if (username == null || username.isBlank())
        {
            return false;
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT COUNT(u) FROM Users u WHERE u.username = :username";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("username", username);
            return query.getSingleResult() > 0;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public Users insertUser(EntityManager em, String username, String password, String role)
    {
        if (em == null || !em.isOpen() || !em.getTransaction().isActive())
        {
            throw new IllegalStateException("An active transaction is required.");
        }
        if (username == null || username.isBlank() || username.length() > 50 || password == null || password.isBlank() || role == null || role.isBlank())
        {
            throw new IllegalArgumentException("Invalid account information.");
        }
        Users newUser = new Users();
        newUser.setUsername(username);
        newUser.setPassword(hashMD5(password));
        newUser.setRole(role);
        em.persist(newUser);
        em.flush();
        return newUser;
    }

    public int insertUser(String username, String password, String role)
    {
        EntityManager em = PersistenceManager.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Users newUser = insertUser(em, username, password, role);
            em.getTransaction().commit();
            return newUser.getUserID();
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            if (em.isOpen())
            {
                em.close();
            }
        }
        return -1;
    }

    public boolean deleteUser(int userId)
    {
        EntityManager em = PersistenceManager.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Users user = em.find(Users.class, userId);
            if (user == null)
            {
                em.getTransaction().rollback();
                return false;
            }
            em.remove(user);
            em.getTransaction().commit();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            if (em.isOpen())
            {
                em.close();
            }
        }
        return false;
    }

    public boolean updatePassword(int userId, String newPassword)
    {
        if (newPassword == null || newPassword.isBlank())
        {
            return false;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Users user = em.find(Users.class, userId);
            if (user == null)
            {
                em.getTransaction().rollback();
                return false;
            }
            user.setPassword(hashMD5(newPassword));
            em.getTransaction().commit();
            return true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
            if (em.getTransaction().isActive())
            {
                em.getTransaction().rollback();
            }
        }
        finally
        {
            if (em.isOpen())
            {
                em.close();
            }
        }
        return false;
    }
}