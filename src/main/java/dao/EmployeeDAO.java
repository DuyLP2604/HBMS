/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Employee;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.util.List;

/**
 *
 * @author TAN LOI
 */
public class EmployeeDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public List<Employee> getAllEmployees() {
        EntityManager em = emf.createEntityManager();
        try {
            String spql = "SLECT e FROM EMPLOYEE e";
            TypedQuery<Employee> query = em.createQuery(spql, Employee.class);
            return query.getResultList();
        } catch (Exception e) {
        }
        return null;
    }

    public Employee getEmployeeById(String id) {
        try (EntityManager em = emf.createEntityManager()) {
            return em.find(Employee.class, id);
        } catch (Exception e) {
        }
        return null;
    }

    /**
     * Thêm nhân viên mới. Bắt buộc phải có HotelID và UserID (tài khoản đăng
     * nhập) vì EMPLOYEE JOIN USERS là INNER JOIN.
     *
     * @param e
     */
    public void insertEmployee(Employee e) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(e);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }

    public void updateEmployee(Employee e) {
        EntityManager em = emf.createEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(e);
            em.getTransaction().commit();
        } catch (Exception ex) {
            if (em.getTransaction().isActive()) {
                em.getTransaction().rollback();
            }
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
    }
}
