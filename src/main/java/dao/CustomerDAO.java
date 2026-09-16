package dao;

import entity.Customer;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;

import java.util.List;


public class CustomerDAO {

    private final EntityManagerFactory emf =
            Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );


    public String generateCustomerID() {

        try (EntityManager em =
                emf.createEntityManager()) {

            String jpql =
                    "SELECT MAX("
                    + "CAST(SUBSTRING("
                    + "c.customerID, 3, "
                    + "LENGTH(c.customerID)"
                    + ") AS Integer)"
                    + ") "
                    + "FROM Customer c";

            TypedQuery<Integer> query =
                    em.createQuery(
                            jpql,
                            Integer.class
                    );

            Integer maxId =
                    query.getSingleResult();

            if (maxId != null) {

                return String.format(
                        "KH%02d",
                        maxId + 1
                );
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return "KH01";
    }


    public boolean insertCustomer(Customer customer) {

        EntityManager em =
                emf.createEntityManager();

        try {

            em.getTransaction().begin();

            em.persist(customer);

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


    public List<Customer> getAllCustomers() {

        try (EntityManager em =
                emf.createEntityManager()) {

            String jpql =
                    "SELECT c FROM Customer c";

            TypedQuery<Customer> query =
                    em.createQuery(
                            jpql,
                            Customer.class
                    );

            return query.getResultList();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return List.of();
    }


    public Customer getCustomerById(String id) {

        try (EntityManager em =
                emf.createEntityManager()) {

            return em.find(
                    Customer.class,
                    id
            );

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public void updateCustomer(Customer customer) {

        EntityManager em =
                emf.createEntityManager();

        try {

            em.getTransaction().begin();

            em.merge(customer);

            em.getTransaction().commit();

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
    }


    public Customer getCustomerByUserId(
            int userId
    ) {

        try (EntityManager em =
                emf.createEntityManager()) {

            String jpql =
                    "SELECT c "
                    + "FROM Customer c "
                    + "WHERE c.userID.userID = :userId";

            TypedQuery<Customer> query =
                    em.createQuery(
                            jpql,
                            Customer.class
                    );

            query.setParameter(
                    "userId",
                    userId
            );

            List<Customer> customers =
                    query.setMaxResults(1)
                         .getResultList();

            if (!customers.isEmpty()) {
                return customers.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


    public Customer getCustomerByEmailOrPhone(
            String identifier
    ) {

        if (identifier == null
                || identifier.trim().isEmpty()) {

            return null;
        }

        String value =
                identifier.trim();

        try (EntityManager em =
                emf.createEntityManager()) {

            String jpql =
                    "SELECT c "
                    + "FROM Customer c "
                    + "WHERE LOWER(c.email) = LOWER(:email) "
                    + "OR c.phone = :phone";

            TypedQuery<Customer> query =
                    em.createQuery(
                            jpql,
                            Customer.class
                    );

            query.setParameter(
                    "email",
                    value
            );

            query.setParameter(
                    "phone",
                    value
            );

            List<Customer> customers =
                    query.setMaxResults(1)
                         .getResultList();

            if (!customers.isEmpty()) {
                return customers.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}