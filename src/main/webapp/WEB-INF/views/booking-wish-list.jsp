<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="en_US" />
<layout:layout title="Booking Wish List" pageCss="booking-wish-list.css" pageJs="booking-wish-list.js" useBootstrap="true" bodyClass="wishlist-body">
    <div class="wishlist-page" data-wishlist-page>
        <header class="wl-heading">
            <div><p class="wl-eyebrow">PLAN YOUR STAY</p><h1>Booking wish list</h1><p class="wl-description">Review your rooms and choose how you would like to pay.</p></div>
            <a class="wl-text-link" href="${pageContext.request.contextPath}/my-bookings"><i class="fa-solid fa-calendar-check" aria-hidden="true"></i> My bookings</a>
        </header>
        <c:if test="${not empty successMessage}"><div class="wl-notice wl-notice-success" role="status"><i class="fa-solid fa-circle-check" aria-hidden="true"></i><span><c:out value="${successMessage}" /></span></div></c:if>
        <c:set var="checkoutError" value="${not empty errorMessage ? errorMessage : sessionScope.bookingError}" />
        <c:if test="${not empty checkoutError}"><div class="wl-notice wl-notice-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><span><c:out value="${checkoutError}" /></span></div><c:remove var="bookingError" scope="session" /></c:if>
        <c:choose>
            <c:when test="${wishList == null or empty wishList.items}">
                <section class="wl-empty" aria-labelledby="wishlistEmptyTitle">
                    <span class="wl-empty-icon"><i class="fa-solid fa-bed" aria-hidden="true"></i></span>
                    <p class="wl-eyebrow">A NEW STAY STARTS HERE</p><h2 id="wishlistEmptyTitle">Find a room you will love</h2>
                    <p>Your wish list is empty. Explore our room types and add the rooms for your next stay.</p>
                    <a class="wl-button wl-button-primary" href="${pageContext.request.contextPath}/room-types">Explore rooms <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                </section>
            </c:when>
            <c:otherwise>
                <fmt:parseDate value="${wishList.checkInDate}" pattern="yyyy-MM-dd" var="wishArrival" />
                <fmt:parseDate value="${wishList.checkOutDate}" pattern="yyyy-MM-dd" var="wishDeparture" />
                <fmt:formatNumber value="${depositAmount}" pattern="#,##0.##" var="wishDepositFormatted" />
                <fmt:formatNumber value="${wishList.totalAmount}" pattern="#,##0.##" var="wishTotalFormatted" />
                <fmt:formatNumber value="${remainingAfterDeposit}" pattern="#,##0.##" var="wishRemainingFormatted" />
                <c:url var="addMoreRoomsUrl" value="/room-types"><c:param name="checkInDate" value="${wishList.checkInDate}" /><c:param name="checkOutDate" value="${wishList.checkOutDate}" /></c:url>
                <section class="wl-stay" aria-label="Your stay dates and guests">
                    <div class="wl-stay-title"><span class="wl-stay-icon"><i class="fa-regular fa-calendar" aria-hidden="true"></i></span><div><span class="wl-stay-kicker">YOUR STAY</span><strong><c:out value="${wishList.numberOfNights}" /> <c:out value="${wishList.numberOfNights eq 1 ? 'night' : 'nights'}" /> away</strong></div></div>
                    <div class="wl-stay-date"><span>Check-in</span><time datetime="${wishList.checkInDate}"><fmt:formatDate value="${wishArrival}" pattern="dd MMM yyyy" /></time></div>
                    <span class="wl-stay-arrow"><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></span>
                    <div class="wl-stay-date"><span>Check-out</span><time datetime="${wishList.checkOutDate}"><fmt:formatDate value="${wishDeparture}" pattern="dd MMM yyyy" /></time></div>
                    <div class="wl-stay-guests"><span><i class="fa-solid fa-bed" aria-hidden="true"></i> <c:out value="${wishList.totalRooms}" /> <c:out value="${wishList.totalRooms eq 1 ? 'room' : 'rooms'}" /></span><span><i class="fa-solid fa-user-group" aria-hidden="true"></i> <c:out value="${wishList.totalGuests}" /> <c:out value="${wishList.totalGuests eq 1 ? 'guest' : 'guests'}" /></span></div>
                </section>
                <div class="wl-layout">
                    <section class="wl-rooms" aria-labelledby="wishlistRoomsTitle">
                        <header class="wl-section-heading"><div><h2 id="wishlistRoomsTitle">Your selected rooms</h2><p>A little closer to your next stay.</p></div><span class="wl-count"><c:out value="${wishList.itemCount}" /> <c:out value="${wishList.itemCount eq 1 ? 'room type' : 'room types'}" /></span></header>
                        <div class="wl-room-list">
                            <c:forEach var="item" items="${wishList.items}">
                                <article class="wl-room-card">
                                    <div class="wl-room-heading"><span class="wl-room-icon"><i class="fa-solid fa-bed" aria-hidden="true"></i></span><div><p class="wl-room-kicker">ROOM TYPE</p><h3><c:out value="${item.typeName}" /></h3></div></div>
                                    <dl class="wl-room-facts"><div><dt>Rooms</dt><dd><c:out value="${item.quantity}" /></dd></div><div><dt>Guests</dt><dd><c:out value="${item.guestCount}" /></dd></div><div><dt>Per room / night</dt><dd><fmt:formatNumber value="${item.unitPrice}" pattern="#,##0.##" /> <small>VND</small></dd></div></dl>
                                    <div class="wl-room-footer"><div><p class="wl-calculation"><c:out value="${item.quantity}" /> <c:out value="${item.quantity eq 1 ? 'room' : 'rooms'}" /> × <c:out value="${wishList.numberOfNights}" /> <c:out value="${wishList.numberOfNights eq 1 ? 'night' : 'nights'}" /></p><p class="wl-room-subtotal"><fmt:formatNumber value="${subtotals[item.roomTypeID]}" pattern="#,##0.##" /> <small>VND</small></p></div>
                                        <form action="${pageContext.request.contextPath}/booking-wish-list" method="post" data-wl-confirm="Remove this room type from your wish list?">
                                            <input type="hidden" name="action" value="remove"><input type="hidden" name="roomTypeID" value="<c:out value='${item.roomTypeID}' />">
                                            <button type="submit" class="wl-remove" aria-label="Remove <c:out value='${item.typeName}' /> from wish list"><i class="fa-regular fa-trash-can" aria-hidden="true"></i> Remove</button>
                                        </form>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                        <div class="wl-list-actions"><a class="wl-button" href="<c:out value='${addMoreRoomsUrl}' />"><i class="fa-solid fa-plus" aria-hidden="true"></i> Add more rooms</a><form action="${pageContext.request.contextPath}/booking-wish-list" method="post" data-wl-confirm="Clear all rooms and dates from your wish list?"><input type="hidden" name="action" value="clear"><button type="submit" class="wl-remove"><i class="fa-regular fa-trash-can" aria-hidden="true"></i> Clear wish list</button></form></div>
                        <p class="wl-availability-note"><i class="fa-solid fa-circle-info" aria-hidden="true"></i> Rooms in your wish list are not reserved yet. Availability and prices are checked again when you continue.</p>
                    </section>
                    <aside class="wl-summary" aria-labelledby="wishlistSummaryTitle">
                        <header class="wl-summary-heading"><p class="wl-eyebrow">ONE STEP CLOSER</p><h2 id="wishlistSummaryTitle">Booking summary</h2></header>
                        <dl class="wl-totals"><div><dt>Total rooms</dt><dd><c:out value="${wishList.totalRooms}" /></dd></div><div><dt>Total guests</dt><dd><c:out value="${wishList.totalGuests}" /></dd></div><div><dt>Length of stay</dt><dd><c:out value="${wishList.numberOfNights}" /> <c:out value="${wishList.numberOfNights eq 1 ? 'night' : 'nights'}" /></dd></div><div class="wl-booking-total"><dt>Booking total</dt><dd><c:out value="${wishTotalFormatted}" /> <small>VND</small></dd></div></dl>
                        <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="post">
                            <fieldset class="wl-payment-options"><legend>How would you like to pay?</legend>
                                <div class="wl-payment-option"><input type="radio" id="paymentDeposit" name="paymentOption" value="DEPOSIT" required data-wl-due="<c:out value='${wishDepositFormatted}' />" data-wl-remaining="<c:out value='${wishRemainingFormatted}' />"><label for="paymentDeposit"><span class="wl-option-title">Pay 30% deposit <span class="wl-percent">30%</span></span><strong><c:out value="${wishDepositFormatted}" /> <small>VND</small></strong><span class="wl-option-description">Secure your booking now. Pay the remaining balance later.</span></label></div>
                                <div class="wl-payment-option"><input type="radio" id="paymentFull" name="paymentOption" value="FULL" required data-wl-due="<c:out value='${wishTotalFormatted}' />" data-wl-remaining="0"><label for="paymentFull"><span class="wl-option-title">Pay in full <span class="wl-percent">100%</span></span><strong><c:out value="${wishTotalFormatted}" /> <small>VND</small></strong><span class="wl-option-description">Cover the full booking amount in one payment.</span></label></div>
                            </fieldset>
                            <div class="wl-due" aria-live="polite" aria-atomic="true"><div><span>Amount due now</span><strong data-wl-due-output>Choose an option</strong></div><p>Remaining after this payment <span data-wl-remaining-output>—</span></p></div>
                            <noscript><p class="wl-noscript">Each option shows its payment amount. The next page confirms the amount due.</p></noscript>
                            <button type="submit" class="wl-button wl-button-primary wl-checkout-button">Continue to payment <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></button>
                            <p class="wl-next-step"><i class="fa-regular fa-credit-card" aria-hidden="true"></i> Choose your payment method on the next step.</p>
                        </form>
                    </aside>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
