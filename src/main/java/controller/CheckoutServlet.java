package controller;

import dao.BookingDAO;
import dto.BookingWishList;
import entity.Booking;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import service.BookingCheckoutService;
import util.BookingWishListSession;

@WebServlet(name = "CheckoutServlet", urlPatterns = {"/checkout"})
public class CheckoutServlet extends HttpServlet
{
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireCustomer(request, response);
        if (user == null)
        {
            return;
        }
        HttpSession session = request.getSession(false);
        Object value = session.getAttribute("pendingBookingID");
        if (!(value instanceof String) || ((String) value).isBlank())
        {
            response.sendRedirect(request.getContextPath() + "/booking-wish-list");
            return;
        }
        String bookingID = ((String) value).trim();
        BookingDAO bookingDAO = new BookingDAO();
        try
        {
            Booking booking = bookingDAO.getByIdWithDetails(bookingID);
            if (booking == null)
            {
                session.removeAttribute("pendingBookingID");
                session.setAttribute("bookingError", "Booking not found.");
                response.sendRedirect(request.getContextPath() + "/booking-wish-list");
                return;
            }
            if (!isOwnedBy(booking, user))
            {
                session.removeAttribute("pendingBookingID");
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "You cannot access another customer's checkout.");
                return;
            }
            if ("PENDING_PAYMENT".equals(booking.getBookingStatus()))
            {
                bookingDAO.expirePendingBookings(booking.getCustomerID().getCustomerID());
                booking = bookingDAO.getByIdWithDetails(bookingID);
                if (booking == null || !isOwnedBy(booking, user))
                {
                    response.sendError(HttpServletResponse.SC_NOT_FOUND, "Booking is no longer available.");
                    return;
                }
            }
            if (!"PENDING_PAYMENT".equals(booking.getBookingStatus()))
            {
                session.removeAttribute("pendingBookingID");
                response.sendRedirect(request.getContextPath() + "/booking?action=detail&id=" + booking.getBookingID().trim());
                return;
            }
            Map<String, Object> summary = bookingDAO.getPaymentSummary(bookingID);
            if (summary.isEmpty())
            {
                throw new IllegalStateException("The booking payment summary could not be loaded.");
            }
            request.setAttribute("bookingID", booking.getBookingID().trim());
            request.setAttribute("booking", booking);
            request.setAttribute("bookingSummary", summary);
            request.setAttribute("paymentOption", booking.getPaymentOption());
            request.setAttribute("initialRequiredAmount", summary.get("initialRequiredAmount"));
            request.getRequestDispatcher("/WEB-INF/views/checkout-pending.jsp").forward(request, response);
        }
        catch (Exception exception)
        {
            throw new ServletException("Unable to load checkout information.", exception);
        }
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
        String redirect;
        try
        {
            synchronized (session)
            {
                BookingWishList wishlist = BookingWishListSession.get(session);
                if (wishlist == null || wishlist.isEmpty())
                {
                    Object pendingBookingID = session.getAttribute("pendingBookingID");
                    if (pendingBookingID instanceof String && !((String) pendingBookingID).isBlank())
                    {
                        redirect = "/checkout";
                    }
                    else
                    {
                        session.setAttribute("bookingError", "Your wish list is empty.");
                        redirect = "/booking-wish-list";
                    }
                }
                else
                {
                    String paymentOption = request.getParameter("paymentOption");
                    paymentOption = paymentOption == null ? "" : paymentOption.trim().toUpperCase(Locale.ROOT);
                    if (!"DEPOSIT".equals(paymentOption) && !"FULL".equals(paymentOption))
                    {
                        throw new IllegalArgumentException("Please choose a 30% deposit or full payment.");
                    }
                    Booking booking = new BookingCheckoutService().createPendingBooking(wishlist, user.getUserID(), paymentOption);
                    if (booking == null || booking.getBookingID() == null || booking.getBookingID().isBlank())
                    {
                        throw new IllegalStateException("Unable to confirm the booking. Check your bookings before trying again.");
                    }
                    session.setAttribute("pendingBookingID", booking.getBookingID().trim());
                    BookingWishListSession.remove(session);
                    session.removeAttribute("bookingError");
                    redirect = "/checkout";
                }
            }
        }
        catch (IllegalArgumentException | IllegalStateException exception)
        {
            log("Checkout was not completed for user " + user.getUserID(), exception);
            session.setAttribute("bookingError", exception.getMessage() == null ? "Unable to complete checkout." : exception.getMessage());
            redirect = "/booking-wish-list";
        }
        catch (Exception exception)
        {
            log("Unexpected checkout failure for user " + user.getUserID(), exception);
            session.setAttribute("bookingError", "Unable to confirm checkout. Check your bookings before trying again.");
            redirect = "/booking-wish-list";
        }
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
        Integer userID = user.getUserID();
        if (userID == null || userID <= 0 || !"Customer".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return null;
        }
        return user;
    }

    private boolean isOwnedBy(Booking booking, Users user)
    {
        if (booking.getCustomerID() == null || booking.getCustomerID().getUserID() == null)
        {
            return false;
        }
        Integer ownerUserID = booking.getCustomerID().getUserID().getUserID();
        return ownerUserID != null && ownerUserID.equals(user.getUserID());
    }

    @Override
    public String getServletInfo()
    {
        return "Customer booking checkout servlet";
    }
}