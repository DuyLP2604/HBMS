package controller;

import com.google.gson.JsonObject;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import dao.BookingDAO;
import dao.PayOsOrderDAO;
import dao.PayOsOrderDAO.Order;
import dao.PaymentmethodDAO;
import dao.UserDAO;
import entity.Booking;
import entity.Users;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.sql.SQLException;
import javax.imageio.ImageIO;
import java.util.LinkedHashMap;
import java.util.Map;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import service.CheckInPayOsService;
import service.CheckInService;
import service.PayOsService;
import service.CheckInService.CheckInRequest;
import util.security.CsrfTokens;

@WebServlet(name = "ProcessCheckInServlet", urlPatterns = {"/process-checkin"})
public class ProcessCheckInServlet extends HttpServlet {
    private static final String REQUESTS = "checkInPaymentRequests";
    private final PayOsOrderDAO payOsOrders = new PayOsOrderDAO();
    protected BookingDAO createBookingDAO() { return new BookingDAO(); }
    protected CheckInService createCheckInService() { return new CheckInService(); }
    protected UserDAO createUserDAO() { return new UserDAO(); }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Users user = requireStaff(request, response);
        if (user == null) return;
        String bookingID = bookingID(request);
        if (bookingID == null) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
            return;
        }
        response.setHeader("Cache-Control", "no-store");
        String action = request.getParameter("action");
        if ("payOsStatus".equals(action) || "payOsImage".equals(action)) {
            showPayOsOrder(request, response, bookingID, "payOsImage".equals(action));
            return;
        }
        try {
            BookingDAO dao = createBookingDAO();
            Booking booking = dao.getByIdWithDetails(bookingID);
            if (booking == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            if ("CHECKED_IN".equals(booking.getBookingStatus())) {
                request.getSession(false).setAttribute("lifecycleSuccess", "This booking is already checked in. No additional room payment was collected.");
                response.sendRedirect(request.getContextPath() + "/staff/stays");
                return;
            }
            Map<String, Object> summary = dao.getPaymentSummary(bookingID);
            CheckInRequest payment = createCheckInService().createRequest(bookingID, user.getUserID(), summary);
            HttpSession session = request.getSession(false);
            synchronized (session) {
                Map<String, CheckInRequest> pending = requests(session);
                while (pending.size() >= 20) pending.remove(pending.keySet().iterator().next());
                pending.put(payment.getToken(), payment);
                request.setAttribute("errorMessage", session.getAttribute("checkInError"));
                session.removeAttribute("checkInError");
            }
            request.setAttribute("booking", booking);
            request.setAttribute("paymentSummary", summary);
            request.setAttribute("checkInRequest", payment);
            request.setAttribute("paymentMethods", new PaymentmethodDAO().getCollectionMethods());
            request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
            request.getRequestDispatcher("/WEB-INF/views/checkin-form.jsp").forward(request, response);
        } catch (IllegalArgumentException | IllegalStateException exception) {
            request.getSession(false).setAttribute("lifecycleError", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/staff/stays");
        } catch (RuntimeException exception) {
            log("Unable to load check-in details.", exception);
            throw new ServletException("Unable to load check-in details. Verify that the check-in database migration is installed.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        Users user = requireStaff(request, response);
        if (user == null) return;
        if (!CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Reload the check-in form and try again.");
            return;
        }
        String action = request.getParameter("action");
        if (!"confirmCheckIn".equals(action) && !"createPayOs".equals(action)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported check-in action.");
            return;
        }
        HttpSession session = request.getSession(false);
        String bookingID = bookingID(request);
        CheckInRequest payment;
        synchronized (session) {
            payment = requests(session).get(request.getParameter("checkInToken"));
        }
        if (bookingID == null || payment == null || !payment.getBookingID().equals(bookingID)
                || payment.getActorUserID() != user.getUserID()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The check-in form expired. Open the booking again.");
            return;
        }
        if ("createPayOs".equals(action)) {
            createPayOsOrder(request, response, payment);
            return;
        }

        try {
            String method = request.getParameter("methodID");
            if (payment.isPayOsRoomBalance() && "PT01".equals(method)) {
                completePaidCheckIn(request, payment);
            } else if (payment.isPayOsRoomBalance()) {
                payOsOrders.withCheckInLock(bookingID, connection -> {
                    if (payOsOrders.findCheckInOrder(connection, bookingID, null) != null) {
                        throw new IllegalStateException("Check the active VietQR payment before collecting by another method.");
                    }
                    collectAndCheckIn(request, payment, method);
                    return null;
                });
            } else {
                collectAndCheckIn(request, payment, method);
            }
        } catch (IllegalArgumentException | IllegalStateException exception) {
            session.setAttribute("checkInError", exception.getMessage());
            response.sendRedirect(request.getContextPath() + "/process-checkin?bookingID=" + bookingID);
            return;
        } catch (Exception exception) {
            log("Unable to finish check-in for " + bookingID, exception);
            session.setAttribute("checkInError", "Check-in could not be confirmed. Review the booking before trying again.");
            response.sendRedirect(request.getContextPath() + "/process-checkin?bookingID=" + bookingID);
            return;
        }
        session.setAttribute("lifecycleSuccess", "Check-in confirmed for " + bookingID
                + ". Room charges are fully paid; only unpaid extra services are due at checkout.");
        response.sendRedirect(request.getContextPath() + "/staff/stays");
    }

    private void collectAndCheckIn(HttpServletRequest request, CheckInRequest payment, String method) {
        Integer walletOwner = null;
        if (payment.getRoomAmount().signum() > 0 && "PT02".equals(method)) {
            walletOwner = authorizeWallet(request, payment);
        }
        createCheckInService().confirm(payment, payment.getActorUserID(), request.getParameterValues("cccd"),
                method, "true".equals(request.getParameter("collectionConfirmed")),
                walletOwner, request.getParameter("bankReference"));
    }

    private void completePaidCheckIn(HttpServletRequest request, CheckInRequest payment) throws SQLException {
        String bookingID = payment.getBookingID();
        Order order = payOsOrders.findCheckInOrder(bookingID, parseOrderCode(request.getParameter("payOsOrderCode")));
        if (order == null || !"PAID".equals(order.status) || order.paymentID == null) {
            throw new IllegalArgumentException("Wait until PayOS confirms the room payment.");
        }

        // The webhook already collected the balance.
        CheckInRequest current = createCheckInService().createRequest(bookingID, payment.getActorUserID(),
                createBookingDAO().getPaymentSummary(bookingID));
        if (current.getRoomAmount().signum() != 0) {
            throw new IllegalStateException("The room balance changed. Reload and review the payment.");
        }
        createCheckInService().confirm(current, current.getActorUserID(), request.getParameterValues("cccd"),
                null, false, null, null);
    }

    private void createPayOsOrder(HttpServletRequest request, HttpServletResponse response, CheckInRequest payment)
            throws IOException {
        response.setHeader("Cache-Control", "no-store");
        Order order;
        try {
            String baseUrl = PayOsService.publicBaseUrl(request.getScheme() + "://" + request.getServerName()
                    + ":" + request.getServerPort() + request.getContextPath());
            order = new CheckInPayOsService().openOrder(payment,
                    baseUrl + "/process-checkin?bookingID=" + payment.getBookingID());
        } catch (Exception exception) {
            log("Unable to open a PayOS check-in order.", exception);
            writePayOsError(response, HttpServletResponse.SC_CONFLICT,
                    "Unable to open VietQR. Reload check-in and review the room balance before trying again.");
            return;
        }
        writePayOsOrder(request, response, order);
    }

    private void showPayOsOrder(HttpServletRequest request, HttpServletResponse response, String bookingID, boolean image)
            throws IOException {
        try {
            String code = request.getParameter("orderCode");
            Order order = payOsOrders.findCheckInOrder(bookingID, code == null && !image ? null : parseOrderCode(code));
            if (!image) {
                writePayOsOrder(request, response, order);
                return;
            }
            if (order == null || !"PENDING".equals(CheckInPayOsService.status(order))) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            BitMatrix qr = new QRCodeWriter().encode(order.checkInQrCode, BarcodeFormat.QR_CODE, 240, 240);
            BufferedImage png = new BufferedImage(qr.getWidth(), qr.getHeight(), BufferedImage.TYPE_BYTE_BINARY);
            for (int y = 0; y < qr.getHeight(); y++) {
                for (int x = 0; x < qr.getWidth(); x++) {
                    png.setRGB(x, y, qr.get(x, y) ? 0xFF000000 : 0xFFFFFFFF);
                }
            }
            response.setContentType("image/png");
            ImageIO.write(png, "png", response.getOutputStream());
        } catch (IllegalArgumentException exception) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException | WriterException exception) {
            log("Unable to load the PayOS check-in order.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    private void writePayOsOrder(HttpServletRequest request, HttpServletResponse response, Order order) throws IOException {
        JsonObject body = new JsonObject();
        String status = CheckInPayOsService.status(order);
        body.addProperty("status", status);
        if (order != null) {
            body.addProperty("orderCode", Long.toString(order.orderCode));
            if ("PENDING".equals(status)) {
                body.addProperty("imageUrl", request.getContextPath() + "/process-checkin?action=payOsImage"
                        + "&bookingID=" + order.bookingID + "&orderCode=" + order.orderCode);
                body.addProperty("checkoutUrl", order.checkInCheckoutUrl);
            }
        }
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body.toString());
    }

    private void writePayOsError(HttpServletResponse response, int status, String message) throws IOException {
        response.setStatus(status);
        JsonObject body = new JsonObject();
        body.addProperty("message", message);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(body.toString());
    }

    private long parseOrderCode(String value) {
        long code = Long.parseLong(value);
        if (code <= 0) throw new IllegalArgumentException("Invalid PayOS order.");
        return code;
    }

    private int authorizeWallet(HttpServletRequest request, CheckInRequest payment) {
        HttpSession session = request.getSession(false);
        synchronized (session) {
            Long lockedUntil = (Long) session.getAttribute("counterWalletLockedUntil");
            if (lockedUntil != null && lockedUntil > System.currentTimeMillis()) {
                throw new IllegalArgumentException("Too many failed wallet authorizations. Retry in five minutes.");
            }
            String username = request.getParameter("walletUsername");
            Users owner = createUserDAO().login(username == null ? "" : username.trim(), request.getParameter("walletPassword"));
            if (owner == null || !"Customer".equalsIgnoreCase(owner.getRole())
                    || owner.getUserID() == null || owner.getUserID() != payment.getCustomerUserID()) {
                Integer failures = (Integer) session.getAttribute("counterWalletFailures");
                failures = failures == null ? 1 : failures + 1;
                session.setAttribute("counterWalletFailures", failures);
                if (failures >= 5) session.setAttribute("counterWalletLockedUntil", System.currentTimeMillis() + 300000L);
                throw new IllegalArgumentException("Wallet authorization failed. The booking customer must enter their own credentials.");
            }
            session.removeAttribute("counterWalletFailures");
            session.removeAttribute("counterWalletLockedUntil");
            return owner.getUserID();
        }
    }

    private String bookingID(HttpServletRequest request) {
        String value = request.getParameter("bookingID");
        return value != null && value.trim().matches("[A-Za-z0-9]{1,6}") ? value.trim() : null;
    }

    private Users requireStaff(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users)) {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) value;
        if (user.getUserID() == null || user.getUserID() <= 0
                || !("Staff".equalsIgnoreCase(user.getRole()) || "Admin".equalsIgnoreCase(user.getRole()))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or admin can manage check-in.");
            return null;
        }
        return user;
    }

    @SuppressWarnings("unchecked")
    private Map<String, CheckInRequest> requests(HttpSession session) {
        Object value = session.getAttribute(REQUESTS);
        if (!(value instanceof Map)) {
            value = new LinkedHashMap<String, CheckInRequest>();
            session.setAttribute(REQUESTS, value);
        }
        return (Map<String, CheckInRequest>) value;
    }
}
