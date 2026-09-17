package dao;

import dto.CustomerSource;
import dto.RevenueChart;
import entity.Payment;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class DashboardDAO {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    /*
     * Revenue is calculated from successful payments,
     * not merely from generated invoices.
     */
    public long getTodayRevenue() {
        LocalDate today = LocalDate.now();

        BigDecimal revenue = getPaidRevenue(
                today,
                today
        );

        return revenue.longValue();
    }

    /*
     * Both startDate and endDate are inclusive.
     */
    public long getRevenueByDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(startDate, endDate);

        BigDecimal revenue = getPaidRevenue(
                startDate,
                endDate
        );

        return revenue.longValue();
    }

    private BigDecimal getPaidRevenue(
            LocalDate startDate,
            LocalDate endDate) {

        EntityManager em = EMF.createEntityManager();

        try {
            LocalDate endExclusive = endDate.plusDays(1);

            String jpql
                    = "SELECT SUM(p.amount) "
                    + "FROM Payment p "
                    + "WHERE p.status = :status "
                    + "AND p.paymentTime >= :startTime "
                    + "AND p.paymentTime < :endTime";

            TypedQuery<BigDecimal> query
                    = em.createQuery(
                            jpql,
                            BigDecimal.class
                    );

            query.setParameter("status", "PAID");

            query.setParameter(
                    "startTime",
                    Timestamp.valueOf(
                            startDate.atStartOfDay()
                    )
            );

            query.setParameter(
                    "endTime",
                    Timestamp.valueOf(
                            endExclusive.atStartOfDay()
                    )
            );

            BigDecimal result = query.getSingleResult();

            return result != null
                    ? result
                    : BigDecimal.ZERO;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to calculate paid revenue.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Counts reserved rooms based on BookingDetail.Quantity.
     * This also counts bookings that have not yet received
     * concrete room assignments.
     */
    public int getOccupiedRoomsCount(
            LocalDate startDate,
            LocalDate endDate) {

        validateDateRange(startDate, endDate);

        EntityManager em = EMF.createEntityManager();

        try {
            LocalDate endExclusive = endDate.plusDays(1);

            String jpql
                    = "SELECT SUM(bd.quantity) "
                    + "FROM BookingDetail bd "
                    + "JOIN bd.bookingID b "
                    + "WHERE b.bookingStatus IN ("
                    + "    :pendingPayment, "
                    + "    :confirmed, "
                    + "    :assigned, "
                    + "    :checkedIn"
                    + ") "
                    + "AND b.checkInDate < :endDate "
                    + "AND b.checkOutDate > :startDate";

            TypedQuery<Long> query = em.createQuery(
                    jpql,
                    Long.class
            );

            query.setParameter(
                    "pendingPayment",
                    "PENDING_PAYMENT"
            );

            query.setParameter(
                    "confirmed",
                    "CONFIRMED"
            );

            query.setParameter(
                    "assigned",
                    "ASSIGNED"
            );

            query.setParameter(
                    "checkedIn",
                    "CHECKED_IN"
            );

            query.setParameter(
                    "startDate",
                    Date.valueOf(startDate)
            );

            query.setParameter(
                    "endDate",
                    Date.valueOf(endExclusive)
            );

            Long count = query.getSingleResult();

            return count != null
                    ? count.intValue()
                    : 0;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to count reserved rooms.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Only ACTIVE rooms are counted because maintenance
     * and inactive rooms cannot be sold.
     */
    public int getTotalRoomsCount() {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT COUNT(r) "
                    + "FROM Room r "
                    + "WHERE r.status = :status";

            TypedQuery<Long> query = em.createQuery(
                    jpql,
                    Long.class
            );

            query.setParameter("status", "ACTIVE");

            Long count = query.getSingleResult();

            return count != null
                    ? count.intValue()
                    : 0;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to count active rooms.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    public int getTotalCustomers() {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT COUNT(c) "
                    + "FROM Customer c";

            TypedQuery<Long> query = em.createQuery(
                    jpql,
                    Long.class
            );

            Long count = query.getSingleResult();

            return count != null
                    ? count.intValue()
                    : 0;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to count customers.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    /*
     * Returns exactly ten calendar days in ascending order.
     * Days without revenue are included with zero revenue.
     */
    public List<RevenueChart> getRevenueLastTenDays() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(9);

        Map<LocalDate, BigDecimal> revenueByDate
                = new LinkedHashMap<>();

        for (int index = 0; index < 10; index++) {
            revenueByDate.put(
                    startDate.plusDays(index),
                    BigDecimal.ZERO
            );
        }

        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT p "
                    + "FROM Payment p "
                    + "WHERE p.status = :status "
                    + "AND p.paymentTime >= :startTime "
                    + "AND p.paymentTime < :endTime "
                    + "ORDER BY p.paymentTime";

            TypedQuery<Payment> query = em.createQuery(
                    jpql,
                    Payment.class
            );

            query.setParameter("status", "PAID");

            query.setParameter(
                    "startTime",
                    Timestamp.valueOf(
                            startDate.atStartOfDay()
                    )
            );

            query.setParameter(
                    "endTime",
                    Timestamp.valueOf(
                            endDate.plusDays(1)
                                    .atStartOfDay()
                    )
            );

            for (Payment payment : query.getResultList()) {
                if (payment.getPaymentTime() == null
                        || payment.getAmount() == null) {
                    continue;
                }

                LocalDate paymentDate = toLocalDate(
                        payment.getPaymentTime()
                );

                BigDecimal oldRevenue
                        = revenueByDate.get(paymentDate);

                if (oldRevenue != null) {
                    revenueByDate.put(
                            paymentDate,
                            oldRevenue.add(
                                    payment.getAmount()
                            )
                    );
                }
            }
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load the revenue chart.",
                    exception
            );
        } finally {
            close(em);
        }

        DateTimeFormatter formatter
                = DateTimeFormatter.ofPattern(
                        "dd/MM/yyyy"
                );

        List<RevenueChart> result
                = new ArrayList<>();

        for (Map.Entry<LocalDate, BigDecimal> entry
                : revenueByDate.entrySet()) {

            result.add(
                    new RevenueChart(
                            entry.getKey().format(formatter),
                            entry.getValue()
                    )
            );
        }

        return result;
    }

    public List<CustomerSource> getCustomerSource() {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT "
                    + "COALESCE(n.nationalityName, 'Unknown'), "
                    + "COUNT(c) "
                    + "FROM Customer c "
                    + "LEFT JOIN c.nationalityID n "
                    + "GROUP BY n.nationalityName "
                    + "ORDER BY COUNT(c) DESC";

            TypedQuery<Object[]> query = em.createQuery(
                    jpql,
                    Object[].class
            );

            List<CustomerSource> result
                    = new ArrayList<>();

            for (Object[] row : query.getResultList()) {
                String country = (String) row[0];

                long total = ((Number) row[1])
                        .longValue();

                result.add(
                        new CustomerSource(
                                country,
                                total
                        )
                );
            }

            return result;
        } catch (Exception exception) {
            throw new IllegalStateException(
                    "Unable to load customer-source data.",
                    exception
            );
        } finally {
            close(em);
        }
    }

    private void validateDateRange(
            LocalDate startDate,
            LocalDate endDate) {

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException(
                    "Start date and end date are required."
            );
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException(
                    "End date must not be before start date."
            );
        }
    }

    private LocalDate toLocalDate(
            java.util.Date date) {

        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date)
                    .toLocalDate();
        }

        return date.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();
    }

    private void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}