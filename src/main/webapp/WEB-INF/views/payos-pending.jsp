<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:url var="statusUrl" value="/payment-status"><c:param name="orderCode" value="${payOsOrderCode}" /></c:url>
<c:url var="returnUrl" value="/payment"><c:param name="orderCode" value="${payOsOrderCode}" /></c:url>
<c:url var="bookingUrl" value="/my-bookings"><c:param name="bookingID" value="${payOsBookingID}" /></c:url>

<layout:layout title="PayOS payment status" useBootstrap="true" pageJs="payment.js">
    <div class="container py-5">
        <div class="card mx-auto" style="max-width: 38rem">
            <div class="card-body p-4">
                <h1 class="h4">Payment status</h1>
                <p>Booking <strong><c:out value="${payOsBookingID}" /></strong></p>
                <c:choose>
                    <c:when test="${payOsReview}">
                        <p class="alert alert-warning" role="status">The transfer needs review. If your bank recorded a transfer, contact the hotel before paying again.</p>
                    </c:when>
                    <c:otherwise>
                        <p id="payos-pending" class="alert alert-info" role="status"
                           data-status-url="<c:out value='${statusUrl}' />"
                           data-return-url="<c:out value='${returnUrl}' />">Waiting for PayOS to confirm your payment.</p>
                    </c:otherwise>
                </c:choose>
                <a class="btn btn-outline-secondary" href="<c:out value='${bookingUrl}' />">View booking</a>
                <a class="btn btn-link" href="<c:out value='${returnUrl}' />">Check again</a>
            </div>
        </div>
    </div>
</layout:layout>
