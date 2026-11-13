package controller;

import dao.PayOsOrderDAO;
import dao.PaymentmethodDAO;
import dao.WalletDAO;
import dto.PayOsLink;
import dto.PayOsOrder;
import dto.WalletSummary;
import entity.Booking;
import entity.Payment;
import entity.Paymentmethod;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import service.PayOsService;
import service.PaymentRequest;
import service.PaymentService;
import util.PaymentPageData;

/** Handles online booking payments, PayOS redirects, and the payment result. */
@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"})
public class PaymentServlet extends HttpServlet {

    /** Open the payment form, show a receipt, or handle the return from PayOS. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        Users user = requireCustomer(request, response);
        if (user == null) {
            return;
        }
        HttpSession session = request.getSession(false);
        if (handlePayOsReturn(request, response, session, user)) {
            return;
        }
        if ("true".equals(request.getParameter("success"))) {
            showSuccess(request, response, session, user);
            return;
        }
        String bookingID = trim(request.getParameter("bookingID"));
        if (bookingID.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Booking ID is required.");
            return;
        }
        PaymentPageData data = loadPaymentPageData(bookingID, user.getUserID(), response);
        if (data == null) {
            return;
        }
        String token = setupPaymentSession(session, request, data.getPaymentRequest());
        setupPaymentAttributes(request, data, token);
        request.getRequestDispatcher("/WEB-INF/views/payment.jsp").forward(request, response);
    }

    /** Load the booking, amount due, and available payment methods. */
    private PaymentPageData loadPaymentPageData(String bookingID, int userID, HttpServletResponse response) throws ServletException, IOException {
        PaymentService service = createPaymentService();
        try {
            Booking booking = service.getBookingForPayment(bookingID, userID);
            if (booking == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested booking was not found.");
                return null;
            }

            PaymentRequest paymentRequest = service.createPaymentRequest(bookingID, userID);
            Map<String, Object> summary = service.getPaymentSummary(bookingID, userID);
            List<Paymentmethod> methods = new PaymentmethodDAO().getBookingMethods();

            return new PaymentPageData(booking, paymentRequest, summary, methods);

        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
            return null;
        } catch (IllegalStateException exception) {
            getServletContext().log("Unable to prepare the booking payment.", exception);
            response.sendError(HttpServletResponse.SC_CONFLICT, "This booking is expired, fully paid, unavailable, or cannot currently be paid. Check your bookings.");
            return null;
        } catch (IOException exception) {
            throw new ServletException("Unable to load the payment page.", exception);
        }
    }

    /** Store the payment request in the session and return its token. */
    private String setupPaymentSession(HttpSession session, HttpServletRequest request, PaymentRequest paymentRequest) {
        String token = UUID.randomUUID().toString();
        synchronized (session) {
            Map<String, PaymentRequest> requests = paymentRequests(session);
            while (requests.size() >= 20) {
                requests.remove(requests.keySet().iterator().next());
            }
            requests.put(token, paymentRequest);

            Object error = session.getAttribute("paymentError");
            if (error != null) {
                request.setAttribute("errorMessage", error.toString());
                session.removeAttribute("paymentError");
            }
        }
        return token;
    }

    /** Prepare the payment form with the balance and usable wallet amount. */
    private void setupPaymentAttributes(HttpServletRequest request, PaymentPageData data, String token) {
        PaymentRequest paymentRequest = data.getPaymentRequest();
        BigDecimal walletBalance = paymentRequest.getWalletBalance();
        BigDecimal offeredWallet = paymentRequest.getOfferedWalletAmount();
        BigDecimal amountToPay = paymentRequest.getAmount();

        request.setAttribute("booking", data.getBooking());
        request.setAttribute("walletBalance", walletBalance);
        request.setAttribute("walletAvailable", walletBalance != null);
        request.setAttribute("walletCanPay", offeredWallet.compareTo(amountToPay) >= 0);
        request.setAttribute("walletContribution", offeredWallet);
        request.setAttribute("externalContribution", amountToPay.subtract(offeredWallet));
        request.setAttribute("bookingSummary", data.getSummary());
        request.setAttribute("paymentMethods", data.getMethods());
        request.setAttribute("paymentOption", data.getSummary().get("paymentOption"));
        request.setAttribute("paymentType", paymentRequest.getPaymentType());
        request.setAttribute("amountToPay", amountToPay);
        request.setAttribute("paymentRequestToken", token);
        request.setAttribute("transferReference", paymentRequest.getTransferReference());
    }

