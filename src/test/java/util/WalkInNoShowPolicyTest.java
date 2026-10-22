package util;

import entity.Booking;
import entity.Customer;
import entity.Users;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WalkInNoShowPolicyTest {
    private static int checks;
    private static Booking booking(String id, String bookedAt, String checkIn, Integer actor, String status) {
        Users owner = new Users(); owner.setUserID(100);
        Customer customer = new Customer(); customer.setUserID(owner);
        Booking b = new Booking(id);
        b.setCustomerID(customer);
        b.setBookedByUserID(actor);
        b.setBookingDate(Timestamp.valueOf(bookedAt));
        b.setCheckInDate(Date.valueOf(checkIn));
        b.setBookingStatus(status);
        return b;
    }
    private static void expect(boolean condition, String label) {
        if (!condition) throw new AssertionError(label);
        checks++;
    }
    public static void main(String[] args) {
        LocalDateTime now = LocalDateTime.of(2026, 10, 22, 16, 30);
        Booking walkIn = booking("BK0041", "2026-10-22 16:20:00", "2026-10-22", 10, "CONFIRMED");
        expect(walkIn.isWalkIn(), "Same-day receptionist booking is a walk-in");
        expect(!BookingTimePolicy.isNoShow(walkIn, now), "New paid walk-in must not be overdue at 16:30");
        walkIn.setBookingStatus("ASSIGNED");
        expect(!BookingTimePolicy.isNoShow(walkIn, now), "Assigned walk-in must not become a no-show");
        expect(!BookingTimePolicy.isNoShow(walkIn, now.plusDays(1)), "Already-arrived guest requires staff cancellation, not no-show");
        Booking advance = booking("BK0042", "2026-10-21 16:20:00", "2026-10-22", 10, "CONFIRMED");
        expect(!advance.isWalkIn(), "Counter booking made in advance retains ordinary arrival policy");
        expect(BookingTimePolicy.isNoShow(advance, now), "Advance booking still overdue after 14:15");
        expect(!BookingTimePolicy.isNoShow(advance, now.withHour(14).withMinute(14)), "No-show must not start early");
        expect(BookingTimePolicy.isNoShow(advance, now.withHour(14).withMinute(15)), "No-show starts at 14:15");
        Booking online = booking("BK0043", "2026-10-22 09:00:00", "2026-10-22", 100, "CONFIRMED");
        expect(!online.isWalkIn() && BookingTimePolicy.isNoShow(online, now), "Same-day self-service booking is not walk-in");
        online.setBookedByUserID(null);
        expect(!online.isWalkIn() && BookingTimePolicy.isNoShow(online, now), "Legacy booking retains its existing policy");
        advance.setBookingStatus("CHECKED_IN");
        expect(!BookingTimePolicy.isNoShow(advance, now), "Checked-in booking is never no-show");
        advance.setBookingStatus("CONFIRMED");
        List<Booking> list = new ArrayList<>(List.of(walkIn, advance));
        Map<String,Boolean> flags = BookingTimePolicy.prepareStaffList(list, now);
        expect(!flags.get("BK0041") && flags.get("BK0042") && list.get(0) == advance, "Staff badges and sort reflect corrected policy");
        expect(!BookingTimePolicy.isNoShow(null, now), "Missing booking is handled safely");
        System.out.println("PASS: " + checks + " walk-in/no-show regression checks");
    }
}
