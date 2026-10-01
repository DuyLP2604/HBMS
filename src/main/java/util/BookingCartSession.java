package util;

import dto.BookingCart;
import jakarta.servlet.http.HttpSession;

public final class BookingCartSession {

    public static final String ATTRIBUTE_NAME
            = "bookingCart";

    private BookingCartSession() {
    }

    public static BookingCart getOrCreate(
            HttpSession session) {

        if (session == null) {
            throw new IllegalArgumentException(
                    "HTTP session is required."
            );
        }

        Object value = session.getAttribute(
                ATTRIBUTE_NAME
        );

        if (value instanceof BookingCart) {
            return (BookingCart) value;
        }

        BookingCart cart = new BookingCart();

        session.setAttribute(
                ATTRIBUTE_NAME,
                cart
        );

        return cart;
    }

    public static BookingCart get(
            HttpSession session) {

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute(
                ATTRIBUTE_NAME
        );

        if (value instanceof BookingCart) {
            return (BookingCart) value;
        }

        return null;
    }

    public static void save(
            HttpSession session,
            BookingCart cart) {

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