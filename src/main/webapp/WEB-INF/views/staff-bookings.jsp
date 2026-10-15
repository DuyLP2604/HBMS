<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Bookings Awaiting Assignment"
    useBootstrap="true"
    >
    <div class="container py-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0">Bookings Awaiting Room Assignment</h1>
            <a href="${pageContext.request.contextPath}/customer?action=add" class="btn btn-success">Add New Customer</a>
        </div>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
        </c:if>
        <p class="text-muted">Check-in: 14:00. From 14:15, unarrived bookings appear first. Refresh this list to see the latest status.</p>
        <div class="table-responsive">
            <table class="table table-bordered table-hover align-middle">
                <thead class="table-light">
                    <tr>
                        <th>Booking</th>
                        <th>Customer</th>
                        <th>Check-in</th>
                        <th>Check-out</th>
                        <th>Total</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>
                </thead>
                <tbody>
                    <c:forEach var="booking" items="${bookings}">
                        <tr class="${noShowBookings[booking.bookingID] ? 'table-danger' : ''}">
                            <td><c:out value="${booking.bookingID}" /></td>
                            <td><c:out value="${booking.customerID.fullName}" /></td>
                            <td><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /> 14:00<br><small class="text-muted">Held until 14:15</small></td>
                            <td><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></td>
                            <td><fmt:formatNumber value="${booking.totalAmount}" type="number" /> VND</td>
                            <td>
                                <span class="badge bg-success"><c:out value="${booking.bookingStatus}" /></span>
                                <c:if test="${noShowBookings[booking.bookingID]}"><span class="badge bg-danger ms-1">Overdue check-in</span></c:if>
                            </td>
                            <td>
                                <a href="${pageContext.request.contextPath}/staff/bookings?bookingID=${booking.bookingID}" class="btn btn-primary btn-sm">Assign Rooms</a>
                                <c:if test="${noShowBookings[booking.bookingID]}">
                                    <form action="${pageContext.request.contextPath}/staff/stays" method="post" class="d-inline" onsubmit="return confirm('Cancel this booking for no-show? No refund applies.');">
                                        <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
                                        <input type="hidden" name="action" value="cancelNoShow">
                                        <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.bookingCsrfToken}' />">
                                        <button type="submit" class="btn btn-outline-danger btn-sm">Cancel No-show</button>
                                    </form>
                                </c:if>
                            </td>
                        </tr>
                    </c:forEach>
                    <c:if test="${empty bookings}">
                        <tr>
                            <td colspan="7" class="text-center text-muted py-4">No bookings are awaiting room assignment.</td>
                        </tr>
                    </c:if>
                </tbody>
            </table>
        </div>
    </div>
</layout:layout>