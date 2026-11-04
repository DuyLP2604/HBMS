package service;

import dao.BookingDAO;
import dao.PayOsOrderDAO;
import dao.PayOsOrderDAO.Order;
import java.math.BigDecimal;
import java.net.URI;
import java.time.Instant;
import service.CheckInService.CheckInRequest;

public class CheckInPayOsService {
    private final PayOsOrderDAO orders = new PayOsOrderDAO();
    private final PayOsService gateway = new PayOsService();

    public Order openOrder(CheckInRequest request, String returnUrl) throws Exception {
        if (!request.isPayOsRoomBalance()) {
            throw new IllegalArgumentException("VietQR at check-in requires a paid deposit and an outstanding room balance.");
        }
        String bookingID = request.getBookingID();
        return orders.withCheckInLock(bookingID, connection -> {
            CheckInRequest current = new CheckInService().createRequest(bookingID, request.getActorUserID(),
                    new BookingDAO().getPaymentSummary(bookingID));
            BigDecimal amount = current.getRoomAmount();
            if (!current.isPayOsRoomBalance() || amount.compareTo(request.getRoomAmount()) != 0) {
                throw new IllegalStateException("The room balance changed. Reload check-in before collecting payment.");
            }
            Order active = orders.findCheckInOrder(connection, bookingID, null);
            if (active != null) {
                return active;
            }

            Order order = new Order();
            order.orderCode = PayOsService.newOrderCode();
            order.bookingID = bookingID;
            order.userID = current.getCustomerUserID();
            order.externalAmount = amount;
            order.status = "PENDING";
            order.checkInExpiresAt = Instant.now().plusSeconds(900).getEpochSecond();

            PayOsService.Link link = gateway.createPaymentLink(order.orderCode, amount, bookingID,
                    returnUrl, returnUrl, order.checkInExpiresAt);
            URI checkout = URI.create(link.checkoutUrl);
            if (!"https".equalsIgnoreCase(checkout.getScheme()) || !"pay.payos.vn".equalsIgnoreCase(checkout.getHost())
                    || checkout.getRawUserInfo() != null || link.checkoutUrl.length() > 500
                    || link.qrCode.isBlank() || link.qrCode.length() > 2000 || link.paymentLinkId.isBlank()) {
                throw new IllegalStateException("PayOS did not return a valid checkout link and QR code.");
            }
            order.checkInQrCode = link.qrCode;
            order.checkInCheckoutUrl = link.checkoutUrl;
            // Save the order before showing its QR code.
            orders.insertCheckInOrder(connection, order, current.getActorUserID(), link.paymentLinkId);
            return order;
        });
    }

    public static String status(Order order) {
        if (order == null) return "NONE";
        if ("PAID".equals(order.status) || "FAILED".equals(order.status)) return order.status;
        return order.checkInExpiresAt > Instant.now().getEpochSecond() ? "PENDING" : "EXPIRED";
    }
}
