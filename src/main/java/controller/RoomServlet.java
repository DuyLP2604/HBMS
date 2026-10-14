/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.RoomDAO;
import dao.RoomTypeDAO;
import entity.Room;
import entity.RoomType;
import entity.Users;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
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
        HttpSession session = request.getSession(false);
        Users u = null;
        if (session != null) {
            u = (Users) session.getAttribute("user");
        }
        action = action == null ? "list" : action;

        switch (action.toLowerCase()) {
            case "viewdetail":
                viewDetail(request, response, request.getParameter("id"));
                break;
            case "create":
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can access this page.");
                    response.sendRedirect(request.getContextPath() + "/room");
                    return;
                }
                request.setAttribute("roomType", rtDao.getAllRoomTypes());
                request.getRequestDispatcher("/WEB-INF/views/add-room.jsp").forward(request, response);
                break;
            case "update":
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can access this page.");
                    response.sendRedirect(request.getContextPath() + "/room");
                    return;
                }
                String updateId = request.getParameter("id");
                Room room = roomDAO.getById(updateId);
                if (room != null) {
                    request.setAttribute("room", room);
                    request.setAttribute("roomTypes", rtDao.getAllRoomTypes());
                    request.getRequestDispatcher("/WEB-INF/views/update-room.jsp").forward(request, response);
                } else {
                    Flash.error(request, "Room not found!");
                    response.sendRedirect(request.getContextPath() + "/room");
                }
                break;
            case "delete":
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can remove room.");
                    response.sendRedirect(request.getContextPath() + "/room");
                    return;
                }
                String deleteId = request.getParameter("id");
                Room roomToDelete = roomDAO.getById(deleteId);
                boolean isOk = false;

                if (roomToDelete != null) {
                    // 1. Thực hiện XÓA TRONG DATABASE TRƯỚC
                    isOk = roomDAO.deleteById(deleteId);
                    if (isOk) {
                        // 2. Nếu DB xóa thành công, tiến hành xóa ảnh vật lý
                        if (roomToDelete.getRoomImage() != null && !roomToDelete.getRoomImage().isEmpty()) {
                            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room";
                            File imageFile = new File(uploadPath + File.separator + roomToDelete.getRoomImage());
                            if (imageFile.exists()) {
                                imageFile.delete(); // Dọn dẹp file
                            }
                        }
                        Flash.success(request, "Delete success!");
                    } else {
                        // DB không xóa được (thường do vướng khóa ngoại constraints)
                        Flash.error(request, "Error! Delete failed (Room might be in use)!");
                    }
                } else {
                    Flash.error(request, "Room not found!");
                }
                response.sendRedirect(request.getContextPath() + "/room");
                break;
            default:
                viewList(request, response);
        }

    }

    /**
     * Post method for admin only
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        Users u = null;
        if (session != null) {
            u = (Users) session.getAttribute("user");
        }
        if (!isAdmin(session)) {
            Flash.error(request, "Permission denied! Only Admin can modify data.");
            response.sendRedirect(request.getContextPath() + "/room");
            return;
        }
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

                // Lưu lại tên file cũ trước khi ghi đè
                String oldImageName = room.getRoomImage();

                // Upload file mới (hàm processImage đã tự động ghi file ra server lúc này)
                String uploadedImage = processImage(request, "roomImage");
                if (uploadedImage != null) {
                    room.setRoomImage(uploadedImage); // Cập nhật tên ảnh mới vào object
                }

                // 1. Thực hiện UPDATE TRONG DATABASE TRƯỚC
                boolean isOkUpdate = roomDAO.update(room);

                // 2. Xử lý logic File dựa trên kết quả Database
                if (isOkUpdate) {
                    Flash.success(request, "Room updated successfully!");

                    // DB thành công + Có ảnh mới -> Xóa ảnh CŨ
                    if (uploadedImage != null && oldImageName != null && !oldImageName.isEmpty()) {
                        String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room";
                        File oldImageFile = new File(uploadPath + File.separator + oldImageName);
                        if (oldImageFile.exists()) {
                            oldImageFile.delete(); // Xóa file cũ đi cho sạch
                        }
                    }
                } else {
                    Flash.error(request, "Error! Cannot save room information.");

                    // DB thất bại + Có ảnh mới vừa lưu -> Xóa ảnh MỚI đi để dọn rác (Rollback)
                    if (uploadedImage != null) {
                        String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room";
                        File newUploadedFile = new File(uploadPath + File.separator + uploadedImage);
                        if (newUploadedFile.exists()) {
                            newUploadedFile.delete();
                        }
                    }
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

    private boolean isAdmin(HttpSession session) {
        if (session == null) {
            return false;
        }
        Users u = (Users) session.getAttribute("user");
        return u != null && "admin".equalsIgnoreCase(u.getRole());
    }

    private void viewList(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setAttribute("roomList", new RoomDAO().getAll());
        request.getRequestDispatcher("/WEB-INF/views/room.jsp").forward(request, response);
    }

    private void viewDetail(HttpServletRequest request, HttpServletResponse response, String id) throws ServletException, IOException {
        request.setAttribute("room", new RoomDAO().getById(id));
        request.getRequestDispatcher("/WEB-INF/views/room-detail.jsp").forward(request, response);
    }

    private String processImage(HttpServletRequest request, String inputName) {
        try {
            Part part = request.getPart(inputName);
            if (part == null || part.getSize() == 0 || part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
                return null;
            }
            String extension = validateImage(part);
            if (extension == null) {
                // Trả về null nếu file bị từ chối (không phải jpg, png, webp hợp lệ)
                return null;
            }
            String uniqueFileName = "room-" + System.currentTimeMillis() + "." + extension;
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

    private String validateImage(Part part) throws IOException {
        String extension = extensionOf(part.getSubmittedFileName());
        if (extension == null) {
            return null;
        }

        byte[] header = new byte[12];
        int read;
        try (InputStream in = part.getInputStream()) {
            read = in.readNBytes(header, 0, header.length);
        }

        return matchesImageSignature(header, read, extension) ? extension : null;
    }

    private String extensionOf(String fileName) {
        if (fileName == null) {
            return null;
        }
        int dot = fileName.lastIndexOf('.');
        if (dot < 0 || dot == fileName.length() - 1) {
            return null;
        }
        String extension = fileName.substring(dot + 1).toLowerCase(java.util.Locale.ROOT);
        switch (extension) {
            case "jpg":
            case "jpeg":
                return "jpg";
            case "png":
                return "png";
            case "webp":
                return "webp";
            default:
                return null;
        }
    }

    private boolean matchesImageSignature(byte[] h, int length, String extension) {
        if (length < 12) {
            return false;
        }
        switch (extension) {
            case "jpg":
                return (h[0] & 0xFF) == 0xFF
                        && (h[1] & 0xFF) == 0xD8
                        && (h[2] & 0xFF) == 0xFF;
            case "png":
                return (h[0] & 0xFF) == 0x89 && h[1] == 'P'
                        && h[2] == 'N' && h[3] == 'G';
            case "webp":
                return h[0] == 'R' && h[1] == 'I' && h[2] == 'F' && h[3] == 'F'
                        && h[8] == 'W' && h[9] == 'E' && h[10] == 'B' && h[11] == 'P';
            default:
                return false;
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
