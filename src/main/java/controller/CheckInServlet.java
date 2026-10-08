package controller;

import dao.BookingDAO;
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

@WebServlet(name = "CheckInServlet", urlPatterns = {"/checkin"})
public class CheckInServlet extends HttpServlet
{
    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException
    {
        Users user = requireStaff(request, response);
        if (user == null)
        {
            return;
        }
        try
        {
            List<Booking> checkedInBookings = bookingDAO.getByStatus("CHECKED_IN");
            request.setAttribute("bookings", checkedInBookings);
            request.getRequestDispatcher("/WEB-INF/views/CheckIn.jsp").forward(request, response);
        }
        catch (Exception ex)
        {
            log("Unable to load checked-in bookings.", ex);
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
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
        String action = request.getParameter("action");
        if (!"checkIn".equals(action))
        {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported check-in action.");
            return;
        }
        HttpSession session = request.getSession(false);
        String bookingID = request.getParameter("bookingID");
        try
        {
            if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6)
            {
                throw new IllegalArgumentException("Mã booking không hợp lệ.");
            }
            bookingID = bookingID.trim();
            bookingDAO.updateStatus(bookingID, "CHECKED_IN", user.getUserID());
            session.removeAttribute("errorMessage");
            session.setAttribute("successMessage", "Đã Check-in thành công cho Booking: " + bookingID);
        }
        catch (Exception ex)
        {
            log("Unable to check in booking " + bookingID + ".", ex);
            session.removeAttribute("successMessage");
            session.setAttribute("errorMessage", "Lỗi khi Check-in: " + ex.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/checkin");
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
        if (user.getUserID() == null || user.getUserID() <= 0)
        {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Please log in again.");
            return null;
        }
        if (!"Staff".equalsIgnoreCase(user.getRole()) && !"Admin".equalsIgnoreCase(user.getRole()))
        {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only staff or admin can manage check-in.");
            return null;
        }
        return user;
    }
}
