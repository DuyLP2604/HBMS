package util;

import dto.BookingWishList;
import jakarta.servlet.http.HttpSession;

public final class BookingWishListSession {

    public static final String ATTRIBUTE_NAME = "bookingWishList";

    private BookingWishListSession() {
    }

    public static BookingWishList getOrCreate(HttpSession session) {
        if (session == null) {
            throw new IllegalArgumentException("HTTP session is required.");
        }

        Object value = session.getAttribute(ATTRIBUTE_NAME);
        if (value instanceof BookingWishList) {
            return (BookingWishList) value;
        }

        BookingWishList wishList = new BookingWishList();
        session.setAttribute(ATTRIBUTE_NAME, wishList);
        return wishList;
    }

    public static BookingWishList get(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(ATTRIBUTE_NAME);
        if (value instanceof BookingWishList) {
            return (BookingWishList) value;
        }
        return null;
    }

    public static void save(HttpSession session, BookingWishList wishList) {
        if (session == null) {
            throw new IllegalArgumentException("HTTP session is required.");
        }
        if (wishList == null) {
            throw new IllegalArgumentException("Booking wishlist is required.");
        }
        session.setAttribute(ATTRIBUTE_NAME, wishList);
    }

    public static void remove(HttpSession session) {
        if (session != null) {
            session.removeAttribute(ATTRIBUTE_NAME);
        }
    }
}
