/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.Nationality;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.List;
import util.PersistenceManager;

/**
 *
 * @author default
 */
public class NationalityDAO {

    public List<Nationality> getAll() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            String jpql = "SELECT n FROM Nationality n";
            TypedQuery<Nationality> query = em.createQuery(jpql, Nationality.class);
            return query.getResultList();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    public Nationality getNationalityById(String nationalityId) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            return em.find(Nationality.class, nationalityId);
        } catch (Exception e) {
        }
        return null;
    }

    public static void main(String[] args) {
        NationalityDAO dao = new NationalityDAO();
        System.out.println(dao.getAll());
    }
}
