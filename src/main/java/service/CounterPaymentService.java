package service;

import dao.BookingDAO;
import dao.PayOsOrderDAO;
import dto.PayOsLink;
import dto.PayOsOrder;
import entity.Booking;
import entity.Payment;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.sql.Connection;
import java.time.Instant;

/** Coordinates cash and PayOS payments at the front desk. */
public class CounterPaymentService {
    private final PayOsOrderDAO orders = new PayOsOrderDAO();
    private final PaymentService payments = new PaymentService();
    private final PayOsService gateway = new PayOsService();

    /** Open or reuse a PayOS order for the room balance due at check-in. */
    public PayOsOrder openCheckInOrder(CheckInRequest request, String returnUrl) throws Exception {
        if (!request.isPayOsRoomBalance()) {
            throw new IllegalArgumentException("VietQR at check-in requires a paid deposit and an outstanding room balance.");
        }
        String bookingID = request.getBookingID();
        return orders.withCounterPaymentLock(bookingID, connection -> {
            CheckInRequest current = new CheckInService().createRequest(bookingID, request.getActorUserID(),
                    new BookingDAO().getPaymentSummary(bookingID));
            BigDecimal amount = current.getRoomAmount();
            if (!current.isPayOsRoomBalance() || amount.compareTo(request.getRoomAmount()) != 0) {
                throw new IllegalStateException("The room balance changed. Reload check-in before collecting payment.");
            }
            PayOsOrder active = orders.findCheckInOrder(connection, bookingID, null);
            if (active != null) return active;
            return createOrder(connection, bookingID, current.getCustomerUserID(), "BALANCE", amount,
                    current.getActorUserID(), returnUrl, Instant.now().plusSeconds(900).getEpochSecond());
        });
    }

    /** Open or reuse a PayOS order for the initial counter payment. */
    public PayOsOrder openInitialOrder(PaymentRequest shown, String returnUrl) throws Exception {
        String bookingID = shown.getBookingID();
        return orders.withCounterPaymentLock(bookingID, connection -> {
            long deadline = validateInitialPayment(shown);
            PayOsOrder active = orders.findInitialCounterOrder(connection, bookingID, null);
            if (active != null) return active;
            requireNoOpenInitialOrder(connection, shown);
            return createOrder(connection, bookingID, shown.getUserID(), shown.getPaymentType(), shown.getAmount(),
                    null, returnUrl, deadline);
        });
    }

    /** Record the cash payment after checking for an active QR order. */
    public Payment collectInitialCash(PaymentRequest shown, int staffUserID) throws Exception {
        return orders.withCounterPaymentLock(shown.getBookingID(), connection -> {
            validateInitialPayment(shown);
            requireNoOpenInitialOrder(connection, shown);
            return payments.collectAtCounter(shown, "PT03", staffUserID, false, false, "");
        });
    }

    /** Block another initial payment while a PayOS order is open for the booking. */
    private void requireNoOpenInitialOrder(Connection connection, PaymentRequest shown) throws Exception {
        if (orders.hasOpenPaymentOrder(connection, shown.getBookingID(), shown.getPaymentType(), true)) {
            throw new IllegalStateException("Check the existing VietQR payment before collecting or generating another code.");
        }
    }

    /** Read the latest booking balance and check that it matches the amount shown to reception. */
    private long validateInitialPayment(PaymentRequest shown) {
        Booking booking = payments.getBookingForPayment(shown.getBookingID(), shown.getUserID());
        if (booking == null || !"PENDING_PAYMENT".equals(booking.getBookingStatus()) || booking.getPaymentDeadline() == null) {
            throw new IllegalStateException("This booking no longer accepts its initial payment. Reload the booking.");
        }
        long deadline = booking.getPaymentDeadline().getTime() / 1000;
        if (deadline <= Instant.now().getEpochSecond()) {
            throw new IllegalStateException("The booking payment deadline has passed. Reload the booking.");
        }
        PaymentRequest current = payments.createPaymentRequest(shown.getBookingID(), shown.getUserID());
        if ((!"DEPOSIT".equals(current.getPaymentType()) && !"FULL".equals(current.getPaymentType()))
                || !current.getPaymentType().equals(shown.getPaymentType())
                || current.getAmount().compareTo(shown.getAmount()) != 0) {
            throw new IllegalStateException("The payment amount changed. Reload the booking before collecting payment.");
        }
        return deadline;
    }

    private PayOsOrder createOrder(Connection connection, String bookingID, int customer, String paymentType,
            BigDecimal amount, Integer checkInStaffUserID, String returnUrl, long expiresAt) throws Exception {
        PayOsOrder order = new PayOsOrder();
        order.orderCode = PayOsService.newOrderCode();
        order.bookingID = bookingID;
        order.userID = customer;
        order.paymentType = paymentType;
        order.externalAmount = amount;
        order.status = "PENDING";
        order.checkInExpiresAt = expiresAt;

        PayOsLink link;
        try {
            link = gateway.createPaymentLink(order.orderCode, amount, bookingID,
                    returnUrl, returnUrl, order.checkInExpiresAt);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw ex;
        } catch (Exception ex) {
            throw new IOException("PayOS could not create the VietQR link.", ex);
        }
        URI checkout = URI.create(link.checkoutUrl);
        if (!"https".equalsIgnoreCase(checkout.getScheme()) || !"pay.payos.vn".equalsIgnoreCase(checkout.getHost())
                || checkout.getRawUserInfo() != null || link.checkoutUrl.length() > 500
                || link.qrCode.isBlank() || link.qrCode.length() > 2000 || link.paymentLinkId.isBlank()) {
            throw new IllegalStateException("PayOS did not return a valid checkout link and QR code.");
        }
        order.checkInQrCode = link.qrCode;
        order.checkInCheckoutUrl = link.checkoutUrl;
        orders.insertCounterOrder(connection, order, checkInStaffUserID, link.paymentLinkId);
        return order;
    }

    public static String status(PayOsOrder order) {
        if (order == null) return "NONE";
        if ("PAID".equals(order.status) || "FAILED".equals(order.status)) return order.status;
        return order.checkInExpiresAt > Instant.now().getEpochSecond() ? "PENDING" : "EXPIRED";
    }
}
