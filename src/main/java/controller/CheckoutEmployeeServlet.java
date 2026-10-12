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
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@WebServlet(name = "CheckoutEmployeeServlet", urlPatterns = {"/CheckoutEmployee"})
public class CheckoutEmployeeServlet extends HttpServlet
{
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }
        String action = request.getParameter("action");
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
        request.setAttribute("canCheckout", summary.get("firstPaidAt") != null && paidAmount.signum() > 0 && remainingAmount.signum() == 0 && ("CHECKED_IN".equals(summary.get("bookingStatus")) || issuedInvoice == null));
        moveFlashMessage(request, request.getSession(false));
        request.getRequestDispatcher("/WEB-INF/views/checkout-detail.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }
        if (!"exportInvoice".equals(request.getParameter("action")))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported checkout action.");
            return;
        }
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
        response.sendRedirect(request.getContextPath() + "/CheckoutEmployee");
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