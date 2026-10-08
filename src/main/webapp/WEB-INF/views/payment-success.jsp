<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Payment Successful" useBootstrap="true">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-7">
                <div class="card shadow-sm">
                    <div class="card-body text-center p-5">
                        <h1 class="h3 text-success mb-3">Payment Successful</h1>
                        <p>Booking: <strong><c:out value="${bookingID}" /></strong></p>
                        <p>Payment: <strong><c:out value="${paymentID}" /></strong></p>
                        <p>Transaction: <strong><c:out value="${transactionCode}" /></strong></p>
                        <p>
                            Payment Type:
                            <strong>
                                <c:choose>
                                    <c:when test="${paymentType eq 'DEPOSIT'}">30% Deposit</c:when>
                                    <c:when test="${paymentType eq 'FULL'}">Full Payment (100%)</c:when>
                                    <c:when test="${paymentType eq 'BALANCE'}">Remaining Balance</c:when>
                                    <c:otherwise>Payment</c:otherwise>
                                </c:choose>
                            </strong>
                        </p>
                        <p>Amount Paid: <strong class="text-success"><fmt:formatNumber value="${paymentAmount}" type="number" /> VND</strong></p>
                        <c:if test="${paymentMethodID eq 'PT09'}">
                            <p>Payment Method: <strong>My Wallet</strong></p>
                            <c:if test="${not empty wallet}"><p>Current Wallet Balance: <strong><fmt:formatNumber value="${wallet.balance}" pattern="#,##0.##" /> VND</strong></p></c:if>
                            <a href="${pageContext.request.contextPath}/wallet" class="btn btn-outline-secondary mb-2">View Wallet &amp; History</a>
                        </c:if>
                        <div class="alert alert-success">
                            <c:choose>
                                <c:when test="${paymentType eq 'DEPOSIT'}">Your 30% deposit was paid successfully. Your booking is now confirmed. You can pay the remaining 70% later.</c:when>
                                <c:when test="${paymentType eq 'FULL'}">Your full payment was recorded successfully. Your booking is now confirmed and awaiting room assignment.</c:when>
                                <c:when test="${paymentType eq 'BALANCE'}">Your balance payment was recorded successfully.</c:when>
                                <c:otherwise>Your payment was recorded successfully.</c:otherwise>
                            </c:choose>
                        </div>
                        <c:if test="${paymentType eq 'DEPOSIT'}">
                            <c:url var="balancePaymentUrl" value="/payment">
                                <c:param name="bookingID" value="${bookingID}" />
                            </c:url>
                            <a href="<c:out value='${balancePaymentUrl}' />" class="btn btn-outline-success mb-2">Pay Remaining Balance</a>
                        </c:if>
                        <a href="${pageContext.request.contextPath}/my-bookings" class="btn btn-primary mb-2">View My Bookings</a>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
