package dao;

import entity.Customer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import util.PersistenceManager;

public class CustomerDAO {

    public String generateCustomerID() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            String jpql = "SELECT MAX(" + "CAST(SUBSTRING(" + "c.customerID, 3, " + "LENGTH(c.customerID)" + ") AS Integer)" + ") " + "FROM Customer c";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            Integer maxId = query.getSingleResult();
            if (maxId != null) {
                return String.format("KH%02d", maxId + 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "KH01";
    }

    public boolean insertCustomer(Customer customer) {
        EntityManager em = PersistenceManager.createEntityManager();
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
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            String jpql = "SELECT c FROM Customer c";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    public Customer getCustomerById(String id) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return em.find(Customer.class, id);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    // cho search
    public List<Customer> getCustomerByName(String name) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            // hoa thanh thuong
            String searchName = "%" + name.trim().toLowerCase() + "%";
            String jpql = "SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE :name";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("name", searchName);
            return query.getResultList();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return List.of();
    }

    // search theo ten hoac sdt
    public List<Customer> searchCustomerByKeyword(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return List.of();
        }
        String Keywordtrim = keyword.trim();
        Customer customerById = getCustomerById(Keywordtrim.toUpperCase());
        if (customerById != null) {
            return List.of(customerById);
        }
        return getCustomerByName(Keywordtrim);
    }

    public void updateCustomer(Customer customer) {

        EntityManager em = PersistenceManager.createEntityManager();

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

    public Customer getCustomerByUserId(int userId) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            String jpql = "SELECT c " + "FROM Customer c " + "WHERE c.userID.userID = :userId";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);

            query.setParameter("userId", userId);
            List<Customer> customers = query.setMaxResults(1).getResultList();

            if (!customers.isEmpty()) {
                return customers.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public Customer getCustomerByEmailOrPhone(String identifier) {

        if (identifier == null || identifier.trim().isEmpty()) {

            return null;
        }

        String value = identifier.trim();

        try (EntityManager em = PersistenceManager.createEntityManager()) {

            String jpql = "SELECT c " + "FROM Customer c " + "WHERE LOWER(c.email) = LOWER(:email) " + "OR c.phone = :phone";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);

            query.setParameter("email", value);
            query.setParameter("phone", value);

            List<Customer> customers = query.setMaxResults(1).getResultList();

            if (!customers.isEmpty()) {
                return customers.get(0);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


/// test
//    public static void main(String[] args) {
//        CustomerDAO dao = new CustomerDAO();
//        System.out.println("========== TEST 1: TÌM BẰNG ID ==========");
//        List<Customer> result1 = dao.searchCustomerByKeyword("KH01");
//        if (result1.isEmpty()) {
//            System.out.println("Không tìm thấy KH01");
//        } else {
//            for (Customer c : result1) {
//                System.out.println("Tìm thấy: " + c.getCustomerID() + " - " + c.getFullName());
//            }
//        }
//        System.out.println("\n========== TEST 2: TÌM BẰNG TÊN ==========");
//        List<Customer> result2 = dao.searchCustomerByKeyword("Phạm Minh Tuấn");
//        if (result2.isEmpty()) {
//            System.out.println("Không tìm thấy người tên Phạm Minh Tuấn");
//        } else {
//            for (Customer c : result2) {
//                System.out.println("Tìm thấy: " + c.getCustomerID() + " - " + c.getFullName());
//            }
//        }
//        System.out.println("\n========== TEST 3: TÌM SAI ==========");
//        List<Customer> result3 = dao.searchCustomerByKeyword("Batman");
//        if (result3.isEmpty()) {
//            System.out.println("Chuẩn! Không tìm thấy Batman.");
//        }
//    }
}
