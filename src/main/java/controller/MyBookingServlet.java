package controller;

import dao.BookingDAO;
import dao.CustomerBookingDAO;
import dao.CustomerDAO;
import dto.CustomerBookingView;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet(name = "MyBookingServlet", urlPatterns = {"/my-bookings"})
public class MyBookingServlet extends HttpServlet
{
    private final CustomerBookingDAO bookingDAO = new CustomerBookingDAO();
    private final BookingDAO coreBookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        HttpSession session = request.getSession(false);
        Object sessionUser = session == null ? null : session.getAttribute("user");
        if (!(sessionUser instanceof Users))
        {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Users user = (Users) sessionUser;
        if (!"Customer".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can access My Bookings.");
            return;
        }
        Integer userID = user.getUserID();
        if (userID == null || userID <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The login session is invalid.");
            return;
        }
        String bookingID = request.getParameter("bookingID");
        bookingID = bookingID == null ? "" : bookingID.trim();
        if (bookingID.length() > 6)
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "A valid booking ID is required.");
            return;
        }
        try
        {
            Customer customer = new CustomerDAO().getCustomerByUserId(userID);
            if (customer == null)
            {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Your customer profile was not found.");
                return;
            }
            coreBookingDAO.expirePendingBookings(customer.getCustomerID());
            Map<String, Object> access = coreBookingDAO.getBookingAccess(userID);
            request.setAttribute("bookingAccess", access);
            request.setAttribute("canCreateBooking", Boolean.TRUE.equals(access.get("canCreateBooking")));
            synchronized (session)
            {
                Object token = session.getAttribute("bookingCsrfToken");
                if (!(token instanceof String) || ((String) token).isBlank())
                {
                    session.setAttribute("bookingCsrfToken", UUID.randomUUID().toString());
                }
                moveFlashMessage(request, session);
            }
            if (bookingID.isEmpty())
            {
                showBookingList(request, response, userID);
            }
            else
            {
                showBookingDetail(request, response, userID, bookingID);
            }
        }
        catch (RuntimeException exception)
        {
            throw new ServletException("Unable to load your current booking information.", exception);
        }
    }

    private void showBookingList(HttpServletRequest request, HttpServletResponse response, int userID) throws ServletException, IOException
    {
        Map<String, Map<String, Object>> summaries = new LinkedHashMap<>();
        for (Booking booking : coreBookingDAO.getByAccountUserId(userID))
        {
            String id = booking.getBookingID().trim();
            Map<String, Object> summary = coreBookingDAO.getPaymentSummary(id);
            if (summary.isEmpty())
            {
                throw new IllegalStateException("The booking payment summary is unavailable.");
            }
            summaries.put(id, summary);
            summaries.put(booking.getBookingID(), summary);
        }
        List<CustomerBookingView> bookings = bookingDAO.getByUserID(userID);
        request.setAttribute("bookings", bookings);
        request.setAttribute("paymentSummaries", summaries);
        request.getRequestDispatcher("/WEB-INF/views/my-bookings.jsp").forward(request, response);
    }

    private void showBookingDetail(HttpServletRequest request, HttpServletResponse response, int userID, String bookingID) throws ServletException, IOException
    {
        Booking ownedBooking = coreBookingDAO.getByIdWithDetails(bookingID);
        if (!isOwnedBy(ownedBooking, userID))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested booking was not found.");
            return;
        }
        CustomerBookingView booking = bookingDAO.getDetail(bookingID, userID);
        if (booking == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested booking was not found.");
            return;
        }
        Map<String, Object> summary = coreBookingDAO.getPaymentSummary(bookingID);
        if (summary.isEmpty())
        {
            throw new IllegalStateException("The booking payment summary is unavailable.");
        }
        request.setAttribute("booking", booking);
        request.setAttribute("coreBooking", ownedBooking);
        request.setAttribute("bookingSummary", summary);
        request.getRequestDispatcher("/WEB-INF/views/my-booking-detail.jsp").forward(request, response);
    }

    private boolean isOwnedBy(Booking booking, int userID)
    {
        return booking != null && booking.getCustomerID() != null && booking.getCustomerID().getUserID() != null && Integer.valueOf(userID).equals(booking.getCustomerID().getUserID().getUserID());
    }

    private void moveFlashMessage(HttpServletRequest request, HttpSession session)
    {
        Object success = session.getAttribute("customerBookingSuccess");
        Object error = session.getAttribute("customerBookingError");
        if (success != null)
        {
            request.setAttribute("successMessage", success.toString());
            session.removeAttribute("customerBookingSuccess");
        }
        if (error != null)
        {
            request.setAttribute("errorMessage", error.toString());
            session.removeAttribute("customerBookingError");
        }
        Object paymentError = session.getAttribute("paymentError");
        if (paymentError != null)
        {
            if (error == null)
            {
                request.setAttribute("errorMessage", paymentError.toString());
            }
            session.removeAttribute("paymentError");
        }
    }

    @Override
    public String getServletInfo()
    {
        return "Customer booking list and details with payment, refund and booking access information";
    }
}