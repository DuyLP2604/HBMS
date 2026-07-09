/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.EmployeeDAO;
import java.io.IOException;
import java.io.PrintWriter;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import model.Employee;

/**
 *
 * @author TAN LOI
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = {"/employee"})
public class EmployeeServlet extends HttpServlet {

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
            out.println("<title>Servlet EmployeeServlet</title>");
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet EmployeeServlet at " + request.getContextPath() + "</h1>");
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
        EmployeeDAO daoEmp = new EmployeeDAO();
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role"); 
        
        if (role == null || (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("Staff"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập!");
            return;
        }
        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("list")){
            List<Employee> list = daoEmp.getAllEmployees();
            request.setAttribute("employees", list);
            request.getRequestDispatcher("employees.jsp").forward(request, response);
            
        } else if (action.equalsIgnoreCase("add")){
            request.getRequestDispatcher("add-employee.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("update")){
            String id = request.getParameter("id");
            Employee e = daoEmp.getEmployeeById(id);
            request.setAttribute("employee", e);
            request.getRequestDispatcher("update-employee-info.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("viewDetail")) {
            String id = request.getParameter("id");
            Employee e = daoEmp.getEmployeeById(id);
            request.setAttribute("employee", e);
            request.getRequestDispatcher("employee-detail.jsp").forward(request, response);
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
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        EmployeeDAO daoEmp = new EmployeeDAO();

        Employee e = new Employee(
            request.getParameter("id"),
            request.getParameter("name"),
            request.getParameter("position"),
            new BigDecimal(request.getParameter("salary")),
            request.getParameter("shift"),
            request.getParameter("address"),
            request.getParameter("phone"),
            request.getParameter("hotelId")
        );

        if ("update".equals(action)) {
            daoEmp.updateEmployee(e);
        } else {
            daoEmp.insertEmployee(e);
        }
        response.sendRedirect("employee?action=list");
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
