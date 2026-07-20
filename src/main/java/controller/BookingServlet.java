/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.BookingDAO;
import dao.CustomerDAO;
import dao.HotelDAO;
import dao.RoomDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Booking;
import model.Customer;
import model.Hotel;
import model.Room;

/**
 *
 * @author Lenovo
 */
@WebServlet(name = "BookingServlet", urlPatterns = {"/booking"})
public class BookingServlet extends HttpServlet {

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        try (PrintWriter out = response.getWriter()) {
            /* TODO output your page here. You may use following sample code. */
            out.println("<!DOCTYPE html>");
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet BookingServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet BookingServlet at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");
        }
    }

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
        HttpSession session = request.getSession(false);
        String role = (String) session.getAttribute("role");
        BookingDAO bDao = new BookingDAO();
        HotelDAO hDao = new HotelDAO();
        CustomerDAO cDao = new CustomerDAO();
        if (role.equalsIgnoreCase("Staff")) {
            
            if (request.getParameter("detail") != null) {
                String id = request.getParameter("id");
                Booking b = bDao.getById(id);
                request.setAttribute("booking", b);
                request.getRequestDispatcher("bookingDetail.jsp").forward(request, response);
            } else if (request.getParameter("add") != null) {
                RoomDAO rDao = new RoomDAO();
                List<Hotel> hList = hDao.getAllHotels();
                List<Room> rList = rDao.getAll();
                request.setAttribute("hotelList", hList);
                request.setAttribute("roomList", rList);
                request.getRequestDispatcher("addBooking.jsp").forward(request, response);
            } else {
                List<Booking> bookingList = bDao.getAll();
                request.setAttribute("bookingList", bookingList);

                request.getRequestDispatcher("booking.jsp").forward(request, response);
            }
        } else if (role.equalsIgnoreCase("Customer")) {
            int id = (int) session.getAttribute("userId");
            Customer c = cDao.getCustomerByUserId(id);
            if (request.getParameter("detail") != null) {
                String userId = request.getParameter("id");
                Booking b = bDao.getById(userId);
                request.setAttribute("booking", b);
                request.getRequestDispatcher("bookingDetail.jsp").forward(request, response);
            }
            List<Booking> bList = bDao.getByUserId(c.getId().toUpperCase());
            request.setAttribute("bookingList", bList);
            request.getRequestDispatcher("booking.jsp").forward(request, response);
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
        if (request.getParameter("add") != null) {
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
