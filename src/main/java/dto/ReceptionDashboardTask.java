package dto;

import entity.Booking;
import java.time.LocalDate;

/** One booking action shown on the reception dashboard. */
public final class ReceptionDashboardTask {
    private final String bookingID;
    private final String guestName;
    private final String phone;
    private final String label;
    private final String tone;
    private final String actionPath;
    private final String actionLabel;
    private final String action;
    final LocalDate date;
    final int priority;
    String dueLabel;
    boolean arrivesToday;
    boolean departureDue;
    boolean needsRoom;
    boolean upcoming;
    boolean review;

    ReceptionDashboardTask(Booking booking, LocalDate date, int priority, String label, String tone,
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
