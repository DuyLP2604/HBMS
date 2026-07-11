/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.HotelDAO;
import dao.EmployeeDAO;
import dao.UserDAO;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.util.List;
import model.Employee;
import model.Hotel;
import model.User;

/**
 *
 * @author TAN LOI
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = {"/employee"})
public class EmployeeServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmployeeDAO daoEmp = new EmployeeDAO();
        HotelDAO daoHotel = new HotelDAO();
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        if (role == null || (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("Staff"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "Bạn không có quyền truy cập!");
            return;
        }
        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("list")) {
            List<Employee> list = daoEmp.getAllEmployees();
            request.setAttribute("employees", list);
            request.getRequestDispatcher("employees.jsp").forward(request, response);

        } else if (action.equalsIgnoreCase("add")) {
            List<Hotel> listHotel = daoHotel.getAllHotels();
            request.setAttribute("listHotel", listHotel);
            request.getRequestDispatcher("add-employee.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("update")) {
            String id = request.getParameter("id");
            Employee e = daoEmp.getEmployeeById(id);
            request.setAttribute("employee", e);
            List<Hotel> listHotel = daoHotel.getAllHotels();
            request.setAttribute("listHotel", listHotel);
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
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        EmployeeDAO daoEmp = new EmployeeDAO();

        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String position = request.getParameter("position");
        double salary = Double.parseDouble(request.getParameter("salary"));
        String shift = request.getParameter("shift");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        String hotelId = request.getParameter("hotelId");

        Hotel hotel = new Hotel();
        hotel.setId(hotelId);

        if ("update".equals(action)) {
            // Không đổi tài khoản đăng nhập khi cập nhật -> chỉ cần Hotel để lấy HotelID
            Employee e = new Employee();
            e.setId(id);
            e.setFullname(name);
            e.setPosition(position);
            e.setSalary(salary);
            e.setShift(shift);
            e.setAddress(address);
            e.setPhone(phone);
            e.setHotel(hotel);

            daoEmp.updateEmployee(e);
        } else {
            // Thêm mới: EMPLOYEE bắt buộc có UserID (JOIN USERS là INNER JOIN)
            // -> cần tạo tài khoản đăng nhập trước.
            // TODO: add-employee.jsp cần có input "username" và "password".
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            UserDAO daoUser = new UserDAO();
            int userId = daoUser.insertUser(username, password, "Staff");

            User user = new User();
            user.setId(userId);

            Employee e = new Employee(id, name, position, salary, shift, address, phone, hotel, user);
            daoEmp.insertEmployee(e);
        }
        response.sendRedirect("employee?action=list");
    }

    /**
     * Returns a short description of the servlet.
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}