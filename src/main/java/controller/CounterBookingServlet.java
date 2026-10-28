package controller;

import dao.*;
import dto.*;
import entity.*;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import service.BookingCheckoutService;
import service.PaymentService;
import service.PaymentService.PaymentRequest;
import util.BookingTimePolicy;

@WebServlet(name = "CounterBookingServlet", urlPatterns = {"/staff/counter-booking"})
public class CounterBookingServlet extends HttpServlet {

    private Users receptionist(HttpServletRequest req, HttpServletResponse res) throws IOException {
        HttpSession session = req.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users)) {
            res.sendRedirect(req.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) value;
        if (!"Staff".equalsIgnoreCase(user.getRole()) || user.getUserID() == null
                || !new EmployeeDAO().isReceptionistByUserId(user.getUserID())) {
            res.sendError(403, "Only receptionists can book at the counter.");
            return null;
        }
        res.setHeader("Cache-Control", "no-store");
        return user;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        if (receptionist(req, res) == null) {
            return;
        }
        HttpSession session = req.getSession(false);
        synchronized (session) {
            if (session.getAttribute("counterCsrf") == null) {
                session.setAttribute("counterCsrf", UUID.randomUUID().toString());
            }
        }
        req.setAttribute("today", BookingTimePolicy.now().toLocalDate());
        req.setAttribute("tomorrow", BookingTimePolicy.now().toLocalDate().plusDays(1));
        req.setAttribute("message", session.getAttribute("counterMessage"));
        session.removeAttribute("counterMessage");
        try {
            String bookingID = trim(req.getParameter("bookingID"));
            if (!bookingID.isEmpty()) {
                BookingDAO dao = new BookingDAO();
                Booking booking = dao.getByIdWithDetails(bookingID);
                if (booking == null) {
                    res.sendError(404);
                    return;
                }
                dao.expirePendingBookings(booking.getCustomerID().getCustomerID());
                booking = dao.getByIdWithDetails(bookingID);
                req.setAttribute("booking", booking);
                req.setAttribute("summary", dao.getPaymentSummary(bookingID));
                req.setAttribute(
                        "canCheckIn",
                        "ASSIGNED".equals(booking.getBookingStatus())
                );
                if ("PENDING_PAYMENT".equals(booking.getBookingStatus())) {
                    String token = UUID.randomUUID().toString();
                    PaymentRequest payment = new PaymentService().createPaymentRequest(bookingID,
                            booking.getCustomerID().getUserID().getUserID());
                    synchronized (session) {
                        Map<String, PaymentRequest> requests = payments(session);
                        while (requests.size() >= 20) {
                            requests.remove(requests.keySet().iterator().next());
                        }
                        requests.put(token, payment);
                    }
                    req.setAttribute("payment", payment);
                    req.setAttribute("paymentToken", token);
                }
            } else {
                String customerID = trim(req.getParameter("customerID"));
                if (!customerID.isEmpty()) {
                    Customer customer = new CustomerDAO().getCustomerById(customerID);
                    if (customer == null) {
                        res.sendError(404);
                        return;
                    }
                    if (customer.getUserID() == null) {
                        throw new IllegalStateException("This customer needs a login account before booking.");
                    }
                    req.setAttribute("selectedCustomer", customer);
                    req.setAttribute("roomTypes", new RoomTypeDAO().getAll());
                    synchronized (session) {
                        if (session.getAttribute("counterCreateToken") == null) {
                            session.setAttribute("counterCreateToken", UUID.randomUUID().toString());
                        }
                    }
                } else if (req.getParameter("phone") != null) {
                    String phone = trim(req.getParameter("phone"));
                    if (!phone.matches("[+0-9 .-]{8,15}")) {
                        throw new IllegalArgumentException("Enter a valid phone number (8–15 characters).");
                    }
                    req.setAttribute("matches", new CustomerDAO().findByPhone(phone));
                    req.setAttribute("searched", true);
                }
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            req.setAttribute("error", ex.getMessage());
        } catch (Exception ex) {
            throw new ServletException("Unable to load counter booking.", ex);
        }
        req.getRequestDispatcher("/WEB-INF/views/counter-booking.jsp").forward(req, res);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse res) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        Users actor = receptionist(req, res);
        if (actor == null) {
            return;
        }
        HttpSession session = req.getSession(false);
        if (!Objects.equals(session.getAttribute("counterCsrf"), req.getParameter("csrfToken"))
                || session.getAttribute("counterCsrf") == null) {
            res.sendError(403);
            return;
        }
        String bookingID = trim(req.getParameter("bookingID"));
        String customerID = trim(req.getParameter("customerID"));
        String redirect = "/staff/counter-booking";
        try {
            if ("create".equals(req.getParameter("action"))) {
                String selectedPaymentOption = trim(req.getParameter("paymentOption"))
                        .toUpperCase(Locale.ROOT);

                if (!Set.of("DEPOSIT", "FULL").contains(selectedPaymentOption)) {
                    throw new IllegalArgumentException(
                            "Please select 30% deposit or full payment.");
                }

                session.setAttribute(
                        "counterFormPaymentOption",
                        selectedPaymentOption
                );
                redirect += "?customerID=" + encode(customerID);
                synchronized (session) {
                    String token = req.getParameter("createToken");
                    if (token != null && token.equals(session.getAttribute("counterLastCreateToken"))) {
                        String previousID
                                = (String) session.getAttribute("counterLastBookingID");

                        Booking previous = new BookingDAO().getByIdWithDetails(previousID);

                        if (previous == null
                                || !selectedPaymentOption.equals(previous.getPaymentOption())) {
                            throw new IllegalArgumentException(
                                    "This form was already submitted with another payment option. "
                                    + "Please review and submit the reloaded form.");
                        }

                        bookingID = previous.getBookingID().trim();
                    } else {
                        if (token == null || !token.equals(session.getAttribute("counterCreateToken"))) {
                            throw new IllegalArgumentException("Reload the booking form before submitting.");
                        }
                        Customer customer = new CustomerDAO().getCustomerById(customerID);
                        if (customer == null || customer.getUserID() == null) {
                            throw new IllegalArgumentException("Select a customer with an account.");
                        }
                        LocalDate checkIn = LocalDate.parse(req.getParameter("checkInDate"));
                        LocalDate checkOut = LocalDate.parse(req.getParameter("checkOutDate"));
                        if (checkIn.isBefore(BookingTimePolicy.now().toLocalDate()) || !checkOut.isAfter(checkIn)) {
                            throw new IllegalArgumentException("Select valid stay dates.");
                        }
                        BookingWishList cart = new BookingWishList();
                        cart.setStayDates(checkIn, checkOut);
                        String[] types = req.getParameterValues("roomTypeID");
                        String[] counts = req.getParameterValues("quantity");
                        String[] guests = req.getParameterValues("guestCount");
                        if (types == null || counts == null || guests == null || types.length != counts.length || types.length != guests.length) {
                            throw new IllegalArgumentException("Invalid room selection.");
                        }
                        for (int i = 0; i < types.length; i++) {
                            int quantity = Integer.parseInt(counts[i]);
                            if (quantity == 0) {
                                continue;
                            }
                            int guestCount = Integer.parseInt(guests[i]);
                            RoomType type = new RoomTypeDAO().getById(types[i]);
                            if (type == null || quantity < 0 || guestCount < 1 || (long) guestCount > (long) type.getCapacity() * quantity) {
                                throw new IllegalArgumentException("Check room quantities and guest counts.");
                            }
                            cart.addOrUpdateItem(new BookingCartItem(type.getRoomTypeID(), type.getTypeName(),
                                    type.getCapacity(), type.getPrice(), quantity, guestCount));
                        }
                        Booking booking = new BookingCheckoutService().createForReceptionist(cart,
                                customer.getUserID().getUserID(), req.getParameter("paymentOption"), actor.getUserID());
                        bookingID = booking.getBookingID().trim();
                        session.setAttribute("counterLastCreateToken", token);
                        session.setAttribute("counterLastBookingID", bookingID);
                        session.removeAttribute("counterCreateToken");
                    }
                }
                redirect = "/staff/counter-booking?bookingID=" + encode(bookingID);
            } else if ("pay".equals(req.getParameter("action"))) {
                redirect += "?bookingID=" + encode(bookingID);
                PaymentRequest payment;
                synchronized (session) {
                    payment = payments(session).get(req.getParameter("paymentToken"));
                }
                if (payment == null || !payment.getBookingID().equals(bookingID)) {
                    throw new IllegalArgumentException("Reload the payment page.");
                }
                String method = trim(req.getParameter("methodID"));

                if (!Set.of("PT01", "PT10").contains(method)
                        || req.getParameter("useWallet") != null) {
                    throw new IllegalArgumentException(
                            "Counter payments only support Cash or VietQR.");
                }

                if (!"true".equals(req.getParameter("collectionConfirmed"))) {
                    throw new IllegalArgumentException(
                            "Confirm that the cash or bank transfer was received.");
                }

                String bankReference = trim(req.getParameter("bankReference"));

                if ("PT10".equals(method) && bankReference.isEmpty()) {
                    throw new IllegalArgumentException(
                            "Enter the bank transaction reference for VietQR.");
                }

                Payment result = new PaymentService().collectAtCounter(
                        payment,
                        method,
                        actor.getUserID(),
                        false,
                        false,
                        "PT10".equals(method) ? bankReference : ""
                );
                session.setAttribute("counterMessage", "PAID".equals(result.getStatus())
                        ? "Payment recorded successfully. You can assign rooms now."
                        : "Payment has already been refunded. Review the booking status.");
            } else {
                throw new IllegalArgumentException("Unsupported action.");
            }
        } catch (IllegalArgumentException | IllegalStateException ex) {
            session.setAttribute("counterMessage", ex.getMessage());
        } catch (Exception ex) {
            log("Counter booking failed.", ex);
            session.setAttribute("counterMessage", "The operation could not be confirmed. Review the booking before retrying.");
        }
        res.sendRedirect(req.getContextPath() + redirect);
    }

    @SuppressWarnings("unchecked")
    private Map<String, PaymentRequest> payments(HttpSession session) {
        Object value = session.getAttribute("counterPayments");
        if (value instanceof Map) {
            return (Map<String, PaymentRequest>) value;
        }
        Map<String, PaymentRequest> result = new LinkedHashMap<>();
        session.setAttribute("counterPayments", result);
        return result;
    }

    private String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private String encode(String value) {
        return java.net.URLEncoder.encode(value, java.nio.charset.StandardCharsets.UTF_8);
    }
}