    /** Validate the payment form and start a direct payment or PayOS payment. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = requireCustomer(request, response);
        if (user == null) {
            return;
        }

        HttpSession session = request.getSession(false);
        String bookingID = trim(request.getParameter("bookingID"));
        String methodID = trim(request.getParameter("methodID"));
        boolean useWallet = "true".equals(request.getParameter("useWallet"));
        String token = trim(request.getParameter("paymentRequestToken"));

        PaymentRequest paymentRequest = getValidPaymentRequest(session, token, user.getUserID(), bookingID);
        if (paymentRequest == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The payment form is invalid or expired. Reload the payment page.");
            return;
        }

        PayOsOrderDAO orders = new PayOsOrderDAO();
        try {
            orders.withCounterPaymentLock(bookingID, connection -> {
                if (orders.hasOpenPaymentOrder(connection, bookingID, paymentRequest.getPaymentType(), false)) {
                    throw new IllegalStateException("A VietQR payment is open at reception. Check it before paying again.");
                }
                submitPayment(request, response, session, methodID, useWallet, paymentRequest);
                return null;
            });
        } catch (IOException exception) {
            throw exception;
        } catch (Exception exception) {
            if (response.isCommitted()) return;
            getServletContext().log("Unable to start the booking payment.", exception);
            session.setAttribute("paymentError", exception instanceof IllegalStateException ? exception.getMessage()
                    : "Unable to start the payment. Check your booking before trying again.");
            response.sendRedirect(request.getContextPath() + "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8));
        }
    }

    private void submitPayment(HttpServletRequest request, HttpServletResponse response, HttpSession session,
            String methodID, boolean useWallet, PaymentRequest paymentRequest) throws IOException {
        if ("PT01".equals(methodID)) {
            if (handlePayOsCheckout(request, response, session, paymentRequest, useWallet)) {
                return;
            }
            methodID = "PT02";
        }

        String redirectPath = processDirectPayment(session, paymentRequest, methodID, paymentRequest.getUserID(),
                useWallet, paymentRequest.getBookingID());
        response.sendRedirect(request.getContextPath() + redirectPath);
    }

    /** Match the submitted token to the customer and booking in the session. */
    private PaymentRequest getValidPaymentRequest(HttpSession session, String token, int userID, String bookingID) {
        PaymentRequest paymentRequest;
        synchronized (session) {
            paymentRequest = paymentRequests(session).get(token);
        }

        if (paymentRequest == null || paymentRequest.getUserID() != userID || !paymentRequest.getBookingID().equals(bookingID)) {
            return null;
        }
        return paymentRequest;
    }

    /** Record a direct payment and choose the next page. */
    private String processDirectPayment(HttpSession session, PaymentRequest paymentRequest, String methodID, int userID, boolean useWallet, String bookingID) {
        try {
            Payment payment = createPaymentService().processPayment(paymentRequest, methodID, userID, useWallet);

            if ("REFUNDED".equals(payment.getStatus())) {
                session.setAttribute("paymentError", "This payment has already been refunded to your wallet.");
                return "/my-bookings";
            }

            storeSuccessfulPaymentInSession(session, payment, paymentRequest, userID, bookingID);
            return "/payment?success=true";

        } catch (IllegalArgumentException | IllegalStateException exception) {
            session.setAttribute("paymentError", exception.getMessage() == null ? "Payment could not be completed." : exception.getMessage());
            return "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8);
        } catch (Exception exception) {
            getServletContext().log("Unable to confirm the payment.", exception);
            session.setAttribute("paymentError", "Payment could not be confirmed. Check your booking before trying again.");
            return "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8);
        }
    }

