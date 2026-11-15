package controller;

import entity.Users;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import service.DataService;

/**
 * Lets admins export and restore database data with Excel workbooks.
 */
@WebServlet(name = "DataServlet", urlPatterns = {"/data"})
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2,
        maxFileSize = 1024 * 1024 * 50,
        maxRequestSize = 1024 * 1024 * 100
)
public class DataServlet extends HttpServlet {

    private static final int MAX_REPORT_DAYS = 60;

    /**
     * Download the database backup as an Excel workbook for an admin.
     *
     * @param request
     * @param response
     * @throws jakarta.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Admin can access this feature.");
            return;
        }

        DataService backupService = new DataService();
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition", "attachment; filename=\"HBMS_Full_Backup_" + System.currentTimeMillis() + ".xlsx\"");

        try {
            backupService.exportDB(response.getOutputStream());
            response.getOutputStream().flush();
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset(); // Xóa sạch header cũ đang bị lỗi
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Lỗi khi xuất dữ liệu Backup: " + e.getMessage());
            }
        }
    }

    /**
     * Read the uploaded workbook and restore the database for an admin.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!requireAdmin(request.getSession())) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Only Admin can access this feature.");
            return;
        }

        LocalDate begin;
        LocalDate end;
        try {
            begin = LocalDate.parse(request.getParameter("from"));
            end = LocalDate.parse(request.getParameter("to"));
        } catch (DateTimeParseException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Begin date and end date are required (yyyy-MM-dd).");
            return;
        }
        if (end.isBefore(begin)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "End date must not be before begin date.");
            return;
        }
        if (ChronoUnit.DAYS.between(begin, end) + 1 > MAX_REPORT_DAYS) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "The date range must not exceed " + MAX_REPORT_DAYS + " days.");
            return;
        }

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"HBMS_Revenue_Report_" + begin + "_to_" + end + ".xlsx\"");

        try {
            new DataService().exportReport(response.getOutputStream(), Date.valueOf(begin), Date.valueOf(end));
            response.getOutputStream().flush();
        } catch (Exception e) {
            if (!response.isCommitted()) {
                response.reset();
                response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to export report: " + e.getMessage());
            }
        }
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
