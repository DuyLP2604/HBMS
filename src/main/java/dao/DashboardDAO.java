package dao;

import dto.CustomerSource;
import dto.RevenueChart;
import dto.RoomTypeUtilization;
import dto.RoomUtilizationReport;
import entity.Payment;
import entity.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TemporalType;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import util.PersistenceManager;
import util.RoomUsageCalculator;

public class DashboardDAO {

    // DashboardServlet sends this date as the start of the "All Time" range.
    private static final LocalDate ALL_TIME_START = LocalDate.of(2010, 1, 1);

    // Stays that really occupy a room. PENDING_PAYMENT (unpaid, may expire) and CANCELLED are excluded.
    // EclipseLink does not accept a List bound to IN (:param), so each status gets its own parameter.
    private static final String OCCUPYING_STATUS_JPQL = "b.bookingStatus IN (:s1, :s2, :s3, :s4)";

    private static <T> TypedQuery<T> bindOccupyingStatuses(TypedQuery<T> query) {
        return query.setParameter("s1", "CONFIRMED")
                .setParameter("s2", "ASSIGNED")
                .setParameter("s3", "CHECKED_IN")
                .setParameter("s4", "CHECKED_OUT");
    }

    public long getTodayRevenue() {
        LocalDate today = LocalDate.now();
        return getPaidRevenue(today, today).longValue();
    }

    public long getRevenueByDateRange(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        return getPaidRevenue(startDate, endDate).longValue();
    }

