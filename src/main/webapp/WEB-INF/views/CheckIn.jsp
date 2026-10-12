<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Checked In" useBootstrap="true">
    <div class="container py-5">
        <h1 class="h3 mb-4 text-success">
            <i class="fa-solid fa-bell-concierge"></i> List of In-house Guests
        </h1>
        
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm">
                <c:out value="${sessionScope.successMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="successMessage" scope="session"/>
        </c:if>

        <div class="card shadow-sm border-0">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0 text-center">
                        <thead class="table-light">
                            <tr>
                                <th>Booking ID</th>
                                <th>Customer Name</th>
                                <th>Check-in</th>
                                <th>Check-out</th>
                                <th>Status</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="booking" items="${bookings}">
                                <tr>
                                    <td class="fw-bold">${booking.bookingID}</td>
                                    <td>${booking.customerID.fullName}</td>
                                    <td><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><span class="badge bg-success">Staying</span></td>
                                </tr>
                            </c:forEach>
                            
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="5" class="text-center text-muted py-4">There are currently no guests staying.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        
        <div class="mt-4">
            <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-secondary">
                <i class="fa-solid fa-arrow-left"></i> Back to dashboard
            </a>
        </div>
    </div>
</layout:layout>
