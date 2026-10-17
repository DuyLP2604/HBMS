package dao;

import dto.RoomTypeAvailability;
import entity.RoomType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.ParameterMode;
import jakarta.persistence.StoredProcedureQuery;
import jakarta.persistence.TypedQuery;
import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import util.PersistenceManager;

public class RoomTypeDAO extends DAOFramework<RoomType> {

    public RoomTypeDAO() {
        super(RoomType.class);
    }

    @Override
    public boolean deleteById(String id) {
        if (isRemovable(id)) {
            return super.deleteById(id);
        }
        return false;
    }

    private boolean isRemovable(String id) {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            // Query đếm số lượng Room đang thuộc RoomType này
            String jpql = "SELECT COUNT(r) FROM Room r WHERE r.roomTypeID.roomTypeID = :id";

            Long count = em.createQuery(jpql, Long.class)
                    .setParameter("id", id)
                    .getSingleResult();

            // Nếu count == 0 nghĩa là không còn phòng nào -> cho phép xóa (trả về true)
            return count != null && count == 0;
        } catch (Exception e) {
        }
        // Nếu xảy ra lỗi hệ thống/DB, mặc định trả về false để đảm bảo an toàn dữ liệu
        return false;
    }

    public String generateRoomTypeID() {
        try (EntityManager em = PersistenceManager.createEntityManager()) {
            // Lấy số lớn nhất từ chuỗi mã loại phòng (cắt từ vị trí số 3 vì 2 ký tự đầu là 'RT')
            String jpql = "SELECT MAX("
                    + "CAST(SUBSTRING(rt.roomTypeID, 3, LENGTH(rt.roomTypeID)) AS Integer)"
                    + ") FROM RoomType rt";
            TypedQuery<Integer> query = em.createQuery(jpql, Integer.class);
            Integer maxId = query.getSingleResult();

            if (maxId != null) {
                return String.format("RT%02d", maxId + 1); // Trả về RT01, RT02, RT03...
            }
        } catch (Exception e) {
            e.printStackTrace(); // In ra lỗi để dễ debug nếu có vấn đề với Database
        }
        return "RT01"; // ID mặc định nếu bảng RoomType đang trống
    }

    public List<RoomTypeAvailability> getAvailability(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        validateDateRange(checkInDate, checkOutDate);

        EntityManager em = PersistenceManager.createEntityManager();

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
