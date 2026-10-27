package dto;

import entity.Booking;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import util.BookingTimePolicy;

/** A read-only snapshot of outstanding front-desk work, using hotel-local time. */
public final class ReceptionDashboard {
    private static final int ROW_LIMIT = 8;
    private final String focus;
    private final List<Task> tasks = new ArrayList<>();
    private int arrivals;
    private int departures;
    private int assignments;
    private int assignmentsToday;
    private int reviews;

    private ReceptionDashboard(String focus) {
        this.focus = List.of("arrivals", "departures", "assignments", "review").contains(focus)
                ? focus : "all";
    }

    public static ReceptionDashboard from(List<Booking> bookings, LocalDateTime now, String focus) {
        ReceptionDashboard dashboard = new ReceptionDashboard(focus == null ? "all" : focus);
        LocalDate today = now.toLocalDate();
        Map<String, Booking> unique = new LinkedHashMap<>();
        for (Booking booking : bookings) {
            if (booking != null && booking.getBookingID() != null) {
                unique.putIfAbsent(booking.getBookingID().trim(), booking);
            }
        }
        for (Booking booking : unique.values()) {
            if (booking.getCheckInDate() == null || booking.getCheckOutDate() == null) continue;
            LocalDate arrival = stayDate(booking.getCheckInDate());
            LocalDate departure = stayDate(booking.getCheckOutDate());
            String status = booking.getBookingStatus();
            boolean waiting = "CONFIRMED".equals(status) || "ASSIGNED".equals(status);
            boolean arrivesToday = waiting && arrival.equals(today);
            boolean departureDue = "CHECKED_IN".equals(status) && !departure.isAfter(today);
            boolean needsRoom = BookingTimePolicy.needsRoomAssignment(booking, now);
            boolean lateArrival = BookingTimePolicy.isNoShow(booking, now);
            boolean unresolvedPastArrival = waiting && arrival.isBefore(today);
            boolean expiredArrival = waiting && !arrival.isAfter(today) && !departure.isAfter(today);
            boolean review = lateArrival || unresolvedPastArrival || expiredArrival || (departureDue && departure.isBefore(today));
            if (arrivesToday) dashboard.arrivals++;
            if (departureDue) dashboard.departures++;
            if (needsRoom) dashboard.assignments++;
            if (needsRoom && arrival.equals(today)) dashboard.assignmentsToday++;
            if (review) dashboard.reviews++;

            Task task = null;
            if (departureDue) {
                task = new Task(booking, departure, departure.isBefore(today) ? 0 : 3,
                        departure.isBefore(today) ? "Departure overdue" : "Check-out due",
                        departure.isBefore(today) ? "attention" : "departure",
                        "/CheckoutEmployee", "Check out", "detail");
            } else if (lateArrival || unresolvedPastArrival || expiredArrival) {
                task = new Task(booking, arrival, 1,
                        lateArrival ? "Arrival overdue" : "Arrival needs review",
                        "attention", "/staff/stays", "Review stays", null);
            } else if (needsRoom) {
                task = new Task(booking, arrival, arrival.isAfter(today) ? 5 : 2,
                        arrival.isAfter(today) ? "Upcoming room assignment" : "Needs room assignment", "assignment",
                        "/staff/bookings", "Assign rooms", null);
            } else if (waiting && !arrival.isAfter(today) && departure.isAfter(today)) {
                boolean canCheckIn = arrival.equals(today) && !now.isBefore(BookingTimePolicy.checkInAt(arrival));
                task = new Task(booking, arrival, 4,
                        canCheckIn ? "Ready for check-in" : "Check-in from 14:00",
                        "arrival", canCheckIn ? "/process-checkin" : "/staff/stays",
                        canCheckIn ? "Check in" : "View stays", null);
            }
            if (task != null) {
                task.arrivesToday = arrivesToday;
                task.departureDue = departureDue;
                task.needsRoom = needsRoom;
                task.upcoming = needsRoom && arrival.isAfter(today);
                task.review = review;
                task.dueLabel = task.date.equals(today) ? "Today" : task.date.format(DateTimeFormatter.ofPattern("dd MMM", Locale.ENGLISH));
                dashboard.tasks.add(task);
            }
        }
        dashboard.tasks.sort(Comparator.comparingInt((Task task) -> task.priority)
                .thenComparing(task -> task.date).thenComparing(Task::getBookingID));
        return dashboard;
    }

    private static LocalDate stayDate(java.util.Date value) {
        return new Date(value.getTime()).toLocalDate();
    }

    private boolean matches(Task task) {
        switch (focus) {
            case "arrivals": return task.arrivesToday;
            case "departures": return task.departureDue;
            case "assignments": return task.needsRoom;
            case "review": return task.review;
            default: return !task.upcoming;
        }
    }

    public String getFocus() { return focus; }
    public int getArrivals() { return arrivals; }
    public int getDepartures() { return departures; }
    public int getAssignments() { return assignments; }
    public int getAssignmentsToday() { return assignmentsToday; }
    public int getAssignmentsUpcoming() { return assignments - assignmentsToday; }
    public int getReviews() { return reviews; }
    public int getTotal() { return (int) tasks.stream().filter(task -> !task.upcoming).count(); }
    public int getFilteredTotal() { return (int) tasks.stream().filter(this::matches).count(); }
    public List<Task> getTasks() {
        return Collections.unmodifiableList(tasks.stream().filter(this::matches).limit(ROW_LIMIT).collect(Collectors.toList()));
    }

    public static final class Task {
        private final String bookingID;
        private final String guestName;
        private final String phone;
        private final String label;
        private final String tone;
        private final String actionPath;
        private final String actionLabel;
        private final String action;
        private final LocalDate date;
        private final int priority;
        private String dueLabel;
        private boolean arrivesToday;
        private boolean departureDue;
        private boolean needsRoom;
        private boolean upcoming;
        private boolean review;

        private Task(Booking booking, LocalDate date, int priority, String label, String tone,
                String actionPath, String actionLabel, String action) {
            this.bookingID = booking.getBookingID().trim();
            this.guestName = booking.getCustomerID() == null ? "Guest" : booking.getCustomerID().getFullName();
            this.phone = booking.getCustomerID() == null ? null : booking.getCustomerID().getPhone();
            this.date = date;
            this.priority = priority;
            this.label = label;
            this.tone = tone;
            this.actionPath = actionPath;
            this.actionLabel = actionLabel;
            this.action = action;
        }

        public String getBookingID() { return bookingID; }
        public String getGuestName() { return guestName; }
        public String getPhone() { return phone; }
        public String getLabel() { return label; }
        public String getTone() { return tone; }
        public String getDueLabel() { return dueLabel; }
        public String getActionPath() { return actionPath; }
        public String getActionLabel() { return actionLabel; }
        public String getAction() { return action; }
    }
}
