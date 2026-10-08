/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import entity.Booking;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 *
 * @author Admin
 */
@WebServlet(name = "CheckInServlet", urlPatterns = {"/checkin"})
public class CheckInServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        try {
            List<Booking> checkedInBookings = bookingDAO.getByStatus("CHECKED_IN");
            request.setAttribute("bookings", checkedInBookings);
            request.getRequestDispatcher("/WEB-INF/views/CheckIn.jsp").forward(request, response);
            
        } catch (Exception e) {
            e.printStackTrace();
            response.sendRedirect(request.getContextPath() + "/error.jsp");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) 
            throws ServletException, IOException {
        
        HttpSession session = request.getSession(false);
        String action = request.getParameter("action");
        String bookingID = request.getParameter("bookingID");

        try {
            if ("checkIn".equals(action)) {
                bookingDAO.updateStatus(bookingID, "CHECKED_IN");
                session.setAttribute("successMessage", "Đã Check-in thành công cho Booking: " + bookingID);
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Lỗi khi Check-in: " + e.getMessage());
        }
        response.sendRedirect(request.getContextPath() + "/checkin");
    }
}