<%-- 
    Document   : addBooking
    Created on : Jul 20, 2026, 12:30:13 PM
    Author     : Lenovo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <title>JSP Page</title>
    </head>

    <body class="bg-light">
        <jsp:include page="components/navbar.jsp" />
        <div class="container my-5" style="max-width: 600px;">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white py-3">
                    <h2 class="h4 mb-0">Add New Reservation</h2>
                </div>
                <div class="card-body">
                    <c:if test="${param.success == '1'}">
                        <div class="alert alert-success">Gửi khiếu nại thành công! Chúng tôi sẽ xử lý sớm nhất.</div>
                    </c:if>

                    <form action="add" method="post">
                        <input type="hidden" name="action" value="add">

                        <div class="mb-3">
                            <label class="form-label">Customer<span class="text-danger">*</span>:</label>
                            <input type="text" name="customer" class="form-control" placeholder="Enter customer name" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Phone Number<span class="text-danger">*</span>:</label>
                            <input type="text" name="phone" class="form-control" placeholder="Enter customer phone number" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Branch<span class="text-danger">*</span>:</label>
                            <select class="form-select" name="hotelId">
                                <option value="" disabled selected>-- Choose branch --</option>
                                <c:forEach items="${hotelList}" var="h">
                                    <option value="${h.id}">${h.address}</option>
                                </c:forEach>
                            </select>
                        </div>
                        
                        <div class="mb-3">
                            <label class="form-label">Branch<span class="text-danger">*</span>:</label>
                            <select class="form-select" name="roomId">
                                <option value="" disabled selected>-- Choose room --</option>
                                <c:forEach items="${roomList}" var="c">
                                    <option value="${c.roomId}">${c.roomNumber}</option>
                                </c:forEach>
                            </select>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Check-out Date<span class="text-danger">*</span></label>
                            <input type="date" name="checkIn" class="form-control" required>
                        </div>

                        <div class="d-flex justify-content-end gap-2">
                            <button type="submit" class="btn btn-success">Add</button>
                            <a href="booking?action=list" class="btn btn-primary">Back</a>
                        </div>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>
