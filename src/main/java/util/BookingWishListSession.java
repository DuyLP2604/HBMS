package util;

import dto.BookingWishList;
import jakarta.servlet.http.HttpSession;

public final class BookingWishListSession {

    public static final String ATTRIBUTE_NAME = "bookingCart";

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

        BookingWishList cart = new BookingWishList();

        session.setAttribute(ATTRIBUTE_NAME, cart);

        return cart;
    }

    public static BookingWishList get(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(
                ATTRIBUTE_NAME
        );

        if (value instanceof BookingWishList) {
            return (BookingWishList) value;
        }

        return null;
    }

    public static void save(
            HttpSession session,
            BookingWishList cart) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "HTTP session is required."
            );
        }

        if (cart == null) {
            throw new IllegalArgumentException(
                    "Booking cart is required."
            );
        }

        session.setAttribute(
                ATTRIBUTE_NAME,
                cart
        );
    }

    public static void remove(
            HttpSession session) {

        if (session != null) {
            session.removeAttribute(
                    ATTRIBUTE_NAME
            );
        }
    }
}
