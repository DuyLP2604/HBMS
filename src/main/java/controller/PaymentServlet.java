package controller;

import dao.PaymentmethodDAO;
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
import java.util.List;
import service.PaymentService;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@WebServlet(
        name = "PaymentServlet",
        urlPatterns = {"/payment"}
)
public class PaymentServlet extends HttpServlet {

    private final PaymentService paymentService
            = new PaymentService();

    private final PaymentmethodDAO paymentmethodDAO
            = new PaymentmethodDAO();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        Users user
                = (Users) session.getAttribute("user");

        if (!"Customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only customers can access the payment page."
            );

            return;
        }

        if ("true".equals(
                request.getParameter("success"))) {

            Object paymentID
                    = session.getAttribute(
                            "successfulPaymentID"
                    );

            Object bookingID
                    = session.getAttribute(
                            "successfulBookingID"
                    );

            Object transactionCode
                    = session.getAttribute(
                            "successfulTransactionCode"
                    );

            if (paymentID == null
                    || bookingID == null) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/booking-cart"
                );

                return;
            }

            request.setAttribute(
                    "paymentID",
                    paymentID
            );

            request.setAttribute(
                    "bookingID",
                    bookingID
            );

            request.setAttribute(
                    "transactionCode",
                    transactionCode
            );

            session.removeAttribute(
                    "successfulPaymentID"
            );

            session.removeAttribute(
                    "successfulBookingID"
            );

            session.removeAttribute(
                    "successfulTransactionCode"
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/payment-success.jsp"
            ).forward(request, response);

            return;
        }

        String bookingID
                = request.getParameter("bookingID");

        if (bookingID == null
                || bookingID.trim().isEmpty()) {

            response.sendError(
                    HttpServletResponse.SC_BAD_REQUEST,
                    "Booking ID is required."
            );

            return;
        }

        Booking booking
                = paymentService.getBookingForPayment(
                        bookingID.trim(),
                        user.getUserID()
                );

        if (booking == null) {
            response.sendError(
                    HttpServletResponse.SC_NOT_FOUND,
                    "The requested booking was not found."
            );

            return;
        }

        if (!"PENDING_PAYMENT".equals(
                booking.getBookingStatus())) {

            response.sendError(
                    HttpServletResponse.SC_CONFLICT,
                    "This booking is not awaiting payment."
            );

            return;
        }

        List<Paymentmethod> paymentMethods
                = paymentmethodDAO.getAll();

        Object paymentError
                = session.getAttribute("paymentError");

        if (paymentError != null) {
            request.setAttribute(
                    "errorMessage",
                    paymentError.toString()
            );

            session.removeAttribute("paymentError");
        }

        request.setAttribute("booking", booking);

        request.setAttribute(
                "paymentMethods",
                paymentMethods
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/payment.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        HttpSession session
                = request.getSession(false);

        if (session == null
                || session.getAttribute("user") == null) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

            return;
        }

        Users user
                = (Users) session.getAttribute("user");

        if (!"Customer".equalsIgnoreCase(
                user.getRole())) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Only customers can make payments."
            );

            return;
        }

        String bookingID
                = request.getParameter("bookingID");

        String methodID
                = request.getParameter("methodID");

        if (bookingID != null) {
            bookingID = bookingID.trim();
        }

        try {
            Payment payment
                    = paymentService.processPayment(
                            bookingID,
                            methodID,
                            user.getUserID()
                    );

            String successfulBookingID
                    = payment.getBookingID()
                            .getBookingID();

            session.setAttribute(
                    "successfulPaymentID",
                    payment.getPaymentID()
            );

            session.setAttribute(
                    "successfulBookingID",
                    successfulBookingID
            );

            session.setAttribute(
                    "successfulTransactionCode",
                    payment.getTransactionCode()
            );

            response.sendRedirect(
                    request.getContextPath()
                    + "/payment?success=true"
            );

        } catch (IllegalArgumentException
                | IllegalStateException ex) {

            session.setAttribute(
                    "paymentError",
                    ex.getMessage()
            );

            if (bookingID == null
                    || bookingID.isEmpty()) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/my-bookings"
                );

                return;
            }

            String encodedBookingID
                    = URLEncoder.encode(
                            bookingID,
                            StandardCharsets.UTF_8
                    );

            response.sendRedirect(
                    request.getContextPath()
                    + "/payment?bookingID="
                    + encodedBookingID
            );

        } catch (Exception ex) {
            ex.printStackTrace();

            session.setAttribute(
                    "paymentError",
                    "Payment could not be completed."
            );

            if (bookingID == null
                    || bookingID.isEmpty()) {

                response.sendRedirect(
                        request.getContextPath()
                        + "/my-bookings"
                );

                return;
            }

            String encodedBookingID
                    = URLEncoder.encode(
                            bookingID,
                            StandardCharsets.UTF_8
                    );

            response.sendRedirect(
                    request.getContextPath()
                    + "/payment?bookingID="
                    + encodedBookingID
            );
        }
    }
}
