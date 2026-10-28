package controller;

import dao.PaymentmethodDAO;
import dao.WalletDAO;
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
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import service.PaymentService;
import service.PaymentService.PaymentRequest;

@WebServlet(name = "PaymentServlet", urlPatterns = {"/payment"})
public class PaymentServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setHeader("Cache-Control", "no-store");
        Users user = requireCustomer(request, response);
        if (user == null)
        {
            return;
        }
        HttpSession session = request.getSession(false);
        if ("true".equals(request.getParameter("success")))
        {
            showSuccess(request, response, session, user);
            return;
        }
        String bookingID = trim(request.getParameter("bookingID"));
        if (bookingID.isEmpty())
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Booking ID is required.");
            return;
        }
        PaymentService service = createPaymentService();
        Booking booking;
        PaymentRequest paymentRequest;
        Map<String, Object> summary;
        List<Paymentmethod> methods;
        try
        {
            booking = service.getBookingForPayment(bookingID, user.getUserID());
            if (booking == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested booking was not found.");
                return;
            }
            paymentRequest = service.createPaymentRequest(bookingID, user.getUserID());
            booking = service.getBookingForPayment(bookingID, user.getUserID());
            summary = service.getPaymentSummary(bookingID, user.getUserID());
            methods = new PaymentmethodDAO().getBookingMethods();
        }
        catch (IllegalArgumentException exception)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, exception.getMessage());
            return;
        }
        catch (IllegalStateException exception)
        {
            getServletContext().log("Unable to prepare the booking payment.", exception);
            response.sendError(HttpServletResponse.SC_CONFLICT, "This booking is expired, fully paid, unavailable, or cannot currently be paid. Check your bookings.");
            return;
        }
        catch (Exception exception)
        {
            throw new ServletException("Unable to load the payment page.", exception);
        }
        String token = UUID.randomUUID().toString();
        synchronized (session)
        {
            Map<String, PaymentRequest> requests = paymentRequests(session);
            while (requests.size() >= 20)
            {
                requests.remove(requests.keySet().iterator().next());
            }
            requests.put(token, paymentRequest);
            Object error = session.getAttribute("paymentError");
            if (error != null)
            {
                request.setAttribute("errorMessage", error.toString());
                session.removeAttribute("paymentError");
            }
        }
        request.setAttribute("booking", booking);
        request.setAttribute("walletBalance", paymentRequest.getWalletBalance());
        request.setAttribute("walletAvailable", paymentRequest.getWalletBalance() != null);
        request.setAttribute("walletCanPay", paymentRequest.getOfferedWalletAmount().compareTo(paymentRequest.getAmount()) >= 0);
        request.setAttribute("walletContribution", paymentRequest.getOfferedWalletAmount());
        request.setAttribute("externalContribution", paymentRequest.getAmount().subtract(paymentRequest.getOfferedWalletAmount()));
        request.setAttribute("bookingSummary", summary);
        request.setAttribute("paymentMethods", methods);
        request.setAttribute("paymentOption", summary.get("paymentOption"));
        request.setAttribute("paymentType", paymentRequest.getPaymentType());
        request.setAttribute("amountToPay", paymentRequest.getAmount());
        request.setAttribute("paymentRequestToken", token);
        request.getRequestDispatcher("/WEB-INF/views/payment.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Users user = requireCustomer(request, response);
        if (user == null)
        {
            return;
        }
        HttpSession session = request.getSession(false);
        String bookingID = trim(request.getParameter("bookingID"));
        String methodID = trim(request.getParameter("methodID"));
        boolean useWallet = "true".equals(request.getParameter("useWallet"));
        String token = trim(request.getParameter("paymentRequestToken"));
        PaymentRequest paymentRequest;
        synchronized (session)
        {
            paymentRequest = paymentRequests(session).get(token);
        }
        if (paymentRequest == null || paymentRequest.getUserID() != user.getUserID() || !paymentRequest.getBookingID().equals(bookingID))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The payment form is invalid or expired. Reload the payment page.");
            return;
        }
        String redirect;
        try
        {
            Payment payment = createPaymentService().processPayment(paymentRequest, methodID, user.getUserID(), useWallet);
            if ("REFUNDED".equals(payment.getStatus()))
            {
                session.setAttribute("paymentError", "This payment has already been refunded to your wallet.");
                redirect = "/my-bookings";
            }
            else
            {
                synchronized (session)
                {
                    session.setAttribute("successfulPaymentID", payment.getPaymentID());
                    session.setAttribute("successfulBookingID", payment.getBookingID().getBookingID().trim());
                    session.setAttribute("successfulTransactionCode", payment.getTransactionCode());
                    session.setAttribute("successfulPaymentType", paymentRequest.getPaymentType());
                    session.setAttribute("successfulPaymentAmount", payment.getAmount());
                    session.setAttribute("successfulWalletAmount", payment.getWalletAmount());
                    session.setAttribute("successfulExternalAmount", payment.getExternalAmount());
                    session.setAttribute("successfulExternalMethodName", payment.getMethodID().getMethodName());
                    session.setAttribute("successfulPaymentMethodID", payment.getMethodID().getMethodID().trim());
                    session.setAttribute("successfulPaymentUserID", user.getUserID());
                    session.removeAttribute("paymentError");
                    if (bookingID.equals(session.getAttribute("pendingBookingID")))
                    {
                        session.removeAttribute("pendingBookingID");
                    }
                }
                redirect = "/payment?success=true";
            }
        }
        catch (IllegalArgumentException | IllegalStateException exception)
        {
            session.setAttribute("paymentError", exception.getMessage() == null ? "Payment could not be completed." : exception.getMessage());
            redirect = "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8);
        }
        catch (Exception exception)
        {
            getServletContext().log("Unable to confirm the payment.", exception);
            session.setAttribute("paymentError", "Payment could not be confirmed. Check your booking before trying again.");
            redirect = "/payment?bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8);
        }
        // Redirect errors must not be treated as a failed payment after the database committed.
        response.sendRedirect(request.getContextPath() + redirect);
    }

    private Users requireCustomer(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) value;
        if (!"Customer".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can make payments.");
            return null;
        }
        Integer userID = user.getUserID();
        if (userID == null || userID <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The login session is invalid.");
            return null;
        }
        return user;
    }

    @SuppressWarnings("unchecked")
    private Map<String, PaymentRequest> paymentRequests(HttpSession session)
    {
        Object value = session.getAttribute("paymentRequests");
        if (value instanceof Map)
        {
            return (Map<String, PaymentRequest>) value;
        }
        Map<String, PaymentRequest> requests = new LinkedHashMap<>();
        session.setAttribute("paymentRequests", requests);
        return requests;
    }

    private void showSuccess(HttpServletRequest request, HttpServletResponse response, HttpSession session, Users user) throws ServletException, IOException
    {
        synchronized (session)
        {
            Object paymentID = session.getAttribute("successfulPaymentID");
            Object bookingID = session.getAttribute("successfulBookingID");
            if (paymentID == null || bookingID == null || !user.getUserID().equals(session.getAttribute("successfulPaymentUserID")))
            {
                response.sendRedirect(request.getContextPath() + "/my-bookings");
                return;
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
        if (request.getAttribute("walletAmount") instanceof java.math.BigDecimal && ((java.math.BigDecimal) request.getAttribute("walletAmount")).signum() > 0)
        {
            try { request.setAttribute("wallet", loadWalletSummary(user.getUserID())); }
            catch (IllegalStateException exception) { getServletContext().log("Payment succeeded, but the updated wallet balance is unavailable.", exception); }
        }
        try
        {
            PaymentService service = createPaymentService();
            String id = String.valueOf(request.getAttribute("bookingID"));
            Booking booking = service.getBookingForPayment(id, user.getUserID());
            if (booking != null)
            {
                Map<String, Object> summary = service.getPaymentSummary(id, user.getUserID());
                if (summary != null && !summary.isEmpty())
                {
                    request.setAttribute("successBooking", booking);
                    request.setAttribute("successBookingSummary", summary);
                }
            }
        }
        catch (RuntimeException exception)
        {
            // A read failure must not make a recorded payment look unsuccessful.
            getServletContext().log("Payment succeeded, but the latest booking summary is unavailable.", exception);
        }
        request.getRequestDispatcher("/WEB-INF/views/payment-success.jsp").forward(request, response);
    }

    protected PaymentService createPaymentService()
    {
        return new PaymentService();
    }

    protected WalletSummary loadWalletSummary(int userID)
    {
        return new WalletDAO().getWalletByUserID(userID);
    }

    private String trim(String value)
    {
        return value == null ? "" : value.trim();
    }

    @Override
    public String getServletInfo()
    {
        return "Customer deposit, full payment and balance payment servlet";
    }
}
