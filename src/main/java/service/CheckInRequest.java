package service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.UUID;

/** Staff check-in request with the room balance and its payment reference token. */
public final class CheckInRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private final String token = UUID.randomUUID().toString();
    private final String bookingID;
    private final int actorUserID;
    private final int customerUserID;
    private final BigDecimal roomAmount;
    private final boolean deposit;
    CheckInRequest(String bookingID, int actorUserID, int customerUserID, BigDecimal roomAmount, boolean deposit) {
        this.bookingID = bookingID;
        this.actorUserID = actorUserID;
        this.customerUserID = customerUserID;
        this.roomAmount = roomAmount;
        this.deposit = deposit;
    }
    public String getToken() { return token; }
    public String getBookingID() { return bookingID; }
    public int getActorUserID() { return actorUserID; }
    public int getCustomerUserID() { return customerUserID; }
    public BigDecimal getRoomAmount() { return roomAmount; }
    public boolean isPayOsRoomBalance() { return deposit && roomAmount.signum() > 0; }
}
