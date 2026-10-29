<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="en_GB" scope="page" />
<fmt:setTimeZone value="Asia/Ho_Chi_Minh" scope="page" />
<c:url var="assignmentListUrl" value="/staff/bookings" />
<c:url var="staysUrl" value="/staff/stays" />
<c:url var="saveAssignmentUrl" value="/staff/room-assignments" />
<c:url var="refreshAssignmentUrl" value="/staff/bookings"><c:param name="bookingID" value="${fn:trim(booking.bookingID)}" /><c:if test="${param.flow eq 'counter'}"><c:param name="flow" value="counter" /></c:if></c:url>
<c:url var="assignmentBackUrl" value="${param.flow eq 'counter' ? '/staff/counter-booking' : '/staff/bookings'}"><c:if test="${param.flow eq 'counter'}"><c:param name="bookingID" value="${fn:trim(booking.bookingID)}" /></c:if></c:url>
<c:set var="requiredRoomTotal" value="0" />
<c:set var="guestTotal" value="0" />
<c:forEach var="item" items="${details}"><c:set var="requiredRoomTotal" value="${requiredRoomTotal + item.quantity}" /><c:set var="guestTotal" value="${guestTotal + item.guestCount}" /></c:forEach>
<layout:layout title="Assign Rooms" pageCss="stay-workspace.css" pageJs="room-assignment.js" bodyClass="stay-workspace-page">
    <div class="stay-workspace">
        <header class="stay-page-heading"><div><p class="stay-eyebrow">ROOM ASSIGNMENT</p><h1>Choose rooms for this stay</h1><p class="stay-page-description">Select the requested number of rooms for each room type.</p></div><a href="${assignmentBackUrl}" class="stay-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> ${param.flow eq 'counter' ? 'Back to counter booking' : 'Back to waiting bookings'}</a></header>
        <c:if test="${not empty errorMessage}"><div class="stay-notice stay-notice-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><c:out value="${errorMessage}" /></div></c:if>
        <c:choose>
            <c:when test="${booking.bookingStatus ne 'CONFIRMED' or empty details}">
                <section class="stay-closed-panel"><span class="stay-empty-icon" aria-hidden="true"><i class="fa-solid fa-key"></i></span><h2>This booking is not awaiting room assignment.</h2><p>Return to the stay overview to review its current status.</p><a href="${staysUrl}" class="stay-button stay-button-primary">View guest stays <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></section>
            </c:when>
            <c:otherwise>
                <form action="${saveAssignmentUrl}" method="post" class="room-assignment-layout" data-room-assignment>
                    <input type="hidden" name="bookingID" value="${fn:escapeXml(fn:trim(booking.bookingID))}">
                    <input type="hidden" name="flow" value="${param.flow eq 'counter' ? 'counter' : ''}">
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.roomAssignmentCsrf)}">
                    <div class="room-assignment-main">
                        <div class="room-assignment-intro"><div><h2>Available rooms</h2><p>Rooms matching this booking's dates and requested types.</p></div><a href="${refreshAssignmentUrl}" class="stay-refresh" data-refresh-rooms><i class="fa-solid fa-rotate-right" aria-hidden="true"></i> Refresh availability</a></div>
                        <noscript><div class="stay-notice stay-notice-error">Enable JavaScript to select rooms and confirm the assignment.</div></noscript>
                        <div class="stay-notice stay-notice-error" role="alert" data-assignment-error hidden></div>
                        <c:forEach var="detail" items="${details}">
                            <fieldset class="room-type-panel" data-room-group data-detail-id="${detail.bookingDetailID}" data-required="${detail.quantity}" data-room-type="${fn:escapeXml(detail.roomTypeID.typeName)}" aria-describedby="room-help-${detail.bookingDetailID}">
                                <legend><c:out value="${detail.roomTypeID.typeName}" /></legend>
                                <div class="room-type-overview"><div><span><i class="fa-solid fa-bed" aria-hidden="true"></i> <c:out value="${detail.quantity}" /> ${detail.quantity eq 1 ? 'room requested' : 'rooms requested'}</span><span><i class="fa-solid fa-user-group" aria-hidden="true"></i> <c:out value="${detail.guestCount}" /> ${detail.guestCount eq 1 ? 'guest' : 'guests'}</span></div><span class="room-selection-count" data-group-count aria-live="polite">Selected 0 of <c:out value="${detail.quantity}" /></span></div>
                                <p class="room-type-help" id="room-help-${detail.bookingDetailID}">Select exactly <c:out value="${detail.quantity}" /> ${detail.quantity eq 1 ? 'room' : 'rooms'}. <span>${fn:length(availableRoomMap[detail.bookingDetailID])} available for this stay.</span></p>
                                <c:choose>
                                    <c:when test="${empty availableRoomMap[detail.bookingDetailID]}"><div class="room-shortage" role="status"><i class="fa-solid fa-circle-info" aria-hidden="true"></i><div><strong>No matching rooms are available.</strong><p>Refresh availability or return to the waiting list to review this booking.</p></div></div></c:when>
                                    <c:otherwise>
                                        <div class="room-choice-grid">
                                            <c:forEach var="room" items="${availableRoomMap[detail.bookingDetailID]}">
                                                <label class="room-choice"><input type="checkbox" name="rooms_${detail.bookingDetailID}" value="${fn:escapeXml(room.roomID)}" data-room-number="${fn:escapeXml(room.roomNumber)}" aria-label="Room ${fn:escapeXml(room.roomNumber)}, ${fn:escapeXml(detail.roomTypeID.typeName)}"><span class="room-choice-body"><i class="fa-solid fa-bed" aria-hidden="true"></i><strong>Room <c:out value="${room.roomNumber}" /></strong><span data-room-state>Available</span></span></label>
                                            </c:forEach>
                                        </div>
                                        <c:if test="${fn:length(availableRoomMap[detail.bookingDetailID]) lt detail.quantity}"><div class="room-shortage" role="status"><i class="fa-solid fa-circle-info" aria-hidden="true"></i><p>Only ${fn:length(availableRoomMap[detail.bookingDetailID])} ${fn:length(availableRoomMap[detail.bookingDetailID]) eq 1 ? 'room is' : 'rooms are'} currently available. This request needs <c:out value="${detail.quantity}" />.</p></div></c:if>
                                    </c:otherwise>
                                </c:choose>
                            </fieldset>
                        </c:forEach>
                    </div>
                    <aside class="room-booking-summary" aria-labelledby="room-summary-title">
                        <div class="room-summary-heading"><p class="stay-eyebrow">THIS STAY</p><h2 id="room-summary-title">Booking summary</h2><span class="room-booking-reference"><c:out value="${booking.bookingID}" /></span></div>
                        <div class="room-summary-body"><h3 class="room-summary-guest"><c:out value="${empty fn:trim(booking.customerID.fullName) ? 'Guest' : booking.customerID.fullName}" /></h3><c:if test="${not empty fn:trim(booking.customerID.phone)}"><p class="room-summary-phone"><i class="fa-solid fa-phone" aria-hidden="true"></i> <c:out value="${booking.customerID.phone}" /></p></c:if>
                            <dl class="room-summary-dates"><div><dt>Arrival</dt><dd><fmt:formatDate value="${booking.checkInDate}" pattern="dd MMM yyyy" /></dd></div><div><dt>Departure</dt><dd><fmt:formatDate value="${booking.checkOutDate}" pattern="dd MMM yyyy" /></dd></div><div><dt>Rooms requested</dt><dd><c:out value="${requiredRoomTotal}" /></dd></div><div><dt>Guests</dt><dd><c:out value="${guestTotal}" /></dd></div></dl>
                            <div class="room-summary-selections"><h3>Selected rooms</h3><c:forEach var="detail" items="${details}"><div class="room-summary-type"><strong><c:out value="${detail.roomTypeID.typeName}" /></strong><span data-selected-summary="${detail.bookingDetailID}">Not selected</span></div></c:forEach></div>
                            <div class="room-summary-progress"><span data-total-selection aria-live="polite">0 of <c:out value="${requiredRoomTotal}" /> rooms selected</span><progress value="0" max="${requiredRoomTotal}" aria-label="Room selection progress" data-selection-progress></progress></div>
                            <button type="submit" class="stay-button stay-button-primary room-confirm-button" disabled data-assignment-submit>Confirm assignment <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></button>
                            <p class="room-summary-note"><i class="fa-solid fa-shield-halved" aria-hidden="true"></i> Room availability is checked again when you confirm.</p>
                        </div>
                    </aside>
                </form>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
