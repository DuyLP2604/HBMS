<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ attribute name="bookings" required="true" type="java.util.Collection" %>
<%@ attribute name="noShowBookings" required="false" type="java.util.Map" %>
<%@ attribute name="waitingMode" required="true" type="java.lang.Boolean" %>
<fmt:setLocale value="en_GB" scope="page" />
<fmt:setTimeZone value="Asia/Ho_Chi_Minh" scope="page" />
<c:url var="dashboardUrl" value="/staff/dashboard" />
<c:url var="staysUrl" value="/staff/stays" />
<c:url var="assignmentsUrl" value="/staff/bookings" />
<c:set var="listUrl" value="${waitingMode ? assignmentsUrl : staysUrl}" />
<div class="stay-workspace" data-stay-list data-mode="${waitingMode ? 'assignments' : 'stays'}">
    <header class="stay-page-heading">
        <div><p class="stay-eyebrow">FRONT DESK</p><h1>${waitingMode ? 'Room assignment' : 'Guest stays'}</h1><p class="stay-page-description">${waitingMode ? 'Match waiting bookings with the right rooms.' : 'Manage arrivals, in-house guests and the next step for each stay.'}</p></div>
        <div class="stay-heading-actions"><a href="${dashboardUrl}" class="stay-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Dashboard</a><a href="${waitingMode ? staysUrl : assignmentsUrl}" class="stay-button stay-button-primary"><i class="fa-solid ${waitingMode ? 'fa-clipboard-list' : 'fa-key'}" aria-hidden="true"></i> ${waitingMode ? 'Guest stays' : 'Room assignment'}</a></div>
    </header>
    <c:if test="${not empty requestScope.successMessage}"><div class="stay-notice stay-notice-success" role="status"><i class="fa-solid fa-circle-check" aria-hidden="true"></i><c:out value="${requestScope.successMessage}" /></div></c:if>
    <c:if test="${not empty requestScope.errorMessage}"><div class="stay-notice stay-notice-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><c:out value="${requestScope.errorMessage}" /></div></c:if>
    <section class="stay-list-panel" aria-labelledby="stay-list-title">
        <div class="stay-list-heading">
            <div><h2 id="stay-list-title">${waitingMode ? 'Bookings awaiting rooms' : 'Stay overview'}</h2><p><span data-hotel-date></span><span class="stay-list-subtitle">${waitingMode ? 'Review arrivals first, then assign rooms.' : 'Bookings needing review appear first.'}</span></p></div>
            <a href="${listUrl}" class="stay-refresh" data-list-refresh><i class="fa-solid fa-rotate-right" aria-hidden="true"></i> Refresh</a>
        </div>
        <div class="stay-list-toolbar">
            <div class="stay-search"><label for="stay-search" class="stay-sr-only">Search by guest name, booking reference or phone</label><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i><input type="search" id="stay-search" placeholder="Search guest, booking or phone" autocomplete="off" disabled data-list-search></div>
            <nav class="stay-filters" aria-label="${waitingMode ? 'Filter waiting bookings' : 'Filter guest stays'}">
                <button type="button" data-stay-filter="all" aria-pressed="true" disabled>${waitingMode ? 'All waiting' : 'All stays'} <span data-filter-count="all">—</span></button>
                <button type="button" data-stay-filter="arrivals" aria-pressed="false" disabled>Arriving today <span data-filter-count="arrivals">—</span></button>
                <c:if test="${not waitingMode}"><button type="button" data-stay-filter="inhouse" aria-pressed="false" disabled>In-house <span data-filter-count="inhouse">—</span></button><button type="button" data-stay-filter="assignments" aria-pressed="false" disabled>Needs rooms <span data-filter-count="assignments">—</span></button></c:if>
                <button type="button" data-stay-filter="review" aria-pressed="false" disabled>Needs review <span data-filter-count="review">—</span></button>
            </nav>
        </div>
        <noscript><p class="stay-noscript">All bookings are listed below. Enable JavaScript to use search and filters.</p></noscript>
        <c:if test="${not empty bookings}">
            <div class="stay-table-wrap" data-list-table>
                <table class="stay-table">
                    <caption class="stay-sr-only">${waitingMode ? 'Bookings awaiting room assignment' : 'Active guest stays'} with arrival dates, departure dates and available actions.</caption>
                    <thead><tr><th scope="col">Guest &amp; booking</th><th scope="col">Stay dates</th><c:if test="${waitingMode}"><th scope="col" class="stay-money-cell">Booking total</th></c:if><th scope="col">Status</th><th scope="col" class="stay-action-cell">Next step</th></tr></thead>
                    <tbody data-stay-rows>
                        <c:forEach var="booking" items="${bookings}">
                            <fmt:formatDate var="arrivalDay" value="${booking.checkInDate}" pattern="yyyy-MM-dd" />
                            <fmt:formatDate var="departureDay" value="${booking.checkOutDate}" pattern="yyyy-MM-dd" />
                            <c:url var="assignUrl" value="/staff/bookings"><c:param name="bookingID" value="${fn:trim(booking.bookingID)}" /><c:if test="${waitingMode and booking.walkIn}"><c:param name="flow" value="counter" /></c:if></c:url>
                            <c:url var="checkInUrl" value="/process-checkin"><c:param name="bookingID" value="${fn:trim(booking.bookingID)}" /></c:url>
                            <c:url var="checkOutUrl" value="/CheckoutEmployee"><c:param name="bookingID" value="${fn:trim(booking.bookingID)}" /><c:param name="action" value="detail" /></c:url>
                            <tr class="${noShowBookings[booking.bookingID] ? 'stay-needs-review' : ''}" data-stay-row data-booking="${fn:escapeXml(fn:trim(booking.bookingID))}" data-status="${fn:escapeXml(booking.bookingStatus)}" data-arrival="${arrivalDay}" data-departure="${departureDay}" data-review="${noShowBookings[booking.bookingID] ? 'true' : 'false'}" data-phone="${fn:escapeXml(booking.customerID.phone)}" data-search="${fn:escapeXml(booking.customerID.fullName)} ${fn:escapeXml(booking.bookingID)} ${fn:escapeXml(booking.customerID.phone)}">
                                <td class="stay-guest-cell"><strong class="stay-guest-name"><c:out value="${empty fn:trim(booking.customerID.fullName) ? 'Guest' : booking.customerID.fullName}" /></strong><div class="stay-guest-reference"><span><c:out value="${booking.bookingID}" /></span><c:if test="${not empty fn:trim(booking.customerID.phone)}"><span class="stay-meta-divider" aria-hidden="true">·</span><span><c:out value="${booking.customerID.phone}" /></span></c:if></div></td>
                                <td class="stay-dates-cell"><div class="stay-date-pair"><div><span>Arrival</span><time datetime="${arrivalDay}"><fmt:formatDate value="${booking.checkInDate}" pattern="dd MMM yyyy" /></time></div><i class="fa-solid fa-arrow-right" aria-hidden="true"></i><div><span>Departure</span><time datetime="${departureDay}"><fmt:formatDate value="${booking.checkOutDate}" pattern="dd MMM yyyy" /></time></div></div><span class="stay-booking-kind">${booking.walkIn ? 'Walk-in' : 'Advance booking'}</span></td>
                                <c:if test="${waitingMode}"><td class="stay-money-cell"><span class="stay-mobile-label">Booking total</span><strong><fmt:formatNumber value="${booking.totalAmount}" groupingUsed="true" maxFractionDigits="0" /></strong> <span class="stay-currency">VND</span></td></c:if>
                                <td class="stay-status-cell"><c:choose><c:when test="${booking.bookingStatus eq 'CONFIRMED'}"><span class="stay-status stay-status-rooms">Awaiting rooms</span></c:when><c:when test="${booking.bookingStatus eq 'ASSIGNED'}"><span class="stay-status stay-status-assigned">Rooms assigned</span></c:when><c:when test="${booking.bookingStatus eq 'CHECKED_IN'}"><span class="stay-status stay-status-inhouse">In-house</span></c:when><c:when test="${booking.bookingStatus eq 'CHECKED_OUT'}"><span class="stay-status stay-status-neutral">Checked out</span></c:when><c:otherwise><span class="stay-status stay-status-neutral">Review status</span></c:otherwise></c:choose><span class="stay-review-label" data-review-label ${noShowBookings[booking.bookingID] ? '' : 'hidden'}><i class="fa-regular fa-flag" aria-hidden="true"></i> Arrival needs review</span></td>
                                <td class="stay-action-cell"><div class="stay-row-actions">
                                    <c:choose><c:when test="${booking.bookingStatus eq 'CONFIRMED'}"><a href="${assignUrl}" class="stay-row-button"><i class="fa-solid fa-key" aria-hidden="true"></i> Assign rooms</a></c:when><c:when test="${booking.bookingStatus eq 'ASSIGNED'}"><a href="${checkInUrl}" class="stay-row-button"><i class="fa-solid fa-arrow-right-to-bracket" aria-hidden="true"></i> Check in</a></c:when><c:when test="${booking.bookingStatus eq 'CHECKED_IN'}"><a href="${checkOutUrl}" class="stay-row-button"><i class="fa-solid fa-arrow-right-from-bracket" aria-hidden="true"></i> Check out</a></c:when><c:otherwise><span class="stay-no-action">No action required</span></c:otherwise></c:choose>
                                    <c:if test="${noShowBookings[booking.bookingID]}">
                                        <form action="${staysUrl}" method="post" class="stay-cancel-form" data-cancel-no-show>
                                            <input type="hidden" name="bookingID" value="${fn:escapeXml(fn:trim(booking.bookingID))}">
                                            <input type="hidden" name="action" value="cancelNoShow">
                                            <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.bookingCsrfToken)}">
                                            <button type="submit" class="stay-row-button stay-row-button-danger" title="Cancel for no-show. No refund applies." aria-label="Cancel no-show for booking ${fn:escapeXml(fn:trim(booking.bookingID))}. No refund applies."><i class="fa-solid fa-ban" aria-hidden="true"></i> Cancel no-show</button>
                                        </form>
                                    </c:if>
                                </div></td>
                            </tr>
                        </c:forEach>
                    </tbody>
                </table>
            </div>
        </c:if>
        <div class="stay-empty" data-list-empty ${not empty bookings ? 'hidden' : ''}><span class="stay-empty-icon" aria-hidden="true"><i class="fa-regular fa-folder-open"></i></span><h3>${empty bookings ? (waitingMode ? 'No bookings are awaiting rooms.' : 'No active stays were found.') : 'No matching bookings.'}</h3><p>${empty bookings ? 'Refresh to load the latest bookings.' : 'Try another search or choose a different view.'}</p><c:if test="${not empty bookings}"><button type="button" class="stay-button stay-button-secondary" data-reset-list>Show all bookings</button></c:if></div>
        <footer class="stay-list-footer"><span data-list-result aria-live="polite">${fn:length(bookings)} ${fn:length(bookings) eq 1 ? 'booking' : 'bookings'}</span><span>Refresh to load the latest booking status.</span></footer>
    </section>
</div>
