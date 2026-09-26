package controller;

import dao.BookingDAO;
import dao.ServiceDAO;
import entity.Booking;
import entity.Service;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;

@WebServlet(
        name = "ServiceServlet",
        urlPatterns = {"/service"}
)
public class ServiceServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Users currentUser = getCurrentUser(request);

        if (currentUser == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        try {
            ServiceDAO serviceDAO = new ServiceDAO();

            List<Service> services
                    = serviceDAO.getAllServices();

            request.setAttribute("list", services);

            request.getRequestDispatcher(
                    "/WEB-INF/views/service.jsp"
            ).forward(request, response);
        } catch (Exception exception) {
            throw new ServletException(
                    "Unable to load the service list.",
                    exception
            );
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        Users currentUser = getCurrentUser(request);

        if (currentUser == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        HttpSession session = request.getSession();

        String selectedServiceID
                = request.getParameter(
                        "selectedService"
                );

        String quantityValue
                = request.getParameter("quantity");

        try {
            if (selectedServiceID == null
                    || selectedServiceID.trim().isEmpty()) {

                throw new IllegalArgumentException(
                        "Please select a service."
                );
            }

            int quantity = parseQuantity(quantityValue);

            BookingDAO bookingDAO = new BookingDAO();

            Booking latestBooking
                    = bookingDAO.getLatestPendingBookingByUserId(
                            currentUser.getUserID()
                    );

            if (latestBooking == null) {
                throw new IllegalStateException(
                        "No booking was found "
                        + "for the current user."
                );
            }

            /*
             * Services are added before final payment.
             * Adding a service changes Booking.TotalAmount.
             */
            if (!"PENDING_PAYMENT".equals(
                    latestBooking.getBookingStatus())) {

                throw new IllegalStateException(
                        "Services can only be added "
                        + "before the booking is paid."
                );
            }

            ServiceDAO serviceDAO = new ServiceDAO();

            serviceDAO.insertBookingService(
                    latestBooking.getBookingID(),
                    selectedServiceID.trim(),
                    quantity
            );

            session.setAttribute(
                    "successMessage",
                    "The service was added successfully."
            );
        } catch (Exception exception) {
            session.setAttribute(
                    "errorMessage",
                    exception.getMessage() != null
                            ? exception.getMessage()
                            : "Unable to add the service."
            );
        }

        response.sendRedirect(
                request.getContextPath() + "/service"
        );
    }

    private Users getCurrentUser(
            HttpServletRequest request) {

        return (Users) request
                .getSession()
                .getAttribute("user");
    }

    private int parseQuantity(String quantityValue) {
        if (quantityValue == null
                || quantityValue.trim().isEmpty()) {

            return 1;
        }

        try {
            int quantity = Integer.parseInt(
                    quantityValue.trim()
            );

            if (quantity <= 0) {
                throw new IllegalArgumentException(
                        "Service quantity must be "
                        + "greater than zero."
                );
            }

            return quantity;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(
                    "Service quantity must be a valid number."
            );
        }
    }

    @Override
    public String getServletInfo() {
        return "Handles booking service selection.";
    }
}