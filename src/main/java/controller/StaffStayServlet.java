package controller;

import entity.Booking;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import service.BookingLifecycleService;

@WebServlet(name = "StaffStayServlet", urlPatterns = {"/staff/stays"})
public class StaffStayServlet extends HttpServlet
{
    private final BookingLifecycleService lifecycleService = new BookingLifecycleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }
        List<Booking> bookings;
        try
        {
            bookings = lifecycleService.getOperationalBookings();
        }
        catch (RuntimeException ex)
        {
            throw new ServletException("Unable to load guest stays.", ex);
        }
        moveFlashMessage(request, request.getSession(false));
        request.setAttribute("bookings", bookings);
        request.getRequestDispatcher("/WEB-INF/views/staff-stays.jsp").forward(request, response);
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
        HttpSession session = request.getSession(false);
        String bookingID = request.getParameter("bookingID");
        String action = request.getParameter("action");
        try
        {
            if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6)
            {
                throw new IllegalArgumentException("A valid booking ID is required.");
            }
            bookingID = bookingID.trim();
            if ("checkIn".equals(action))
            {
                lifecycleService.checkIn(bookingID, user.getUserID());
                setFlash(session, "lifecycleSuccess", "The guest was checked in successfully.");
            }
            else if ("checkOut".equals(action))
            {
                lifecycleService.checkOut(bookingID, user.getUserID());
                setFlash(session, "lifecycleSuccess", "The guest was checked out successfully.");
            }
            else
            {
                throw new IllegalArgumentException("Unsupported booking action.");
            }
        }
        catch (IllegalArgumentException | IllegalStateException ex)
        {
            setFlash(session, "lifecycleError", ex.getMessage());
        }
        catch (Exception ex)
        {
            log("Unable to update the guest stay.", ex);
            setFlash(session, "lifecycleError", "The booking status could not be confirmed. Check the booking before trying again.");
        }
        response.sendRedirect(request.getContextPath() + "/staff/stays");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or admin can manage guest stays.");
            return null;
        }
        if (user.getUserID() == null || user.getUserID() <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Your login session is invalid. Please log in again.");
            return null;
        }
        return user;
    }

    private void setFlash(HttpSession session, String key, String message)
    {
        synchronized (session)
        {
            session.removeAttribute("lifecycleSuccess");
            session.removeAttribute("lifecycleError");
            session.setAttribute(key, message);
        }
    }

    private void moveFlashMessage(HttpServletRequest request, HttpSession session)
    {
        synchronized (session)
        {
            Object success = session.getAttribute("lifecycleSuccess");
            Object error = session.getAttribute("lifecycleError");
            if (success != null)
            {
                request.setAttribute("successMessage", success);
                session.removeAttribute("lifecycleSuccess");
            }
            if (error != null)
            {
                request.setAttribute("errorMessage", error);
                session.removeAttribute("lifecycleError");
            }
        }
    }
}