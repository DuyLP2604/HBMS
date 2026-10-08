package dao;
import entity.Customer;
import entity.Nationality;
import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import util.PersistenceManager;
public class CustomerDAO
{
    public String generateCustomerID()
    {
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT MAX(CAST(SUBSTRING(c.customerID, 3, LENGTH(c.customerID)) AS Integer)) FROM Customer c";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            Integer maxId = query.getSingleResult();
            if (maxId != null)
            {
                return String.format("KH%02d", maxId + 1);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return "KH01";
    }

    public boolean insertCustomer(Customer customer)
    {
        EntityManager em = PersistenceManager.createEntityManager();
        try
        {
            em.getTransaction().begin();
            em.persist(customer);
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

    public List<Customer> getAllCustomers()
    {
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT c FROM Customer c";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            return query.getResultList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return List.of();
    }

    public Customer getCustomerById(String id)
    {
        if (id == null || id.trim().isEmpty())
        {
            return null;
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            return em.find(Customer.class, id.trim().toUpperCase(Locale.ROOT));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public List<Customer> getCustomerByName(String name)
    {
        if (name == null || name.trim().isEmpty())
        {
            return List.of();
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String searchName = "%" + name.trim().toLowerCase(Locale.ROOT) + "%";
            String jpql = "SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE :name";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("name", searchName);
            return query.getResultList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return List.of();
    }

    public List<Customer> getCustomerByPhone(String phone)
    {
        if (phone == null || phone.trim().isEmpty())
        {
            return List.of();
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String searchPhone = "%" + phone.trim() + "%";
            String jpql = "SELECT c FROM Customer c WHERE c.phone LIKE :phone";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("phone", searchPhone);
            return query.getResultList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return List.of();
    }

    public List<Customer> searchCustomerByKeyword(String keyword)
    {
        if (keyword == null || keyword.trim().isEmpty())
        {
            return List.of();
        }
        String value = keyword.trim();
        Customer customerById = getCustomerById(value);
        if (customerById != null)
        {
            return List.of(customerById);
        }
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT c FROM Customer c WHERE LOWER(c.fullName) LIKE :name OR c.phone LIKE :phone";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("name", "%" + value.toLowerCase(Locale.ROOT) + "%");
            query.setParameter("phone", "%" + value + "%");
            return query.getResultList();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return List.of();
    }

    public boolean updateCustomer(Customer customer)
    {
        if (customer == null || customer.getCustomerID() == null || customer.getCustomerID().isBlank())
        {
            return false;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Customer existingCustomer = em.find(Customer.class, customer.getCustomerID());
            if (existingCustomer == null)
            {
                em.getTransaction().rollback();
                return false;
            }
            Nationality nationality = null;
            if (customer.getNationalityID() != null)
            {
                nationality = em.find(Nationality.class, customer.getNationalityID().getNationalityID());
                if (nationality == null)
                {
                    throw new IllegalArgumentException("Invalid nationality.");
                }
            }
            existingCustomer.setFullName(customer.getFullName());
            existingCustomer.setPhone(customer.getPhone());
            existingCustomer.setEmail(customer.getEmail());
            existingCustomer.setAddress(customer.getAddress());
            existingCustomer.setNationalityID(nationality);
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

    public Customer getCustomerByUserId(int userId)
    {
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT c FROM Customer c WHERE c.userID.userID = :userId";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("userId", userId);
            List<Customer> customers = query.setMaxResults(1).getResultList();
            return customers.isEmpty() ? null : customers.get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public Customer getCustomerByEmailOrPhone(String identifier)
    {
        if (identifier == null || identifier.trim().isEmpty())
        {
            return null;
        }
        String value = identifier.trim();
        try (EntityManager em = PersistenceManager.createEntityManager())
        {
            String jpql = "SELECT c FROM Customer c WHERE LOWER(c.email) = LOWER(:email) OR c.phone = :phone";
            TypedQuery<Customer> query = em.createQuery(jpql, Customer.class);
            query.setParameter("email", value);
            query.setParameter("phone", value);
            List<Customer> customers = query.setMaxResults(1).getResultList();
            return customers.isEmpty() ? null : customers.get(0);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return null;
    }

    public boolean insertCustomerWithAccount(Customer customer, String username, String password)
    {
        if (customer == null || customer.getCustomerID() == null || customer.getCustomerID().isBlank())
        {
            return false;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try
        {
            em.getTransaction().begin();
            Users account = new UserDAO().insertUser(em, username, password, "Customer");
            customer.setUserID(account);
            if (customer.getNationalityID() != null)
            {
                Nationality nationality = em.find(Nationality.class, customer.getNationalityID().getNationalityID());
                if (nationality == null)
                {
                    throw new IllegalArgumentException("Invalid nationality.");
                }
                customer.setNationalityID(nationality);
            }
            em.persist(customer);
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