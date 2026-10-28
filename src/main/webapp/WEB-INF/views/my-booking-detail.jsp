<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="en_GB" scope="page" />
<fmt:setTimeZone value="Asia/Ho_Chi_Minh" scope="page" />
<c:set var="bookingState" value="${bookingSummary.bookingStatus}" />
<c:set var="paymentState" value="${bookingSummary.paymentStatus}" />
<c:set var="canCancel" value="${bookingState eq 'PENDING_PAYMENT' or bookingState eq 'CONFIRMED' or bookingState eq 'ASSIGNED'}" />
<c:set var="canPayRemaining" value="${not empty bookingSummary.firstPaidAt and ((bookingSummary.roomRemainingAmount gt 0 and (bookingState eq 'CONFIRMED' or bookingState eq 'ASSIGNED')) or (bookingState eq 'CHECKED_IN' and bookingSummary.remainingAmount gt 0))}" />
<c:set var="roomCount" value="${0}" />
<c:forEach var="item" items="${booking.items}"><c:set var="roomCount" value="${roomCount + item.quantity}" /></c:forEach>
<c:choose>
    <c:when test="${bookingState eq 'PENDING_PAYMENT'}"><c:set var="statusLabel" value="Awaiting payment" /><c:set var="statusTone" value="pending" /><c:set var="statusMessage" value="Complete your payment to confirm this stay." /></c:when>
    <c:when test="${bookingState eq 'CONFIRMED'}"><c:set var="statusLabel" value="Confirmed" /><c:set var="statusTone" value="confirmed" /><c:set var="statusMessage" value="Your booking is confirmed. We look forward to welcoming you." /></c:when>
    <c:when test="${bookingState eq 'ASSIGNED'}"><c:set var="statusLabel" value="Rooms assigned" /><c:set var="statusTone" value="confirmed" /><c:set var="statusMessage" value="Your room numbers have been assigned. You can find them below." /></c:when>
    <c:when test="${bookingState eq 'CHECKED_IN'}"><c:set var="statusLabel" value="Checked in" /><c:set var="statusTone" value="confirmed" /><c:set var="statusMessage" value="You are checked in. Enjoy your stay with us." /></c:when>
    <c:when test="${bookingState eq 'CHECKED_OUT'}"><c:set var="statusLabel" value="Stay completed" /><c:set var="statusTone" value="neutral" /><c:set var="statusMessage" value="Thank you for staying with us. We hope to welcome you again." /></c:when>
    <c:when test="${bookingState eq 'CANCELLED'}"><c:set var="statusLabel" value="Cancelled" /><c:set var="statusTone" value="cancelled" /><c:set var="statusMessage" value="This booking has been cancelled." /></c:when>
    <c:otherwise><c:set var="statusLabel" value="Status unavailable" /><c:set var="statusTone" value="neutral" /><c:set var="statusMessage" value="Please contact reception if you need help with this booking." /></c:otherwise>
</c:choose>
<c:choose>
    <c:when test="${paymentState eq 'FULLY_PAID'}"><c:set var="paymentLabel" value="Paid in full" /><c:set var="paymentTone" value="confirmed" /></c:when>
    <c:when test="${paymentState eq 'DEPOSIT_PAID'}"><c:set var="paymentLabel" value="Partially paid" /><c:set var="paymentTone" value="partial" /></c:when>
    <c:when test="${paymentState eq 'REFUNDED'}"><c:set var="paymentLabel" value="Refunded to wallet" /><c:set var="paymentTone" value="confirmed" /></c:when>
    <c:when test="${paymentState eq 'NO_REFUND'}"><c:set var="paymentLabel" value="No refund" /><c:set var="paymentTone" value="cancelled" /></c:when>
    <c:when test="${paymentState eq 'UNPAID'}"><c:set var="paymentLabel" value="Unpaid" /><c:set var="paymentTone" value="pending" /></c:when>
    <c:otherwise><c:set var="paymentLabel" value="Payment status unavailable" /><c:set var="paymentTone" value="neutral" /></c:otherwise>
