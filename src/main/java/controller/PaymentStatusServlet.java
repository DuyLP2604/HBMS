package controller;

import dao.PayOsOrderDAO;
import dto.PayOsOrder;
import entity.Users;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Set;

/** Returns the PayOS order status for the customer who owns it. */
@WebServlet("/payment-status")
public class PaymentStatusServlet extends HttpServlet {
    /** Return the status of a PayOS order owned by the signed-in customer. */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-store");
        HttpSession session = request.getSession(false);
        Object user = session == null ? null : session.getAttribute("user");
        if (!(user instanceof Users) || !"Customer".equalsIgnoreCase(((Users) user).getRole())) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        try {
            PayOsOrder order = new PayOsOrderDAO().find(Long.parseLong(request.getParameter("orderCode")));
            if (order == null || !Integer.valueOf(order.userID).equals(((Users) user).getUserID())) {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                return;
            }
            String status = Set.of("PAID", "FAILED").contains(order.status) ? order.status : "PENDING";
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"status\":\"" + status + "\"}");
        } catch (NumberFormatException exception) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        } catch (SQLException exception) {
            log("Unable to load PayOS order status.", exception);
            response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        }
    }
}
