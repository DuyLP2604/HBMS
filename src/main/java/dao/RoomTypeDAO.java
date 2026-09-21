package dao;

import dto.RoomTypeAvailability;
import entity.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.Persistence;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class RoomTypeDAO {

    private static final EntityManagerFactory EMF
            = Persistence.createEntityManagerFactory(
                    "my_persistence_unit"
            );

    public List<RoomType> getAllRoomTypes() {
        EntityManager em = EMF.createEntityManager();

        try {
            String jpql
                    = "SELECT rt FROM RoomType rt "
                    + "ORDER BY rt.price ASC, rt.typeName ASC";

            TypedQuery<RoomType> query
                    = em.createQuery(jpql, RoomType.class);

            return query.getResultList();
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to load room types.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public RoomType getById(String roomTypeID) {
        if (roomTypeID == null || roomTypeID.trim().isEmpty()) {
            return null;
        }

        EntityManager em = EMF.createEntityManager();

        try {
            return em.find(
                    RoomType.class,
                    roomTypeID.trim()
            );
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to find the requested room type.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<RoomTypeAvailability> getAvailability(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        validateDateRange(checkInDate, checkOutDate);

        EntityManager em = EMF.createEntityManager();

        try {
            StoredProcedureQuery query
                    = em.createStoredProcedureQuery(
                            "SP_GET_ROOM_TYPE_AVAILABILITY"
                    );

            query.registerStoredProcedureParameter(
                    "CheckInDate",
                    Date.class,
                    ParameterMode.IN
            );

            query.registerStoredProcedureParameter(
                    "CheckOutDate",
                    Date.class,
                    ParameterMode.IN
            );

            query.setParameter(
                    "CheckInDate",
                    Date.valueOf(checkInDate)
            );

            query.setParameter(
                    "CheckOutDate",
                    Date.valueOf(checkOutDate)
            );

            @SuppressWarnings("unchecked")
            List<Object[]> rows = query.getResultList();

            List<RoomTypeAvailability> result
                    = new ArrayList<>();

            for (Object[] row : rows) {
                result.add(mapAvailability(row));
            }

            return result;
        } catch (Exception ex) {
            throw new RuntimeException(
                    "Unable to load room-type availability.",
                    ex
            );
        } finally {
            if (em.isOpen()) {
                em.close();
            }
        }
    }

    public List<RoomTypeAvailability> getAvailableRoomTypes(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        List<RoomTypeAvailability> availability
                = getAvailability(
                        checkInDate,
                        checkOutDate
                );

        List<RoomTypeAvailability> availableTypes
                = new ArrayList<>();

        for (RoomTypeAvailability item : availability) {
            if (item.isAvailable()) {
                availableTypes.add(item);
            }
        }

        return availableTypes;
    }

    private RoomTypeAvailability mapAvailability(Object[] row) {

        String roomTypeID = row[0] == null
                ? null
                : row[0].toString().trim();

        String typeName = row[1] == null
                ? null
                : row[1].toString();

        int capacity = toInt(row[2]);

        BigDecimal price = toBigDecimal(row[3]);

        String roomTypeImage = row[4] == null
                ? null
                : row[4].toString();

        long totalActiveRooms = toLong(row[5]);
        long reservedRooms = toLong(row[6]);
        long availableRooms = toLong(row[7]);

        return new RoomTypeAvailability(
                roomTypeID,
                typeName,
                capacity,
                price,
                roomTypeImage,
                totalActiveRooms,
                reservedRooms,
                availableRooms
        );
    }

    private void validateDateRange(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        if (checkInDate == null || checkOutDate == null) {
            throw new IllegalArgumentException(
                    "Check-in date and check-out date are required."
            );
        }

        if (!checkOutDate.isAfter(checkInDate)) {
            throw new IllegalArgumentException(
                    "Check-out date must be later than check-in date."
            );
        }
    }

    private int toInt(Object value) {
        if (value == null) {
            return 0;
        }

        return ((Number) value).intValue();
    }

    private long toLong(Object value) {
        if (value == null) {
            return 0L;
        }

        return ((Number) value).longValue();
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }

        if (value instanceof BigDecimal) {
            return (BigDecimal) value;
        }

        return new BigDecimal(value.toString());
    }

}
