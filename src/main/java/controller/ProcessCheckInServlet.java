/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import entity.Booking;
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

/**
 *
 * @author Admin
 */
@WebServlet(name = "ProcessCheckInServlet", urlPatterns = {"/process-checkin"})
public class ProcessCheckInServlet extends HttpServlet {

    private final BookingDAO bookingDAO = new BookingDAO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        String bookingID = request.getParameter("bookingID");
        if (bookingID == null || bookingID.isBlank()) {
            response.sendRedirect(request.getContextPath() + "/staff/stays");
            return;
        }

        try {
            Booking booking = bookingDAO.getByIdWithDetails(bookingID.trim());

            if (booking != null) {
                request.setAttribute("booking", booking);
                request.getRequestDispatcher("/WEB-INF/views/checkin-form.jsp").forward(request, response);
            } else {
                session.setAttribute("errorMessage", "Booking information not found.");
                response.sendRedirect(request.getContextPath() + "/staff/stays");
            }
        } catch (Exception e) {
            e.printStackTrace();
            session.setAttribute("errorMessage", "Lỗi tải form Check-in: " + e.toString());
            response.sendRedirect(request.getContextPath() + "/staff/stays");
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("user") == null) {
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        Users user = (Users) session.getAttribute("user");
        String action = request.getParameter("action");

        if ("confirmCheckIn".equals(action)) {
            String bookingID = request.getParameter("bookingID");
            String[] cccdArray = request.getParameterValues("cccd");

            try {
                String joinedCCCDs = "";
                if (cccdArray != null && cccdArray.length > 0) {
                    List<String> validList = new ArrayList<>();
                    for (String cccd : cccdArray) {
                        if (cccd != null && !cccd.isBlank()) {
                            validList.add(cccd.trim());
                        }
                    }
                    joinedCCCDs = String.join(", ", validList);
                }

                bookingDAO.updateBookingCCCDs(bookingID, joinedCCCDs);
                bookingDAO.updateStatus(bookingID, "CHECKED_IN", user.getUserID());

                session.setAttribute("successMessage", "Check-in thành công cho Booking " + bookingID + "!");
            } catch (Exception e) {
                e.printStackTrace();
                session.setAttribute("errorMessage", "Lỗi trong quá trình Check-in: " + e.getMessage());
            }

            response.sendRedirect(request.getContextPath() + "/staff/stays");
        }
    }
}
