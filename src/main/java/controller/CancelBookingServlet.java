package controller;

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
import java.util.Map;
import service.BookingLifecycleService;

@WebServlet(name = "CancelBookingServlet", urlPatterns = {"/my-bookings/cancel"})
public class CancelBookingServlet extends HttpServlet
{
    private final BookingLifecycleService lifecycleService = new BookingLifecycleService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        response.setHeader("Allow", "POST");
        response.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED, "Use the cancellation form to cancel a booking.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        request.setCharacterEncoding("UTF-8");
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
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only customers can cancel their own bookings here.");
            return;
        }
        Integer userID = user.getUserID();
        if (userID == null || userID <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "The login session is invalid.");
            return;
        }
        if (!validCsrfToken(request, session))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "The cancellation form is invalid or expired. Reload My Bookings.");
            return;
        }
        String bookingID = request.getParameter("bookingID");
        try
        {
            Map<String, Object> result = lifecycleService.cancelByCustomerWithResult(bookingID, userID);
            session.removeAttribute("customerBookingError");
            session.setAttribute("customerBookingSuccess", result.get("message"));
        }
        catch (IllegalArgumentException | IllegalStateException exception)
        {
            session.removeAttribute("customerBookingSuccess");
            session.setAttribute("customerBookingError", exception.getMessage() == null ? "The cancellation could not be confirmed." : exception.getMessage());
        }
        catch (Exception exception)
        {
            getServletContext().log("Unable to confirm the customer booking cancellation.", exception);
            session.removeAttribute("customerBookingSuccess");
            session.setAttribute("customerBookingError", "Cancellation could not be confirmed. Check your booking and wallet before trying again.");
        }
        response.sendRedirect(request.getContextPath() + "/my-bookings");
    }

    private boolean validCsrfToken(HttpServletRequest request, HttpSession session)
    {
        Object storedToken = session.getAttribute("bookingCsrfToken");
        String submittedToken = request.getParameter("csrfToken");
        if (!(storedToken instanceof String) || ((String) storedToken).isBlank() || submittedToken == null || submittedToken.length() > 100)
        {
            return false;
        }
        return MessageDigest.isEqual(((String) storedToken).getBytes(StandardCharsets.UTF_8), submittedToken.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public String getServletInfo()
    {
        return "Customer cancellation with wallet refund and booking restrictions";
    }
}