/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.InvoiceDAO;
import entity.Users;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 *
 * @author Admin
 */
@WebServlet(
        name = "CheckoutEmployeeServlet", 
        urlPatterns = {"/CheckoutEmployee"}
)
public class CheckoutEmployeeServlet extends HttpServlet {
    private final InvoiceDAO invoiceDAO = new InvoiceDAO();
    
    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }
        Users user = (Users) session.getAttribute("user");
        if ("Customer".equalsIgnoreCase(user.getRole())) {
            response.sendRedirect(request.getContextPath() + "/home");
            return;
        }
        String action = request.getParameter("action");

        if ("detail".equals(action)) {
            showCheckoutDetail(request, response);
        } else {
            showCheckoutList(request, response);
        }
    }

    private void showCheckoutList(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        try {
            List<Map<String, String>> occupiedRooms = invoiceDAO.getOccupiedRooms();
            
            String customerName = request.getParameter("customerName");
            if (customerName != null && !customerName.trim().isEmpty()) {
                List<Map<String, String>> filteredRooms = new ArrayList<>();
                String keyword = customerName.toLowerCase().trim();

                for (Map<String, String> room : occupiedRooms) {
                    String nameInList = room.get("customerName");
                    if (nameInList != null && nameInList.toLowerCase().contains(keyword)) {
                        filteredRooms.add(room);
                    }
                }
                occupiedRooms = filteredRooms;
            }
            
            request.setAttribute("occupiedRooms", occupiedRooms);
            request.getRequestDispatcher("/WEB-INF/views/checkout-employee.jsp").forward(request, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }

    private void showCheckoutDetail(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String bookingID = request.getParameter("bookingID");
            
            if (bookingID == null || bookingID.trim().isEmpty()) {
                response.sendRedirect(request.getContextPath() + "/CheckoutEmployee");
                return;
            }

            // Lấy dữ liệu Phòng và Dịch vụ từ InvoiceDAO
            Map<String, String> roomDetail = invoiceDAO.getRoomDetailByBooking(bookingID);
            List<Map<String, String>> services = invoiceDAO.getServicesByBooking(bookingID);

            if (roomDetail == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Không tìm thấy thông tin Booking.");
                return;
            }

            // Tính toán tiền cọc và tiền cần thanh toán
            double baseTotal = Double.parseDouble(roomDetail.get("baseTotal"));
            double deposit = baseTotal * 0.3; // Cọc 30%
            double finalAmount = baseTotal - deposit;

            request.setAttribute("roomDetail", roomDetail);
            request.setAttribute("services", services);
            request.setAttribute("deposit", deposit);
            request.setAttribute("finalAmount", finalAmount);

            request.getRequestDispatcher("/WEB-INF/views/checkout-detail.jsp").forward(request, response);

        } catch (Exception ex) {
            ex.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);

        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Users user = (Users) session.getAttribute("user");
        String action = request.getParameter("action");

        if ("exportInvoice".equals(action)) {
            String bookingID = request.getParameter("bookingID");

            entity.Employee emp = user.getEmployee();
            if (emp == null) {
                session.setAttribute("msg", "Lỗi: Tài khoản của bạn chưa được liên kết với hồ sơ Lễ tân!");
                response.sendRedirect(request.getContextPath() + "/CheckoutEmployee");
                return;
            }
            
            String employeeID = emp.getEmployeeID(); 

            try {
                boolean success = invoiceDAO.processCheckout(bookingID, employeeID);

                if (success) {
                    session.setAttribute(
                            "msg", 
                            "Checkout thành công! Đã xuất hóa đơn cho Booking: " + bookingID
                    );
                }
            } catch (IllegalStateException | IllegalArgumentException ex) {
                session.setAttribute("msg", "Lỗi Checkout: " + ex.getMessage());
            } catch (Exception ex) {
                ex.printStackTrace();
                session.setAttribute("msg", "Hệ thống xảy ra lỗi khi Checkout.");
            }

            response.sendRedirect(request.getContextPath() + "/CheckoutEmployee");
        }
    }
}
