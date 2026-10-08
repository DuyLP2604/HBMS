<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="My Bookings" useBootstrap="true" pageCss="my-bookings.css" pageJs="my-bookings.js" bodyClass="my-bookings-page">
    <div class="container my-bookings">
        <header class="bookings-heading">
            <div>
                <p class="bookings-eyebrow"><span></span> YOUR HOTEL EXPERIENCE</p>
                <h1>My Bookings</h1>
                <p class="bookings-subtitle">Your stays, payments and booking details, all in one place.</p>
            </div>
            <div class="bookings-heading-actions">
                <a href="${pageContext.request.contextPath}/wallet" class="booking-btn booking-btn-wallet"><i class="fa-solid fa-wallet" aria-hidden="true"></i> My Wallet</a>
                <c:choose>
                    <c:when test="${canCreateBooking}"><a href="${pageContext.request.contextPath}/room-types" class="booking-btn booking-btn-gold"><i class="fa-solid fa-plus" aria-hidden="true"></i> Book Another Stay</a></c:when>
                    <c:otherwise><button type="button" class="booking-btn booking-btn-gold" disabled><i class="fa-solid fa-plus" aria-hidden="true"></i> Book Another Stay</button></c:otherwise>
                </c:choose>
            </div>
        </header>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success alert-dismissible fade show bookings-notice" role="alert"><c:out value="${successMessage}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger alert-dismissible fade show bookings-notice" role="alert"><c:out value="${errorMessage}" /><button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button></div>
        </c:if>
        <c:if test="${not empty bookingAccess.bookingLockedUntil}">
            <div class="alert alert-warning bookings-notice" role="alert"><i class="fa-solid fa-clock me-2" aria-hidden="true"></i>Creating new bookings is temporarily locked until <strong><fmt:formatDate value="${bookingAccess.bookingLockedUntil}" pattern="dd/MM/yyyy HH:mm:ss" /></strong>. You can still pay or cancel an eligible existing booking.</div>
        </c:if>
        <c:if test="${not empty bookingAccess.pendingBookingID}">
            <c:url var="pendingPaymentUrl" value="/payment"><c:param name="bookingID" value="${bookingAccess.pendingBookingID}" /></c:url>
            <div class="booking-pending-notice" role="status">
                <span class="booking-notice-icon"><i class="fa-solid fa-hourglass-half" aria-hidden="true"></i></span>
                <div class="booking-notice-copy"><strong>Complete your booking payment</strong><p>Booking <b><c:out value="${bookingAccess.pendingBookingID}" /></b> is awaiting its initial payment. Pay the deposit/full amount or cancel it before creating another booking.</p></div>
                <a href="<c:out value='${pendingPaymentUrl}' />" class="booking-btn booking-btn-dark">Continue to Payment <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
            </div>
        </c:if>

        <section class="bookings-panel" aria-labelledby="bookingHistoryTitle">
            <div class="bookings-panel-heading">
                <div><h2 id="bookingHistoryTitle">Booking History</h2><p>Review your reservations and payment activity.</p></div>
                <span class="bookings-currency"><i class="fa-solid fa-coins" aria-hidden="true"></i> Amounts in VND</span>
            </div>
            <c:choose>
                <c:when test="${empty bookings}">
                    <div class="bookings-empty"><span class="bookings-empty-icon"><i class="fa-solid fa-bed" aria-hidden="true"></i></span><h3>Your next stay starts here</h3><p>You do not have any bookings yet.</p><c:if test="${canCreateBooking}"><a href="${pageContext.request.contextPath}/room-types" class="booking-btn booking-btn-gold">Explore Rooms <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></c:if></div>
                </c:when>
                <c:otherwise>
                    <div class="bookings-table-wrap">
                        <table class="bookings-table">
                            <caption class="visually-hidden">Your booking dates, status, payments and available actions. All monetary amounts are in VND.</caption>
                            <thead><tr><th scope="col">Booking</th><th scope="col">Your Stay</th><th scope="col" class="booking-money-col">Total</th><th scope="col">Payment</th><th scope="col" class="booking-money-col">Remaining</th><th scope="col">Actions</th></tr></thead>
                            <tbody>
                                <c:forEach var="booking" items="${bookings}">
                                    <c:set var="summary" value="${paymentSummaries[booking.bookingID]}" />
                                    <c:if test="${not empty summary}">
                                        <c:set var="bookingState" value="${summary.bookingStatus}" />
                                        <c:url var="detailUrl" value="/my-bookings"><c:param name="bookingID" value="${booking.bookingID}" /></c:url>
                                        <c:url var="paymentUrl" value="/payment"><c:param name="bookingID" value="${booking.bookingID}" /></c:url>
                                        <tr>
                                            <td data-label="Booking" class="booking-reference-cell">
                                                <a href="<c:out value='${detailUrl}' />" class="booking-reference">#<c:out value="${booking.bookingID}" /></a>
                                                <c:choose>
                                                    <c:when test="${bookingState eq 'CANCELLED'}"><span class="booking-badge booking-badge-red">Cancelled</span></c:when>
                                                    <c:when test="${bookingState eq 'PENDING_PAYMENT'}"><span class="booking-badge booking-badge-amber">Pending Payment</span></c:when>
                                                    <c:when test="${bookingState eq 'CONFIRMED'}"><span class="booking-badge booking-badge-blue">Confirmed</span></c:when>
                                                    <c:when test="${bookingState eq 'ASSIGNED'}"><span class="booking-badge booking-badge-indigo">Assigned</span></c:when>
                                                    <c:when test="${bookingState eq 'CHECKED_IN'}"><span class="booking-badge booking-badge-green">Checked In</span></c:when>
                                                    <c:when test="${bookingState eq 'CHECKED_OUT'}"><span class="booking-badge booking-badge-gray">Checked Out</span></c:when>
                                                    <c:otherwise><span class="booking-badge booking-badge-gray"><c:out value="${bookingState}" /></span></c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td data-label="Your Stay" class="booking-stay-cell">
                                                <div class="booking-dates"><div><span class="booking-date-label">Check-in</span><span><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></span></div><i class="fa-solid fa-arrow-right" aria-hidden="true"></i><div><span class="booking-date-label">Check-out</span><span><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></span></div></div>
                                                <span class="booking-nights"><i class="fa-regular fa-moon" aria-hidden="true"></i> <c:out value="${booking.numberOfNights}" /> <c:out value="${booking.numberOfNights eq 1 ? 'night' : 'nights'}" /></span>
                                            </td>
                                            <td data-label="Total" class="booking-money-col"><span class="booking-money"><fmt:formatNumber value="${summary.totalAmount}" pattern="#,##0.##" /></span></td>
                                            <td data-label="Payment" class="booking-payment-cell">
                                                <c:choose>
                                                    <c:when test="${summary.paymentStatus eq 'FULLY_PAID'}"><span class="booking-badge booking-badge-green">Fully Paid</span></c:when>
                                                    <c:when test="${summary.paymentStatus eq 'DEPOSIT_PAID'}"><span class="booking-badge booking-badge-blue">Partially Paid</span></c:when>
                                                    <c:when test="${summary.paymentStatus eq 'REFUNDED'}"><span class="booking-badge booking-badge-indigo">Refunded</span></c:when>
                                                    <c:when test="${summary.paymentStatus eq 'NO_REFUND'}"><span class="booking-badge booking-badge-red">No Refund</span></c:when>
                                                    <c:when test="${summary.paymentStatus eq 'UNPAID'}"><span class="booking-badge booking-badge-amber">Unpaid</span></c:when>
                                                    <c:otherwise><span class="booking-badge booking-badge-gray"><c:out value="${summary.paymentStatus}" /></span></c:otherwise>
                                                </c:choose>
                                                <small class="booking-payment-note">Paid: <fmt:formatNumber value="${summary.totalPaidAmount}" pattern="#,##0.##" /></small>
                                                <c:if test="${summary.refundedAmount gt 0}"><small class="booking-refund-note"><i class="fa-solid fa-wallet" aria-hidden="true"></i> Refund: <fmt:formatNumber value="${summary.refundedAmount}" pattern="#,##0.##" /></small></c:if>
                                            </td>
                                            <td data-label="Remaining" class="booking-money-col"><span class="booking-money ${summary.remainingAmount gt 0 ? 'booking-money-due' : 'booking-money-zero'}"><fmt:formatNumber value="${summary.remainingAmount}" pattern="#,##0.##" /></span></td>
                                            <td data-label="Actions" class="booking-actions-cell">
                                                <div class="booking-row-actions">
                                                    <a href="<c:out value='${detailUrl}' />" class="booking-btn booking-btn-detail">Details <i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i></a>
                                                    <c:if test="${bookingState eq 'PENDING_PAYMENT'}"><a href="<c:out value='${paymentUrl}' />" class="booking-btn booking-btn-dark">Pay <c:out value="${summary.paymentOption eq 'DEPOSIT' ? 'Deposit' : 'Full Amount'}" /></a></c:if>
                                                    <c:if test="${not empty summary.firstPaidAt and summary.remainingAmount gt 0 and (bookingState eq 'CONFIRMED' or bookingState eq 'ASSIGNED' or bookingState eq 'CHECKED_IN')}"><a href="<c:out value='${paymentUrl}' />" class="booking-btn booking-btn-dark">Pay Remaining Balance</a></c:if>
                                                    <c:if test="${bookingState eq 'PENDING_PAYMENT' or bookingState eq 'CONFIRMED' or bookingState eq 'ASSIGNED'}">
                                                        <form action="${pageContext.request.contextPath}/my-bookings/cancel" method="post" class="booking-cancel-form" onsubmit="return confirm('Cancel this booking? Refund eligibility follows the 24-hour payment policy.');">
                                                            <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
                                                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.bookingCsrfToken}' />">
                                                            <button type="submit" class="booking-btn booking-btn-cancel">Cancel</button>
                                                        </form>
                                                    </c:if>
                                                </div>
                                                <c:if test="${bookingState eq 'PENDING_PAYMENT'}"><small class="booking-expiry"><i class="fa-regular fa-clock" aria-hidden="true"></i> Expires: <span class="payment-countdown" data-deadline="<c:out value='${booking.paymentDeadline.time}' />"></span></small></c:if>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </c:otherwise>
            </c:choose>
            <div class="bookings-panel-footer"><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Open a booking to view its full details and payment history.</div>
        </section>
    </div>
</layout:layout>
