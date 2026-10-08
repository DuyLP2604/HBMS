<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="bookingState" value="${bookingSummary.bookingStatus}" />
<layout:layout title="Booking Details" useBootstrap="true">
    <div class="container py-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0">Booking <c:out value="${booking.bookingID}" /></h1>
            <a href="${pageContext.request.contextPath}/my-bookings" class="btn btn-secondary">Back</a>
        </div>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success" role="alert"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>
        <c:if test="${not empty bookingAccess.bookingLockedUntil}">
            <div class="alert alert-warning" role="alert">Creating new bookings is temporarily locked until <strong><fmt:formatDate value="${bookingAccess.bookingLockedUntil}" pattern="dd/MM/yyyy HH:mm:ss" /></strong>. You can still pay or cancel an eligible existing booking.</div>
        </c:if>
        <div class="card shadow-sm mb-4">
            <div class="card-body">
                <div class="row g-3">
                    <div class="col-md-4"><strong>Check-in:</strong><br><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></div>
                    <div class="col-md-4"><strong>Check-out:</strong><br><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></div>
                    <div class="col-md-4"><strong>Nights:</strong><br><c:out value="${booking.numberOfNights}" /></div>
                    <div class="col-md-4"><strong>Booking Status:</strong><br><c:out value="${bookingState}" /></div>
                    <div class="col-md-4"><strong>Payment Status:</strong><br><c:out value="${bookingSummary.paymentStatus}" /></div>
                    <div class="col-md-4"><strong>Payment Option:</strong><br><c:out value="${bookingSummary.paymentOption eq 'DEPOSIT' ? '30% Deposit' : 'Full Payment'}" /></div>
                    <div class="col-md-4"><strong>Total Amount:</strong><br><fmt:formatNumber value="${bookingSummary.totalAmount}" type="number" /> VND</div>
                    <div class="col-md-4"><strong>Total Paid:</strong><br><fmt:formatNumber value="${bookingSummary.totalPaidAmount}" type="number" /> VND</div>
                    <div class="col-md-4"><strong>Remaining Balance:</strong><br><fmt:formatNumber value="${bookingSummary.remainingAmount}" type="number" /> VND</div>
                    <div class="col-md-4"><strong>Refunded to Wallet:</strong><br><fmt:formatNumber value="${bookingSummary.refundedAmount}" type="number" /> VND</div>
                    <c:if test="${bookingState eq 'PENDING_PAYMENT'}">
                        <div class="col-md-4"><strong>Initial Amount to Pay:</strong><br><fmt:formatNumber value="${bookingSummary.initialRequiredAmount}" type="number" /> VND</div>
                        <div class="col-md-4"><strong>Payment Deadline:</strong><br><fmt:formatDate value="${coreBooking.paymentDeadline}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                    </c:if>
                    <c:if test="${not empty bookingSummary.firstPaidAt}">
                        <div class="col-md-4"><strong>First Successful Payment:</strong><br><fmt:formatDate value="${bookingSummary.firstPaidAt}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                        <div class="col-md-4"><strong>Refund Cutoff:</strong><br><fmt:formatDate value="${bookingSummary.refundDeadline}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                    </c:if>
                    <c:if test="${not empty bookingSummary.cancelledAt}">
                        <div class="col-md-4"><strong>Cancelled At:</strong><br><fmt:formatDate value="${bookingSummary.cancelledAt}" pattern="dd/MM/yyyy HH:mm:ss" /></div>
                    </c:if>
                </div>
            </div>
        </div>
        <c:choose>
            <c:when test="${bookingSummary.paymentStatus eq 'REFUNDED'}">
                <div class="alert alert-success">A refund of <strong><fmt:formatNumber value="${bookingSummary.refundedAmount}" type="number" /> VND</strong> is recorded in your system wallet.</div>
            </c:when>
            <c:when test="${bookingSummary.paymentStatus eq 'NO_REFUND'}">
                <div class="alert alert-warning">This booking was cancelled without a refund under the 24-hour payment policy.</div>
            </c:when>
        </c:choose>
        <div class="alert alert-info">
            Cancel less than 24 hours after the first successful deposit or full payment to receive a 100% refund of the paid amount into your system wallet. Cancellation at or after 24 hours is not refundable. The period is measured from payment, not check-in. Paying the remaining balance does not restart it. Bookings cannot be cancelled after check-in.
        </div>
        <h2 class="h4 mb-3">Rooms</h2>
        <c:forEach var="item" items="${booking.items}">
            <div class="card shadow-sm mb-3">
                <div class="card-body">
                    <div class="row">
                        <div class="col-md-5">
                            <h3 class="h5"><c:out value="${item.roomTypeName}" /></h3>
                            <p class="mb-1">Quantity: <strong><c:out value="${item.quantity}" /></strong></p>
                            <p class="mb-1">Guests: <strong><c:out value="${item.guestCount}" /></strong></p>
                            <p class="mb-0">Subtotal: <strong><fmt:formatNumber value="${item.subtotal}" type="number" /> VND</strong></p>
                        </div>
                        <div class="col-md-7">
                            <h4 class="h6">Assigned Rooms</h4>
                            <c:choose>
                                <c:when test="${empty item.roomNumbers and bookingState eq 'CANCELLED'}"><div class="text-muted">No rooms were assigned before cancellation.</div></c:when>
                                <c:when test="${empty item.roomNumbers}"><div class="alert alert-warning mb-0">No rooms have been assigned yet.</div></c:when>
                                <c:otherwise>
                                    <ul class="list-group">
                                        <c:forEach var="roomNumber" items="${item.roomNumbers}">
                                            <li class="list-group-item">Room <strong><c:out value="${roomNumber}" /></strong></li>
                                        </c:forEach>
                                    </ul>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </div>
            </div>
        </c:forEach>
        <c:url var="paymentUrl" value="/payment">
            <c:param name="bookingID" value="${booking.bookingID}" />
        </c:url>
        <div class="d-flex flex-wrap gap-2 mt-4">
            <c:if test="${bookingState eq 'PENDING_PAYMENT'}">
                <a href="<c:out value='${paymentUrl}' />" class="btn btn-success">Continue to Payment</a>
            </c:if>
            <c:if test="${not empty bookingSummary.firstPaidAt and bookingSummary.remainingAmount gt 0 and (bookingState eq 'CONFIRMED' or bookingState eq 'ASSIGNED' or bookingState eq 'CHECKED_IN')}">
                <a href="<c:out value='${paymentUrl}' />" class="btn btn-success">Pay Remaining Balance</a>
            </c:if>
            <c:if test="${bookingState eq 'PENDING_PAYMENT' or bookingState eq 'CONFIRMED' or bookingState eq 'ASSIGNED'}">
                <form action="${pageContext.request.contextPath}/my-bookings/cancel" method="post" class="m-0" onsubmit="return confirm('Cancel this booking? Refund eligibility follows the 24-hour payment policy.');">
                    <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.bookingCsrfToken}' />">
                    <button type="submit" class="btn btn-outline-danger">Cancel Booking</button>
                </form>
            </c:if>
        </div>
    </div>
</layout:layout>