/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import dao.CustomerDAO;
import dao.HotelDAO;
import dao.RoomDAO;
import entity.Booking;
import entity.Customer;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;

/**
 *
 * @author Lenovo
 */
@WebServlet(name = "BookingServlet", urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        BookingDAO bDao = new BookingDAO();
        HotelDAO hDao = new HotelDAO();
        CustomerDAO cDao = new CustomerDAO();
        if (role.equalsIgnoreCase("Staff")) {

            if (action.equalsIgnoreCase("detail")) {
                String id = request.getParameter("id");
                request.setAttribute("booking", bDao.getById(id));
                request.getRequestDispatcher("/WEB-INF/views/bookingDetail.jsp").forward(request, response);
            } else if (action.equalsIgnoreCase("add")) {
                RoomDAO rDao = new RoomDAO();
                request.setAttribute("hotelList", hDao.getAllHotels());
                request.setAttribute("roomList", rDao.getAll());
                request.getRequestDispatcher("/WEB-INF/views/addBooking.jsp").forward(request, response);
            } else if (action.equalsIgnoreCase("list")) {
                request.setAttribute("bookingList", bDao.getAll());
                request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
            } else {
                request.setAttribute("bookingList", bDao.getAll());
                request.getRequestDispatcher("booking.jsp").forward(request, response);
            }
        } else if (role.equalsIgnoreCase("Customer")) {
            int id = (int) session.getAttribute("userId");
            Customer c = cDao.getCustomerByUserId(id);
            if (action.equalsIgnoreCase("detail")) {
                String userId = request.getParameter("id");
                request.setAttribute("booking", bDao.getById(userId));
                request.getRequestDispatcher("/WEB-INF/views/bookingDetail.jsp").forward(request, response);
            }
            List<Booking> bList = bDao.getByUserId(c.getCustomerID().toUpperCase());
            request.setAttribute("bookingList", bList);
            request.getRequestDispatcher("/WEB-INF/views/booking.jsp").forward(request, response);
        }

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String action = request.getParameter("action");
        if ("add".equalsIgnoreCase(action)) {
            String cusName = request.getParameter("customer");
            String phone = request.getParameter("phone");
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
