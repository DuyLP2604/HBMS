<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="en_GB" scope="page" />
<fmt:setTimeZone value="Asia/Ho_Chi_Minh" scope="page" />
<c:set var="assignedRoomCount" value="0" />
<c:forEach var="detail" items="${booking.bookingDetailCollection}"><c:set var="assignedRoomCount" value="${assignedRoomCount + fn:length(detail.roomAssignmentCollection)}" /></c:forEach>
<c:url var="staysUrl" value="/staff/stays" />
<layout:layout title="Guest Check-in" pageCss="guest-workspace.css" pageJs="checkin-payment.js" bodyClass="stay-workspace-page">
    <div class="stay-workspace guest-workspace">
        <header class="stay-page-heading">
            <div><p class="stay-eyebrow">FRONT DESK / CHECK-IN</p><h1>Welcome your guest</h1><p class="stay-page-description">Verify guest documents and settle the room balance.</p></div>
            <a href="${staysUrl}" class="stay-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to guest stays</a>
        </header>
        <c:if test="${not empty errorMessage}"><div class="stay-notice stay-notice-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><c:out value="${errorMessage}" /></div></c:if>
        <noscript><div class="stay-notice stay-notice-error">Enable JavaScript to manage guest documents and confirm check-in.</div></noscript>
        <form action="${pageContext.request.contextPath}/process-checkin" method="post" id="checkInForm" class="checkin-layout" data-required-rooms="${assignedRoomCount}">
            <input type="hidden" name="action" value="confirmCheckIn">
            <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
            <input type="hidden" name="checkInToken" value="<c:out value='${checkInRequest.token}' />">
            <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
            <div class="checkin-main">
                <section class="guest-panel" aria-labelledby="checkin-booking-title">
                    <div class="guest-panel-heading"><h2 id="checkin-booking-title">This booking</h2><span class="guest-state">Rooms assigned</span></div>
                    <div class="guest-panel-body">
                        <div class="guest-identity"><span class="guest-avatar" aria-hidden="true"><i class="fa-regular fa-user"></i></span><div class="guest-identity-text"><h3 class="guest-customer-name"><c:out value="${empty fn:trim(booking.customerID.fullName) ? 'Guest' : booking.customerID.fullName}" /></h3><div class="guest-contact"><span><c:out value="${booking.bookingID}" /></span><c:if test="${not empty fn:trim(booking.customerID.phone)}"><span><i class="fa-solid fa-phone" aria-hidden="true"></i><c:out value="${booking.customerID.phone}" /></span></c:if><c:if test="${not empty fn:trim(booking.customerID.email)}"><span><i class="fa-regular fa-envelope" aria-hidden="true"></i><c:out value="${booking.customerID.email}" /></span></c:if></div></div></div>
                        <dl class="guest-facts">
                            <div><dt>Arrival</dt><dd><fmt:formatDate value="${booking.checkInDate}" pattern="dd MMM yyyy" /></dd></div>
                            <div><dt>Departure</dt><dd><fmt:formatDate value="${booking.checkOutDate}" pattern="dd MMM yyyy" /></dd></div>
                            <div><dt>Length of stay</dt><dd><fmt:formatNumber value="${(booking.checkOutDate.time - booking.checkInDate.time) / 86400000}" maxFractionDigits="0" /> nights</dd></div>
                            <div><dt>Assigned rooms</dt><dd class="guest-rooms"><c:forEach var="detail" items="${booking.bookingDetailCollection}"><c:forEach var="assignment" items="${detail.roomAssignmentCollection}"><span class="guest-room-chip"><i class="fa-solid fa-bed" aria-hidden="true"></i><c:out value="${assignment.roomID.roomNumber}" /></span></c:forEach></c:forEach></dd></div>
                        </dl>
                    </div>
                </section>
                <section class="guest-panel" aria-labelledby="checkin-documents-title">
                    <div class="guest-panel-heading"><div><h2 id="checkin-documents-title">Guest documents</h2><p class="guest-panel-description">One representative for each assigned room.</p></div><p class="guest-progress" data-document-progress aria-live="polite">0 of <c:out value="${assignedRoomCount}" /> representatives entered</p></div>
                    <div class="guest-panel-body">
                        <c:forEach var="detail" items="${booking.bookingDetailCollection}">
                            <c:forEach var="assignment" items="${detail.roomAssignmentCollection}">
                                <div class="checkin-room" data-room-number="${fn:escapeXml(assignment.roomID.roomNumber)}">
                                    <div class="guest-room-heading"><div class="guest-room-title"><span class="guest-room-icon" aria-hidden="true"><i class="fa-solid fa-bed"></i></span><div><h3>Room <c:out value="${assignment.roomID.roomNumber}" /></h3><span class="guest-room-type"><c:out value="${detail.roomTypeID.typeName}" /></span></div></div><span class="guest-room-count" data-room-guest-count>1 of 4 guests</span></div>
                                    <div class="checkin-guests"><div class="checkin-guest-row guest-document-row"><label class="guest-field"><span><span data-document-label>Representative CCCD</span> <em class="guest-required">*</em></span><input type="text" name="cccd" placeholder="Enter 12-digit CCCD" required pattern="[0-9]{12}" minlength="12" maxlength="12" inputmode="numeric" autocomplete="off" data-representative-document></label></div></div>
                                    <button type="button" class="guest-inline-action" data-add-guest><i class="fa-solid fa-plus" aria-hidden="true"></i> Add a roommate</button>
                                    <p class="guest-help">Enter each guest's identity number. Up to 4 guests per room.</p>
                                </div>
                            </c:forEach>
                        </c:forEach>
                        <c:if test="${assignedRoomCount eq 0}"><div class="stay-notice stay-notice-error">No assigned rooms were found. Return to Guest Stays and review the room assignment.</div></c:if>
                    </div>
                </section>
            </div>
            <aside class="guest-panel guest-payment-panel" aria-labelledby="checkin-payment-title">
                <div class="guest-panel-heading"><div><p class="stay-eyebrow">BEFORE CHECK-IN</p><h2 id="checkin-payment-title">Room payment</h2></div><i class="fa-solid fa-receipt" aria-hidden="true"></i></div>
                <div class="guest-panel-body">
                    <dl class="guest-payment-lines"><div><dt>Room charges</dt><dd><fmt:formatNumber value="${paymentSummary.roomTotal}" pattern="#,##0.##" /> VND</dd></div><div><dt>Already paid toward rooms</dt><dd><fmt:formatNumber value="${paymentSummary.roomPaidAmount}" pattern="#,##0.##" /> VND</dd></div></dl>
                    <div class="guest-payment-due"><span>Room balance due now</span><div class="guest-payment-amount"><fmt:formatNumber value="${checkInRequest.roomAmount}" pattern="#,##0.##" /> <small>VND</small></div></div>
                    <c:choose>
                        <c:when test="${checkInRequest.roomAmount gt 0}">
                            <label class="guest-field" for="checkInMethod"><span>Payment method <em class="guest-required">*</em></span><select name="methodID" id="checkInMethod" required><option value="">Select a method</option><c:forEach var="method" items="${paymentMethods}"><option value="<c:out value='${method.methodID}' />"><c:out value="${method.methodName}" /></option></c:forEach></select></label>
                            <label id="checkInBankReferenceGroup" class="guest-field" for="checkInBankReference" hidden><span>Verified bank transaction reference <em class="guest-required">*</em></span><input type="text" name="bankReference" id="checkInBankReference" maxlength="100" autocomplete="off" disabled><small>Confirm receipt in the bank account before recording a VietQR payment.</small></label>
                            <div id="checkInWalletAuthorization" class="guest-wallet" hidden><h3>Customer wallet authorization</h3><p class="guest-help">The booking customer must authorize their own wallet. Its balance must cover the room amount due.</p><label class="guest-field" for="walletUsername"><span>Customer username</span><input type="text" name="walletUsername" id="walletUsername" maxlength="50" autocomplete="off" disabled></label><label class="guest-field" for="walletPassword"><span>Customer password</span><input type="password" name="walletPassword" id="walletPassword" autocomplete="off" disabled></label></div>
                            <label class="guest-payment-confirm" for="collectionConfirmed"><input type="checkbox" id="collectionConfirmed" name="collectionConfirmed" value="true" required><span>I confirm payment of the outstanding room balance.</span></label>
                            <c:if test="${empty paymentMethods}"><p class="guest-field-error" role="alert">No payment methods are currently available. Reload this page or contact the administrator.</p></c:if>
                        </c:when>
                        <c:otherwise><div class="guest-paid"><i class="fa-solid fa-circle-check" aria-hidden="true"></i><span>Room charges are fully paid. No additional room payment is required.</span></div></c:otherwise>
                    </c:choose>
                    <button type="submit" class="stay-button stay-button-primary guest-confirm-button" id="confirmCheckIn"><span>${checkInRequest.roomAmount gt 0 ? 'Collect payment & check in' : 'Complete check-in'}</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></button>
                    <div class="guest-service-note"><i class="fa-solid fa-circle-info" aria-hidden="true"></i><p><c:choose><c:when test="${paymentSummary.serviceRemainingAmount gt 0}">Extra services: <strong><fmt:formatNumber value="${paymentSummary.serviceRemainingAmount}" pattern="#,##0.##" /> VND</strong><br>Payable at checkout.</c:when><c:otherwise>Only extra services, if any, will be due at checkout.</c:otherwise></c:choose></p></div>
                    <p class="guest-form-note" data-checkin-hint>Enter guest documents to complete check-in.</p>
                </div>
            </aside>
        </form>
    </div>
</layout:layout>
