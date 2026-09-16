/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controller;

import dao.HotelDAO;
import dao.EmployeeDAO;
import dao.UserDAO;
import entity.Employee;
import entity.Hotel;
import entity.Users;
import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.List;
import util.flash.Flash;

/**
 *
 * @author TAN LOI
 */
@WebServlet(name = "EmployeeServlet", urlPatterns = {"/employee"})
public class EmployeeServlet extends HttpServlet {

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request
     * @param response
     * @throws jakarta.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        EmployeeDAO daoEmp = new EmployeeDAO();
        HotelDAO daoHotel = new HotelDAO();
        HttpSession session = request.getSession();
        String role = (String) session.getAttribute("role");

        if (role == null || (!role.equalsIgnoreCase("Admin") && !role.equalsIgnoreCase("Staff"))) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "You do not have permission to access this page!");
            return;
        }
        String action = request.getParameter("action");
        if (action.equalsIgnoreCase("list")) {
            List<Employee> list = daoEmp.getAllEmployees();
            request.setAttribute("employees", list);
            request.getRequestDispatcher("/WEB-INF/views/employees.jsp").forward(request, response);

        } else if (action.equalsIgnoreCase("add")) {
            List<Hotel> listHotel = daoHotel.getAllHotels();
            request.setAttribute("listHotel", listHotel);
            request.getRequestDispatcher("/WEB-INF/views/add-employee.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("update")) {
            String id = request.getParameter("id");
            Employee e = daoEmp.getEmployeeById(id);
            request.setAttribute("employee", e);
            List<Hotel> listHotel = daoHotel.getAllHotels();
            request.setAttribute("listHotel", listHotel);
            request.getRequestDispatcher("/WEB-INF/views/update-employee-info.jsp").forward(request, response);
        } else if (action.equalsIgnoreCase("viewDetail")) {
            String id = request.getParameter("id");
            Employee e = daoEmp.getEmployeeById(id);
            request.setAttribute("employee", e);
            request.getRequestDispatcher("/WEB-INF/views/employee-detail.jsp").forward(request, response);
        }
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request
     * @param response
     * @throws jakarta.servlet.ServletException
     * @throws java.io.IOException
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        String action = request.getParameter("action");
        EmployeeDAO daoEmp = new EmployeeDAO();

        String id = request.getParameter("id");
        String name = request.getParameter("name");
        String position = request.getParameter("position");
        BigDecimal salary = BigDecimal.valueOf(Double.parseDouble(request.getParameter("salary")));
        String shift = request.getParameter("shift");
        String address = request.getParameter("address");
        String phone = request.getParameter("phone");
        String hotelId = request.getParameter("hotelId");

        Hotel hotel = new Hotel();
        hotel.setHotelID(hotelId);

        if ("update".equals(action)) {
            // Không đổi tài khoản đăng nhập khi cập nhật -> chỉ cần Hotel để lấy HotelID
            Employee e = new Employee();
            e.setEmployeeID(id);
            e.setFullName(name);
            e.setPosition(position);
            e.setSalary(salary);
            e.setShift(shift);
            e.setAddress(address);
            e.setPhone(phone);
            e.setHotelID(hotel);

            daoEmp.updateEmployee(e);
            Flash.success(
                    request,
                    "Employee information updated successfully."
            );
        } else {
            // Thêm mới: EMPLOYEE bắt buộc có UserID (JOIN USERS là INNER JOIN)
            // -> cần tạo tài khoản đăng nhập trước.
            // TODO: add-employee.jsp cần có input "username" và "password".
            String username = request.getParameter("username");
            String password = request.getParameter("password");

            UserDAO daoUser = new UserDAO();
            int userId = daoUser.insertUser(username, password, "Staff");

            Users user = new Users();
            user.setUserID(userId);

            Employee e = new Employee();
            // id, name, position, salary, shift, address, phone, hotel, user
            e.setEmployeeID(id);
            e.setFullName(name);
            e.setPosition(position);
            e.setSalary(salary);
            e.setShift(shift);
            e.setAddress(address);
            e.setPhone(phone);
            e.setHotelID(hotel);
            e.setUserID(user);
            daoEmp.insertEmployee(e);
        }
        Flash.success(
                request,
                "Employee added successfully."
        );
        response.sendRedirect("employee?action=list");
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }
}
