package util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Pure date arithmetic for the dashboard's room-utilization report.
 *
 * A "room-day" is one room occupied for one night. A stay from check-in
 * (inclusive) to check-out (exclusive) occupies the nights
 * checkIn, checkIn + 1, ..., checkOut - 1; the check-out day itself is free
 * again for the next guest.
 */
public final class RoomUsageCalculator {

    private RoomUsageCalculator() {
    }

    /**
     * Number of nights of the stay [checkIn, checkOut) that fall inside the
     * report period [from, to] (both inclusive). Returns 0 when they do not overlap.
     */
    public static long nightsInPeriod(LocalDate checkIn, LocalDate checkOut, LocalDate from, LocalDate to) {
        LocalDate first = checkIn.isAfter(from) ? checkIn : from;
        LocalDate lastExclusive = checkOut.isBefore(to.plusDays(1)) ? checkOut : to.plusDays(1);
        long nights = ChronoUnit.DAYS.between(first, lastExclusive);
        return Math.max(0, nights);
    }

    /**
     * Number of days in [from, to], both inclusive.
     */
    public static long daysInPeriod(LocalDate from, LocalDate to) {
        return ChronoUnit.DAYS.between(from, to) + 1;
    }

    /**
     * used / available as a percentage rounded to one decimal; 0 when nothing was available.
     */
    public static double occupancyRate(long used, long available) {
        if (available <= 0) {
            return 0.0;
        }
        return Math.round(used * 1000.0 / available) / 10.0;
    }
}
