/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 *
 * @author default
 */
//Changed to entity manager
public class UserDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public String hashMD5(String password) {
        String hash = "";
        try {
            MessageDigest md = MessageDigest.getInstance("md5");
            byte[] bytes = md.digest(password.getBytes());
            for (byte b : bytes) {
                hash += String.format("%02x", b);
            }
        } catch (NoSuchAlgorithmException e) {
        }
        return hash;
    }

    public Users login(String username, String password) {
        EntityManager em = emf.createEntityManager();
        String jpql = "SELECT u FROM Users u WHERE u.username = :username AND u.password = :password";
        try {
            TypedQuery<Users> query = em.createQuery(jpql, Users.class);
            query.setParameter("username", username);
            query.setParameter("password", hashMD5(password));

            List<Users> users = query.getResultList();
            if (!users.isEmpty()) {
                return users.get(0);
            }
        } catch (Exception e) {
        } finally {
            em.close();
        }
        return null;
    }

    public boolean isUsernameExists(String username) {
        EntityManager em = emf.createEntityManager();
        String jpql = "SELECT u FROM Users u WHERE u.username = :username";

        try {
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("username", username);

            long count = query.getSingleResult();
            return count > 0;

        } catch (Exception e) {
        } finally {
            em.close();
        }

        return false;
    }

    public int insertUser(String username, String password, String role) {
        EntityManager em = emf.createEntityManager();

        try {
            Users newUser = new Users();
            newUser.setUsername(username);
            newUser.setPassword(hashMD5(password));
            newUser.setRole(role);

            em.getTransaction().begin();

            em.persist(newUser);
            em.getTransaction().commit();
            return newUser.getUserID();

        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return -1;
    }

    public boolean deleteUser(int userId) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            Users u = em.find(Users.class, userId);
            if (u != null) {
                em.remove(u);
                em.getTransaction().commit();
                return true;
            } else {
                em.getTransaction().rollback();
                return false;
            }
        } catch (Exception e) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return false;
    }
}
