<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Checkout Management" pageCss="checkout.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container-fluid my-4 px-4">
        
        <c:if test="${not empty sessionScope.msg}">
            <div class="alert alert-info alert-dismissible fade show shadow-sm" role="alert">
                <strong>Notification:</strong> <c:out value="${sessionScope.msg}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
            <c:remove var="msg" scope="session"/>
        </c:if>

        <div class="card shadow-sm">
            <div class="card-header bg-white py-3">
                <h2 class="h4 mb-0 text-primary fw-bold">Checkout Manager</h2>
            </div>
            <div class="card-body">
                <div class="row mb-4">
                    <div class="col-md-5">
                        <form action="${pageContext.request.contextPath}/CheckoutEmployee" method="get" class="d-flex gap-2">
                            <div class="input-group">
                                <span class="input-group-text bg-white"><i class="bi bi-search"></i></span>
                                <input type="text" name="customerName" class="form-control" 
                                       placeholder="Tìm kiếm theo họ và tên customer..." value="${param.customerName}">
                                <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                            </div>
                        </form>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-hover align-middle border text-center">
                        <thead class="table-light">
                            <tr>
                                <th>Booking ID</th>
                                <th>Room</th>
                                <th>Customer name</th>
                                <th>Check-in</th>
                                <th>Checkout</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="room" items="${occupiedRooms}">
                                <tr>
                                    <td class="fw-bold text-secondary">${room.bookingID}</td>
                                    <td><span class="badge bg-info text-dark fs-6">${room.roomNumber}</span></td>
                                    <td>${room.customerName}</td>
                                    <td>${room.checkInDate}</td>
                                    <td>
                                        <a href="${pageContext.request.contextPath}/CheckoutEmployee?action=detail&bookingID=${room.bookingID}" 
                                           class="btn btn-outline-success btn-sm fw-bold px-3">
                                            Now <i class="bi bi-arrow-right-short"></i>
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            
                            <c:if test="${empty occupiedRooms}">
                                <tr>
                                    <td colspan="5" class="text-center text-muted py-4">
                                        No data for rooms pending checkout.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </div>
</layout:layout>