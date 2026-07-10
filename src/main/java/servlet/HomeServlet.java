/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package servlet;

import dao.HomeDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.LocalDate;
import java.util.List;
import model.CustomerSource;
import model.RevenueChart;

/**
 *
 * @author ADMIN
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
        HomeDAO homedao = new HomeDAO();

        String from = request.getParameter("from");
        String to = request.getParameter("to");

        LocalDate startDate;
        LocalDate endDate;

        if (from == null || from.isEmpty()
                || to == null || to.isEmpty()) {
            startDate = LocalDate.now();
            endDate = LocalDate.now();
        } else {
            startDate = LocalDate.parse(from);
            endDate = LocalDate.parse(to);
        }

        long revenue = homedao.getRevenueByDateRange(startDate, endDate);
        int occupied = homedao.getOccupiedRoomsCount(startDate, endDate);
        int totalRooms = homedao.getTotalRoomsCount();
        int totalCustomers = homedao.getTotalCustomers();
        List<RevenueChart> revenueChart = homedao.getRevenueLastTenDays();
        List<CustomerSource> customerSource = homedao.getCustomerSource();

        request.setAttribute("todayRevenue", revenue);
        request.setAttribute("occupiedRooms", occupied);
        request.setAttribute("totalRooms", totalRooms);
        request.setAttribute("totalCustomers", totalCustomers);
        request.setAttribute("revenueChart", revenueChart);
        request.setAttribute("customerSource", customerSource);

        request.getRequestDispatcher("home.jsp").forward(request, response);
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
