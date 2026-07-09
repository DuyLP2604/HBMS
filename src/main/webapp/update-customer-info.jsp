<%-- 
    Document   : updateinfo
    Created on : 9 thg 7, 2026, 19:17:10
    Author     : TAN LOI
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Update Info</title>
    </head>
    <body>
        <h2>Cập nhật thông tin khách hàng</h2>
        <form action="customers?action=update" method="post">
            Mã KH: <input type="text" name="id" value="${customer.id}" readonly><br><br>
            Họ tên: <input type="text" name="name" value="${customer.name}" required><br><br>
            SĐT: <input type="text" name="phone" value="${customer.phone}"><br><br>
            Email: <input type="email" name="email" value="${customer.email}"><br><br>
            Địa chỉ: <input type="text" name="address" value="${customer.address}"><br><br>
            CCCD: <input type="text" name="cccd" value="${customer.cccd}"><br><br>
            Passport: <input type="text" name="passport" value="${customer.passport}"><br><br>
            Quốc tịch: <input type="text" name="nation" value="${customer.nation}"><br><br>
            <button type="submit">Cập nhật</button>
            <a href="customers">Hủy</a>
        </form>
    </body>
</html>
