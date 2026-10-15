package controller;

import dao.RoomTypeDAO;
import dto.RoomTypeAvailability;
import entity.RoomType;
import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.List;
import util.flash.Flash;

@WebServlet(name = "RoomTypeServlet", urlPatterns = {"/room-types"})
@MultipartConfig(fileSizeThreshold = 1024 * 1024,
        maxFileSize = 1024 * 1024 * 5,
        maxRequestSize = 1024 * 1024 * 5 * 5)
public class RoomTypeServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String action = request.getParameter("action");
        action = action == null ? "search" : action;
        HttpSession session = request.getSession(false);
        RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

        switch (action.toLowerCase()) {
            case "detail":
                showRoomDetail(request, response);
                break;

            case "list": // Trang quản lý danh sách loại phòng dành cho Admin
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can access this page.");
                    response.sendRedirect(request.getContextPath() + "/room-types");
                    return;
                }
                request.setAttribute("roomTypeList", roomTypeDAO.getAll());
                request.getRequestDispatcher("/WEB-INF/views/room-type.jsp").forward(request, response);
                break;

            case "create":
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can access this page.");
                    response.sendRedirect(request.getContextPath() + "/room-types");
                    return;
                }
                request.getRequestDispatcher("/WEB-INF/views/add-room-type.jsp").forward(request, response);
                break;

            case "update":
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can access this page.");
                    response.sendRedirect(request.getContextPath() + "/room-types");
                    return;
                }
                String updateId = request.getParameter("id");
                RoomType roomTypeToUpdate = roomTypeDAO.getById(updateId);
                if (roomTypeToUpdate != null) {
                    request.setAttribute("roomType", roomTypeToUpdate);
                    request.getRequestDispatcher("/WEB-INF/views/update-room-type.jsp").forward(request, response);
                } else {
                    Flash.error(request, "Room type not found!");
                    response.sendRedirect(request.getContextPath() + "/room-types?action=manage");
                }
                break;

            case "delete":
                if (!isAdmin(session)) {
                    Flash.error(request, "Permission denied! Only Admin can remove room type.");
                    response.sendRedirect(request.getContextPath() + "/room-types");
                    return;
                }
                String deleteId = request.getParameter("id");
                RoomType roomTypeToDelete = roomTypeDAO.getById(deleteId);

                if (roomTypeToDelete != null) {
                    // 1. Thực hiện XÓA TRONG DATABASE TRƯỚC
                    boolean isOk = roomTypeDAO.deleteById(deleteId);
                    if (isOk) {
                        // 2. Nếu DB xóa thành công, tiến hành xóa ảnh vật lý
                        if (roomTypeToDelete.getRoomTypeImage() != null && !roomTypeToDelete.getRoomTypeImage().isEmpty()) {
                            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room-types";
                            File imageFile = new File(uploadPath + File.separator + roomTypeToDelete.getRoomTypeImage());
                            if (imageFile.exists()) {
                                imageFile.delete(); // Dọn dẹp file
                            }
                        }
                        Flash.success(request, "Room type deleted successfully!");
                    } else {
                        Flash.error(request, "Error! Delete failed (Room type might be in use by existing rooms)!");
                    }
                } else {
                    Flash.error(request, "Room type not found!");
                }
                response.sendRedirect(request.getContextPath() + "/room-types?action=manage");
                break;

            default:
                // Trọng tâm trang tìm kiếm phòng cho Khách hàng
                showSearchPage(request, response, roomTypeDAO);
                break;
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (!isAdmin(session)) {
            Flash.error(request, "Permission denied! Only Admin can modify data.");
            response.sendRedirect(request.getContextPath() + "/room-types");
            return;
        }

        String action = request.getParameter("action");
        RoomTypeDAO roomTypeDAO = new RoomTypeDAO();

        if ("create".equalsIgnoreCase(action)) {
            try {
                String typeName = request.getParameter("typeName");
                int capacity = Integer.parseInt(request.getParameter("capacity"));
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(request.getParameter("price")));
                String description = request.getParameter("description");

                // Tạo ID mới (Ví dụ RT05, RT06... tùy thuộc vào hàm trong DAO của bạn)
                String newRoomTypeId = roomTypeDAO.generateRoomTypeID();

                RoomType newRoomType = new RoomType();
                newRoomType.setRoomTypeID(newRoomTypeId);
                newRoomType.setTypeName(typeName);
                newRoomType.setCapacity(capacity);
                newRoomType.setPrice(price);
                newRoomType.setDescription(description);

                // Upload ảnh
                String uploadedImage = processImage(request, "roomTypeImage");
                if (uploadedImage != null) {
                    newRoomType.setRoomTypeImage(uploadedImage);
                }

                boolean isOk = roomTypeDAO.insert(newRoomType);
                if (isOk) {
                    Flash.success(request, "Room type added successfully!");
                } else {
                    Flash.error(request, "Error! Cannot add new room type.");
                    // Xóa ảnh rác nếu insert DB thất bại
                    if (uploadedImage != null) {
                        deletePhysicalFile(request, uploadedImage);
                    }
                }
            } catch (NumberFormatException e) {
                Flash.error(request, "Invalid input number format for capacity or price.");
            } catch (Exception e) {
                Flash.error(request, "Error processing create room type.");
            }
            response.sendRedirect(request.getContextPath() + "/room-types?action=manage");

        } else if ("update".equalsIgnoreCase(action)) {
            try {
                String roomTypeId = request.getParameter("id");
                String typeName = request.getParameter("typeName");
                int capacity = Integer.parseInt(request.getParameter("capacity"));
                BigDecimal price = BigDecimal.valueOf(Double.parseDouble(request.getParameter("price")));
                String description = request.getParameter("description");

                RoomType roomType = roomTypeDAO.getById(roomTypeId);
                if (roomType != null) {
                    roomType.setTypeName(typeName);
                    roomType.setCapacity(capacity);
                    roomType.setPrice(price);
                    roomType.setDescription(description);

                    String oldImageName = roomType.getRoomTypeImage();
                    String uploadedImage = processImage(request, "roomTypeImage");

                    if (uploadedImage != null) {
                        roomType.setRoomTypeImage(uploadedImage);
                    }

                    // Update DB
                    boolean isOkUpdate = roomTypeDAO.update(roomType);

                    if (isOkUpdate) {
                        Flash.success(request, "Room type updated successfully!");
                        // Xóa ảnh cũ nếu upload ảnh mới thành công
                        if (uploadedImage != null && oldImageName != null && !oldImageName.isEmpty()) {
                            deletePhysicalFile(request, oldImageName);
                        }
                    } else {
                        Flash.error(request, "Error! Cannot update room type information.");
                        // Rollback: Xóa ảnh mới upload nếu DB update thất bại
                        if (uploadedImage != null) {
                            deletePhysicalFile(request, uploadedImage);
                        }
                    }
                } else {
                    Flash.error(request, "Room type not found for update.");
                }
            } catch (NumberFormatException e) {
                Flash.error(request, "Invalid input number format.");
            } catch (Exception e) {
                Flash.error(request, "Error processing update room type.");
            }
            response.sendRedirect(request.getContextPath() + "/room-types?action=manage");
        }
    }

    // ================= HELPER METHODS =================
    private void showSearchPage(HttpServletRequest request, HttpServletResponse response, RoomTypeDAO roomTypeDAO) throws ServletException, IOException {
        moveFlashMessage(request);
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
        LocalDate tomorrow = today.plusDays(1);
        request.setAttribute("today", today.toString());

        String checkInValue = request.getParameter("checkInDate");
        String checkOutValue = request.getParameter("checkOutDate");

        if (checkInValue == null || checkInValue.trim().isEmpty()) {
            checkInValue = today.toString();
        }
        if (checkOutValue == null || checkOutValue.trim().isEmpty()) {
            checkOutValue = tomorrow.toString();
        }

        request.setAttribute("checkInDate", checkInValue);
        request.setAttribute("checkOutDate", checkOutValue);

        try {
            LocalDate checkInDate = LocalDate.parse(checkInValue.trim());
            LocalDate checkOutDate = LocalDate.parse(checkOutValue.trim());

            if (checkInDate.isBefore(today)) {
                throw new IllegalArgumentException("Check-in date cannot be in the past.");
            }
            if (!checkOutDate.isAfter(checkInDate)) {
                throw new IllegalArgumentException("Check-out date must be after check-in date.");
            }

            List<RoomTypeAvailability> availabilityList = roomTypeDAO.getAvailability(checkInDate, checkOutDate);
            request.setAttribute("availabilityList", availabilityList);
            request.setAttribute("searched", true);
        } catch (DateTimeParseException ex) {
            request.setAttribute("errorMessage", "Dates must use the yyyy-MM-dd format.");
        } catch (IllegalArgumentException ex) {
            request.setAttribute("errorMessage", ex.getMessage());
        } catch (Exception ex) {
            request.setAttribute("errorMessage", "Unable to load room availability.");
        }

        request.getRequestDispatcher("/WEB-INF/views/room-type-search.jsp").forward(request, response);
    }

    private void showRoomDetail(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String id = request.getParameter("id");
        if (id != null && !id.trim().isEmpty()) {
            RoomTypeDAO roomTypeDAO = new RoomTypeDAO();
            String roomTypeId = id.trim();

            RoomType roomType = roomTypeDAO.getById(roomTypeId);
            request.setAttribute("roomType", roomType);

            if (roomType != null) {
                LocalDate today = LocalDate.now(ZoneId.of("Asia/Ho_Chi_Minh"));
                LocalDate tomorrow = today.plusDays(1);
                long availableRooms = 0;

                try {
                    List<RoomTypeAvailability> availList = roomTypeDAO.getAvailability(today, tomorrow);
                    for (RoomTypeAvailability rta : availList) {
                        if (rta.getRoomTypeID().equalsIgnoreCase(roomTypeId)) {
                            availableRooms = rta.getAvailableRooms();
                            break;
                        }
                    }
                } catch (Exception e) {
                }

                request.setAttribute("availableRooms", availableRooms);
            }
        }
        request.getRequestDispatcher("/WEB-INF/views/room-type-detail.jsp").forward(request, response);
    }

    private boolean isAdmin(HttpSession session) {
        if (session == null) {
            return false;
        }
        Users u = (Users) session.getAttribute("user");
        return u != null && "admin".equalsIgnoreCase(u.getRole());
    }

    private void moveFlashMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return;
        }
        Object success = session.getAttribute("bookingSuccess");
        Object error = session.getAttribute("bookingError");
        if (success != null) {
            request.setAttribute("successMessage", success.toString());
            session.removeAttribute("bookingSuccess");
        }
        if (error != null) {
            request.setAttribute("errorMessage", error.toString());
            session.removeAttribute("bookingError");
        }
    }

    // ================= FILE UPLOAD LOGIC =================
    private String processImage(HttpServletRequest request, String inputName) {
        try {
            Part part = request.getPart(inputName);
            if (part == null || part.getSize() == 0 || part.getSubmittedFileName() == null || part.getSubmittedFileName().isEmpty()) {
                return null;
            }
            String extension = validateImage(part);
            if (extension == null) {
                return null;
            }
            String uniqueFileName = "room-type-" + System.currentTimeMillis() + "." + extension;
            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room-types";
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

    private void deletePhysicalFile(HttpServletRequest request, String fileName) {
        if (fileName != null && !fileName.isEmpty()) {
            String uploadPath = request.getServletContext().getRealPath("") + File.separator + "assets" + File.separator + "images" + File.separator + "room-types";
            File file = new File(uploadPath + File.separator + fileName);
            if (file.exists()) {
                file.delete();
            }
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

    @Override
    public String getServletInfo() {
        return "RoomType controller with CRUD";
    }
}
