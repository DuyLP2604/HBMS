<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Payment" useBootstrap="true" pageCss="wallet.css" pageJs="payment.js">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-8">
                <div class="card shadow-sm">
                    <div class="card-header bg-primary text-white">
                        <h1 class="h4 mb-0">Booking Payment</h1>
                    </div>
                    <div class="card-body p-4">
                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
                        </c:if>
                        <dl class="row">
                            <dt class="col-sm-5">Booking Reference</dt>
                            <dd class="col-sm-7"><c:out value="${booking.bookingID}" /></dd>
                            <dt class="col-sm-5">Check-in</dt>
                            <dd class="col-sm-7"><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></dd>
                            <dt class="col-sm-5">Check-out</dt>
                            <dd class="col-sm-7"><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></dd>
                            <dt class="col-sm-5">Booking Status</dt>
                            <dd class="col-sm-7"><c:out value="${bookingSummary.bookingStatus}" /></dd>
                            <dt class="col-sm-5">Total Booking Amount</dt>
                            <dd class="col-sm-7"><fmt:formatNumber value="${bookingSummary.totalAmount}" type="number" /> VND</dd>
                            <dt class="col-sm-5">Already Paid</dt>
                            <dd class="col-sm-7"><fmt:formatNumber value="${bookingSummary.totalPaidAmount}" type="number" /> VND</dd>
                            <dt class="col-sm-5">Payment Type</dt>
                            <dd class="col-sm-7 fw-semibold">
                                <c:choose>
                                    <c:when test="${paymentType eq 'DEPOSIT'}">30% Deposit</c:when>
                                    <c:when test="${paymentType eq 'FULL'}">Full Payment (100%)</c:when>
                                    <c:when test="${paymentType eq 'BALANCE'}">Remaining Balance</c:when>
                                    <c:otherwise>Unavailable</c:otherwise>
                                </c:choose>
                            </dd>
                            <c:if test="${paymentType eq 'DEPOSIT' or paymentType eq 'FULL'}">
                                <dt class="col-sm-5">Payment Deadline</dt>
                                <dd class="col-sm-7"><fmt:formatDate value="${booking.paymentDeadline}" pattern="dd/MM/yyyy HH:mm:ss" /></dd>
                                <dt class="col-sm-5">Time Remaining</dt>
                                <dd class="col-sm-7"><span id="paymentCountdown" class="fw-semibold text-danger" data-deadline="<c:out value='${booking.paymentDeadline.time}' />" role="timer">Calculating...</span></dd>
                            </c:if>
                            <dt class="col-sm-5">Amount to Pay Now</dt>
                            <dd class="col-sm-7 fw-bold text-success"><fmt:formatNumber value="${amountToPay}" type="number" /> VND</dd>
                        </dl>
                        <c:choose>
                            <c:when test="${paymentType eq 'DEPOSIT'}">
                                <div class="alert alert-info">Pay 30% to confirm your booking. You can pay the remaining 70% later.</div>
                            </c:when>
                            <c:when test="${paymentType eq 'FULL'}">
                                <div class="alert alert-info">Pay the full booking amount to confirm your booking.</div>
                            </c:when>
                            <c:when test="${paymentType eq 'BALANCE'}">
                                <div class="alert alert-info">This payment covers your current outstanding balance, including any additional service charges. It does not restart the 24-hour refund period.</div>
                            </c:when>
                        </c:choose>
                        <div class="wallet-payment-panel">
                            <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mb-2">
                                <strong>My Wallet</strong>
                                <a href="${pageContext.request.contextPath}/wallet" class="small">View Wallet &amp; History</a>
                            </div>
                            <c:choose>
                                <c:when test="${walletAvailable}">
                                    <div class="wallet-amount mb-2"><fmt:formatNumber value="${wallet.balance}" pattern="#,##0.##" /> VND</div>
                                    <c:choose>
                                        <c:when test="${walletCanPay}"><div class="small text-success">Your wallet covers this payment.</div></c:when>
                                        <c:otherwise><div class="small text-danger">Insufficient wallet balance. You need <fmt:formatNumber value="${walletShortfall}" pattern="#,##0.##" /> VND more. Please select another payment method.</div></c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:otherwise><div class="small text-muted">Your wallet is not available yet. Please select another payment method.</div></c:otherwise>
                            </c:choose>
                        </div>
                        <c:choose>
                            <c:when test="${empty paymentRequestToken or empty amountToPay or amountToPay le 0 or (paymentType ne 'DEPOSIT' and paymentType ne 'FULL' and paymentType ne 'BALANCE')}">
                                <div class="alert alert-warning">Payment information is unavailable. Open your booking again to continue.</div>
                                <a href="${pageContext.request.contextPath}/my-bookings" class="btn btn-secondary w-100">Back to My Bookings</a>
                            </c:when>
                            <c:when test="${empty paymentMethods}">
                                <div class="alert alert-warning">No payment methods are currently available.</div>
                                <a href="${pageContext.request.contextPath}/my-bookings" class="btn btn-secondary w-100">Back to My Bookings</a>
                            </c:when>
                            <c:otherwise>
                                <form id="paymentForm" action="${pageContext.request.contextPath}/payment" method="post" data-wallet-can-pay="<c:out value='${walletCanPay}' />">
                                    <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
                                    <input type="hidden" name="paymentRequestToken" value="<c:out value='${paymentRequestToken}' />">
                                    <div class="mb-4">
                                        <label for="methodID" class="form-label fw-semibold">Payment Method</label>
                                        <select id="methodID" name="methodID" class="form-select" required>
                                            <option value="" disabled selected>Select a payment method</option>
                                            <c:forEach var="method" items="${paymentMethods}">
                                                <c:choose>
                                                    <c:when test="${method.methodID eq 'PT09'}">
                                                        <option value="PT09" ${walletCanPay ? '' : 'disabled'}>My Wallet — <c:choose><c:when test="${walletAvailable}"><fmt:formatNumber value="${wallet.balance}" pattern="#,##0.##" /> VND<c:if test="${not walletCanPay}"> (Insufficient balance)</c:if></c:when><c:otherwise>Unavailable</c:otherwise></c:choose></option>
                                                    </c:when>
                                                    <c:otherwise><option value="<c:out value='${method.methodID}' />"><c:out value="${method.methodName}" /></option></c:otherwise>
                                                </c:choose>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div id="walletPaymentNotice" class="alert alert-info d-none" role="status">This payment will be deducted from your wallet. The balance is checked again when you pay.</div>
                                    <div id="expiredMessage" class="alert alert-danger d-none" role="alert">The payment time limit has expired.</div>
                                    <div class="d-flex gap-2">
                                        <a href="${pageContext.request.contextPath}/my-bookings" class="btn btn-outline-secondary">Back</a>
                                        <button id="paymentButton" type="submit" class="btn btn-success flex-grow-1">
                                            <c:choose>
                                                <c:when test="${paymentType eq 'DEPOSIT'}">Pay 30% Deposit</c:when>
                                                <c:when test="${paymentType eq 'FULL'}">Pay Full Amount</c:when>
                                                <c:otherwise>Pay Remaining Balance</c:otherwise>
                                            </c:choose>
                                        </button>
                                    </div>
                                </form>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
