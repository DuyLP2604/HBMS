package service;

import dao.BookingDAO;

/** Cancels unpaid bookings that pass their payment deadline. */
public class BookingExpirationService
{
    /** Run overdue booking cleanup for all customers. */
    public int expirePendingBookings()
    {
        return new BookingDAO().expirePendingBookings();
    }

    public int expirePendingBookings(String customerID)
    {
        if (customerID == null || customerID.isBlank() || customerID.trim().length() > 6)
        {
            throw new IllegalArgumentException("A valid customer ID is required.");
        }
        return new BookingDAO().expirePendingBookings(customerID.trim());
    }
}