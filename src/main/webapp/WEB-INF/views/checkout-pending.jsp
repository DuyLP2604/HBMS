<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Booking Created" useBootstrap="true">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card shadow-sm">
                    <div class="card-body p-4 p-md-5">
                        <div class="text-center mb-4">
                            <h1 class="h3 text-success mb-3">Booking Created Successfully</h1>
                            <p class="mb-2">Your booking reference is:</p>
                            <p class="fs-3 fw-bold"><c:out value="${bookingID}" /></p>
                            <p class="text-muted">Complete your payment before the deadline to secure your booking.</p>
                        </div>
                        <div class="alert alert-warning text-center">
                            Booking status: <strong><c:out value="${booking.bookingStatus}" /></strong>
                        </div>
                        <div class="border rounded p-3 mb-4">
                            <div class="d-flex justify-content-between gap-3 mb-3">
                                <span>Check-in:</span>
                                <strong><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3 mb-3">
                                <span>Check-out:</span>
                                <strong><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3 mb-3">
                                <span>Total Booking Amount:</span>
                                <strong><fmt:formatNumber value="${booking.totalAmount}" type="number" /> VND</strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3 mb-3">
                                <span>Payment Option:</span>
                                <strong>
                                    <c:choose>
                                        <c:when test="${paymentOption eq 'DEPOSIT'}">30% Deposit</c:when>
                                        <c:when test="${paymentOption eq 'FULL'}">Full Payment (100%)</c:when>
                                        <c:otherwise>Unavailable</c:otherwise>
                                    </c:choose>
                                </strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3 mb-3">
                                <span>Amount to Pay Now:</span>
                                <strong class="text-success"><fmt:formatNumber value="${initialRequiredAmount}" type="number" /> VND</strong>
                            </div>
                            <div class="d-flex justify-content-between gap-3">
                                <span>Payment Deadline:</span>
                                <strong class="text-danger"><fmt:formatDate value="${booking.paymentDeadline}" pattern="dd/MM/yyyy HH:mm:ss" /></strong>
                            </div>
                        </div>
                        <c:if test="${paymentOption eq 'DEPOSIT'}">
                            <div class="alert alert-info">Your booking will be confirmed after the 30% deposit is paid successfully. You can pay the remaining 70% later.</div>
                        </c:if>
                        <div class="alert alert-warning">
                            <p class="mb-2">You cannot create another booking until this booking is paid with a deposit or in full, or cancelled.</p>
                            <p class="mb-0">If payment is not completed before the deadline, this booking will expire and be cancelled.</p>
                        </div>
                        <div class="border rounded p-3 mb-4">
                            <h2 class="h6 mb-2">Cancellation and Refund Policy</h2>
                            <p class="mb-2">Cancel less than 24 hours after your first successful deposit or full payment to receive a 100% refund of the amount you have paid. The refund will be credited to your system wallet.</p>
                            <p class="mb-2">Cancellation at or after 24 hours is not refundable. This period starts from the first successful payment, not from the check-in time.</p>
                            <p class="mb-0">Paying the remaining balance does not restart the 24-hour refund period.</p>
                        </div>
                        <c:url var="paymentUrl" value="/payment">
                            <c:param name="bookingID" value="${bookingID}" />
                        </c:url>
                        <div class="text-center">
                            <a href="<c:out value='${paymentUrl}' />" class="btn btn-success px-4">Continue to Payment</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>