<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows the PayOS checkout link or QR code while confirmation is pending.
--%>
<c:url var="statusUrl" value="/payment-status"><c:param name="orderCode" value="${payOsOrderCode}" /></c:url>
<c:url var="returnUrl" value="/payment"><c:param name="orderCode" value="${payOsOrderCode}" /></c:url>
<c:url var="bookingUrl" value="/my-bookings"><c:param name="bookingID" value="${payOsBookingID}" /></c:url>

<layout:layout title="Payment Status" pageCss="booking-completion.css" pageJs="payment.js"
               useBootstrap="true" bodyClass="booking-completion-body">
    <div class="booking-completion pay-return">
        <header class="ps-heading">
            <span class="ps-check bp-return-icon ${payOsReview ? 'bp-review' : ''}">
                <i class="fa-solid ${payOsReview ? 'fa-circle-exclamation' : 'fa-clock'}" aria-hidden="true"></i>
            </span>
            <p class="bc-eyebrow">BOOKING PAYMENT</p>
            <h1>${payOsReview ? 'Payment needs review' : 'Waiting for confirmation'}</h1>
            <p>Booking <strong><c:out value="${payOsBookingID}" /></strong></p>
        </header>
        <section class="bc-panel bp-return-panel">
            <c:choose>
                <c:when test="${payOsReview}">
                    <p class="bp-notice bp-notice-review" role="status">If your bank recorded a transfer, contact the hotel before paying again.</p>
                </c:when>
                <c:otherwise>
                    <p id="payos-pending" class="bp-notice" role="status"
                       data-status-url="<c:out value='${statusUrl}' />"
                       data-return-url="<c:out value='${returnUrl}' />">Waiting for PayOS to confirm your payment. This page updates automatically.</p>
                </c:otherwise>
            </c:choose>
            <div class="ps-actions">
                <a class="bc-button bc-button-primary" href="<c:out value='${bookingUrl}' />">View booking</a>
                <a class="bc-button bc-button-secondary" href="<c:out value='${returnUrl}' />">
                    <i class="fa-solid fa-rotate-right" aria-hidden="true"></i> Check again
                </a>
            </div>
        </section>
    </div>
</layout:layout>
