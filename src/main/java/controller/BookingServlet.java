package controller;

import dao.BookingDAO;
import dao.CustomerDAO;
import dao.HotelDAO;
import dao.RoomDAO;
import entity.Booking;
import entity.Customer;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import util.flash.Flash;

@WebServlet(name = "BookingServlet", urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireUser(request, response);
        if (user == null)
        {
            return;
        }
        String action = getAction(request);
        if ("cancel".equals(action))
        {
            response.setHeader("Allow", "POST");
            response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use POST to cancel a booking.");
            return;
        }
        if (!"list".equals(action) && !"detail".equals(action) && !"add".equals(action))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }
        if ("add".equals(action) && !canManageBookings(user))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        BookingDAO bookingDAO = new BookingDAO();
        CustomerDAO customerDAO = new CustomerDAO();
        try
        {
            Customer ownCustomer = null;
            if (!canManageBookings(user))
            {
                ownCustomer = customerDAO.getCustomerByUserId(user.getUserID());
                if (ownCustomer == null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Customer profile not found.");
                    return;
                }
                bookingDAO.expirePendingBookings(ownCustomer.getCustomerID());
            }
            prepareCancelToken(request);
            if ("detail".equals(action))
            {
                String bookingID = getBookingID(request);
                if (bookingID == null)
                {
                    response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
                    return;
                }
                Booking booking = bookingDAO.getByIdWithDetails(bookingID);
                if (booking == null)
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                    return;
                }
                if (!canManageBookings(user) && !belongsTo(booking, ownCustomer))
                {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot view another customer's booking.");
                    return;
                }
                request.setAttribute("booking", booking);
                request.setAttribute("bookingSummary", bookingDAO.getPaymentSummary(bookingID));
                request.getRequestDispatcher("/WEB-INF/views/bookingDetail.jsp").forward(request, response);
                return;
            }
            if ("add".equals(action))
            {
                request.setAttribute("hotelList", new HotelDAO().getAllHotels());
                request.setAttribute("roomList", new RoomDAO().getAll());
                request.getRequestDispatcher("/WEB-INF/views/addBooking.jsp").forward(request, response);
                return;
            }
            if (canManageBookings(user))
            {
                request.setAttribute("bookingList", bookingDAO.getAll());
            }
            else
            {
                request.setAttribute("bookingList", bookingDAO.getByCustomerId(ownCustomer.getCustomerID()));
                request.setAttribute("bookingAccess", bookingDAO.getBookingAccess(user.getUserID()));
            }
            request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
        }
        catch (Exception exception)
        {
            throw new ServletException("Unable to load the booking page.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
        Users user = requireUser(request, response);
        if (user == null)
        {
            return;
        }
        String action = getAction(request);
        if ("add".equals(action))
        {
            if (!canManageBookings(user))
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            response.sendError(HttpServletResponse.SC_NOT_IMPLEMENTED, "The booking creation form must be connected to createBooking with room requests, dates and a server-defined booking ID and payment deadline.");
            return;
        }
        if (!"cancel".equals(action))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported booking action.");
            return;
        }
        if (!hasValidCancelToken(request))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Invalid cancellation request. Reload the booking page.");
            return;
        }
        String bookingID = getBookingID(request);
        if (bookingID == null)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
            return;
        }
        BookingDAO bookingDAO = new BookingDAO();
        try
        {
            Booking booking = bookingDAO.getByIdWithDetails(bookingID);
            if (booking == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking not found.");
                return;
            }
            if (!canManageBookings(user))
            {
                Customer customer = new CustomerDAO().getCustomerByUserId(user.getUserID());
                if (!belongsTo(booking, customer))
                {
                    response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot cancel another customer's booking.");
                    return;
                }
            }
            String reason = canManageBookings(user) ? "STAFF_REQUEST" : "CUSTOMER_REQUEST";
            bookingDAO.cancelBooking(bookingID, user.getUserID(), reason);
            Map<String, Object> summary = bookingDAO.getPaymentSummary(bookingID);
            if ("REFUNDED".equals(summary.get("paymentStatus")))
            {
                Flash.success(request, "Booking cancelled. Eligible payments have been refunded to the customer's system wallet.");
            }
            else if ("NO_REFUND".equals(summary.get("paymentStatus")))
            {
                Flash.success(request, "Booking cancelled. The refund period has expired.");
            }
            else
            {
                Flash.success(request, "Booking cancelled successfully.");
            }
        }
        catch (BookingDAO.BookingOperationException exception)
        {
            log("Booking cancellation failed for " + bookingID, exception);
            if (exception.getSqlErrorCode() == 51003 || exception.getSqlErrorCode() == 51004)
            {
                response.sendError(HttpServletResponse.SC_FORBIDDEN);
                return;
            }
            if (exception.getSqlErrorCode() == 51001)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            Flash.error(request, exception.getSqlErrorCode() == 51006 ? "Only bookings before check-in can be cancelled." : "Unable to confirm cancellation. Check the booking status before trying again.");
        }
        catch (Exception exception)
        {
            log("Unable to confirm booking cancellation for " + bookingID, exception);
            Flash.error(request, "Unable to confirm cancellation. Check the booking status before trying again.");
        }
        response.sendRedirect(request.getContextPath() + "/booking?action=list");
    }

    private Users requireUser(HttpServletRequest request, HttpServletResponse response) throws IOException
    {
        HttpSession session = request.getSession(false);
        Object value = session == null ? null : session.getAttribute("user");
        if (!(value instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return null;
        }
        Users user = (Users) value;
        Integer userID = user.getUserID();
        if (userID == null || userID <= 0 || (!canManageBookings(user) && !"Customer".equalsIgnoreCase(user.getRole())))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private boolean canManageBookings(Users user)
    {
        return "Staff".equalsIgnoreCase(user.getRole()) || "Admin".equalsIgnoreCase(user.getRole());
    }

    private boolean belongsTo(Booking booking, Customer customer)
    {
        return customer != null && customer.getCustomerID() != null && booking.getCustomerID() != null && booking.getCustomerID().getCustomerID() != null && customer.getCustomerID().trim().equalsIgnoreCase(booking.getCustomerID().getCustomerID().trim());
    }

    private String getAction(HttpServletRequest request)
    {
        String action = request.getParameter("action");
        return action == null || action.isBlank() ? "list" : action.trim().toLowerCase(Locale.ROOT);
    }

    private String getBookingID(HttpServletRequest request)
    {
        String id = request.getParameter("id");
        if (id == null || id.isBlank())
        {
            id = request.getParameter("bookingID");
        }
        return id == null || id.isBlank() || id.trim().length() > 6 ? null : id.trim();
    }

    private void prepareCancelToken(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        synchronized (session)
        {
            Object token = session.getAttribute("bookingCsrfToken");
            if (!(token instanceof String))
            {
                token = UUID.randomUUID().toString();
                session.setAttribute("bookingCsrfToken", token);
            }
            request.setAttribute("bookingCsrfToken", token);
        }
    }

    private boolean hasValidCancelToken(HttpServletRequest request)
    {
        HttpSession session = request.getSession(false);
        Object stored = session == null ? null : session.getAttribute("bookingCsrfToken");
        String submitted = request.getParameter("csrfToken");
        return stored instanceof String && submitted != null && MessageDigest.isEqual(((String) stored).getBytes(StandardCharsets.UTF_8), submitted.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getServletInfo()
    {
        return "Booking list, details and cancellation servlet";
    }
}