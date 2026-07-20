/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.InvoiceDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.Provider.Service;
import java.util.List;
import java.util.Map;

import model.User;

/**
 *
 * @author Admin
 */
@WebServlet(name = "InvoiceServlet", urlPatterns = {"/invoice"})
public class InvoiceServlet extends HttpServlet {

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
            out.println("<title>Servlet InvoiceServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet InvoiceServlet at " + request.getContextPath() + "</h1>");
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
        HttpSession session = request.getSession();
        User u = (User) session.getAttribute("user");
        if (u == null || "Customer".equals(u.getRole())) {
            response.sendRedirect("index.jsp");
            return;
        }
        InvoiceDAO dao = new InvoiceDAO();
        String bookingId = request.getParameter("bookingId");
        String action = request.getParameter("action");
        request.setAttribute("occupiedRooms", dao.getOccupiedRooms());
        request.setAttribute("paidInvoices", dao.getPaidInvoices());
        if ("print".equals(action)) {

            if (bookingId != null && !bookingId.isBlank()) {
                Map<String, String> room = dao.getRoomDetailByBooking(bookingId);
                List<Map<String, String>> services = dao.getServicesByBooking(
                        bookingId);

                double roomTotal = Double.parseDouble(room.get("roomTotal"));

                double serviceTotal = 0;

                for (Map<String, String> s : services) {

                    serviceTotal
                            += Double.parseDouble(
                                    s.get("unitPrice"));
                }

                double subTotal
                        = roomTotal + serviceTotal;

                double vat
                        = subTotal * 0.08;

                double grandTotal
                        = subTotal + vat;

                String employeeId
                        = dao.getEmployeeById(
                                u.getId());

                if (!dao.isBookingPaid(
                        bookingId)) {

                    dao.processCheckout(
                            bookingId,
                            grandTotal,
                            employeeId);
                }

                request.setAttribute(
                        "selectedRoom",
                        room);

                request.setAttribute(
                        "bookingServices",
                        services);

                request.setAttribute(
                        "serviceTotal",
                        serviceTotal);

                request.setAttribute(
                        "vat",
                        vat);

                request.setAttribute(
                        "grandTotal",
                        grandTotal);
            }

            request.getRequestDispatcher(
                    "invoice.jsp")
                    .forward(request,
                            response);

            return;
        }

        if (bookingId != null
                && !bookingId.isBlank()) {

            Map<String, String> room
                    = dao.getRoomDetailByBooking(
                            bookingId);

            List<Map<String, String>> services
                    = dao.getServicesByBooking(
                            bookingId);

            double roomTotal
                    = Double.parseDouble(room.get("roomTotal"));

            double serviceTotal = 0;

            for (Map<String, String> s : services) {

                serviceTotal
                        += Double.parseDouble(
                                s.get("unitPrice"));
            }

            double subTotal
                    = roomTotal + serviceTotal;

            double vat
                    = subTotal * 0.08;

            double grandTotal
                    = subTotal + vat;

            request.setAttribute(
                    "selectedRoom",
                    room);

            request.setAttribute(
                    "bookingServices",
                    services);

            request.setAttribute(
                    "serviceTotal",
                    serviceTotal);

            request.setAttribute(
                    "vat",
                    vat);

            request.setAttribute(
                    "grandTotal",
                    grandTotal);
        }

        request.getRequestDispatcher(
                "checkout.jsp")
                .forward(request,
                        response);
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
//        HttpSession session = request.getSession();
//        User u = (User) session.getAttribute("user");
//        if (u == null || "Customer".equals(u.getRole())) {
//            response.sendRedirect("index.jsp");
//            return;
//        }
//        String action = request.getParameter("action");
//
//        if ("processCheckout".equals(action)) {
//            String bookingId = request.getParameter("bookingId");
//            double grandTotal = Double.parseDouble(request.getParameter("grandTotal"));
//            InvoiceDAO dao = new InvoiceDAO();
//            String employeeId = dao.getEmployeeById(u.getId());
//            boolean isSuccess = dao.processCheckout(bookingId, grandTotal, employeeId);
//
//            if (isSuccess) {
//                response.sendRedirect("invoice?status=success");
//            } else {
//                response.sendRedirect("invoice?status=error");
//            }
//        }
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
