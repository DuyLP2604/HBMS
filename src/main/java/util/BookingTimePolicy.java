package util;

import entity.Booking;
import java.sql.Date;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BookingTimePolicy
{
    public static final ZoneId HOTEL_ZONE = ZoneId.of("Asia/Ho_Chi_Minh");
    private static final LocalTime SAME_DAY_CUTOFF = LocalTime.of(10, 0);
    private static final LocalTime CHECK_IN_TIME = LocalTime.of(14, 0);
    private static final LocalTime NO_SHOW_TIME = LocalTime.of(14, 15);

    private BookingTimePolicy()
    {
    }

    public static LocalDateTime now()
    {
        return LocalDateTime.now(Clock.system(HOTEL_ZONE));
    }

    public static LocalDate minimumCheckInDate(LocalDateTime now)
    {
        return now.toLocalTime().isBefore(SAME_DAY_CUTOFF) ? now.toLocalDate() : now.toLocalDate().plusDays(1);
    }

    public static void validateStayDates(LocalDate checkIn, LocalDate checkOut)
    {
        validateStayDates(checkIn, checkOut, now());
    }

    public static void validateStayDates(LocalDate checkIn, LocalDate checkOut, LocalDateTime now)
    {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn))
        {
            throw new IllegalArgumentException("Check-out date must be after check-in date.");
        }
        if (checkIn.isBefore(now.toLocalDate()))
        {
            throw new IllegalArgumentException("Check-in date cannot be in the past.");
        }
        if (checkIn.isBefore(minimumCheckInDate(now)))
        {
            throw new IllegalArgumentException("Same-day bookings are available only before 10:00 Vietnam time. From 10:00, select tomorrow or a later check-in date.");
        }
    }

    public static LocalDateTime checkInAt(LocalDate checkIn)
    {
        return checkIn.atTime(CHECK_IN_TIME);
    }

    public static boolean isNoShow(Booking booking, LocalDateTime now)
    {
        return booking != null && booking.getCheckInDate() != null && !booking.isWalkIn()
                && ("CONFIRMED".equals(booking.getBookingStatus()) || "ASSIGNED".equals(booking.getBookingStatus()))
                && !now.isBefore(stayDate(booking).atTime(NO_SHOW_TIME));
    }

    /** Read-only worklist eligibility; room availability is still checked when rooms are assigned. */
    public static boolean needsRoomAssignment(Booking booking, LocalDateTime now)
    {
        if (booking == null || !"CONFIRMED".equals(booking.getBookingStatus())
                || booking.getCheckInDate() == null || booking.getCheckOutDate() == null)
        {
            return false;
        }
        LocalDate arrival = stayDate(booking);
        LocalDate departure = new Date(booking.getCheckOutDate().getTime()).toLocalDate();
        return !arrival.isBefore(now.toLocalDate()) && departure.isAfter(arrival) && !isNoShow(booking, now);
    }

    public static Map<String, Boolean> prepareStaffList(List<Booking> bookings, LocalDateTime now)
    {
        bookings.sort(Comparator.comparingInt((Booking b) -> isNoShow(b, now) ? 0 : ("CHECKED_IN".equals(b.getBookingStatus()) ? 2 : 1))
                .thenComparing(BookingTimePolicy::stayDate)
                .thenComparing(b -> b.getBookingID() == null ? "" : b.getBookingID()));
        Map<String, Boolean> flags = new LinkedHashMap<>();
        for (Booking booking : bookings)
        {
            if (booking.getBookingID() != null)
            {
                flags.put(booking.getBookingID(), isNoShow(booking, now));
                flags.put(booking.getBookingID().trim(), isNoShow(booking, now));
            }
        }
        return flags;
    }

    private static LocalDate stayDate(Booking booking)
    {
        return booking.getCheckInDate() == null ? LocalDate.MAX : new Date(booking.getCheckInDate().getTime()).toLocalDate();
    }
}
