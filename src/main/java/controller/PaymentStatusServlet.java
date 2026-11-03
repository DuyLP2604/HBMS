package controller;

import dao.PayOsOrderDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/payment-status")
public class PaymentStatusServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Object uid = req.getSession().getAttribute("userID"); // use the same session key as your PaymentServlet
        resp.setContentType("application/json");
        if (uid == null) {
            resp.setStatus(401);
            return;
        }
        try {
            String s = new PayOsOrderDAO().latestStatus(req.getParameter("bookingID"), (Integer) uid);
            resp.getWriter().write("{\"status\":\"" + s + "\"}");
        } catch (IOException | SQLException e) {
            resp.setStatus(500);
        }
    }
}