</c:choose>
<c:url var="bookingsUrl" value="/my-bookings" />
<c:url var="paymentUrl" value="/payment"><c:param name="bookingID" value="${fn:trim(booking.bookingID)}" /></c:url>
<c:url var="cancelUrl" value="/my-bookings/cancel" />
<layout:layout title="Booking Details" pageCss="my-booking-detail.css" bodyClass="booking-detail-page">
    <div class="booking-detail">
        <header class="bd-page-heading">
            <div><p class="bd-eyebrow">YOUR RESERVATION</p><h1>Booking details</h1><p class="bd-page-description">Your stay, rooms and payment details in one place.</p></div>
            <a href="${bookingsUrl}" class="bd-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> My bookings</a>
        </header>
        <c:if test="${not empty successMessage}"><div class="bd-notice bd-notice-success" role="status"><i class="fa-solid fa-circle-check" aria-hidden="true"></i><c:out value="${successMessage}" /></div></c:if>
        <c:if test="${not empty errorMessage}"><div class="bd-notice bd-notice-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><c:out value="${errorMessage}" /></div></c:if>
        <c:if test="${not empty bookingAccess.bookingLockedUntil}"><div class="bd-notice bd-notice-info" role="status"><i class="fa-solid fa-circle-info" aria-hidden="true"></i><span>New bookings are temporarily unavailable until <strong><fmt:formatDate value="${bookingAccess.bookingLockedUntil}" pattern="dd MMM yyyy, HH:mm:ss" /></strong>. You can still pay or cancel eligible existing bookings.</span></div></c:if>
        <section class="bd-hero" aria-labelledby="bd-reservation-title">
            <div class="bd-hero-heading"><div><span class="bd-small-label">Booking reference</span><h2 id="bd-reservation-title"><c:out value="${booking.bookingID}" /></h2></div><span class="bd-badge bd-badge-${statusTone}" data-booking-status><span class="bd-status-dot" aria-hidden="true"></span><c:out value="${statusLabel}" /></span></div>
            <div class="bd-stay-dates">
                <div class="bd-date"><span class="bd-small-label">Check-in</span><strong><fmt:formatDate value="${booking.checkInDate}" pattern="dd MMM yyyy" /></strong><span><fmt:formatDate value="${booking.checkInDate}" pattern="EEEE" /> · standard check-in from 14:00</span></div>
                <span class="bd-date-arrow" aria-hidden="true"><i class="fa-solid fa-arrow-right-long"></i></span>
                <div class="bd-date"><span class="bd-small-label">Check-out</span><strong><fmt:formatDate value="${booking.checkOutDate}" pattern="dd MMM yyyy" /></strong><span><fmt:formatDate value="${booking.checkOutDate}" pattern="EEEE" /></span></div>
                <div class="bd-stay-length"><span><i class="fa-regular fa-moon" aria-hidden="true"></i> <strong><c:out value="${booking.numberOfNights}" /></strong> ${booking.numberOfNights eq 1 ? 'night' : 'nights'}</span><span><i class="fa-solid fa-bed" aria-hidden="true"></i> <strong><c:out value="${roomCount}" /></strong> ${roomCount eq 1 ? 'room' : 'rooms'}</span></div>
            </div>
            <div class="bd-state-message bd-state-${statusTone}"><i class="fa-solid ${bookingState eq 'CANCELLED' ? 'fa-circle-xmark' : bookingState eq 'PENDING_PAYMENT' ? 'fa-clock' : 'fa-circle-check'}" aria-hidden="true"></i><span><c:out value="${statusMessage}" /><c:if test="${bookingState eq 'CANCELLED' and bookingSummary.cancellationReason eq 'PAYMENT_TIMEOUT'}"> The payment deadline passed before the initial payment was completed.</c:if><c:if test="${bookingState eq 'CANCELLED' and bookingSummary.cancellationReason eq 'NO_SHOW'}"> This booking was cancelled for no-show.</c:if></span></div>
        </section>
        <div class="bd-layout">
            <div class="bd-main">
                <section class="bd-rooms" aria-labelledby="bd-rooms-title">
                    <div class="bd-section-heading"><h2 id="bd-rooms-title">Your rooms</h2><span><c:out value="${fn:length(booking.items)}" /> ${fn:length(booking.items) eq 1 ? 'room type' : 'room types'}</span></div>
                    <c:choose>
                        <c:when test="${empty booking.items}"><div class="bd-room-empty"><i class="fa-solid fa-bed" aria-hidden="true"></i><h3>No room details to display</h3><p>Please contact reception for help with your room information.</p></div></c:when>
                        <c:otherwise><c:forEach var="item" items="${booking.items}">
                            <article class="bd-room-card">
                                <div class="bd-room-heading"><span class="bd-room-icon" aria-hidden="true"><i class="fa-solid fa-bed"></i></span><div><h3><c:out value="${empty fn:trim(item.roomTypeName) ? 'Room type' : item.roomTypeName}" /></h3><div class="bd-room-facts"><span><c:out value="${item.quantity}" /> ${item.quantity eq 1 ? 'room' : 'rooms'}</span><span class="bd-fact-dot" aria-hidden="true">·</span><span><c:out value="${item.guestCount}" /> ${item.guestCount eq 1 ? 'guest' : 'guests'}</span></div></div><div class="bd-room-price"><span class="bd-small-label">Subtotal</span><strong><fmt:formatNumber value="${item.subtotal}" pattern="#,##0.##" /> <span>VND</span></strong></div></div>
                                <div class="bd-room-assignment"><span class="bd-small-label">Assigned rooms</span><c:choose><c:when test="${not empty item.roomNumbers}"><ul class="bd-room-numbers"><c:forEach var="roomNumber" items="${item.roomNumbers}"><li><i class="fa-solid fa-key" aria-hidden="true"></i> Room <c:out value="${roomNumber}" /></li></c:forEach></ul></c:when><c:when test="${bookingState eq 'CANCELLED'}"><p>No room numbers are recorded for this cancelled booking.</p></c:when><c:when test="${bookingState eq 'CHECKED_IN' or bookingState eq 'CHECKED_OUT'}"><p>No room numbers are recorded for this stay.</p></c:when><c:otherwise><p>Room numbers will appear here once assigned by reception.</p></c:otherwise></c:choose></div>
                            </article>
                        </c:forEach></c:otherwise>
                    </c:choose>
                </section>
                <section class="bd-record bd-panel" aria-labelledby="bd-record-title">
                    <h2 id="bd-record-title">Booking record</h2>
                    <dl class="bd-record-grid">
                        <div><dt>Booking created</dt><dd><c:choose><c:when test="${not empty booking.bookingDate}"><fmt:formatDate value="${booking.bookingDate}" pattern="dd MMM yyyy, HH:mm:ss" /></c:when><c:otherwise>Not recorded</c:otherwise></c:choose></dd></div>
                        <c:if test="${not empty bookingSummary.firstPaidAt}"><div><dt>First successful payment</dt><dd><fmt:formatDate value="${bookingSummary.firstPaidAt}" pattern="dd MMM yyyy, HH:mm:ss" /></dd></div></c:if>
                        <c:if test="${not empty bookingSummary.cancelledAt}"><div><dt>Cancelled at</dt><dd><fmt:formatDate value="${bookingSummary.cancelledAt}" pattern="dd MMM yyyy, HH:mm:ss" /></dd></div></c:if>
                        <c:if test="${not empty bookingSummary.refundDeadline}"><div><dt>Refund cutoff</dt><dd><fmt:formatDate value="${bookingSummary.refundDeadline}" pattern="dd MMM yyyy, HH:mm:ss" /></dd></div></c:if>
                    </dl>
                    <p class="bd-timezone"><i class="fa-regular fa-clock" aria-hidden="true"></i> All dates and times are shown in Vietnam time.</p>
                </section>
                <details class="bd-policy bd-panel">
                    <summary><span><i class="fa-regular fa-file-lines" aria-hidden="true"></i> Booking &amp; cancellation policy</span><i class="fa-solid fa-chevron-down" aria-hidden="true"></i></summary>
                    <div class="bd-policy-body"><p>Refunds, when eligible, are returned to your internal wallet.</p><ul><li>Bookings created more than 24 hours before check-in at 14:00 may qualify for a full refund of all successful payments.</li><li>Eligible bookings must be cancelled before 24 hours have passed since booking creation. Later payments do not restart this period.</li><li>Bookings created within 24 hours of check-in, including exactly 24 hours, are not refundable.</li><li>Bookings cannot be cancelled after check-in.</li><li>If you have not checked in by 14:15 on your arrival date, staff may cancel your booking for no-show without a refund.</li></ul></div>
                </details>
            </div>
            <aside class="bd-sidebar" aria-label="Payment and booking actions">
                <section class="bd-payment bd-panel" aria-labelledby="bd-payment-title">
                    <div class="bd-payment-heading"><h2 id="bd-payment-title">Payment summary</h2><span class="bd-badge bd-badge-${paymentTone}" data-payment-status><c:out value="${paymentLabel}" /></span></div>
                    <div class="bd-payment-highlight">
                        <c:choose>
                            <c:when test="${bookingState eq 'PENDING_PAYMENT'}"><span class="bd-small-label">${bookingSummary.paymentOption eq 'DEPOSIT' ? 'Deposit to pay now' : 'Amount to pay now'}</span><strong data-highlight-amount><fmt:formatNumber value="${bookingSummary.initialRequiredAmount}" pattern="#,##0.##" /> <span>VND</span></strong></c:when>
                            <c:when test="${paymentState eq 'REFUNDED'}"><span class="bd-small-label">Refunded to your wallet</span><strong data-highlight-amount><fmt:formatNumber value="${bookingSummary.refundedAmount}" pattern="#,##0.##" /> <span>VND</span></strong></c:when>
                            <c:when test="${bookingState ne 'CANCELLED' and bookingSummary.remainingAmount gt 0}"><span class="bd-small-label">Remaining balance</span><strong data-highlight-amount><fmt:formatNumber value="${bookingSummary.remainingAmount}" pattern="#,##0.##" /> <span>VND</span></strong></c:when>
                            <c:otherwise><span class="bd-small-label">Booking total</span><strong data-highlight-amount><fmt:formatNumber value="${bookingSummary.totalAmount}" pattern="#,##0.##" /> <span>VND</span></strong></c:otherwise>
                        </c:choose>
                        <span class="bd-payment-plan">${bookingSummary.paymentOption eq 'DEPOSIT' ? '30% deposit plan' : 'Full payment plan'}</span>
                    </div>
                    <dl class="bd-payment-lines">
                        <div><dt>Booking total</dt><dd><fmt:formatNumber value="${bookingSummary.totalAmount}" pattern="#,##0.##" /> <span>VND</span></dd></div>
                        <div><dt>Total paid</dt><dd><fmt:formatNumber value="${bookingSummary.totalPaidAmount}" pattern="#,##0.##" /> <span>VND</span></dd></div>
                        <c:if test="${bookingState ne 'CANCELLED'}"><div class="bd-balance-line"><dt>Remaining balance</dt><dd><fmt:formatNumber value="${bookingSummary.remainingAmount}" pattern="#,##0.##" /> <span>VND</span></dd></div></c:if>
                        <c:if test="${bookingSummary.refundedAmount gt 0}"><div class="bd-refund-line"><dt>Refunded to wallet</dt><dd><fmt:formatNumber value="${bookingSummary.refundedAmount}" pattern="#,##0.##" /> <span>VND</span></dd></div></c:if>
                    </dl>
                    <c:if test="${bookingState eq 'PENDING_PAYMENT' and not empty coreBooking.paymentDeadline}"><div class="bd-payment-deadline"><i class="fa-regular fa-clock" aria-hidden="true"></i><div><span>Complete payment before</span><strong><fmt:formatDate value="${coreBooking.paymentDeadline}" pattern="dd MMM yyyy, HH:mm:ss" /></strong><span>Vietnam time</span></div></div></c:if>
                    <c:choose>
                        <c:when test="${bookingState eq 'PENDING_PAYMENT'}"><a href="<c:out value='${paymentUrl}' />" class="bd-button bd-button-primary" data-payment-action>Continue to payment <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a><p class="bd-action-note">Your booking is confirmed after the required payment succeeds.</p></c:when>
                        <c:when test="${canPayRemaining}"><a href="<c:out value='${paymentUrl}' />" class="bd-button bd-button-primary" data-payment-action>Pay remaining balance <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></c:when>
                        <c:when test="${paymentState eq 'REFUNDED'}"><p class="bd-payment-note"><i class="fa-solid fa-wallet" aria-hidden="true"></i> This refund is recorded in your internal wallet.</p></c:when>
                        <c:when test="${paymentState eq 'NO_REFUND'}"><p class="bd-payment-note bd-note-danger"><i class="fa-solid fa-circle-info" aria-hidden="true"></i> This booking was cancelled without a refund under the cancellation policy.</p></c:when>
                        <c:when test="${bookingState eq 'CANCELLED' and bookingSummary.totalPaidAmount eq 0}"><p class="bd-payment-note">No successful payment was recorded before cancellation.</p></c:when>
                    </c:choose>
                </section>
                <c:if test="${canCancel}">
                    <section class="bd-cancellation bd-panel" aria-labelledby="bd-cancellation-title">
                        <h2 id="bd-cancellation-title">Change of plans?</h2>
                        <c:choose>
                            <c:when test="${bookingSummary.totalPaidAmount eq 0}"><p>No successful payment has been recorded, so there is no payment to refund.</p></c:when>
                            <c:when test="${bookingSummary.canRefundNow}"><p class="bd-refund-available"><i class="fa-solid fa-wallet" aria-hidden="true"></i> Currently eligible for a refund</p><p>Refundable amount: <strong><fmt:formatNumber value="${bookingSummary.totalPaidAmount}" pattern="#,##0.##" /> VND</strong> to your internal wallet.</p></c:when>
                            <c:when test="${bookingSummary.canRefundNow ne null or empty bookingSummary.refundDeadline}"><p class="bd-note-danger">This booking is not currently eligible for a refund under the cancellation policy.</p></c:when>
                            <c:otherwise><p>Refund eligibility follows the booking cancellation policy.</p></c:otherwise>
                        </c:choose>
                        <c:if test="${not empty bookingSummary.refundDeadline and bookingSummary.totalPaidAmount gt 0}"><p class="bd-refund-cutoff">Refund cutoff <strong><fmt:formatDate value="${bookingSummary.refundDeadline}" pattern="dd MMM yyyy, HH:mm:ss" /></strong></p></c:if>
                        <p class="bd-cancel-note">Final eligibility is checked when your cancellation is submitted.</p>
                        <form action="${cancelUrl}" method="post" onsubmit="return confirm('Cancel this booking? Refund eligibility follows the booking cancellation policy.');" data-cancel-booking>
                            <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.bookingCsrfToken}' />">
                            <button type="submit" class="bd-button bd-button-cancel">Cancel booking</button>
                        </form>
                    </section>
                </c:if>
            </aside>
        </div>
    </div>
</layout:layout>
