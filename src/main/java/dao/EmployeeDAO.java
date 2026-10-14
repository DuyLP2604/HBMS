package dao;

import entity.Employee;
import entity.Users;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import java.util.Locale;
import util.EmployeeIdGenerator;
import util.PersistenceManager;

public class EmployeeDAO {

    public List<Employee> getAllEmployees() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT e FROM Employee e ORDER BY e.employeeID";
            return em.createQuery(jpql, Employee.class).getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load the employee list.", exception);
        } finally {
            close(em);
        }
    }

    public Employee getEmployeeById(String employeeID) {
        if (employeeID == null || employeeID.trim().isEmpty()) {
            return null;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.find(Employee.class, employeeID.trim());
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to find the employee.", exception);
        } finally {
            close(em);
        }
    }

    public Employee getEmployeeByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return null;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<Employee> result = em.createQuery("SELECT e FROM Employee e WHERE LOWER(e.email) = :email", Employee.class)
                    .setParameter("email", normalizedEmail)
                    .setMaxResults(1)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to find the employee by email.", exception);
        } finally {
            close(em);
        }
    }

    public boolean isEmailExists(String email) {
        return isEmailExists(email, null);
    }

    /*
     * When updating, exclude the employee being edited.
     * Keeping their current email is therefore allowed.
     */
    public boolean isEmailExists(String email, String excludeEmployeeID) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return false;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return emailExists(em, normalizedEmail, excludeEmployeeID);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to check employee email.", exception);
        } finally {
            close(em);
        }
    }

    public boolean isPhoneExists(String phone) {
        return isPhoneExists(phone, null);
    }

    /*
     * When updating, exclude the employee being edited.
     * The phone number must already be normalised (see EmployeeValidator.normalizePhone).
     */
    public boolean isPhoneExists(String phone, String excludeEmployeeID) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return phoneExists(em, phone.trim(), excludeEmployeeID);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to check employee phone number.", exception);
        } finally {
            close(em);
        }
    }

    /*
     * Inserts an employee linked to an existing user account.
     */
    public void insertEmployee(Employee employee) {
        validateEmployee(employee);
        if (employee.getUserID() == null || employee.getUserID().getUserID() == null) {
            throw new IllegalArgumentException("An existing user account is required.");
        }
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            ensureEmailAvailable(em, employee.getEmail(), null);
            ensurePhoneAvailable(em, employee.getPhone(), null);

            Users managedUser = em.find(Users.class, employee.getUserID().getUserID());
            if (managedUser == null) {
                throw new IllegalArgumentException("The linked user account was not found.");
            }

            employee.setUserID(managedUser);
            em.persist(employee);

            em.getTransaction().commit();
        } catch (IllegalArgumentException exception) {
            rollback(em);
            throw exception;
        } catch (Exception exception) {
            rollback(em);
            throw new IllegalStateException("Unable to add the employee.", exception);
        } finally {
            close(em);
        }
    }

    /*
     * Creates the account and employee in one transaction.
     */
    public void insertEmployeeWithUser(Employee employee, Users user) {
        validateEmployee(employee);
        validateUser(user);

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            ensureEmailAvailable(em, employee.getEmail(), null);
            ensurePhoneAvailable(em, employee.getPhone(), null);
            ensureUsernameAvailable(em, user.getUsername());

            em.persist(user);
            employee.setUserID(user);
            em.persist(employee);

            em.getTransaction().commit();
        } catch (IllegalArgumentException exception) {
            rollback(em);
            throw exception;
        } catch (Exception exception) {
            rollback(em);
            throw new IllegalStateException("Unable to create the employee and user account.", exception);
        } finally {
            close(em);
        }
    }

    public void updateEmployee(Employee employee) {
        validateEmployee(employee);

        EntityManager em = PersistenceManager.createEntityManager();
        try {
            em.getTransaction().begin();

            Employee managedEmployee = em.find(Employee.class, employee.getEmployeeID());
            if (managedEmployee == null) {
                throw new IllegalArgumentException("Employee not found: " + employee.getEmployeeID());
            }

            ensureEmailAvailable(em, employee.getEmail(), managedEmployee.getEmployeeID());
            ensurePhoneAvailable(em, employee.getPhone(), managedEmployee.getEmployeeID());

            managedEmployee.setFullName(employee.getFullName());
            managedEmployee.setPosition(employee.getPosition());
            managedEmployee.setSalary(employee.getSalary());
            managedEmployee.setShift(employee.getShift());
            managedEmployee.setAddress(employee.getAddress());
            managedEmployee.setPhone(employee.getPhone());
            managedEmployee.setEmail(employee.getEmail());

            em.getTransaction().commit();
        } catch (IllegalArgumentException exception) {
            rollback(em);
            throw exception;
        } catch (Exception exception) {
            rollback(em);
            throw new IllegalStateException("Unable to update the employee.", exception);
        } finally {
            close(em);
        }
    }

    public boolean isReceptionistByUserId(int userID) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(e) FROM Employee e WHERE e.userID.userID = :userID AND LOWER(e.position) = :position", Long.class)
                    .setParameter("userID", userID)
                    .setParameter("position", "receptionist")
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            close(em);
        }
    }

    public Employee getByUserId(int userID) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<Employee> result = em.createQuery("SELECT e FROM Employee e WHERE e.userID.userID = :userID", Employee.class)
                    .setParameter("userID", userID)
                    .getResultList();
            return result.isEmpty() ? null : result.get(0);
        } catch (Exception exception) {
            throw new RuntimeException("Unable to find employee information.", exception);
        } finally {
            close(em);
        }
    }

    public List<String> getAllPositions() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createNamedQuery("Employee.findDistinctPositions", String.class).getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load position list.", exception);
        } finally {
            close(em);
        }
    }

    public List<String> getAllShifts() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            return em.createNamedQuery("Employee.findDistinctShifts", String.class).getResultList();
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load shift list.", exception);
        } finally {
            close(em);
        }
    }

    /*
     * Next free employee ID (e.g. NV32). The matching username is the same text
     * in lower case (nv32), so the number is skipped when that username is
     * already taken by any account. Throws instead of guessing, because a wrong
     * ID would collide with an existing employee.
     */
    public String generateEmployeeID() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            List<String> ids = em.createQuery("SELECT e.employeeID FROM Employee e", String.class).getResultList();
            int number = EmployeeIdGenerator.nextNumber(ids);
            while (usernameTaken(em, EmployeeIdGenerator.usernameOf(EmployeeIdGenerator.formatId(number)))) {
                number++;
            }
            return EmployeeIdGenerator.formatId(number);
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to generate the next employee ID.", exception);
        } finally {
            close(em);
        }
    }

    private boolean usernameTaken(EntityManager em, String username) {
        Long count = em.createQuery("SELECT COUNT(u) FROM Users u WHERE u.username = :username", Long.class)
                .setParameter("username", username)
                .getSingleResult();
        return count != null && count > 0;
    }

    private String normalizeEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private boolean emailExists(EntityManager em, String email, String excludeEmployeeID) {
        String jpql = "SELECT COUNT(e) FROM Employee e WHERE LOWER(e.email) = :email";
        boolean excludeCurrentEmployee = excludeEmployeeID != null && !excludeEmployeeID.trim().isEmpty();

        if (excludeCurrentEmployee) {
            jpql += " AND e.employeeID <> :employeeID";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("email", email);

        if (excludeCurrentEmployee) {
            query.setParameter("employeeID", excludeEmployeeID.trim());
        }

        return query.getSingleResult() > 0;
    }

    private void ensureEmailAvailable(EntityManager em, String email, String excludeEmployeeID) {
        if (email != null && emailExists(em, email, excludeEmployeeID)) {
            throw new IllegalArgumentException("Email is already used by another employee.");
        }
    }

    private boolean phoneExists(EntityManager em, String phone, String excludeEmployeeID) {
        String jpql = "SELECT COUNT(e) FROM Employee e WHERE e.phone = :phone";
        boolean excludeCurrentEmployee = excludeEmployeeID != null && !excludeEmployeeID.trim().isEmpty();

        if (excludeCurrentEmployee) {
            jpql += " AND e.employeeID <> :employeeID";
        }

        TypedQuery<Long> query = em.createQuery(jpql, Long.class);
        query.setParameter("phone", phone);

        if (excludeCurrentEmployee) {
            query.setParameter("employeeID", excludeEmployeeID.trim());
        }

        return query.getSingleResult() > 0;
    }

    // EMPLOYEE.Phone is UNIQUE in the database; report a clear message instead of a SQL error.
    private void ensurePhoneAvailable(EntityManager em, String phone, String excludeEmployeeID) {
        if (phone != null && !phone.trim().isEmpty() && phoneExists(em, phone.trim(), excludeEmployeeID)) {
            throw new IllegalArgumentException("Phone number is already used by another employee.");
        }
    }

    // USERS.Username is UNIQUE in the database.
    private void ensureUsernameAvailable(EntityManager em, String username) {
        Long count = em.createQuery("SELECT COUNT(u) FROM Users u WHERE u.username = :username", Long.class)
                .setParameter("username", username.trim())
                .getSingleResult();
        if (count != null && count > 0) {
            throw new IllegalArgumentException("Username is already taken.");
        }
    }

    private void validateEmployee(Employee employee) {
        if (employee == null) {
            throw new IllegalArgumentException("Employee must not be null.");
        }

        if (employee.getEmployeeID() == null || employee.getEmployeeID().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee ID is required.");
        }

        if (employee.getFullName() == null || employee.getFullName().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee name is required.");
        }

        if (employee.getPosition() == null || employee.getPosition().trim().isEmpty()) {
            throw new IllegalArgumentException("Employee position is required.");
        }

        String email = normalizeEmail(employee.getEmail());

        if (email != null) {
            if (email.length() > 100) {
                throw new IllegalArgumentException("Email must not exceed 100 characters.");
            }

            if (!email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
                throw new IllegalArgumentException("Invalid email address.");
            }
        }

        employee.setEmail(email);
    }

    private void validateUser(Users user) {
        if (user == null) {
            throw new IllegalArgumentException("User account must not be null.");
        }

        if (user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            throw new IllegalArgumentException("Username is required.");
        }

        if (user.getPassword() == null || user.getPassword().trim().isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }

        if (user.getRole() == null || user.getRole().trim().isEmpty()) {
            throw new IllegalArgumentException("User role is required.");
        }
    }

    private void rollback(EntityManager em) {
        if (em != null && em.getTransaction().isActive()) {
            em.getTransaction().rollback();
        }
    }

    private void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}