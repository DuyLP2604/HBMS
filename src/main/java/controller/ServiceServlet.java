package controller;

import dao.BookingDAO;
import dao.EmployeeDAO;
import dao.ServiceDAO;
import entity.Booking;
import entity.BookingService;
import entity.Employee;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import util.flash.Flash;
import util.security.CsrfTokens;

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
            boolean canFulfill = canFulfillOrders(currentUser);
            boolean isReceptionistOrAdmin = "Admin".equalsIgnoreCase(currentUser.getRole())
                    || ("Staff".equalsIgnoreCase(currentUser.getRole()) && new dao.EmployeeDAO().isReceptionistByUserId(currentUser.getUserID()));
            request.setAttribute("canFulfillOrders", canFulfill);
            request.setAttribute("canCancelService", isReceptionistOrAdmin);
            if (canFulfill || isReceptionistOrAdmin) {
                request.setAttribute("serviceOrderCsrfToken", CsrfTokens.getOrCreate(request.getSession()));
            }
            int requestedPage = 1;
            try {
                String pageParam = request.getParameter("page");
                if (pageParam != null) {
                    requestedPage = Math.max(1, Integer.parseInt(pageParam.trim()));
                }
            } catch (NumberFormatException ignored) {
            }
            int pageSize = 10;
            long totalOrders = serviceDAO.countBookedServices();
            int totalPages = (int) Math.max(1L, (totalOrders + pageSize - 1L) / pageSize);
            int currentPage = Math.min(requestedPage, totalPages);
            int offset = (currentPage - 1) * pageSize;
            List<BookingService> orders = serviceDAO.getBookedServicesPaged(offset, pageSize);

            request.setAttribute("orders", orders);
            request.setAttribute("currentPage", currentPage);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalOrders", totalOrders);
            request.setAttribute("pageSize", pageSize);
            request.setAttribute("startIndex", orders.isEmpty() ? 0 : offset + 1);
            request.setAttribute("endIndex", orders.isEmpty() ? 0 : offset + orders.size());
            request.setAttribute("pageStart", Math.max(1, currentPage - 2));
            request.setAttribute("pageEnd", Math.min(totalPages, currentPage + 2));
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

            request.setAttribute("list", serviceDAO.getActiveServices());

            Booking targetBooking = resolveTargetBooking(request, currentUser, bookingDAO);
            request.setAttribute("currentBooking", targetBooking);

            if (targetBooking != null) {
                request.setAttribute("myServices", serviceDAO.getServicesByBookingId(targetBooking.getBookingID()));
            }

            boolean isReceptionistOrAdmin = "Admin".equalsIgnoreCase(currentUser.getRole())
                    || ("Staff".equalsIgnoreCase(currentUser.getRole()) && new dao.EmployeeDAO().isReceptionistByUserId(currentUser.getUserID()));
            request.setAttribute("canCancelService", isReceptionistOrAdmin);

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
        if ("toggleStatus".equalsIgnoreCase(action)) {
            try {
                String serviceID = request.getParameter("serviceID");
                new ServiceDAO().toggleServiceStatus(serviceID);
                session.setAttribute("successMessage", "Cập nhật trạng thái dịch vụ thành công!");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage());
            }
            response.sendRedirect(request.getContextPath() + "/service?action=manage");
            return;
        }
        if ("cancelOrder".equalsIgnoreCase(action)) {
            boolean isReceptionistOrAdmin = "Admin".equalsIgnoreCase(currentUser.getRole())
                    || ("Staff".equalsIgnoreCase(currentUser.getRole()) && new dao.EmployeeDAO().isReceptionistByUserId(currentUser.getUserID()));
            if (!isReceptionistOrAdmin) {
                response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Receptionist or Admin can cancel services.");
                return;
            }
            String bookingID = request.getParameter("bookingID");
            String serviceID = request.getParameter("serviceID");
            String from = request.getParameter("from");
            try {
                new ServiceDAO().cancelBookingService(bookingID, serviceID, currentUser.getUserID());
                session.setAttribute("successMessage", "Service request cancelled successfully.");
            } catch (Exception e) {
                session.setAttribute("errorMessage", e.getMessage());
            }
            if ("orders".equalsIgnoreCase(from)) {
                response.sendRedirect(request.getContextPath() + "/service?action=orders");
                return;
            }
            if (bookingID != null && !bookingID.isBlank()) {
                response.sendRedirect(request.getContextPath() + "/service?bookingID=" + bookingID.trim());
            } else {
                response.sendRedirect(request.getContextPath() + "/service");
            }
            return;
        }
        boolean accepting = "acceptOrder".equalsIgnoreCase(action);
        if (accepting || "completeOrder".equalsIgnoreCase(action)) {
            handleServiceOrder(request, response, currentUser, accepting);
            return;
        }

        String selectedServiceID = request.getParameter("selectedService");
        Booking targetBooking = null;
        String quantityValue = request.getParameter("quantity");
        String inputBookingID = request.getParameter("bookingID");

        try {
            if (selectedServiceID == null || selectedServiceID.trim().isEmpty()) {
                throw new IllegalArgumentException("Vui lòng chọn dịch vụ.");
            }
            int quantity = parseQuantity(quantityValue);

            BookingDAO bookingDAO = new BookingDAO();

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
                        || !Objects.equals(targetBooking.getCustomerID().getUserID().getUserID(), currentUser.getUserID())) {
                    throw new IllegalStateException("Bạn không có quyền thêm dịch vụ vào đơn đặt phòng này.");
                }
            }

            String status = targetBooking.getBookingStatus();
            if ("CHECKED_OUT".equals(status) || "CANCELLED".equals(status)) {
                throw new IllegalStateException("Đơn đặt phòng này đã hoàn tất hoặc bị hủy.");
            }
            if ("Customer".equalsIgnoreCase(currentUser.getRole())) {
                if ("CHECKED_IN".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("Quý khách đã nhận phòng. Vui lòng liên hệ nhân viên khách sạn để được phục vụ thêm dịch vụ.");
                }
                if (!"CONFIRMED".equalsIgnoreCase(status) && !"ASSIGNED".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("Đơn đặt phòng không ở trạng thái sẵn sàng để đặt dịch vụ.");
                }
            } else if ("Staff".equalsIgnoreCase(currentUser.getRole()) || "Admin".equalsIgnoreCase(currentUser.getRole())) {
                if (!"CHECKED_IN".equalsIgnoreCase(status)) {
                    throw new IllegalStateException("Nhân viên chỉ có thể thêm dịch vụ cho khách đang lưu trú tại khách sạn (In-house).");
                }
            }

            ServiceDAO serviceDAO = new ServiceDAO();
            String serviceID = selectedServiceID.trim();
            if (serviceID.length() > 4) {
                throw new IllegalArgumentException("Mã dịch vụ không hợp lệ.");
            }
            serviceDAO.insertBookingService(targetBooking.getBookingID(), serviceID, quantity, currentUser.getUserID());

            session.setAttribute("successMessage", "Thêm dịch vụ thành công!");
        } catch (IllegalArgumentException | IllegalStateException exception) {
            session.setAttribute("errorMessage", exception.getMessage() != null ? exception.getMessage() : "Không thể thêm dịch vụ.");
        }

        if (("Staff".equalsIgnoreCase(currentUser.getRole()) || "Admin".equalsIgnoreCase(currentUser.getRole())) && targetBooking != null) {
            response.sendRedirect(request.getContextPath() + "/service?bookingID=" + targetBooking.getBookingID().trim());
        } else {
            response.sendRedirect(request.getContextPath() + "/service");
        }
    }

    private void handleServiceOrder(HttpServletRequest request, HttpServletResponse response, Users user,
            boolean accepting) throws IOException {
        if (!canFulfillOrders(user) || !CsrfTokens.isValid(request)) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        String bookingID = request.getParameter("bookingID");
        String serviceID = request.getParameter("serviceID");
        if (bookingID == null || bookingID.isBlank() || bookingID.trim().length() > 6
                || serviceID == null || serviceID.isBlank() || serviceID.trim().length() > 4) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        try {
            boolean changed = new ServiceDAO().updateBookingServiceStatus(bookingID.trim(), serviceID.trim(),
                    user.getUserID(), accepting ? "IN_PROGRESS" : "COMPLETED");
            if (changed) {
                Flash.success(request, accepting
                        ? "Bạn đã tiếp nhận đơn dịch vụ." : "Đã hoàn thành dịch vụ.");
            } else {
                Flash.error(request, accepting
                        ? "Đơn đã được tiếp nhận hoặc booking đã đóng."
                        : "Chỉ nhân viên đã nhận đơn đang thực hiện mới có thể hoàn thành.");
            }
        } catch (IllegalStateException exception) {
            log("Unable to update service order.", exception);
            Flash.error(request, exception.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/service?action=orders");
    }

    private boolean canFulfillOrders(Users user) {
        if (!"Staff".equalsIgnoreCase(user.getRole()) || user.getUserID() == null) {
            return false;
        }
        Employee employee = new EmployeeDAO().getByUserId(user.getUserID());
        return employee != null && "Service staff".equalsIgnoreCase(employee.getPosition());
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
                            && Objects.equals(booking.getCustomerID().getUserID().getUserID(), currentUser.getUserID())) {
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
