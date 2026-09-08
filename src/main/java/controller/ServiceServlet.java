/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.ServiceDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.List;
import model.Service;
import model.User;

/**
 *
 * @author ADMIN
 */
@WebServlet(name = "ServiceServlet", urlPatterns = {"/service"})
public class ServiceServlet extends HttpServlet {

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
        User u = (User) request.getSession().getAttribute("user");
        if (u == null) {
            response.sendRedirect("login");
            return;
        }
        ServiceDAO dao = new ServiceDAO();
        List<Service> list = dao.getAllServices();
        request.setAttribute("list", list);
        request.getRequestDispatcher("service.jsp").forward(request, response);
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
        User u = (User) request.getSession().getAttribute("user");
        
        ServiceDAO dao = new ServiceDAO();
        String selectedService = request.getParameter("selectedService");
        
        if (selectedService != null && !selectedService.isEmpty()) {
        try {
            String[] parts = selectedService.split("\\|");
            String serviceName = parts[0];
            double price = Double.parseDouble(parts[1]);

            String bookingID = dao.getBookingIDByUser(u.getId());

            if (bookingID != null) {
                String serviceID = dao.generateServiceID();
                String roomID = dao.getRoomID(bookingID);
                String hotelID = dao.getHotelID(roomID);

                Service newService = new Service(serviceID, serviceName, price, hotelID, roomID);
                dao.insertService(newService);

                dao.insertBookingService(bookingID, serviceID);
            }
        } catch (Exception e) {
        }
    }
        
        response.sendRedirect("service");
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
