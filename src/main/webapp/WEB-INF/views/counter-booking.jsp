<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Lets reception select rooms, create a booking, and collect the initial payment.
--%>
<layout:layout title="Book for Customer" useBootstrap="true" pageCss="shared-ui.css" pageJs="checkin-payment.js" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell">
        <header class="ui-page-heading">
            <div><p class="ui-eyebrow">FRONT DESK</p><h1>Book for Customer</h1><p class="ui-description">Find customer → Book → Pay → Assign rooms → Check in</p></div>
            <a class="ui-button ui-button-secondary" href="${pageContext.request.contextPath}/staff/dashboard"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to Dashboard</a>
        </header>
        <c:if test="${not empty message}"><div class="ui-notice ui-notice-info" role="alert"><i class="fa-solid fa-circle-info" aria-hidden="true"></i><span><c:out value="${message}"/></span></div></c:if>
        <c:if test="${not empty error}"><div class="ui-notice ui-notice-danger" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><span><c:out value="${error}"/></span></div></c:if>
        <c:choose>
            <c:when test="${not empty booking}">
                <div class="ui-counter-payment">
                    <section class="ui-panel" aria-label="Booking information">
                        <div class="ui-panel-body">
                            <div class="ui-section-heading"><h2>Booking <c:out value="${booking.bookingID}"/></h2></div>
                            <div class="ui-booking-customer"><span class="ui-avatar" aria-hidden="true"><i class="fa-regular fa-user"></i></span><div><h3><c:out value="${booking.customerID.fullName}"/></h3><p class="ui-muted"><c:out value="${booking.customerID.phone}"/></p></div></div>
                            <dl class="ui-facts">
                                <div><dt>Check-in date</dt><dd><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy"/></dd></div>
                                <div><dt>Check-out date</dt><dd><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy"/></dd></div>
                                <div class="ui-field-full"><dt>Booking status</dt><dd><span class="ui-status"><c:out value="${booking.bookingStatus}"/></span></dd></div>
                            </dl>
                            <dl class="ui-money-lines">
                                <div><dt>Total</dt><dd><fmt:formatNumber value="${booking.totalAmount}"/> VND</dd></div>
                                <div><dt>Paid</dt><dd><fmt:formatNumber value="${summary.totalPaidAmount}"/> VND</dd></div>
                            </dl>
                            <c:choose>
                                <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">
                                    <div class="ui-result-action"><a class="ui-button ui-button-primary" href="${pageContext.request.contextPath}/staff/bookings?bookingID=${fn:escapeXml(booking.bookingID)}&amp;flow=counter">Assign Room Now <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></div>
                                </c:when>
                                <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">
                                    <c:choose>
                                        <c:when test="${canCheckIn}">
                                            <div class="ui-result-action"><a class="ui-button ui-button-primary" href="${pageContext.request.contextPath}/process-checkin?bookingID=${fn:escapeXml(booking.bookingID)}">Check In Now <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></div>
                                            <p class="ui-help">Verify guest identity documents in the next step. Check-in requires all assigned rooms to be ready.</p>
                                        </c:when>
                                        <c:otherwise><div class="ui-notice ui-notice-warning">Check-in is currently unavailable for this booking.</div></c:otherwise>
                                    </c:choose>
                                </c:when>
                                <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}"><div class="ui-notice ui-notice-success">Guest checked in.</div></c:when>
                            </c:choose>
                        </div>
                    </section>
                    <c:if test="${not empty payment}">
                        <section class="ui-panel" aria-label="Collect payment at counter">
                            <div class="ui-panel-body">
                                <div class="ui-section-heading"><h2>Collect payment at counter</h2></div>
                                <div class="ui-payment-amount"><span><c:out value="${payment.paymentType}"/></span><strong><fmt:formatNumber value="${payment.amount}"/> <small>VND</small></strong></div>
                                <div class="ui-notice ui-notice-warning"><i class="fa-regular fa-clock" aria-hidden="true"></i><span>Pay before <strong><fmt:formatDate value="${booking.paymentDeadline}" pattern="dd/MM/yyyy HH:mm:ss"/></strong>. Unpaid bookings expire after 15 minutes.</span></div>
                                <form method="post" action="${pageContext.request.contextPath}/staff/counter-booking"
                                      id="checkInForm" data-initial-counter-payment="true">
                                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.counterCsrf)}">
                                    <input type="hidden" name="action" value="pay">
                                    <input type="hidden" name="bookingID" value="${fn:escapeXml(booking.bookingID)}">
                                    <input type="hidden" name="paymentToken" value="${fn:escapeXml(paymentToken)}">
                                    <div class="ui-field">
                                        <label for="checkInMethod">Payment method</label>
                                        <select id="checkInMethod" name="methodID" required>
                                            <option value="PT03">Cash</option>
                                            <option value="PT01">VietQR via PayOS</option>
                                        </select>
                                    </div>
                                    <section id="checkInPayOs" class="ui-counter-qr" aria-label="VietQR via PayOS" hidden>
                                        <input type="hidden" id="checkInPayOsOrderCode">
                                        <p class="ui-help">Scan to pay the amount due. The booking updates when PayOS confirms the transfer.</p>
                                        <button type="button" class="ui-button ui-button-secondary" id="checkInPayOsCreate">Generate VietQR</button>
                                        <img id="checkInPayOsImage" width="240" height="240" alt="VietQR for this booking" hidden>
                                        <a id="checkInPayOsCheckout" class="ui-button ui-button-secondary" target="_blank" rel="noopener noreferrer" hidden>Open PayOS</a>
                                        <p id="checkInPayOsStatus" class="ui-help" role="status"></p>
                                    </section>
                                    <div class="ui-checkbox-field" id="checkInCollectionGroup">
                                        <input type="checkbox" id="collectionConfirmed" name="collectionConfirmed" value="true" required>
                                        <label for="collectionConfirmed">I confirm the cash was received.</label>
                                    </div>
                                    <button class="ui-button ui-button-primary ui-button-wide" type="submit" id="confirmCheckIn">
                                        <i class="fa-solid fa-receipt" aria-hidden="true"></i> Record Payment
                                    </button>
                                </form>
                            </div>
                        </section>
                    </c:if>
                </div>
                <div class="ui-after-panel"><a class="ui-button ui-button-secondary" href="${pageContext.request.contextPath}/staff/counter-booking">Book for another customer</a></div>
            </c:when>
            <c:when test="${not empty selectedCustomer}">
                <script src="${pageContext.request.contextPath}/js/counter-booking-payment-option.js" defer></script>
                <script src="${pageContext.request.contextPath}/js/counter-booking-availability.js" defer></script>
                <div class="ui-counter-create">
                    <section class="ui-panel" aria-label="Booking information">
                        <form method="post" action="${pageContext.request.contextPath}/staff/counter-booking">
                            <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.counterCsrf)}">
                            <input type="hidden" name="createToken" value="${fn:escapeXml(sessionScope.counterCreateToken)}">
                            <input type="hidden" name="action" value="create"><input type="hidden" name="customerID" value="${fn:escapeXml(selectedCustomer.customerID)}">
                            <div class="ui-panel-body">
                                <div class="ui-section-heading"><h2>Booking information</h2></div>
                                <div class="ui-form-grid">
                                    <div class="ui-field"><label for="checkInDate">Check-in date</label><input type="date" id="checkInDate" name="checkInDate" min="${today}" value="${today}" required></div>
                                    <div class="ui-field"><label for="checkOutDate">Check-out date</label><input type="date" id="checkOutDate" name="checkOutDate" min="${tomorrow}" value="${tomorrow}" required></div>
                                </div>
                                <p class="ui-help">Counter bookings can start today. Check-in follows payment and room assignment.
                                    <span id="counterRoomAvailability" role="status">Availability is checked when you submit.</span>
                                </p>
                                <div class="ui-table-scroll" role="region" aria-label="Room type selection">
                                    <table class="ui-table ui-booking-table" data-room-availability-url="${pageContext.request.contextPath}/staff/counter-booking">
                                        <thead><tr><th scope="col">Room type</th><th scope="col">Price / night</th><th scope="col">Capacity / room</th><th scope="col">Available rooms</th><th scope="col">Rooms</th><th scope="col">Guests</th></tr></thead>
                                        <tbody>
                                            <c:forEach items="${roomTypes}" var="rt">
                                                <tr data-room-type="${fn:escapeXml(fn:trim(rt.roomTypeID))}">
                                                    <td class="ui-name"><c:out value="${rt.typeName}"/><input type="hidden" name="roomTypeID" value="${fn:escapeXml(rt.roomTypeID)}"></td>
                                                    <td class="ui-price"><fmt:formatNumber value="${rt.price}"/> VND</td><td><c:out value="${rt.capacity}"/></td>
                                                    <td data-available-rooms aria-describedby="counterRoomAvailability">—</td>
                                                    <td><input type="number" name="quantity" min="0" max="999" value="0" required class="ui-quantity" aria-label="Room quantity for ${fn:escapeXml(rt.typeName)}"></td>
                                                    <td><input type="number" name="guestCount" min="0" max="9999" value="0" required class="ui-quantity" aria-label="Guest count for ${fn:escapeXml(rt.typeName)}"></td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                    </table>
                                </div>
                                <div class="ui-field ui-field-spacing">
                                    <label for="paymentOption">Payment option</label>
                                    <select id="paymentOption" name="paymentOption" aria-describedby="counterPaymentHelp" required>
                                        <option value="FULL">Full payment (100%)</option>
                                        <option id="counterDepositOption" value="DEPOSIT" hidden disabled>30% deposit</option>
                                    </select>
                                    <small id="counterPaymentHelp" class="ui-help">Check-in today requires full payment. A 30% deposit is available for tomorrow or later.</small>
                                </div>
                            </div>
                            <div class="ui-panel-footer ui-booking-actions">
                                <button class="ui-button ui-button-primary">Create Booking &amp; Continue to Payment <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></button>
                                <a class="ui-button ui-button-secondary" href="${pageContext.request.contextPath}/staff/counter-booking">Choose another customer</a>
                            </div>
                        </form>
                    </section>
                    <aside class="ui-panel ui-selected-customer" aria-label="Selected customer">
                        <div class="ui-panel-body">
                            <span class="ui-avatar" aria-hidden="true"><i class="fa-regular fa-user"></i></span>
                            <h2>Book for <c:out value="${selectedCustomer.fullName}"/></h2>
                            <span class="ui-id"><c:out value="${selectedCustomer.customerID}"/></span>
                            <dl class="ui-contact-list"><div><dt>Phone Number</dt><dd><c:out value="${selectedCustomer.phone}"/></dd></div><div><dt>Email</dt><dd><c:out value="${selectedCustomer.email}"/></dd></div></dl>
                        </div>
                    </aside>
                </div>
            </c:when>
            <c:otherwise>
                <section class="ui-panel ui-counter-search" aria-label="Find customer by phone">
                    <div class="ui-panel-body">
                        <div class="ui-section-heading"><h2>Find customer by phone</h2></div>
                        <form method="get" class="ui-search-form" action="${pageContext.request.contextPath}/staff/counter-booking">
                            <div class="ui-field ui-search-field"><label for="counterPhone">Customer phone number</label><input id="counterPhone" type="tel" name="phone" value="${fn:escapeXml(param.phone)}" required maxlength="15" minlength="8" aria-label="Customer phone number" placeholder="Enter phone number"></div>
                            <button class="ui-button ui-button-primary"><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i> Search</button>
                        </form>
                        <c:if test="${searched}">
                            <c:choose>
                                <c:when test="${not empty matches}">
                                    <div class="ui-table-scroll ui-search-results" role="region" aria-label="Matching customers">
                                        <table class="ui-table ui-customer-matches"><thead><tr><th scope="col">Name</th><th scope="col">Phone</th><th scope="col">Email</th><th scope="col">Action</th></tr></thead><tbody>
                                            <c:forEach items="${matches}" var="customer"><tr><td class="ui-name"><c:out value="${customer.fullName}"/></td><td><c:out value="${customer.phone}"/></td><td><c:out value="${customer.email}"/></td><td><a class="ui-button ui-button-soft ui-button-small" href="${pageContext.request.contextPath}/staff/counter-booking?customerID=${fn:escapeXml(customer.customerID)}">Book for this customer</a></td></tr></c:forEach>
                                        </tbody></table>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="ui-search-results ui-empty-search"><i class="fa-regular fa-address-book" aria-hidden="true"></i><p>No customer found. Register their information and login account first.</p>
                                        <c:url var="registerUrl" value="/customer"><c:param name="action" value="add"/><c:param name="flow" value="counter"/><c:param name="phone" value="${param.phone}"/></c:url>
                                        <a class="ui-button ui-button-primary" href="${fn:escapeXml(registerUrl)}">Register New Customer</a>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </c:if>
                    </div>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
