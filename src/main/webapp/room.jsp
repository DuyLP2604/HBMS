<%-- 
    Document   : manage-rooms
    Created on : Jul 14, 2026, 12:35:05 AM
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
        <div class="container my-5">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                    <h2 class="h4 mb-0">Room List</h2>
                    <a href="booking?action=add" class="btn btn-success btn-sm">+ Add new room</a>
                </div>
                <div class="card-body">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <form action="customer" method="get" class="d-flex gap-2">
                                <input type="hidden" name="action" value="list">
                                <div class="input-group">
                                    <span class="input-group-text bg-white">MS</span>
                                    <input type="text" name="searchId" class="form-control" value="${param.searchId}" placeholder="Nhập ID cần tìm...">
                                    <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                                    <c:if test="${not empty param.searchId}">
                                        <a href="customer?action=list" class="btn btn-secondary">Hủy lọc</a>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle border">
                            <thead class="table-light">
                                <tr>
                                    <th>Room ID</th>
                                    <th>Room number</th>
                                    <th>Image</th>
                                    <th>Price (VND/night)</th>
                                    <th>Hotel Name</th>
                                    <th>Status</th>
                                    <th class="text-center">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${roomList}" var="c" >
                                    <c:if test="${empty param.searchId or c.id.trim().equalsIgnoreCase(param.searchId.trim())}">
                                        <tr>

                                            <td>${c.roomId}</td>
                                            <td>${c.roomNumber}</td>
                                            <td>
                                                <img width="100" src="${pageContext.request.contextPath}/assets/images/room/${c.roomImage}" alt="${c.roomId}"/>
                                            </td>
                                            <td>${c.price} VND</td>
                                            <td>${c.hotel.name}</td>
                                            <td>
                                                <span class="badge rounded-pill ${c.status == 'Available' ? 'bg-success' : (booking.priority == 'Occupied' ? 'bg-warning text-dark' : 'bg-danger')}">
                                                    ${c.status}
                                                </span>
                                            </td>
                                            <td class="text-center">
                                                <a href="room?action=viewDetail&id=${c.roomId}" class="btn btn-outline-info btn-sm me-1">Chi tiết</a>
                                                <a href="room?action=update&id=${c.roomId}" class="btn btn-outline-warning btn-sm">Sửa</a>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
