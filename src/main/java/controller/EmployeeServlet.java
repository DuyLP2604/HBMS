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
import util.flash.Flash;

@WebServlet(name = "EmployeeServlet", urlPatterns = {"/employee"})
public class EmployeeServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        EmployeeDAO daoEmp = new EmployeeDAO();
        HotelDAO daoHotel = new HotelDAO();

        HttpSession session = request.getSession();

        String role =
                (String) session.getAttribute("role");

        if (role == null
                || (!role.equalsIgnoreCase("Admin")
                && !role.equalsIgnoreCase("Staff"))) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "You do not have permission to access this page!"
            );

            return;
        }

        String action =
                request.getParameter("action");


        if (action.equalsIgnoreCase("list")) {

            List<Employee> list =
                    daoEmp.getAllEmployees();

            request.setAttribute(
                    "employees",
                    list
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/employees.jsp"
                    )
                    .forward(request, response);


        } else if (action.equalsIgnoreCase("add")) {

            List<Hotel> listHotel =
                    daoHotel.getAllHotels();

            request.setAttribute(
                    "listHotel",
                    listHotel
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/add-employee.jsp"
                    )
                    .forward(request, response);


        } else if (action.equalsIgnoreCase("update")) {

            String id =
                    request.getParameter("id");

            Employee e =
                    daoEmp.getEmployeeById(id);

            request.setAttribute(
                    "employee",
                    e
            );

            List<Hotel> listHotel =
                    daoHotel.getAllHotels();

            request.setAttribute(
                    "listHotel",
                    listHotel
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/update-employee-info.jsp"
                    )
                    .forward(request, response);


        } else if (action.equalsIgnoreCase("viewDetail")) {

            String id =
                    request.getParameter("id");

            Employee e =
                    daoEmp.getEmployeeById(id);

            request.setAttribute(
                    "employee",
                    e
            );

            request
                    .getRequestDispatcher(
                            "/WEB-INF/views/employee-detail.jsp"
                    )
                    .forward(request, response);
        }
    }


    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String action =
                request.getParameter("action");

        EmployeeDAO daoEmp =
                new EmployeeDAO();


        String id =
                request.getParameter("id");

        String name =
                request.getParameter("name");

        String position =
                request.getParameter("position");

        double salary =
                Double.parseDouble(
                        request.getParameter("salary")
                );

        String shift =
                request.getParameter("shift");

        String address =
                request.getParameter("address");

        String phone =
                request.getParameter("phone");

        String hotelId =
                request.getParameter("hotelId");


        Hotel hotel =
                new Hotel();

        hotel.setId(hotelId);


        if ("update".equals(action)) {

            // Keep the existing login account when updating employee information
            Employee e =
                    new Employee();

            e.setId(id);
            e.setFullname(name);
            e.setPosition(position);
            e.setSalary(salary);
            e.setShift(shift);
            e.setAddress(address);
            e.setPhone(phone);
            e.setHotel(hotel);

            daoEmp.updateEmployee(e);


            Flash.success(
                    request,
                    "Employee information updated successfully."
            );


        } else {

            // Create a login account before creating a new employee
            String username =
                    request.getParameter("username");

            String password =
                    request.getParameter("password");


            UserDAO daoUser =
                    new UserDAO();

            int userId =
                    daoUser.insertUser(
                            username,
                            password,
                            "Staff"
                    );


            User user =
                    new User();

            user.setId(userId);


            Employee e =
                    new Employee(
                            id,
                            name,
                            position,
                            salary,
                            shift,
                            address,
                            phone,
                            hotel,
                            user
                    );

            daoEmp.insertEmployee(e);


            Flash.success(
                    request,
                    "Employee added successfully."
            );
        }


        response.sendRedirect(
                request.getContextPath()
                + "/employee?action=list"
        );
    }


    @Override
    public String getServletInfo() {
        return "Handles employee management.";
    }
}

