/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.RoomDAO;
import dao.RoomTypeDAO;
import entity.Room;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 *
 * @author Lenovo
 */
@WebServlet(name = "RoomServlet", urlPatterns = {"/room"})
public class RoomServlet extends HttpServlet {

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
        RoomDAO roomDAO = new RoomDAO();

        if ("viewDetail".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");
            Room room = roomDAO.getById(id);
            request.setAttribute("room", room);
            request.getRequestDispatcher("/WEB-INF/views/room-detail.jsp").forward(request, response);
        } else if ("update".equalsIgnoreCase(action)) {
            String roomId = request.getParameter("id");
            RoomTypeDAO rtDao = new RoomTypeDAO();
            Room room = roomDAO.getById(roomId);
            if (room != null) {
                request.setAttribute("room", room);
                request.setAttribute("roomTypes", rtDao.getAllRoomTypes());
                request.getRequestDispatcher("/WEB-INF/views/update-room.jsp").forward(request, response);
            }
        } else {
            request.setAttribute("roomList", roomDAO.getAll());
            request.getRequestDispatcher("/WEB-INF/views/room.jsp").forward(request, response);
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
        RoomDAO roomDAO = new RoomDAO();
        if ("update".equalsIgnoreCase(action)) {
            String roomId = request.getParameter("id");
            String roomNumber = request.getParameter("roomNumber");
            String roomTypeId = request.getParameter("roomTypeID");
            String status = request.getParameter("status");
            RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

            Room room = roomDAO.getById(roomId);
            if (room != null) {
                room.setRoomNumber(roomNumber);
                room.setStatus(status);
                entity.RoomType newRoomType = roomTypeDAO.getById(roomTypeId);
                if (newRoomType != null) {
                    room.setRoomTypeID(newRoomType);
                }

                roomDAO.update(room);
                util.flash.Flash.success(request, "Update success!");
            } else {
                util.flash.Flash.error(request, "Error! Cannot update room information.");
            }

            response.sendRedirect(request.getContextPath() + "/room?action=list");
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Room controller";
    }// </editor-fold>

}
