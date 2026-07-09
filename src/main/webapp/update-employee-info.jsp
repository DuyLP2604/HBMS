<%-- 
    Document   : update-employee-info
    Created on : 9 thg 7, 2026, 20:30:01
    Author     : TAN LOI
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h2>Chỉnh sửa thông tin nhân viên (Dành cho Quản lý)</h2>
        <form action="employees?action=update" method="post">
            Mã NV: <input type="text" name="id" value="${employee.id}" readonly><br><br>
            Họ tên: <input type="text" name="name" value="${employee.name}" required><br><br>
            Chức vụ: <input type="text" name="position" value="${employee.position}"><br><br>
            Lương: <input type="number" name="salary" value="${employee.salary}"><br><br>
            Ca làm: <input type="text" name="shift" value="${employee.shift}"><br><br>
            Địa chỉ: <input type="text" name="address" value="${employee.address}"><br><br>
            SĐT: <input type="text" name="phone" value="${employee.phone}"><br><br>
            Mã KS: <input type="text" name="hotelId" value="${employee.hotelId}"><br><br>
            <button type="submit">Cập nhật</button>
            <a href="employees">Hủy</a>
        </form>
    </body>
</html>
