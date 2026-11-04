package controller;

import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;
import java.io.IOException;
import service.ExportDataService;
import service.ImportDataService;
import util.flash.Flash;

@WebServlet(name = "DataServlet", urlPatterns = {"/data"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 50,
        maxRequestSize = 1024 * 1024 * 100
)
public class DataServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Admin can Backup/Restore Database.");
            return;
        }

        ExportDataService backupService = new ExportDataService();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"HBMS_Full_Backup_" + System.currentTimeMillis() + ".xlsx\"");

        try {
            backupService.exportDB(response.getOutputStream());
            response.getOutputStream().flush(); // Đẩy toàn bộ dữ liệu ra luồng
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset(); // Xóa sạch header cũ đang bị lỗi
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xuất dữ liệu Backup: " + e.getMessage());
            }
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Admin can Backup/Restore Database.");
            return;
        }

        String action = request.getParameter("action");
        if (action == null || action.trim().isEmpty()) {
            action = "importFullDB";
        }

        ImportDataService backupService = new ImportDataService();
        if ("importFullDB".equals(action)) {
            try {
                Part filePart = request.getPart("backupFile");
                if (filePart != null && filePart.getSize() > 0) {
                    try (java.io.InputStream input = filePart.getInputStream()) {
                        backupService.importDB(input);
                    }
                    Flash.success(request, "Phục hồi dữ liệu từ Excel thành công!");
                } else {
                    Flash.error(request, "Vui lòng chọn file Excel để import.");
                }
            } catch (Exception e) {
                // Truy tìm nguyên nhân gốc rễ (Root Cause) của lỗi SQL
                Throwable rootCause = e;
                while (rootCause.getCause() != null && rootCause.getCause() != rootCause) {
                    rootCause = rootCause.getCause();
                }

                // Print ra console server để bạn dễ check log cụ thể dòng nào lỗi
                e.printStackTrace();

                // Trả lỗi gốc ra giao diện cho Admin thấy
                Flash.error(request, "Lỗi nạp dữ liệu: " + rootCause.getMessage());
            }
        }

        response.sendRedirect(request.getContextPath() + "/dashboard");
    }

    private boolean requireAdmin(HttpSession session) throws IOException {
        if (session == null) {
            return false;
        }
        Users u = (Users) session.getAttribute("user");
        if (u == null || !"Admin".equalsIgnoreCase(u.getRole())) {
            return false;
        }
        return true;
    }
}
