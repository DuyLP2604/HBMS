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
import java.math.BigDecimal;

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
        if ("manage".equalsIgnoreCase(action)) {
            if (!canManageServices(currentUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied. Only staff or admin can manage services.");
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

        // Staff CRUD actions
        if ("create".equalsIgnoreCase(action)) {
            if (!canManageServices(currentUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }
            try {
                String serviceName = request.getParameter("serviceName");
                BigDecimal unitPrice = new BigDecimal(request.getParameter("unitPrice"));
                new ServiceDAO().createService(serviceName, unitPrice);
                session.setAttribute("successMessage", "Tạo dịch vụ mới thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Không thể tạo dịch vụ mới.");
            }
            response.sendRedirect(request.getContextPath() + "/service?action=manage");
            return;
        }

        if ("updatePrice".equalsIgnoreCase(action)) {
            if (!canManageServices(currentUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }
            try {
                String serviceID = request.getParameter("serviceID");
                BigDecimal unitPrice = new BigDecimal(request.getParameter("unitPrice"));
                new ServiceDAO().updateServicePrice(serviceID, unitPrice);
                session.setAttribute("successMessage", "Cập nhật đơn giá dịch vụ thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Không thể cập nhật giá dịch vụ.");
            }
            response.sendRedirect(request.getContextPath() + "/service?action=manage");
            return;
        }

        if ("delete".equalsIgnoreCase(action)) {
            if (!canManageServices(currentUser)) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied.");
                return;
            }
            try {
                String serviceID = request.getParameter("serviceID");
                new ServiceDAO().deleteService(serviceID);
                session.setAttribute("successMessage", "Xóa dịch vụ thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage() != null ? e.getMessage() : "Không thể xóa dịch vụ.");
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
                throw new IllegalStateException("Không tìm thấy đơn đặt phòng hợp lệ. Vui lòng đặt phòng trước khi chọn dịch vụ.");
            }

            // Customer chỉ được đặt vào booking của chính mình (Staff/Admin có thể hỗ trợ)
            if ("Customer".equalsIgnoreCase(currentUser.getRole())) {
                if (targetBooking.getCustomerID() == null || targetBooking.getCustomerID().getUserID() == null
                        || targetBooking.getCustomerID().getUserID().getUserID() != currentUser.getUserID()) {
                    throw new IllegalStateException("Bạn không có quyền thêm dịch vụ vào đơn đặt phòng này.");
                }
            }

            String status = targetBooking.getBookingStatus();
            if ("CHECKED_OUT".equals(status) || "CANCELLED".equals(status)) {
                throw new IllegalStateException("Không thể thêm dịch vụ cho phòng đã trả hoặc đã hủy.");
            }

            ServiceDAO serviceDAO = new ServiceDAO();
            serviceDAO.insertBookingService(targetBooking.getBookingID(), selectedServiceID.trim(), quantity);

            session.setAttribute("successMessage", "Đã thêm dịch vụ thành công vào đơn đặt phòng (" + targetBooking.getBookingID() + ").");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            session.setAttribute("errorMessage", exception.getMessage() != null ? exception.getMessage() : "Không thể thêm dịch vụ.");
        } catch (Exception exception) {
            session.setAttribute("errorMessage", "Đã có lỗi xảy ra khi thêm dịch vụ. Vui lòng thử lại.");
        }

        String redirectUrl = request.getContextPath() + "/service";
        if (inputBookingID != null && !inputBookingID.trim().isEmpty()) {
            redirectUrl += "?bookingID=" + inputBookingID.trim();
        }
        response.sendRedirect(redirectUrl);
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

    private boolean canManageServices(Users user) {
        if (user == null) {
            return false;
        }
        return "Admin".equalsIgnoreCase(user.getRole()) || "Staff".equalsIgnoreCase(user.getRole());
    }
}
