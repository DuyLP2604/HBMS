/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.HotelDAO;
import entity.Hotel;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import util.HotelContent;

/**
 * Home page. The hotel name, addresses and images come from the database.
 *
 * @author default
 */
@WebServlet(name = "HomeServlet", urlPatterns = {"/home"})
public class HomeServlet extends HttpServlet {

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

        Hotel hotel = new HotelDAO().getHotel();

        String name = Hotel.FIXED_NAME;
        String address = null;
        String hero = null;
        String room = null;
        String pool = null;
        String restaurant = null;

        // If the hotel cannot be read, the page still opens with defaults.
        if (hotel != null) {
            if (hotel.getHotelName() != null && !hotel.getHotelName().trim().isEmpty()) {
                name = hotel.getHotelName().trim();
            }
            address = hotel.getAddress();
            hero = hotel.getHotelImage();
            room = hotel.getRoomImage();
            pool = hotel.getPoolImage();
            restaurant = hotel.getRestaurantImage();
        }

        request.setAttribute("hotelName", name);
        request.setAttribute("hotelAddresses", HotelContent.addressLines(address));
        request.setAttribute("heroImage",
                HotelContent.safeImage(hero, HotelContent.DEFAULT_HERO_IMAGE));
        request.setAttribute("roomImage",
                HotelContent.safeImage(room, HotelContent.DEFAULT_ROOM_IMAGE));
        request.setAttribute("poolImage",
                HotelContent.safeImage(pool, HotelContent.DEFAULT_POOL_IMAGE));
        request.setAttribute("restaurantImage",
                HotelContent.safeImage(restaurant, HotelContent.DEFAULT_RESTAURANT_IMAGE));

        request
            .getRequestDispatcher("/WEB-INF/views/index.jsp")
            .forward(request, response);
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
