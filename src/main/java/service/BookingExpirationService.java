package service;

import dao.BookingDAO;

public class BookingExpirationService
{
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