    private void storeSuccessfulPaymentInSession(HttpSession session, Payment payment, PaymentRequest paymentRequest, int userID, String bookingID) {
        synchronized (session) {
            session.setAttribute("successfulPaymentID", payment.getPaymentID());
            session.setAttribute("successfulBookingID", payment.getBookingID().getBookingID().trim());
            session.setAttribute("successfulTransactionCode", payment.getTransactionID() == null ? null : payment.getTransactionID().getTransactionCode());
            session.setAttribute("successfulPaymentType", paymentRequest.getPaymentType());
            session.setAttribute("successfulPaymentAmount", payment.getAmount());
            session.setAttribute("successfulWalletAmount", payment.getWalletAmount());
            session.setAttribute("successfulExternalAmount", payment.getExternalAmount());
            session.setAttribute("successfulExternalMethodName", payment.getMethodID().getMethodName());
            session.setAttribute("successfulPaymentMethodID", payment.getMethodID().getMethodID().trim());
            session.setAttribute("successfulPaymentUserID", userID);
            session.removeAttribute("paymentError");

            if (bookingID.equals(session.getAttribute("pendingBookingID"))) {
                session.removeAttribute("pendingBookingID");
            }
        }
    }

    private Users requireCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) value;
        if (!"Customer".equalsIgnoreCase(user.getRole())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can make payments.");
            return null;
        }
        Integer userID = user.getUserID();
        if (userID == null || userID <= 0) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The login session is invalid.");
            return null;
        }
        return user;
    }

    @SuppressWarnings("unchecked")
    private Map<String, PaymentRequest> paymentRequests(HttpSession session) {
        Object value = session.getAttribute("paymentRequests");
        if (value instanceof Map) {
            return (Map<String, PaymentRequest>) value;
        }
        Map<String, PaymentRequest> requests = new LinkedHashMap<>();
        session.setAttribute("paymentRequests", requests);
        return requests;
    }

    /** Load the receipt and booking details for the completed payment. */
    private void showSuccess(HttpServletRequest request, HttpServletResponse response, HttpSession session, Users user) throws ServletException, IOException {
        if (!extractSessionPaymentData(session, request, user)) {
            response.sendRedirect(request.getContextPath() + "/my-bookings");
            return;
        }

        populateWalletSummaryIfUsed(request, user.getUserID());

        populateSuccessBookingSummary(request, user.getUserID());

        request.getRequestDispatcher("/WEB-INF/views/payment-success.jsp").forward(request, response);
    }

    protected PaymentService createPaymentService() {
        return new PaymentService();
    }

    protected WalletSummary loadWalletSummary(int userID) {
        return new WalletDAO().getWalletByUserID(userID);
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    @Override
    public String getServletInfo() {
        return "Customer deposit, full payment and balance payment servlet";
    }

    /** Read the PayOS order and show its receipt or pending status. */
    private boolean handlePayOsReturn(HttpServletRequest request, HttpServletResponse response, HttpSession session, Users user)
            throws ServletException, IOException {
        String orderCodeParam = request.getParameter("orderCode");
        if (orderCodeParam == null || orderCodeParam.isBlank()) {
            return false;
        }

        PayOsOrder order;
        try {
            order = new PayOsOrderDAO().find(Long.parseLong(orderCodeParam));
        } catch (NumberFormatException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return true;
        } catch (java.sql.SQLException exception) {
            log("Unable to load the PayOS return order.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
            return true;
        }

        if (order == null || !Integer.valueOf(order.userID).equals(user.getUserID())) {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return true;
        }
        String bookingQuery = "bookingID=" + URLEncoder.encode(order.bookingID, StandardCharsets.UTF_8);
        if ("PAID".equals(order.status)) {
            if (order.paymentID == null) {
                response.sendRedirect(request.getContextPath() + "/my-bookings?" + bookingQuery);
                return true;
            }
            synchronized (session) {
                session.setAttribute("successfulPaymentID", order.paymentID.trim());
                session.setAttribute("successfulBookingID", order.bookingID);
                session.setAttribute("successfulTransactionCode", order.reference);
                session.setAttribute("successfulPaymentType", order.paymentType);
                session.setAttribute("successfulPaymentAmount", order.amount);
                session.setAttribute("successfulWalletAmount", order.walletAmount);
                session.setAttribute("successfulExternalAmount", order.externalAmount);
                session.setAttribute("successfulExternalMethodName", "VietQR (PayOS)");
                session.setAttribute("successfulPaymentMethodID", "PT01");
                session.setAttribute("successfulPaymentUserID", user.getUserID());
                if (order.bookingID.equals(session.getAttribute("pendingBookingID"))) {
                    session.removeAttribute("pendingBookingID");
                }
            }
            showSuccess(request, response, session, user);
            return true;
        }
        if ("true".equals(request.getParameter("payosCancelled")) && !"FAILED".equals(order.status)) {
            response.sendRedirect(request.getContextPath() + "/payment?" + bookingQuery);
            return true;
        }

        request.setAttribute("payOsBookingID", order.bookingID);
        request.setAttribute("payOsOrderCode", order.orderCode);
        request.setAttribute("payOsReview", "FAILED".equals(order.status));
        request.getRequestDispatcher("/WEB-INF/views/payos-pending.jsp").forward(request, response);
        return true;
    }

    /** Create a PayOS payment link and save its order before redirecting the customer. */
    private boolean handlePayOsCheckout(HttpServletRequest request, HttpServletResponse response, HttpSession session,
            PaymentRequest paymentRequest, boolean useWallet) throws IOException {
        String bookingID = paymentRequest.getBookingID();
        int userID = paymentRequest.getUserID();
        BigDecimal walletAmount = useWallet ? paymentRequest.getOfferedWalletAmount() : BigDecimal.ZERO;
        BigDecimal shortfall = paymentRequest.getAmount().subtract(walletAmount);

        // PayOS collects only the amount left after the wallet contribution.
        if (shortfall.signum() <= 0) {
            return false;
        }

        try {
            PaymentRequest current = createPaymentService().createPaymentRequest(bookingID, userID);
            if (!current.getPaymentType().equals(paymentRequest.getPaymentType())
                    || current.getAmount().compareTo(paymentRequest.getAmount()) != 0
                    || (useWallet && current.getOfferedWalletAmount().compareTo(walletAmount) != 0)) {
                throw new IllegalStateException("The payment amount or wallet balance has changed. Reload and try again.");
            }
            long orderCode = PayOsService.newOrderCode();

            PayOsOrderDAO orderDao = new PayOsOrderDAO();
            PayOsOrder order = new PayOsOrder();
            order.orderCode = orderCode;
            order.bookingID = bookingID;
            order.userID = userID;
            order.paymentType = paymentRequest.getPaymentType();
            order.amount = paymentRequest.getAmount();
            order.walletAmount = walletAmount;
            order.externalAmount = shortfall;

            String baseUrl = PayOsService.publicBaseUrl(request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath());
            String returnUrl = baseUrl + "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8) + "&orderCode=" + orderCode;
            String cancelUrl = returnUrl + "&payosCancelled=true";

            // Use the booking deadline for the PayOS link.
            Booking booking = createPaymentService().getBookingForPayment(bookingID, userID);
            long expiredAtEpochSeconds = 0;
            if (booking != null && booking.getPaymentDeadline() != null) {
                expiredAtEpochSeconds = booking.getPaymentDeadline().getTime() / 1000;
            } else {
                expiredAtEpochSeconds = (System.currentTimeMillis() / 1000) + (15 * 60);
            }

            PayOsLink link = new PayOsService().createPaymentLink(
                    orderCode, shortfall, bookingID, returnUrl, cancelUrl, expiredAtEpochSeconds);

            orderDao.insert(order, link.paymentLinkId);

            response.sendRedirect(link.checkoutUrl);
        } catch (Exception e) {
            getServletContext().log("PayOS Gateway Init Error", e);
            session.setAttribute("paymentError", "Unable to start PayOS checkout. Check your booking before trying again.");
            response.sendRedirect(request.getContextPath() + "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8));
        }

        return true;
    }

    private boolean extractSessionPaymentData(HttpSession session, HttpServletRequest request, Users user) {
        synchronized (session) {
            Object paymentID = session.getAttribute("successfulPaymentID");
            Object bookingID = session.getAttribute("successfulBookingID");
            Object paymentUserID = session.getAttribute("successfulPaymentUserID");

            if (paymentID == null || bookingID == null || !user.getUserID().equals(paymentUserID)) {
                return false;
            }

            request.setAttribute("paymentID", paymentID);
            request.setAttribute("bookingID", bookingID);
            request.setAttribute("transactionCode", session.getAttribute("successfulTransactionCode"));
            request.setAttribute("paymentType", session.getAttribute("successfulPaymentType"));
            request.setAttribute("paymentAmount", session.getAttribute("successfulPaymentAmount"));
            request.setAttribute("walletAmount", session.getAttribute("successfulWalletAmount"));
            request.setAttribute("externalAmount", session.getAttribute("successfulExternalAmount"));
            request.setAttribute("externalMethodName", session.getAttribute("successfulExternalMethodName"));
            request.setAttribute("paymentMethodID", session.getAttribute("successfulPaymentMethodID"));

            clearSessionPaymentAttributes(session);
            return true;
        }
    }

    private void clearSessionPaymentAttributes(HttpSession session) {
        session.removeAttribute("successfulPaymentID");
        session.removeAttribute("successfulBookingID");
        session.removeAttribute("successfulTransactionCode");
        session.removeAttribute("successfulPaymentType");
        session.removeAttribute("successfulPaymentAmount");
        session.removeAttribute("successfulWalletAmount");
        session.removeAttribute("successfulExternalAmount");
        session.removeAttribute("successfulExternalMethodName");
        session.removeAttribute("successfulPaymentMethodID");
        session.removeAttribute("successfulPaymentUserID");
    }

    /** Reload the customer's wallet balance when a payment used wallet funds. */
    private void populateWalletSummaryIfUsed(HttpServletRequest request, int userID) {
        Object walletAmount = request.getAttribute("walletAmount");
        if (walletAmount instanceof java.math.BigDecimal && ((java.math.BigDecimal) walletAmount).signum() > 0) {
            try {
                request.setAttribute("wallet", loadWalletSummary(userID));
            } catch (IllegalStateException exception) {
                getServletContext().log("Payment succeeded, but the updated wallet balance is unavailable.", exception);
            }
        }
    }

    private void populateSuccessBookingSummary(HttpServletRequest request, int userID) {
        try {
            PaymentService service = createPaymentService();
            String bookingID = String.valueOf(request.getAttribute("bookingID"));
            Booking booking = service.getBookingForPayment(bookingID, userID);
            if (booking != null) {
                Map<String, Object> summary = service.getPaymentSummary(bookingID, userID);
                if (summary != null && !summary.isEmpty()) {
                    request.setAttribute("successBooking", booking);
                    request.setAttribute("successBookingSummary", summary);
                }
            }
        } catch (RuntimeException exception) {
            // The payment is saved even if its summary cannot load.
            getServletContext().log("Payment succeeded, but the latest booking summary is unavailable.", exception);
        }
    }
}
