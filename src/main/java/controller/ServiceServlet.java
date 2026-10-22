package controller;

import dao.BookingDAO;
import dao.ServiceDAO;
import entity.Booking;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet(name = "ServiceServlet", urlPatterns = {"/service"})
public class ServiceServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String action = request.getParameter("action");
        if ("orders".equalsIgnoreCase(action)) {
            if (!canAccessServices(currentUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }
            ServiceDAO serviceDAO = new ServiceDAO();
            request.setAttribute("orders", serviceDAO.getAllBookedServices());
            request.getRequestDispatcher("/WEB-INF/views/service-orders.jsp").forward(request, response);
            return;
        }

        if ("manage".equalsIgnoreCase(action)) {
            if (!canAccessServices(currentUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }
            ServiceDAO serviceDAO = new ServiceDAO();
            request.setAttribute("services", serviceDAO.getAllServices());
            request.getRequestDispatcher("/WEB-INF/views/services.jsp").forward(request, response);
            return;
        }

        try {
            ServiceDAO serviceDAO = new ServiceDAO();
            BookingDAO bookingDAO = new BookingDAO();

            request.setAttribute("list", serviceDAO.getAllServices());

            Booking targetBooking = resolveTargetBooking(request, currentUser, bookingDAO);
            request.setAttribute("currentBooking", targetBooking);

            if (targetBooking != null) {
                request.setAttribute("myServices", serviceDAO.getServicesByBookingId(targetBooking.getBookingID()));
            }

            request.getRequestDispatcher("/WEB-INF/views/service.jsp").forward(request, response);
        } catch (Exception exception) {
            throw new ServletException("Unable to load the service page.", exception);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        Users currentUser = getCurrentUser(request);
        if (currentUser == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        HttpSession session = request.getSession();
        String action = request.getParameter("action");

        if ("create".equalsIgnoreCase(action)) {
            try {
                String serviceName = request.getParameter("serviceName");
                double unitPrice = parsePrice(request.getParameter("unitPrice"));
                new ServiceDAO().createService(serviceName, unitPrice);
                session.setAttribute("successMessage", "Tạo dịch vụ mới thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/service?action=manage");
            return;
        }

        if ("update".equalsIgnoreCase(action)) {
            try {
                String serviceID = request.getParameter("serviceID");
                String serviceName = request.getParameter("serviceName");
                double unitPrice = parsePrice(request.getParameter("unitPrice"));

                new ServiceDAO().updateService(serviceID, serviceName, unitPrice);
                session.setAttribute("successMessage", "Cập nhật dịch vụ thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/service?action=manage");
            return;
        }
        if ("delete".equalsIgnoreCase(action)) {
            try {
                String serviceID = request.getParameter("serviceID");
                new ServiceDAO().deleteService(serviceID);
                session.setAttribute("successMessage", "Xóa dịch vụ thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/service?action=manage");
            return;
        }

        String selectedServiceID = request.getParameter("selectedService");
        String quantityValue = request.getParameter("quantity");
        String inputBookingID = request.getParameter("bookingID");

        try {
            if (selectedServiceID == null || selectedServiceID.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn dịch vụ.");
            }
            int quantity = parseQuantity(quantityValue);

            BookingDAO bookingDAO = new BookingDAO();
            Booking targetBooking;

            if (inputBookingID != null && !inputBookingID.trim().isEmpty()) {
                targetBooking = bookingDAO.getByIdWithDetails(inputBookingID.trim());
            } else {
                targetBooking = bookingDAO.getLatestActiveBookingByUserId(currentUser.getUserID());
            }

            if (targetBooking == null) {
                throw new IllegalStateException("Không tìm thấy đơn đặt phòng hợp lệ.");
            }

            if ("Customer".equalsIgnoreCase(currentUser.getRole())) {
                if (targetBooking.getCustomerID() == null || targetBooking.getCustomerID().getUserID() == null
                        || targetBooking.getCustomerID().getUserID().getUserID() != currentUser.getUserID()) {
                    throw new IllegalStateException("Bạn không có quyền thêm dịch vụ vào đơn đặt phòng này.");
                }
            }

            String status = targetBooking.getBookingStatus();
            if ("CHECKED_OUT".equals(status) || "CANCELLED".equals(status)) {
                throw new IllegalStateException("Đơn đặt phòng này đã hoàn tất hoặc bị hủy.");
            }

            ServiceDAO serviceDAO = new ServiceDAO();
            serviceDAO.insertBookingService(targetBooking.getBookingID(), selectedServiceID.trim(), quantity);

            session.setAttribute("successMessage", "Thêm dịch vụ thành công!");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            session.setAttribute("errorMessage", exception.getMessage() != null ? exception.getMessage() : "Không thể thêm dịch vụ.");
        }

        if ("Staff".equalsIgnoreCase(currentUser.getRole()) || "Admin".equalsIgnoreCase(currentUser.getRole())) {
            response.sendRedirect(request.getContextPath() + "/checkout");
        } else {
            response.sendRedirect(request.getContextPath() + "/service");
        }
    }

    private boolean canAccessServices(Users user) {
        if (user == null) {
            return false;
        }
        return "Admin".equalsIgnoreCase(user.getRole()) || "Staff".equalsIgnoreCase(user.getRole());
    }

    private Booking resolveTargetBooking(HttpServletRequest request, Users currentUser, BookingDAO bookingDAO) {
        String inputBookingID = request.getParameter("bookingID");
        if (inputBookingID != null && !inputBookingID.trim().isEmpty()) {
            Booking booking = bookingDAO.getByIdWithDetails(inputBookingID.trim());
            if (booking != null) {
                if ("Customer".equalsIgnoreCase(currentUser.getRole())) {
                    if (booking.getCustomerID() != null && booking.getCustomerID().getUserID() != null
                            && booking.getCustomerID().getUserID().getUserID() == currentUser.getUserID()) {
                        return booking;
                    }
                } else {
                    return booking;
                }
            }
        }

        if ("Customer".equalsIgnoreCase(currentUser.getRole())) {
            return bookingDAO.getLatestActiveBookingByUserId(currentUser.getUserID());
        }
        return null;
    }

    private Users getCurrentUser(HttpServletRequest request) {
        return (Users) request.getSession().getAttribute("user");
    }

    private int parseQuantity(String quantityValue) {
        if (quantityValue == null || quantityValue.trim().isEmpty()) {
            return 1;
        }
        try {
            int quantity = Integer.parseInt(quantityValue.trim());
            if (quantity <= 0) {
                throw new IllegalArgumentException("Số lượng dịch vụ phải lớn hơn 0.");
            }
            return quantity;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Số lượng dịch vụ không hợp lệ.");
        }
    }

    private double parsePrice(String priceValue) {
        if (priceValue == null || priceValue.trim().isEmpty()) {
            return 0.0;
        }
        try {
            double price = Double.parseDouble(priceValue.trim());
            if (price < 0) {
                throw new IllegalArgumentException("Đơn giá không được âm.");
            }
            return price;
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Đơn giá không hợp lệ.");
        }
    }
}
