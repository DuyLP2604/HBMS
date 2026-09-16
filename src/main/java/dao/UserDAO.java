package dao;

import entity.Users;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;


public class UserDAO {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );


    public String hashMD5(String password) {

        StringBuilder hash =
                new StringBuilder();

        try {

            MessageDigest md =
                    MessageDigest.getInstance("MD5");

            byte[] bytes =
                    md.digest(password.getBytes());

            for (byte b : bytes) {

                hash.append(
                        String.format("%02x", b)
                );
            }

        } catch (NoSuchAlgorithmException e) {

            e.printStackTrace();
        }

        return hash.toString();
    }


    public Users login(
            String username,
            String password
    ) {

        try (EntityManager em =
                emf.createEntityManager()) {

            String jpql =
                    "SELECT u "
                    + "FROM Users u "
                    + "WHERE u.username = :username "
                    + "AND u.password = :password";

            TypedQuery<Users> query =
                    em.createQuery(
                            jpql,
                            Users.class
                    );

            query.setParameter(
                    "username",
                    username
            );

            query.setParameter(
                    "password",
                    hashMD5(password)
            );

            List<Users> users =
                    query.setMaxResults(1)
                         .getResultList();

            if (!users.isEmpty()) {
                return users.get(0);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        return null;
    }


    public boolean isUsernameExists(
            String username
    ) {

        try (EntityManager em =
                emf.createEntityManager()) {

            String jpql =
                    "SELECT COUNT(u) "
                    + "FROM Users u "
                    + "WHERE u.username = :username";

            TypedQuery<Long> query =
                    em.createQuery(
                            jpql,
                            Long.class
                    );

            query.setParameter(
                    "username",
                    username
            );

            Long count =
                    query.getSingleResult();

            return count > 0;

        } catch (Exception e) {

            e.printStackTrace();
        }

        return false;
    }


    public int insertUser(
            String username,
            String password,
            String role
    ) {

        EntityManager em =
                emf.createEntityManager();

        try {

            Users newUser =
                    new Users();

            newUser.setUsername(
                    username
            );

            newUser.setPassword(
                    hashMD5(password)
            );

            newUser.setRole(
                    role
            );


            em.getTransaction().begin();

            em.persist(newUser);

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


    public boolean deleteUser(
            int userId
    ) {

        EntityManager em =
                emf.createEntityManager();

        try {

            em.getTransaction().begin();


            Users user =
                    em.find(
                            Users.class,
                            userId
                    );


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


    /*
     * Update user's password
     */
    public boolean updatePassword(
            int userId,
            String newPassword
    ) {

        EntityManager em =
                emf.createEntityManager();

        try {

            em.getTransaction().begin();


            Users user =
                    em.find(
                            Users.class,
                            userId
                    );


            /*
             * User does not exist
             */
            if (user == null) {

                em.getTransaction().rollback();

                return false;
            }


            /*
             * Update hashed password
             */
            user.setPassword(
                    hashMD5(newPassword)
            );


            /*
             * user is already managed by EntityManager,
             * so merge() is not required.
             */
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
}