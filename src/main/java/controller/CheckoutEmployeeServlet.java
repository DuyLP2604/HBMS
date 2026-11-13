package controller;

import dao.BookingDAO;
import dao.InvoiceDAO;
import dao.PayOsOrderDAO;
import dto.PayOsLink;
import dto.PayOsOrder;
import dao.PaymentmethodDAO;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.UUID;
import service.PaymentService;
import service.PaymentRequest;
import service.PayOsService;
import util.security.CsrfTokens;
import util.CounterPaymentSupport;
import util.PersistenceManager;
import java.net.URI;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;

/** Lets reception review stay charges and complete checkout with an invoice. */
@WebServlet(name = "CheckoutEmployeeServlet", urlPatterns = {"/CheckoutEmployee"})
public class CheckoutEmployeeServlet extends HttpServlet
{
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();
    private final PayOsOrderDAO payOsOrders = new PayOsOrderDAO();
    private static final String PAYMENT_REQUESTS = "checkoutPaymentRequests";

    /** Open the reception checkout list or the details for one booking. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }
        String action = request.getParameter("action");
        response.setHeader("Cache-Control", "no-store");
        
        // Bắt luồng PayOS Image và Status cho màn hình Checkout
        if ("payOsStatus".equals(action) || "payOsImage".equals(action))
        {
            showPayOsOrder(request, response, "payOsImage".equals(action));
            return;
        }
        
        if ("detail".equals(action))
        {
            showCheckoutDetail(request, response);
        }
        else if (action == null || action.isBlank() || "list".equals(action))
        {
            showCheckoutList(request, response);
        }
        else
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported checkout action.");
        }
    }

    /** Load checked-in stays and issued invoices for reception. */
    private void showCheckoutList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        List<Map<String, String>> occupiedRooms;
        try
        {
            occupiedRooms = invoiceDAO.getOccupiedRooms();
            String customerName = request.getParameter("customerName");
            if (customerName != null && !customerName.isBlank())
            {
                String keyword = customerName.trim().toLowerCase(Locale.ROOT);
                List<Map<String, String>> filteredRooms = new ArrayList<>();
                for (Map<String, String> room : occupiedRooms)
                {
                    String name = room.get("customerName");
                    if (name != null && name.toLowerCase(Locale.ROOT).contains(keyword))
                    {
                        filteredRooms.add(room);
                    }
                }
                occupiedRooms = filteredRooms;
            }
        }
        catch (Exception ex)
        {
            throw new ServletException("Unable to load the checkout list.", ex);
        }
        request.setAttribute("occupiedRooms", occupiedRooms);
        moveFlashMessage(request, request.getSession(false));
        request.getRequestDispatcher("/WEB-INF/views/checkout-employee.jsp").forward(request, response);
    }

    /** Load room and service charges and determine whether the stay can be closed. */
    private void showCheckoutDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        String bookingID = request.getParameter("bookingID");
        if (bookingID == null || bookingID.isBlank())
        {
            response.sendRedirect(request.getContextPath() + "/CheckoutEmployee");
            return;
        }
        if (bookingID.trim().length() > 6)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
            return;
        }
        bookingID = bookingID.trim();
        Map<String, String> roomDetail;
        List<Map<String, String>> services;
        Map<String, Object> summary;
        BigDecimal totalAmount;
        BigDecimal paidAmount;
        BigDecimal remainingAmount;
        Map<String, String> issuedInvoice;
        try
        {
            summary = bookingDAO.getPaymentSummary(bookingID);
            if (summary == null || summary.isEmpty())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested booking was not found.");
                return;
            }
            if (!"CHECKED_IN".equals(summary.get("bookingStatus")) && !"CHECKED_OUT".equals(summary.get("bookingStatus")))
            {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Only checked-in/out bookings can use this checkout page.");
                return;
            }
            roomDetail = invoiceDAO.getRoomDetailByBooking(bookingID);
            if (roomDetail == null || roomDetail.isEmpty())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The booking details were not found.");
                return;
            }
            services = invoiceDAO.getServicesByBooking(bookingID);
            totalAmount = requireAmount(summary, "totalAmount");
            paidAmount = requireAmount(summary, "totalPaidAmount");
            remainingAmount = requireAmount(summary, "remainingAmount");
            issuedInvoice = invoiceDAO.getInvoiceByBooking(bookingID);
        }
        catch (Exception ex)
        {
            throw new ServletException("Unable to load the checkout details.", ex);
        }
        request.setAttribute("roomDetail", roomDetail);
        request.setAttribute("services", services);
        request.setAttribute("bookingID", bookingID);
        request.setAttribute("bookingSummary", summary);
        request.setAttribute("paymentOption", summary.get("paymentOption"));
        request.setAttribute("totalAmount", totalAmount);
        request.setAttribute("paidAmount", paidAmount);
        request.setAttribute("remainingAmount", remainingAmount);
        request.setAttribute("deposit", paidAmount);
        request.setAttribute("finalAmount", remainingAmount);
        request.setAttribute("issuedInvoice", issuedInvoice);
        HttpSession session = request.getSession(false);
        request.setAttribute("csrfToken", CsrfTokens.getOrCreate(session));
        if ("CHECKED_IN".equals(summary.get("bookingStatus")) && summary.get("firstPaidAt") != null
                && remainingAmount.signum() > 0)
        {
            Object customer = summary.get("customerUserID");
            if (!(customer instanceof Number) || ((Number) customer).intValue() <= 0)
            {
                throw new ServletException("The booking customer account is unavailable.");
            }
            Users staff = (Users) session.getAttribute("user");
            PaymentRequest payment = new PaymentService().createPaymentRequest(bookingID, ((Number) customer).intValue());
            if (!"BALANCE".equals(payment.getPaymentType()) || payment.getAmount().compareTo(remainingAmount) != 0)
            {
                response.sendError(HttpServletResponse.SC_CONFLICT, "The balance changed while loading checkout. Reload and review the amount.");
                return;
            }
            // Bind the displayed amount and booking owner to this receptionist's session.
            String token = UUID.randomUUID().toString();
            synchronized (session)
            {
                Map<String, CheckoutPayment> pending = paymentRequests(session);
                while (pending.size() >= 20) pending.remove(pending.keySet().iterator().next());
                pending.put(token, new CheckoutPayment(staff.getUserID(), payment));
            }
            request.setAttribute("checkoutPaymentToken", token);
            request.setAttribute("paymentMethods", new PaymentmethodDAO().getCollectionMethods());
        }
        request.setAttribute("canCheckout", summary.get("firstPaidAt") != null && paidAmount.signum() > 0 && remainingAmount.signum() == 0 && ("CHECKED_IN".equals(summary.get("bookingStatus")) || issuedInvoice == null));
        moveFlashMessage(request, request.getSession(false));
        request.getRequestDispatcher("/WEB-INF/views/checkout-detail.jsp").forward(request, response);
    }

    /** Check the booking balance and issue the invoice when checkout can proceed. */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }
        String action = request.getParameter("action");
        if (!CsrfTokens.isValid(request))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Reload checkout and try again.");
            return;
        }
        
        // Điều hướng các logic thanh toán (Cash & QR PayOS)
        if ("collectCash".equals(action) || "createPayOs".equals(action))
        {
            collectPayment(request, response, user, action);
            return;
        }
        if (!"exportInvoice".equals(action))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported checkout action.");
            return;
        }
        
        // Xử lý tạo Invoice sau khi thanh toán xong
        HttpSession session = request.getSession(false);
        try
        {
            String bookingID = request.getParameter("bookingID");
            if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6)
            {
                throw new IllegalArgumentException("A valid booking ID is required.");
            }
            bookingID = bookingID.trim();
            Map<String, Object> summary = bookingDAO.getPaymentSummary(bookingID);
            if (summary == null || summary.isEmpty())
            {
                throw new IllegalArgumentException("The requested booking was not found.");
            }
            if (!"CHECKED_IN".equals(summary.get("bookingStatus")) && !"CHECKED_OUT".equals(summary.get("bookingStatus")))
            {
                throw new IllegalStateException("Only checked-in/out bookings can use this checkout action.");
            }
            BigDecimal remainingAmount = requireAmount(summary, "remainingAmount");
            if (remainingAmount.signum() > 0)
            {
                String amount = NumberFormat.getNumberInstance(new Locale("vi", "VN")).format(remainingAmount);
                throw new IllegalStateException("The booking still owes " + amount + " VND. Complete payment before checkout.");
            }
            if (summary.get("firstPaidAt") == null || requireAmount(summary, "totalPaidAmount").signum() <= 0)
            {
                throw new IllegalStateException("The booking has no successful payment. Checkout cannot continue.");
            }
            boolean success = invoiceDAO.processCheckout(bookingID, user.getUserID());
            if (success)
            {
                setFlash(session, true, "Checkout and the invoice are confirmed for booking " + bookingID + ".");
            }
            else
            {
                setFlash(session, false, "Checkout could not be confirmed. Check the booking and invoice before trying again.");
            }
        }
        catch (IllegalArgumentException | IllegalStateException ex)
        {
            setFlash(session, false, ex.getMessage());
        }
        catch (Exception ex)
        {
            log("Unable to complete checkout and issue the invoice.", ex);
            setFlash(session, false, "Checkout could not be confirmed. Check the booking and invoice before trying again.");
        }
        String id = validBookingID(request);
        response.sendRedirect(request.getContextPath() + "/CheckoutEmployee"
                + (id == null ? "" : "?action=detail&bookingID=" + id));
    }

    /** Record cash or open QR; neither action marks the stay as checked out. */
    private void collectPayment(HttpServletRequest request, HttpServletResponse response, Users user, String action)
            throws IOException
    {
        HttpSession session = request.getSession(false);
        String bookingID = validBookingID(request);
        CheckoutPayment shown;
        synchronized (session)
        {
            shown = paymentRequests(session).get(request.getParameter("checkoutPaymentToken"));
        }
        if (bookingID == null || shown == null || shown.actorUserID != user.getUserID()
                || !bookingID.equals(shown.payment.getBookingID()))
        {
            CounterPaymentSupport.writeError(response, HttpServletResponse.SC_BAD_REQUEST,
                    "The payment form expired. Reload checkout before collecting payment.");
            return;
        }
        try
        {
            if ("createPayOs".equals(action))
            {
                String baseUrl = PayOsService.publicBaseUrl(request.getScheme() + "://" + request.getServerName()
                        + ":" + request.getServerPort() + request.getContextPath());
                PayOsOrder order = openCheckoutOrder(shown.payment, user.getUserID(),
                        baseUrl + "/CheckoutEmployee?action=detail&bookingID=" + bookingID);
                CounterPaymentSupport.writeOrder(request, response, order, "/CheckoutEmployee");
                return;
            }
            if (!"PT03".equals(request.getParameter("methodID"))
                    || !"true".equals(request.getParameter("collectionConfirmed")))
            {
                throw new IllegalArgumentException("Select cash and confirm that the outstanding amount was received.");
            }
            payOsOrders.withCounterPaymentLock(bookingID, connection -> {
                validateCheckoutPayment(shown.payment);
                requireNoOpenPayment(connection, bookingID);
                return new PaymentService().collectAtCounter(shown.payment, "PT03", user.getUserID(), false, false, "");
            });
            setFlash(session, true, "Cash payment recorded. Review the updated balance, then confirm checkout and issue the invoice.");
        }
        catch (Exception exception)
        {
            log("Unable to collect checkout payment for " + bookingID, exception);
            String message = exception instanceof IllegalArgumentException || exception instanceof IllegalStateException
                    || exception instanceof SecurityException ? exception.getMessage()
                    : "Unable to collect payment. Reload checkout and check payment history before trying again.";
            if ("createPayOs".equals(action))
            {
                CounterPaymentSupport.writeError(response, HttpServletResponse.SC_CONFLICT, message);
                return;
            }
            setFlash(session, false, message);
        }
        response.sendRedirect(request.getContextPath() + "/CheckoutEmployee?action=detail&bookingID=" + bookingID);
    }

    private void showPayOsOrder(HttpServletRequest request, HttpServletResponse response, boolean image) throws IOException
    {
        String bookingID = validBookingID(request);
        if (bookingID == null)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        try
        {
            String code = request.getParameter("orderCode");
            PayOsOrder order;
            try (Connection connection = PersistenceManager.openConnection())
            {
                order = findCheckoutOrder(connection, bookingID,
                        code == null && !image ? null : CounterPaymentSupport.parseOrderCode(code));
            }
            if (image) CounterPaymentSupport.writeQrImage(response, order);
            else CounterPaymentSupport.writeOrder(request, response, order, "/CheckoutEmployee");
        }
        catch (IllegalArgumentException exception)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        }
        catch (java.sql.SQLException | com.google.zxing.WriterException exception)
        {
            log("Unable to load checkout QR.", exception);
            response.sendError(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }

    /** Use the existing payment service to validate the amount displayed on the checkout form. */
    private void validateCheckoutPayment(PaymentRequest shown)
    {
        Map<String, Object> summary = bookingDAO.getPaymentSummary(shown.getBookingID());
        Object owner = summary.get("customerUserID");
        if (!"CHECKED_IN".equals(summary.get("bookingStatus")) || summary.get("firstPaidAt") == null
                || !(owner instanceof Number) || ((Number) owner).intValue() != shown.getUserID())
        {
            throw new IllegalStateException("Reload checkout and verify the booking before collecting payment.");
        }
        if (!"BALANCE".equals(shown.getPaymentType())
                || shown.getAmount().compareTo(requireAmount(summary, "remainingAmount")) != 0)
        {
            throw new IllegalStateException("The outstanding amount changed. Reload checkout before collecting payment.");
        }
    }

    private void requireNoOpenPayment(Connection connection, String bookingID) throws SQLException
    {
        if (payOsOrders.hasOpenPaymentOrder(connection, bookingID, "BALANCE", true))
        {
            throw new IllegalStateException("Check the existing QR payment before collecting cash or generating another code.");
        }
    }

    /** Create the same PayOS QR used at check-in, for the outstanding checkout BALANCE. */
    private PayOsOrder openCheckoutOrder(PaymentRequest shown, int staffUserID, String returnUrl) throws Exception
    {
        if (!new dao.EmployeeDAO().isReceptionistByUserId(staffUserID))
        {
            throw new SecurityException("Only receptionists can collect checkout payments.");
        }
        return payOsOrders.withCounterPaymentLock(shown.getBookingID(), connection -> {
            validateCheckoutPayment(shown);
            PayOsOrder active = findCheckoutOrder(connection, shown.getBookingID(), null);
            if (active != null) return active;
            requireNoOpenPayment(connection, shown.getBookingID());
            PayOsOrder order = new PayOsOrder();
            order.orderCode = PayOsService.newOrderCode();
            order.bookingID = shown.getBookingID();
            order.userID = shown.getUserID();
            order.paymentType = "BALANCE";
            order.externalAmount = shown.getAmount();
            order.status = "PENDING";
            order.checkInExpiresAt = Instant.now().plusSeconds(900).getEpochSecond();
            PayOsLink link;
            try
            {
                link = new PayOsService().createPaymentLink(order.orderCode, order.externalAmount, order.bookingID,
                        returnUrl, returnUrl, order.checkInExpiresAt);
            }
            catch (InterruptedException exception)
            {
                Thread.currentThread().interrupt();
                throw exception;
            }
            URI checkout = URI.create(link.checkoutUrl);
            if (!"https".equalsIgnoreCase(checkout.getScheme()) || !"pay.payos.vn".equalsIgnoreCase(checkout.getHost())
                    || checkout.getRawUserInfo() != null || link.checkoutUrl.length() > 500
                    || link.qrCode.isBlank() || link.qrCode.length() > 2000 || link.paymentLinkId.isBlank())
            {
                throw new IllegalStateException("PayOS did not return a valid payment QR.");
            }
            order.checkInQrCode = link.qrCode;
            order.checkInCheckoutUrl = link.checkoutUrl;
            // Ordinary BALANCE orders are already supported by the existing checkout webhook.
            payOsOrders.insertCounterOrder(connection, order, null, link.paymentLinkId);
            return order;
        });
    }

    /** Read checkout QR details from the existing PAYOS_ORDER table without changing the DAO. */
    private PayOsOrder findCheckoutOrder(Connection connection, String bookingID, Long code) throws SQLException
    {
        String filter = code == null
                ? "AND o.Status <> 'PAID' AND o.CheckInExpiresAt > ? ORDER BY o.CreatedAt DESC, o.OrderCode DESC"
                : "AND o.OrderCode = ?";
        // Đã update SQL condition ở dòng `AND b.BookingStatus NOT IN ('CHECKED_IN', 'CHECKED_OUT')` để QR hiển thị mượt mà.
        String sql = "SELECT TOP (1) o.OrderCode, o.PaymentID, o.ExternalAmount, o.CheckInExpiresAt, "
                + "o.CheckInQrCode, o.CheckInCheckoutUrl, CASE WHEN o.Status IN ('PENDING','PROCESSING') "
                + "AND (b.BookingStatus NOT IN ('CHECKED_IN', 'CHECKED_OUT') OR b.RemainingAmount <> o.ExternalAmount) "
                + "THEN 'FAILED' ELSE o.Status END AS Status FROM dbo.PAYOS_ORDER o "
                + "JOIN dbo.V_BOOKING_PAYMENT_SUMMARY b ON b.BookingID = o.BookingID "
                + "WHERE o.BookingID = ? AND o.PaymentType = 'BALANCE' AND o.CheckInStaffUserID IS NULL "
                + "AND o.CheckInQrCode IS NOT NULL " + filter;
        try (PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setString(1, bookingID);
            statement.setLong(2, code == null ? Instant.now().getEpochSecond() : code);
            try (ResultSet result = statement.executeQuery())
            {
                if (!result.next()) return null;
                PayOsOrder order = new PayOsOrder();
                order.bookingID = bookingID;
                order.orderCode = result.getLong("OrderCode");
                order.paymentID = result.getString("PaymentID");
                order.externalAmount = result.getBigDecimal("ExternalAmount");
                order.status = result.getString("Status");
                order.checkInExpiresAt = result.getLong("CheckInExpiresAt");
                order.checkInQrCode = result.getString("CheckInQrCode");
                order.checkInCheckoutUrl = result.getString("CheckInCheckoutUrl");
                return order;
            }
        }
    }

    private String validBookingID(HttpServletRequest request)
    {
        String value = request.getParameter("bookingID");
        return value != null && value.trim().matches("[A-Za-z0-9]{1,6}") ? value.trim() : null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, CheckoutPayment> paymentRequests(HttpSession session)
    {
        Object value = session.getAttribute(PAYMENT_REQUESTS);
        if (!(value instanceof Map))
        {
            value = new LinkedHashMap<String, CheckoutPayment>();
            session.setAttribute(PAYMENT_REQUESTS, value);
        }
        return (Map<String, CheckoutPayment>) value;
    }

    private static final class CheckoutPayment implements Serializable
    {
        private static final long serialVersionUID = 1L;
        private final int actorUserID;
        private final PaymentRequest payment;
        private CheckoutPayment(int actorUserID, PaymentRequest payment)
        {
            this.actorUserID = actorUserID;
            this.payment = payment;
        }
    }

    private Users requireStaff(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute("user");
        if (!(sessionUser instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) sessionUser;
        if (!"Staff".equalsIgnoreCase(user.getRole()) && !"Admin".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or admin can manage checkout.");
            return null;
        }
        if (user.getUserID() == null || user.getUserID() <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your login session is invalid. Please log in again.");
            return null;
        }
        return user;
    }

    private BigDecimal requireAmount(Map<String, Object> summary, String key)
    {
        Object value = summary.get(key);
        if (!(value instanceof BigDecimal) || ((BigDecimal) value).signum() < 0)
        {
            throw new IllegalStateException("The payment summary is unavailable. Please reload the booking.");
        }
        return (BigDecimal) value;
    }

    private void setFlash(HttpSession session, boolean success, String message)
    {
        synchronized (session)
        {
            session.setAttribute("msg", message);
            session.setAttribute("checkoutMessageSuccess", success);
        }
    }

    private void moveFlashMessage(HttpServletRequest request, HttpSession session)
    {
        synchronized (session)
        {
            Object message = session.getAttribute("msg");
            Object success = session.getAttribute("checkoutMessageSuccess");
            if (message != null)
            {
                request.setAttribute("msg", message);
                request.setAttribute(Boolean.TRUE.equals(success) ? "successMessage" : "errorMessage", message);
                session.removeAttribute("msg");
            }
            session.removeAttribute("checkoutMessageSuccess");
        }
    }
}