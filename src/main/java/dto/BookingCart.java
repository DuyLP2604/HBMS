package dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

public class BookingCart implements Serializable {

    private static final long serialVersionUID = 1L;

    private LocalDate checkInDate;
    private LocalDate checkOutDate;

    private final Map<String, BookingCartItem> itemMap
            = new LinkedHashMap<>();

    public BookingCart() {
    }

    public BookingCart(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        setStayDates(checkInDate, checkOutDate);
    }

    public LocalDate getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(LocalDate checkInDate) {
        this.checkInDate = checkInDate;

        if (checkInDate != null && checkOutDate != null) {
            validateDates(checkInDate, checkOutDate);
        }
    }

    public LocalDate getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(LocalDate checkOutDate) {
        this.checkOutDate = checkOutDate;

        if (checkInDate != null && checkOutDate != null) {
            validateDates(checkInDate, checkOutDate);
        }
    }

    public void setStayDates(
            LocalDate checkInDate,
            LocalDate checkOutDate) {

        validateDates(checkInDate, checkOutDate);

        this.checkInDate = checkInDate;
        this.checkOutDate = checkOutDate;
    }

    public Collection<BookingCartItem> getItems() {
        return new ArrayList<>(itemMap.values());
    }

    public BookingCartItem getItem(String roomTypeID) {
        if (roomTypeID == null) {
            return null;
        }

        return itemMap.get(roomTypeID.trim());
    }

    /**
     * Adds a new room type or replaces an existing item with the same
     * RoomTypeID.
     */
    public void addOrUpdateItem(BookingCartItem item) {
        if (item == null) {
            throw new IllegalArgumentException(
                    "Booking cart item is required."
            );
        }

        item.validateGuestCapacity();

        itemMap.put(
                item.getRoomTypeID(),
                item
        );
    }

    public boolean removeItem(String roomTypeID) {
        if (roomTypeID == null
                || roomTypeID.trim().isEmpty()) {

            return false;
        }

        return itemMap.remove(roomTypeID.trim()) != null;
    }

    public void clear() {
        itemMap.clear();
        checkInDate = null;
        checkOutDate = null;
    }

    public boolean isEmpty() {
        return itemMap.isEmpty();
    }

    public int getItemCount() {
        return itemMap.size();
    }

    public int getTotalRooms() {
        int totalRooms = 0;

        for (BookingCartItem item : itemMap.values()) {
            totalRooms += item.getQuantity();
        }

        return totalRooms;
    }

    public int getTotalGuests() {
        int totalGuests = 0;

        for (BookingCartItem item : itemMap.values()) {
            totalGuests += item.getGuestCount();
        }

        return totalGuests;
    }

    public long getNumberOfNights() {
        if (checkInDate == null || checkOutDate == null) {
            return 0;
        }

        return ChronoUnit.DAYS.between(
                checkInDate,
                checkOutDate
        );
    }

    public BigDecimal getTotalAmount() {
        long numberOfNights = getNumberOfNights();
        BigDecimal total = BigDecimal.ZERO;

        for (BookingCartItem item : itemMap.values()) {
            total = total.add(
                    item.calculateSubtotal(numberOfNights)
            );
        }

        return total;
    }

    public boolean hasValidDates() {
        return checkInDate != null
                && checkOutDate != null
                && checkOutDate.isAfter(checkInDate);
    }

    private void validateDates(
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

    @Override
    public String toString() {
        return "BookingCart{"
                + "checkInDate=" + checkInDate
                + ", checkOutDate=" + checkOutDate
                + ", numberOfNights=" + getNumberOfNights()
                + ", totalRooms=" + getTotalRooms()
                + ", totalGuests=" + getTotalGuests()
                + ", totalAmount=" + getTotalAmount()
                + '}';
    }
}