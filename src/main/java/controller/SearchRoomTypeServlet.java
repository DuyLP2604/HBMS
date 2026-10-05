package controller;
import dao.RoomTypeDAO;
import dto.RoomTypeAvailability;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
@WebServlet(name = "SearchRoomTypeServlet", urlPatterns = {"/room-types"})
public class SearchRoomTypeServlet extends HttpServlet {
    private final RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        moveFlashMessage(request);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        request.setAttribute("today", today.toString());
        String checkInValue = request.getParameter("checkInDate");
        String checkOutValue = request.getParameter("checkOutDate");
        request.setAttribute("checkInDate", checkInValue);
        request.setAttribute("checkOutDate", checkOutValue);
        boolean hasCheckIn = checkInValue != null && !checkInValue.trim().isEmpty();
        boolean hasCheckOut = checkOutValue != null && !checkOutValue.trim().isEmpty();
        if (hasCheckIn && hasCheckOut) {
            try {
                LocalDate checkInDate = LocalDate.parse(checkInValue.trim());
                LocalDate checkOutDate = LocalDate.parse(checkOutValue.trim());
                if (checkInDate.isBefore(today)) {
                    throw new IllegalArgumentException("Check-in date cannot be in the past.");
                }
                if (!checkOutDate.isAfter(checkInDate)) {
                    throw new IllegalArgumentException("Check-out date must be after check-in date.");
                }
                List<RoomTypeAvailability> availabilityList = roomTypeDAO.getAvailability(checkInDate, checkOutDate);
                request.setAttribute("availabilityList", availabilityList);
                request.setAttribute("searched", true);
            } catch (DateTimeParseException ex) {
                request.setAttribute("errorMessage", "Dates must use the yyyy-MM-dd format.");
            } catch (IllegalArgumentException ex) {
                request.setAttribute("errorMessage", ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                request.setAttribute("errorMessage", "Unable to load room availability.");
            }
        } else if (hasCheckIn || hasCheckOut) {
            request.setAttribute("errorMessage", "Both check-in and check-out dates are required.");
        }
        request.getRequestDispatcher("/WEB-INF/views/room-type-search.jsp").forward(request, response);
    }

    private void moveFlashMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object success = session.getAttribute("bookingSuccess");
        Object error = session.getAttribute("bookingError");
        if (success != null) {
            request.setAttribute("successMessage", success.toString());
            session.removeAttribute("bookingSuccess");
        }
        if (error != null) {
            request.setAttribute("errorMessage", error.toString());
            session.removeAttribute("bookingError");
        }
    }
}