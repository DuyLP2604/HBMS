<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="My Bookings"
    useBootstrap="true"
    pageJs="my-bookings.js"
    >
    <div class="container py-5">
        
        <!-- Header -->
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0">My Bookings</h1>
            <a href="${pageContext.request.contextPath}/room-types" class="btn btn-primary fw-bold">Book Another Stay</a>
        </div>

        <%-- Flash success message --%>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm" role="alert">
                <c:out value="${successMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        
        <%-- Flash error message --%>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show shadow-sm" role="alert">
                <c:out value="${errorMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>

        <c:choose>
            <c:when test="${empty bookings}">
                <div class="alert alert-info text-center py-4">You do not have any bookings yet.</div>
            </c:when>
            <c:otherwise>
                
                <!-- Bảng Danh sách Booking -->
                <div class="card shadow-sm border-0">
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover align-middle mb-0 text-center">
                                <thead class="table-light">
                                    <tr>
                                        <th class="py-3">Booking ID</th>
                                        <th>Stay</th>
                                        <th>Nights</th>
                                        <th class="text-end">Total (VND)</th>
                                        <th>Payment</th>
                                        <th>Status</th>
                                        <th>Actions</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="booking" items="${bookings}">
                                        <tr>
                                            <!-- Mã Đơn -->
                                            <td class="fw-bold text-dark"><c:out value="${booking.bookingID}" /></td>
                                            
                                            <!-- Thời gian ở -->
                                            <td>
                                                <fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /> 
                                                <span class="text-muted mx-1">–</span> 
                                                <fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" />
                                            </td>
                                            
                                            <!-- Số đêm -->
                                            <td><c:out value="${booking.numberOfNights}" /></td>
                                            
                                            <!-- Tổng tiền -->
                                            <td class="text-end fw-bold text-danger">
                                                <fmt:formatNumber value="${booking.totalAmount}" type="number" />
                                            </td>
                                            
                                            <!-- Trạng thái Thanh toán -->
                                            <td>
                                                <c:choose>
                                                    <c:when test="${booking.paymentStatus eq 'PAID'}">
                                                        <span class="badge bg-success">PAID</span>
                                                    </c:when>
                                                    <c:when test="${booking.paymentStatus eq 'REFUNDED'}">
                                                        <span class="badge bg-secondary">REFUNDED</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-warning text-dark"><c:out value="${booking.paymentStatus}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            
                                            <!-- Trạng thái Booking -->
                                            <td>
                                                <c:choose>
                                                    <c:when test="${booking.bookingStatus eq 'CANCELLED'}">
                                                        <span class="badge bg-danger">CANCELLED</span>
                                                    </c:when>
                                                    <c:when test="${booking.bookingStatus eq 'PENDING_PAYMENT'}">
                                                        <span class="badge bg-warning text-dark">PENDING PAYMENT</span>
                                                    </c:when>
                                                    <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">
                                                        <span class="badge bg-info text-dark">CONFIRMED</span>
                                                    </c:when>
                                                    <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">
                                                        <span class="badge bg-primary">ASSIGNED</span>
                                                    </c:when>
                                                    <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}">
                                                        <span class="badge bg-success">CHECKED IN</span>
                                                    </c:when>
                                                    <c:when test="${booking.bookingStatus eq 'CHECKED_OUT'}">
                                                        <span class="badge bg-secondary">CHECKED OUT</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary"><c:out value="${booking.bookingStatus}" /></span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            
                                            <!-- Thao tác -->
                                            <td>
                                                <div class="d-flex flex-column gap-2 align-items-center py-2">
                                                    <div class="d-flex gap-2 justify-content-center">
                                                        <!-- Nút Detail -->
                                                        <a href="${pageContext.request.contextPath}/my-bookings?bookingID=${booking.bookingID}" class="btn btn-outline-primary btn-sm px-3">Details</a>
                                                        
                                                        <!-- Nút Pay Now -->
                                                        <c:if test="${booking.bookingStatus eq 'PENDING_PAYMENT'}">
                                                            <a href="${pageContext.request.contextPath}/payment?bookingID=${booking.bookingID}" class="btn btn-success btn-sm px-3">Pay Now</a>
                                                        </c:if>

                                                        <!-- Nút Cancel -->
                                                        <c:if test="${booking.bookingStatus eq 'PENDING_PAYMENT' or booking.bookingStatus eq 'CONFIRMED' or booking.bookingStatus eq 'ASSIGNED'}">
                                                            <form action="${pageContext.request.contextPath}/my-bookings/cancel" method="post" class="m-0" onsubmit="return confirm('Are you sure you want to cancel this booking?');">
                                                                <input type="hidden" name="bookingID" value="${booking.bookingID}">
                                                                <button type="submit" class="btn btn-outline-danger btn-sm px-3">Cancel</button>
                                                            </form>
                                                        </c:if>
                                                    </div>
                                                    
                                                    <c:if test="${booking.bookingStatus eq 'PENDING_PAYMENT'}">
                                                        <small class="text-danger mt-1">
                                                            Expires: <span class="payment-countdown fw-bold" data-deadline="${booking.paymentDeadline.time}"></span>
                                                        </small>
                                                    </c:if>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>