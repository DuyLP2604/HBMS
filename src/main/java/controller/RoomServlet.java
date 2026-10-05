/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.RoomDAO;
import dao.RoomTypeDAO;
import entity.Room;
import entity.RoomType;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import java.io.File;
import java.nio.file.Paths;
import util.flash.Flash;

/**
 *
 * @author Lenovo
 */
@WebServlet(name = "RoomServlet", urlPatterns = {"/room"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
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
        RoomTypeDAO rtDao = new RoomTypeDAO();
        if (action == null) {
            request.setAttribute("roomList", roomDAO.getAll());
            request.getRequestDispatcher("/WEB-INF/views/room.jsp").forward(request, response);
        }
        if ("viewDetail".equalsIgnoreCase(action)) {
            String id = request.getParameter("id");
            Room room = roomDAO.getById(id);
            request.setAttribute("room", room);
            request.getRequestDispatcher("/WEB-INF/views/room-detail.jsp").forward(request, response);
        } else if ("create".equalsIgnoreCase(action)) {
            request.setAttribute("roomType", rtDao.getAllRoomTypes());
            request.getRequestDispatcher("/WEB-INF/views/add-room.jsp").forward(request, response);
        } else if (action.equals("delete")) {
            //TODO: Show pop-up "Are you sure to delete room and redirect url: /room?action=delete&id=?"
            String roomId = request.getParameter("id");
            Room room = roomDAO.getById(roomId);
            boolean isOk = false;
            if (room != null) {
                isOk = roomDAO.deleteById(roomId);
            }
            if (isOk) {
                Flash.success(request, "Delete success!");
            } else {
                Flash.error(request, "Error! Delete failed!");
            }
            response.sendRedirect(request.getContextPath() + "/room?action=list");
        } else if ("update".equalsIgnoreCase(action)) {
            String roomId = request.getParameter("id");

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
                RoomType newRoomType = roomTypeDAO.getById(roomTypeId);
                if (newRoomType != null) {
                    room.setRoomTypeID(newRoomType);
                }
                String uploadedImage = processImage(request, "roomImage");
                if (uploadedImage != null) {
                    room.setRoomImage(uploadedImage);
                }
                boolean isOk = roomDAO.update(room);
                if (isOk) {
                    Flash.success(request, "Room updated successfully!");
                } else {
                    Flash.error(request, "Error! Cannot save room information.");
                }
            } else {
                Flash.error(request, "Error! Cannot update room information.");
            }

            response.sendRedirect(request.getContextPath() + "/room?action=list");
        } else if ("create".equalsIgnoreCase(action)) {
            String roomNumber = request.getParameter("roomNumber");
            String roomTypeId = request.getParameter("roomTypeID");
            String status = request.getParameter("status");

            RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
            RoomType newRoomType = roomTypeDAO.getById(roomTypeId);

            if (newRoomType != null) {
                String newRoomId = roomDAO.generateRoomID();

                Room newRoom = new Room(newRoomId, roomNumber, status);
                newRoom.setRoomTypeID(newRoomType);

                String uploadedImage = processImage(request, "roomImage");
                if (uploadedImage != null) {
                    newRoom.setRoomImage(uploadedImage);
                }

                boolean isOk = roomDAO.insert(newRoom);
                if (isOk) {
                    Flash.success(request, "Room added successfully!");
                } else {
                    Flash.error(request, "Error! Cannot add new room.");
                }
            } else {
                Flash.error(request, "Error! Invalid room type.");
            }

            response.sendRedirect(request.getContextPath() + "/room?action=list");
        }
    }

    private String processImage(HttpServletRequest request, String inputName) {
        try {
            Part part = request.getPart(inputName);
            if (part == null || part.getSize() == 0 || part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
                return null;
            }

            String fileName = Paths.get(part.getSubmittedFileName()).getFileName().toString();
            String uniqueFileName = System.currentTimeMillis() + "_" + fileName;
            if (uniqueFileName.length() > 100) {
                uniqueFileName = uniqueFileName.substring(uniqueFileName.length() - 100);
            }
            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room";
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }
            part.write(uploadPath + File.separator + uniqueFileName);
            return uniqueFileName;
        } catch (ServletException | IOException e) {
            return null;
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
