<%-- 
    Document   : manage-bookings
    Created on : Jul 14, 2026, 12:34:42 AM
    Author     : Lenovo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <title>Booking Page</title>
        
    </head>
    <body class="bg-light">
        <jsp:include page="components/navbar.jsp" />
        <c:if test="${sessionScope.user.role eq 'Staff'}">
            <div class="container my-5">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                        <h2 class="h4 mb-0">Reservation list</h2>
                        <a href="booking?action=add" class="btn btn-success btn-sm">+ Add new reservation</a>
                    </div>
                <div class="card-body">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <form action="booking" method="get" class="d-flex gap-2">
                                <input type="hidden" name="action" value="list">
                                <div class="input-group">
                                    <span class="input-group-text bg-white">ID</span>
                                    <input type="text" name="searchId" class="form-control" value="${param.searchId}" placeholder="Enter ID to search...">
                                    <button type="submit" class="btn btn-primary">Search</button>
                                    <c:if test="${not empty param.searchId}">
                                        <a href="booking?action=list" class="btn btn-secondary">Hủy lọc</a>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle text-center border">
                            <thead class="table-light">
                                <tr>
                                    <th>Booking ID</th>
                                    <th>Customer</th>
                                    <th>Room Number</th>
                                    <th>Time</th>
                                    <th>Status</th>
                                    <th>Price (VND/night)</th>
                                    <th class="text-center">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="b" items="${bookingList}">
                                    <c:if test="${empty param.searchId or c.id.trim().equalsIgnoreCase(param.searchId.trim())}">
                                        <tr>
                                            
                                            <td>${b.bookingId}</td>
                                            <td>
                                                ${b.customer.fullname}
                                                <br> 
                                                <br>
                                                Phone: ${b.customer.phone}
                                            </td>
                                            <td>${b.room.roomNumber}</td>
                                            <td>${b.checkInDate} - ${b.checkOutDate}</td>
                                            <td>
                                                <span class="badge rounded-pill ${b.bookingStatus == 'Completed' ? 'bg-success' : (b.bookingStatus == 'Refused' ? 'bg-danger' : 'bg-secondary')}">
                                                    ${b.bookingStatus}
                                                </span>
                                            </td>
                                            <td>${b.room.price} VND</td>
                                            <td class="text-center">
                                                <a href="booking?action=detail&id=${b.bookingId}" class="btn btn-outline-info btn-sm me-1">View</a>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </c:if>
        <c:if test="${sessionScope.user.role eq 'Customer'}">
            <div class="container my-5">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                        <h2 class="h4 mb-0">Reservation list</h2>
                        <a href="booking?action=add" class="btn btn-success btn-sm">+ Add new reservation</a>
                    </div>
                <div class="card-body">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <form action="booking" method="get" class="d-flex gap-2">
                                <input type="hidden" name="action" value="list">
                                <div class="input-group">
                                    <span class="input-group-text bg-white">ID</span>
                                    <input type="text" name="searchId" class="form-control" value="${param.searchId}" placeholder="Enter ID to search...">
                                    <button type="submit" class="btn btn-primary">Search</button>
                                    <c:if test="${not empty param.searchId}">
                                        <a href="booking?action=list" class="btn btn-secondary">Hủy lọc</a>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle text-center border">
                            <thead class="table-light">
                                <tr>
                                    <th>Booking ID</th>
                                    <th>Room Number</th>
                                    <th>Time</th>
                                    <th>Status</th>
                                    <th>Price (VND/night)</th>
                                    <th class="text-center">Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="b" items="${bookingList}">
                                    <c:if test="${empty param.searchId or c.id.trim().equalsIgnoreCase(param.searchId.trim())}">
                                        <tr>
                                            
                                            <td>${b.bookingId}</td>
                                            <td>${b.room.roomNumber}</td>
                                            <td>${b.checkInDate} - ${b.checkOutDate}</td>
                                            <td>
                                                <span class="badge rounded-pill ${b.bookingStatus == 'Completed' ? 'bg-success' : (b.bookingStatus == 'Refused' ? 'bg-danger' : 'bg-secondary')}">
                                                    ${b.bookingStatus}
                                                </span>
                                            </td>
                                            <td>${b.room.price} VND</td>
                                            <td class="text-center">
                                                <a href="booking?action=detail&id=${b.bookingId}" class="btn btn-outline-info btn-sm me-1">View</a>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </c:if>
    </body>
</html>
