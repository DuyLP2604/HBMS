/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dao;

import entity.RevenueChart;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import entity.CustomerSource;
import java.math.BigDecimal;

/**
 *
 * @author ADMIN
 */
public class DashboardDAO {

    EntityManagerFactory emf = Persistence.createEntityManagerFactory("my_persistence_unit");

    public long getTodayRevenue() {
        try (EntityManager em = emf.createEntityManager()) {
            //coalesce = isnull
            String jpql = "SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.invoiceDate = CURRENT_DATE";
            TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
            BigDecimal result = query.getSingleResult();
            if (result != null) {
                return result.longValue();
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public long getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT COALESCE(SUM(i.totalAmount), 0) FROM Invoice i WHERE i.invoiceDate BETWEEN :startDate AND :endDate";
            TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);

            query.setParameter("startDate", java.sql.Date.valueOf(startDate));
            query.setParameter("endDate", java.sql.Date.valueOf(endDate));

            BigDecimal result = query.getSingleResult();
            return result != null ? result.longValue() : 0;
        } catch (Exception e) {
        }
        return 0;
    }

    public int getOccupiedRoomsCount(LocalDate startDate, LocalDate endDate) {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT COUNT(DISTINCT b.roomID) FROM Booking b WHERE b.checkInDate <= :endDate AND b.checkOutDate >= :startDate";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("endDate", java.sql.Date.valueOf(endDate));
            query.setParameter("startDate", java.sql.Date.valueOf(startDate));

            Long count = query.getSingleResult();
            if (count != null) {
                return count.intValue();
            }
        } catch (Exception e) {
        }
        return 0;
    }

    public int getTotalRoomsCount() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT COUNT(r) FROM Room r";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
        }
        return 0;
    }

    public int getTotalCustomers() {
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT COUNT(c) FROM Customer c";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception e) {
        }
        return 0;
    }

    public List<RevenueChart> getRevenueLastTenDays() {
        List<RevenueChart> list = new ArrayList<>();
        try (EntityManager em = emf.createEntityManager()) {
            String jpql = "SELECT i.invoiceDate, SUM(i.totalAmount) FROM Invoice i GROUP BY i.invoiceDate ORDER BY i.invoiceDate DESC";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            query.setMaxResults(10);

            List<Object[]> results = query.getResultList();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy");

            for (Object[] row : results) {
                java.util.Date date = (java.util.Date) row[0];
                BigDecimal revenue = (BigDecimal) row[1];
                list.add(new RevenueChart(sdf.format(date), revenue.longValue()));
            }
        } catch (Exception e) {
        }

        Collections.reverse(list);
        return list;
    }

    public List<CustomerSource> getCustomerSource() {
        List<CustomerSource> list = new ArrayList<>();
        EntityManager em = emf.createEntityManager();
        String jpql = "SELECT c.nationalityID.nationalityName, COUNT(c) "
                + "FROM Customer c "
                + "GROUP BY c.nationalityID.nationalityName";

        try {
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            List<Object[]> results = query.getResultList();
            for (Object[] row : results) {
                String nationalityName = (String) row[0];
                int total = ((Number) row[1]).intValue();
                list.add(new CustomerSource(nationalityName, total));
            }
        } catch (Exception e) {
        } finally {
            if (em != null && em.isOpen()) {
                em.close();
            }
        }
        return list;
    }
}
