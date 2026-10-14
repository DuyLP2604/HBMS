<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Guest Stays" useBootstrap="true" bodyClass="bg-light">
    <div class="container py-5">
        
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0 text-gray-800"><i class="fa-solid fa-clipboard-list me-2"></i>Guest Stay Management</h1>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <i class="fa-solid fa-circle-check me-2"></i> <c:out value="${successMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
                <i class="fa-solid fa-triangle-exclamation me-2"></i> <c:out value="${errorMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <div class="card shadow-sm border-0">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light text-secondary">
                            <tr>
                                <th class="ps-4">Booking</th>
                                <th>Customer</th>
                                <th>Check-in</th>
                                <th>Check-out</th>
                                <th>Status</th>
                                <th class="text-center pe-4">Action</th>
                            </tr>
                        </thead>
                        <tbody class="border-top-0">
                            <c:forEach var="booking" items="${bookings}">
                                <tr>
                                    <td class="ps-4 fw-semibold text-primary"><c:out value="${booking.bookingID}" /></td>
                                    <td class="fw-medium"><c:out value="${booking.customerID.fullName}" /></td>
                                    <td><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></td>
                                    
                                    <td>
                                        <c:choose>
                                            <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">
                                                <span class="badge bg-primary px-2 py-1"><c:out value="${booking.bookingStatus}" /></span>
                                            </c:when>
                                            <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">
                                                <span class="badge bg-warning text-dark px-2 py-1"><c:out value="${booking.bookingStatus}" /></span>
                                            </c:when>
                                            <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}">
                                                <span class="badge bg-success px-2 py-1"><c:out value="${booking.bookingStatus}" /></span>
                                            </c:when>
                                            <c:when test="${booking.bookingStatus eq 'CHECKED_OUT'}">
                                                <span class="badge bg-secondary px-2 py-1"><c:out value="${booking.bookingStatus}" /></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-dark px-2 py-1"><c:out value="${booking.bookingStatus}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    
                                    <!-- Xử lý nút Action gọn gàng, tránh bị ô trống -->
                                    <td class="text-center pe-4">
                                        <c:choose>
                                            <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">
                                                <a href="${pageContext.request.contextPath}/staff/bookings?bookingID=${booking.bookingID}" 
                                                   class="btn btn-primary btn-sm rounded-pill px-3 shadow-sm">
                                                    <i class="fa-solid fa-key me-1"></i> Assign Rooms
                                                </a>
                                            </c:when>
                                            <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">
                                                <a href="${pageContext.request.contextPath}/process-checkin?bookingID=${booking.bookingID}" 
                                                   class="btn btn-success btn-sm rounded-pill px-3 shadow-sm">
                                                    <i class="fa-solid fa-bell-concierge me-1"></i> Check In
                                                </a>
                                            </c:when>
                                            <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}">
                                                <span class="text-muted small fst-italic"><i class="fa-solid fa-check text-success me-1"></i> Currently staying</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="text-muted small"><i class="fa-solid fa-minus"></i></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="6" class="text-center text-muted py-5">
                                        <i class="fa-solid fa-folder-open fs-1 mb-3 text-secondary opacity-50"></i>
                                        <p class="mb-0 fs-5">No active stays were found.</p>
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