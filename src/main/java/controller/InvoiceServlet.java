package controller;

import dao.BookingDAO;
import dao.InvoiceDAO;
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

@WebServlet(name = "InvoiceServlet", urlPatterns = {"/invoice"})
public class InvoiceServlet extends HttpServlet
{
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (requireStaff(request, response) == null)
        {
            return;
        }
        String action = request.getParameter("action");
        boolean print = "print".equals(action);
        if (action != null && !action.isBlank() && !"list".equals(action) && !print)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported invoice action.");
            return;
        }
        String bookingID = request.getParameter("bookingId");
        if (bookingID == null || bookingID.isBlank())
        {
            bookingID = request.getParameter("bookingID");
        }
        if (bookingID != null)
        {
            bookingID = bookingID.trim();
        }
        if (bookingID != null && bookingID.length() > 6)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
            return;
        }
        if (print && (bookingID == null || bookingID.isEmpty()))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A booking ID is required to print an invoice.");
            return;
        }
        if (bookingID != null && !bookingID.isEmpty())
        {
            Map<String, Object> summary;
            Map<String, String> invoice = null;
            try
            {
                summary = bookingDAO.getPaymentSummary(bookingID);
                if (print && summary != null && !summary.isEmpty())
                {
                    invoice = invoiceDAO.getInvoiceByBooking(bookingID);
                }
            }
            catch (Exception ex)
            {
                throw new ServletException("Unable to load the booking payment status.", ex);
            }
            if (summary == null || summary.isEmpty())
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested booking was not found.");
                return;
            }
            if ("CANCELLED".equals(summary.get("bookingStatus")))
            {
                response.sendError(HttpServletResponse.SC_CONFLICT, "A cancelled booking cannot use this invoice page.");
                return;
            }
            if (print && (!"CHECKED_OUT".equals(summary.get("bookingStatus")) || !"FULLY_PAID".equals(summary.get("paymentStatus"))))
            {
                response.sendError(HttpServletResponse.SC_CONFLICT, "Complete checkout and issue the invoice from Checkout Management before printing.");
                return;
            }
            if (print && invoice == null)
            {
                response.sendRedirect(request.getContextPath() + "/CheckoutEmployee?action=detail&bookingID=" + URLEncoder.encode(bookingID, StandardCharsets.UTF_8));
                return;
            }
            if (!loadBookingDetails(request, response, bookingID, summary, invoice))
            {
                return;
            }
        }
        try
        {
            request.setAttribute("occupiedRooms", invoiceDAO.getOccupiedRooms());
            request.setAttribute("paidInvoices", invoiceDAO.getPaidInvoices());
        }
        catch (Exception ex)
        {
            throw new ServletException("Unable to load the invoice list.", ex);
        }
        request.getRequestDispatcher(print ? "/WEB-INF/views/invoice.jsp" : "/WEB-INF/views/checkout.jsp").forward(request, response);
    }

    private boolean loadBookingDetails(HttpServletRequest request, HttpServletResponse response, String bookingID, Map<String, Object> summary, Map<String, String> invoice) throws ServletException, IOException
    {
        Map<String, String> room;
        List<Map<String, String>> services;
        BigDecimal totalAmount;
        BigDecimal serviceTotal = BigDecimal.ZERO;
        try
        {
            room = invoiceDAO.getRoomDetailByBooking(bookingID);
            services = invoiceDAO.getServicesByBooking(bookingID);
            Object amount = summary.get("totalAmount");
            if (!(amount instanceof BigDecimal) || ((BigDecimal) amount).signum() < 0)
            {
                throw new IllegalStateException("The booking total is unavailable.");
            }
            totalAmount = (BigDecimal) amount;
            if (invoice != null)
            {
                BigDecimal invoiceTotal = new BigDecimal(invoice.get("totalAmount"));
                if (invoiceTotal.compareTo(totalAmount) != 0)
                {
                    throw new IllegalStateException("The active invoice total does not match the booking total.");
                }
                totalAmount = invoiceTotal;
            }
            if (services == null)
            {
                throw new IllegalStateException("The booking service details are unavailable.");
            }
            for (Map<String, String> service : services)
            {
                serviceTotal = serviceTotal.add(requireServiceSubtotal(service));
            }
            if (serviceTotal.compareTo(totalAmount) > 0)
            {
                throw new IllegalStateException("The booking charges are inconsistent.");
            }
        }
        catch (Exception ex)
        {
            throw new ServletException("Unable to load the invoice details.", ex);
        }
        if (room == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The booking details were not found.");
            return false;
        }
        BigDecimal roomTotal = totalAmount.subtract(serviceTotal);
        Map<String, String> selectedRoom = new LinkedHashMap<>(room);
        selectedRoom.put("bookingID", bookingID);
        selectedRoom.put("roomTotal", roomTotal.toPlainString());
        selectedRoom.put("baseTotal", totalAmount.toPlainString());
        if (invoice != null)
        {
            selectedRoom.put("invoiceID", invoice.get("invoiceID"));
            selectedRoom.put("invoiceDate", invoice.get("invoiceDate"));
            request.setAttribute("invoice", invoice);
            request.setAttribute("invoiceID", invoice.get("invoiceID"));
            request.setAttribute("invoiceDate", invoice.get("invoiceDate"));
        }
        request.setAttribute("bookingID", bookingID);
        request.setAttribute("selectedRoom", selectedRoom);
        request.setAttribute("bookingServices", services);
        request.setAttribute("bookingSummary", summary);
        request.setAttribute("roomTotal", roomTotal);
        request.setAttribute("serviceTotal", serviceTotal);
        request.setAttribute("subTotal", totalAmount);
        request.setAttribute("vat", BigDecimal.ZERO);
        request.setAttribute("grandTotal", totalAmount);
        request.setAttribute("paidAmount", summary.get("totalPaidAmount"));
        request.setAttribute("remainingAmount", summary.get("remainingAmount"));
        return true;
    }

    private BigDecimal requireServiceSubtotal(Map<String, String> service)
    {
        String subtotal = service == null ? null : service.get("subtotal");
        if (subtotal == null || subtotal.isBlank())
        {
            throw new IllegalStateException("A booking service subtotal is missing.");
        }
        BigDecimal amount = new BigDecimal(subtotal.trim());
        if (amount.signum() < 0)
        {
            throw new IllegalStateException("A booking service subtotal is invalid.");
        }
        return amount;
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or admin can access invoices.");
            return null;
        }
        if (user.getUserID() == null || user.getUserID() <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your login session is invalid. Please log in again.");
            return null;
        }
        return user;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        if (requireStaff(request, response) == null)
        {
            return;
        }
        response.setHeader("Allow", "GET");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use Checkout Management to complete checkout and issue invoices.");
    }

    @Override
    public String getServletInfo()
    {
        return "Invoice viewing and printing servlet";
    }
}