    private BigDecimal getPaidRevenue(LocalDate startDate, LocalDate endDate) {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            LocalDate endExclusive = endDate.plusDays(1);
            String jpql = "SELECT SUM(p.amount) FROM Payment p WHERE p.status = :status AND p.paymentTime >= :startTime AND p.paymentTime < :endTime";
            TypedQuery<BigDecimal> query = em.createQuery(jpql, BigDecimal.class);
            query.setParameter("status", "PAID");
            query.setParameter("startTime", Timestamp.valueOf(startDate.atStartOfDay()));
            query.setParameter("endTime", Timestamp.valueOf(endExclusive.atStartOfDay()));
            BigDecimal result = query.getSingleResult();
            return result != null ? result : BigDecimal.ZERO;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to calculate paid revenue.", exception);
        } finally {
            close(em);
        }
    }

    public int getOccupiedRoomsCount(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            LocalDate endExclusive = endDate.plusDays(1);
            // ĐÃ SỬA: Đếm số lượng phòng khách đặt mua (dựa trên BookingDate)
            String jpql = "SELECT SUM(bd.quantity) FROM BookingDetail bd JOIN bd.bookingID b WHERE b.bookingStatus IN (:pendingPayment, :confirmed, :assigned, :checkedIn) AND b.bookingDate >= :startDate AND b.bookingDate < :endDate";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("pendingPayment", "PENDING_PAYMENT");
            query.setParameter("confirmed", "CONFIRMED");
            query.setParameter("assigned", "ASSIGNED");
            query.setParameter("checkedIn", "CHECKED_IN");
            query.setParameter("startDate", Timestamp.valueOf(startDate.atStartOfDay()));
            query.setParameter("endDate", Timestamp.valueOf(endExclusive.atStartOfDay()));
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to count reserved rooms.", exception);
        } finally {
            close(em);
        }
    }

    public int getTotalRoomsCount() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT COUNT(r) FROM Room r WHERE r.status = :status";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("status", "ACTIVE");
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to count active rooms.", exception);
        } finally {
            close(em);
        }
    }

    // ĐÃ SỬA: Đếm khách hàng theo thời gian tạo đơn
    public int getTotalCustomers(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            LocalDate endExclusive = endDate.plusDays(1);
            String jpql = "SELECT COUNT(DISTINCT b.customerID) FROM Booking b WHERE b.bookingDate >= :startDate AND b.bookingDate < :endDate";
            TypedQuery<Long> query = em.createQuery(jpql, Long.class);
            query.setParameter("startDate", Timestamp.valueOf(startDate.atStartOfDay()));
            query.setParameter("endDate", Timestamp.valueOf(endExclusive.atStartOfDay()));
            Long count = query.getSingleResult();
            return count != null ? count.intValue() : 0;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to count customers.", exception);
        } finally {
            close(em);
        }
    }

    // ĐÃ SỬA: Biểu đồ tự động chia nhóm
    public List<RevenueChart> getRevenueChartData(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            if (startDate.getYear() <= 2000) {
                String minDateJpql = "SELECT MIN(p.paymentTime) FROM Payment p WHERE p.status = 'PAID'";
                java.util.Date firstDate = em.createQuery(minDateJpql, java.util.Date.class).getSingleResult();
                startDate = firstDate != null ? toLocalDate(firstDate) : LocalDate.now();
            }

            LocalDate endExclusive = endDate.plusDays(1);
            long daysBetween = ChronoUnit.DAYS.between(startDate, endDate);

            String jpql = "SELECT p FROM Payment p WHERE p.status = 'PAID' AND p.paymentTime >= :startTime AND p.paymentTime < :endTime ORDER BY p.paymentTime";
            TypedQuery<Payment> query = em.createQuery(jpql, Payment.class);
            query.setParameter("startTime", Timestamp.valueOf(startDate.atStartOfDay()));
            query.setParameter("endTime", Timestamp.valueOf(endExclusive.atStartOfDay()));
            List<Payment> payments = query.getResultList();

            Map<String, BigDecimal> buckets = new LinkedHashMap<>();

            if (daysBetween > 365) {
                for (int y = startDate.getYear(); y <= endDate.getYear(); y++) {
                    buckets.put(String.valueOf(y), BigDecimal.ZERO);
                }
                for (Payment p : payments) {
                    String key = String.valueOf(toLocalDate(p.getPaymentTime()).getYear());
                    if (buckets.containsKey(key)) {
                        buckets.put(key, buckets.get(key).add(p.getAmount()));
                    }
                }
            } else if (daysBetween > 60) {
                LocalDate current = startDate.withDayOfMonth(1);
                LocalDate endMonth = endDate.withDayOfMonth(1);
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yyyy");
                while (!current.isAfter(endMonth)) {
                    buckets.put(current.format(fmt), BigDecimal.ZERO);
                    current = current.plusMonths(1);
                }
                for (Payment p : payments) {
                    String key = toLocalDate(p.getPaymentTime()).format(fmt);
                    if (buckets.containsKey(key)) {
                        buckets.put(key, buckets.get(key).add(p.getAmount()));
                    }
                }
            } else if (daysBetween > 14) {
                LocalDate current = startDate;
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
                List<LocalDate> weekStarts = new ArrayList<>();
                while (!current.isAfter(endDate)) {
                    weekStarts.add(current);
                    LocalDate next = current.plusDays(6);
                    if (next.isAfter(endDate)) {
                        next = endDate;
                    }
                    buckets.put(current.format(fmt) + "-" + next.format(fmt), BigDecimal.ZERO);
                    current = current.plusDays(7);
                }
                for (Payment p : payments) {
                    LocalDate d = toLocalDate(p.getPaymentTime());
                    for (int i = 0; i < weekStarts.size(); i++) {
                        LocalDate ws = weekStarts.get(i);
                        LocalDate we = (i == weekStarts.size() - 1) ? endDate : weekStarts.get(i + 1).minusDays(1);
                        if (!d.isBefore(ws) && !d.isAfter(we)) {
                            String label = ws.format(fmt) + "-" + we.format(fmt);
                            buckets.put(label, buckets.get(label).add(p.getAmount()));
                            break;
                        }
                    }
                }
            } else {
                LocalDate current = startDate;
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM");
                while (!current.isAfter(endDate)) {
                    buckets.put(current.format(fmt), BigDecimal.ZERO);
                    current = current.plusDays(1);
                }
                for (Payment p : payments) {
                    String key = toLocalDate(p.getPaymentTime()).format(fmt);
                    if (buckets.containsKey(key)) {
                        buckets.put(key, buckets.get(key).add(p.getAmount()));
                    }
                }
            }

            List<RevenueChart> result = new ArrayList<>();
            for (Map.Entry<String, BigDecimal> entry : buckets.entrySet()) {
                result.add(new RevenueChart(entry.getKey(), entry.getValue()));
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load the revenue chart.", exception);
        } finally {
            close(em);
        }
    }

    public List<CustomerSource> getCustomerSource() {
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            String jpql = "SELECT COALESCE(n.nationalityName, 'Unknown'), COUNT(c) FROM Customer c LEFT JOIN c.nationalityID n GROUP BY n.nationalityName ORDER BY COUNT(c) DESC";
            TypedQuery<Object[]> query = em.createQuery(jpql, Object[].class);
            List<CustomerSource> result = new ArrayList<>();
            for (Object[] row : query.getResultList()) {
                String country = (String) row[0];
                long total = ((Number) row[1]).longValue();
                result.add(new CustomerSource(country, total));
            }
            return result;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load customer-source data.", exception);
        } finally {
            close(em);
        }
    }

    /**
     * Room-usage report for [startDate, endDate] (both inclusive).
     *
     * Used room-days: for every booking line (room type x quantity) the nights
     * of the stay that fall inside the period, times the quantity.
     * Available room-days: ACTIVE rooms x days in the period.
     * Occupancy = used / available.
     *
     * For the "All Time" range the period starts at the first check-in, so the
     * rate is not diluted by years in which the hotel had no bookings.
     */
    public RoomUtilizationReport getRoomUtilization(LocalDate startDate, LocalDate endDate) {
        validateDateRange(startDate, endDate);
        EntityManager em = PersistenceManager.createEntityManager();
        try {
            LocalDate from = startDate;
            if (!from.isAfter(ALL_TIME_START)) {
                java.util.Date firstCheckIn = bindOccupyingStatuses(em.createQuery(
                        "SELECT MIN(b.checkInDate) FROM Booking b WHERE " + OCCUPYING_STATUS_JPQL,
                        java.util.Date.class))
                        .getSingleResult();
                from = firstCheckIn != null ? toLocalDate(firstCheckIn) : endDate;
            }

            RoomUtilizationReport report = new RoomUtilizationReport();
            if (from.isAfter(endDate)) {
                report.setPeriodLabel("No bookings in this period");
                return report;
            }
            long totalDays = RoomUsageCalculator.daysInPeriod(from, endDate);

            // Active rooms per room type.
            Map<String, Integer> roomsByType = new LinkedHashMap<>();
            List<Object[]> roomRows = em.createQuery(
                    "SELECT r.roomTypeID.roomTypeID, COUNT(r) FROM Room r WHERE r.status = :status GROUP BY r.roomTypeID.roomTypeID",
                    Object[].class)
                    .setParameter("status", "ACTIVE")
                    .getResultList();
            for (Object[] row : roomRows) {
                roomsByType.put((String) row[0], ((Number) row[1]).intValue());
            }

            // Used room-days per room type.
            Map<String, Long> usedByType = new LinkedHashMap<>();
            List<Object[]> stayRows = bindOccupyingStatuses(em.createQuery(
                    "SELECT rt.roomTypeID, bd.quantity, b.checkInDate, b.checkOutDate "
                    + "FROM BookingDetail bd JOIN bd.bookingID b JOIN bd.roomTypeID rt "
                    + "WHERE " + OCCUPYING_STATUS_JPQL + " AND b.checkInDate <= :toDate AND b.checkOutDate > :fromDate",
                    Object[].class))
                    .setParameter("fromDate", java.sql.Date.valueOf(from), TemporalType.DATE)
                    .setParameter("toDate", java.sql.Date.valueOf(endDate), TemporalType.DATE)
                    .getResultList();
            for (Object[] row : stayRows) {
                long nights = RoomUsageCalculator.nightsInPeriod(
                        toLocalDate((java.util.Date) row[2]), toLocalDate((java.util.Date) row[3]), from, endDate);
                long roomDays = nights * ((Number) row[1]).longValue();
                usedByType.merge((String) row[0], roomDays, Long::sum);
            }

            List<RoomType> roomTypes = em.createQuery("SELECT rt FROM RoomType rt ORDER BY rt.typeName", RoomType.class)
                    .getResultList();

            List<RoomTypeUtilization> byType = new ArrayList<>();
            int totalRooms = 0;
            long totalUsed = 0;
            long totalAvailable = 0;
            for (RoomType roomType : roomTypes) {
                int rooms = roomsByType.getOrDefault(roomType.getRoomTypeID(), 0);
                long used = usedByType.getOrDefault(roomType.getRoomTypeID(), 0L);
                long available = rooms * totalDays;
                byType.add(new RoomTypeUtilization(roomType.getRoomTypeID(), roomType.getTypeName(), rooms,
                        used, available, RoomUsageCalculator.occupancyRate(used, available)));
                totalRooms += rooms;
                totalUsed += used;
                totalAvailable += available;
            }

            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            report.setPeriodLabel(from.equals(endDate) ? from.format(fmt) : from.format(fmt) + " - " + endDate.format(fmt));
            report.setTotalDays(totalDays);
            report.setActiveRooms(totalRooms);
            report.setUsedRoomDays(totalUsed);
            report.setAvailableRoomDays(totalAvailable);
            report.setOccupancyRate(RoomUsageCalculator.occupancyRate(totalUsed, totalAvailable));
            report.setByRoomType(byType);
            return report;
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to load the room utilization report.", exception);
        } finally {
            close(em);
        }
    }

    private void validateDateRange(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and end date are required.");
        }
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must not be before start date.");
        }
    }

    private LocalDate toLocalDate(java.util.Date date) {
        if (date instanceof java.sql.Date) {
            return ((java.sql.Date) date).toLocalDate();
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    private void close(EntityManager em) {
        if (em != null && em.isOpen()) {
            em.close();
        }
    }
}
