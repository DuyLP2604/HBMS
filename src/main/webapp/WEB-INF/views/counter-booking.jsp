<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Book for Customer" useBootstrap="true" pageCss="staff-dashboard.css" bodyClass="bg-light">
<div class="container py-5">
    <div class="d-flex justify-content-between align-items-center mb-4"><div><h1>Book for Customer</h1>
        <p class="text-muted">Find customer → Book → Pay → Assign rooms → Check in</p></div>
        <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/dashboard">Back to Dashboard</a></div>
    <c:if test="${not empty message}"><div class="alert alert-info" role="alert"><c:out value="${message}"/></div></c:if>
    <c:if test="${not empty error}"><div class="alert alert-danger" role="alert"><c:out value="${error}"/></div></c:if>
    <c:choose>
        <c:when test="${not empty booking}">
            <div class="card shadow-sm mb-4"><div class="card-body p-4">
                <h2 class="h4">Booking <c:out value="${booking.bookingID}"/></h2>
                <p><strong><c:out value="${booking.customerID.fullName}"/></strong> · <c:out value="${booking.customerID.phone}"/></p>
                <p><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy"/> → <fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy"/>
                    · <span class="badge bg-primary"><c:out value="${booking.bookingStatus}"/></span></p>
                <p>Total: <strong><fmt:formatNumber value="${booking.totalAmount}"/> VND</strong> · Paid: <fmt:formatNumber value="${summary.totalPaidAmount}"/> VND</p>
                <c:choose>
                    <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">
                        <a class="btn btn-primary" href="${pageContext.request.contextPath}/staff/bookings?bookingID=${fn:escapeXml(booking.bookingID)}&amp;flow=counter">Assign Room Now</a>
                    </c:when>
                    <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">
                        <c:choose><c:when test="${canCheckIn}">
                            <a class="btn btn-success" href="${pageContext.request.contextPath}/process-checkin?bookingID=${fn:escapeXml(booking.bookingID)}">Check In Now</a>
                            <p class="text-muted mt-2">Verify guest identity documents in the next step. Check-in requires all assigned rooms to be ready.</p>
                        </c:when><c:otherwise><div class="alert alert-warning">Check-in opens on the arrival date at 14:00 Vietnam time. Early check-in is not enabled.</div></c:otherwise></c:choose>
                    </c:when>
                    <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}"><div class="alert alert-success">Guest checked in.</div></c:when>
                </c:choose>
            </div></div>
            <c:if test="${not empty payment}">
            <div class="card shadow-sm"><div class="card-body p-4"><h2 class="h4">Collect payment at counter</h2>
                <p><c:out value="${payment.paymentType}"/>: <strong><fmt:formatNumber value="${payment.amount}"/> VND</strong>
                    · Wallet balance: <fmt:formatNumber value="${payment.walletBalance}"/> VND</p>
                <p class="text-muted">Pay before <fmt:formatDate value="${booking.paymentDeadline}" pattern="dd/MM/yyyy HH:mm:ss"/>. Unpaid bookings expire after 15 minutes.</p>
                <form method="post" action="${pageContext.request.contextPath}/staff/counter-booking">
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.counterCsrf)}">
                    <input type="hidden" name="action" value="pay">
                    <input type="hidden" name="bookingID" value="${fn:escapeXml(booking.bookingID)}">
                    <input type="hidden" name="paymentToken" value="${fn:escapeXml(paymentToken)}">
                    <label class="form-label" for="methodID">Payment method</label>
                    <select id="methodID" name="methodID" class="form-select mb-3" required>
                        <option value="PT01">Cash</option><option value="PT10">VietQR — manually verified bank transfer</option>
                        <option value="PT09">Internal wallet</option>
                    </select>
                    <label class="form-label" for="bankReference">Bank transaction reference (required for VietQR)</label>
                    <input id="bankReference" name="bankReference" maxlength="100" class="form-control mb-3" autocomplete="off">
                    <p class="small text-muted">For VietQR, let the customer scan the hotel's bank QR and verify the incoming transfer in the bank account before recording payment.</p>
                    <div class="form-check mb-3"><input class="form-check-input" id="useWallet" type="checkbox" name="useWallet" value="true">
                        <label class="form-check-label" for="useWallet">Use wallet first and pay the shortfall with the selected method</label></div>
                    <fieldset class="border rounded p-3 mb-3"><legend class="float-none w-auto fs-6 px-2">Customer authorization (required only for wallet use)</legend>
                        <p class="small text-muted">The customer enters their own credentials. The receptionist stays signed in.</p>
                        <label class="form-label" for="walletUsername">Customer username</label>
                        <input class="form-control mb-2" id="walletUsername" name="walletUsername" maxlength="50" autocomplete="off">
                        <label class="form-label" for="walletPassword">Customer password</label>
                        <input class="form-control" type="password" id="walletPassword" name="walletPassword" autocomplete="off">
                    </fieldset>
                    <div class="form-check mb-3"><input type="checkbox" id="collectionConfirmed" name="collectionConfirmed" value="true" required class="form-check-input">
                        <label for="collectionConfirmed" class="form-check-label">I confirm the cash / bank transfer was received, or the customer authorized wallet payment.</label></div>
                    <button class="btn btn-primary" type="submit">Record Payment</button>
                </form>
            </div></div>
            </c:if>
            <a class="btn btn-outline-secondary mt-3" href="${pageContext.request.contextPath}/staff/counter-booking">Book for another customer</a>
        </c:when>
        <c:when test="${not empty selectedCustomer}">
            <div class="card shadow-sm"><div class="card-body p-4">
                <h2 class="h4">Book for <c:out value="${selectedCustomer.fullName}"/></h2>
                <p><c:out value="${selectedCustomer.customerID}"/> · <c:out value="${selectedCustomer.phone}"/> · <c:out value="${selectedCustomer.email}"/></p>
                <form method="post" action="${pageContext.request.contextPath}/staff/counter-booking">
                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.counterCsrf)}">
                    <input type="hidden" name="createToken" value="${fn:escapeXml(sessionScope.counterCreateToken)}">
                    <input type="hidden" name="action" value="create"><input type="hidden" name="customerID" value="${fn:escapeXml(selectedCustomer.customerID)}">
                    <div class="row mb-3"><div class="col-md-6"><label class="form-label" for="checkInDate">Check-in date</label>
                        <input type="date" id="checkInDate" name="checkInDate" min="${today}" value="${today}" required class="form-control"></div>
                        <div class="col-md-6"><label class="form-label" for="checkOutDate">Check-out date</label>
                        <input type="date" id="checkOutDate" name="checkOutDate" min="${tomorrow}" value="${tomorrow}" required class="form-control"></div></div>
                    <p class="text-muted">Counter bookings can start today. Check-in opens at 14:00, after payment and room assignment. Availability is checked again when you submit.</p>
                    <div class="table-responsive"><table class="table align-middle"><thead><tr><th>Room type</th><th>Price / night</th><th>Capacity / room</th><th>Rooms</th><th>Guests</th></tr></thead><tbody>
                        <c:forEach items="${roomTypes}" var="rt"><tr>
                            <td><c:out value="${rt.typeName}"/><input type="hidden" name="roomTypeID" value="${fn:escapeXml(rt.roomTypeID)}"></td>
                            <td><fmt:formatNumber value="${rt.price}"/> VND</td><td><c:out value="${rt.capacity}"/></td>
                            <td><input type="number" name="quantity" min="0" max="999" value="0" required class="form-control" aria-label="Room quantity for ${fn:escapeXml(rt.typeName)}"></td>
                            <td><input type="number" name="guestCount" min="0" max="9999" value="0" required class="form-control" aria-label="Guest count for ${fn:escapeXml(rt.typeName)}"></td>
                        </tr></c:forEach>
                    </tbody></table></div>
                    <label class="form-label" for="paymentOption">Payment option</label><select class="form-select mb-3" id="paymentOption" name="paymentOption" required>
                        <option value="FULL">Full payment</option><option value="DEPOSIT">30% deposit</option></select>
                    <button class="btn btn-primary">Create Booking &amp; Continue to Payment</button>
                    <a class="btn btn-outline-secondary" href="${pageContext.request.contextPath}/staff/counter-booking">Choose another customer</a>
                </form>
            </div></div>
        </c:when>
        <c:otherwise>
            <div class="card shadow-sm"><div class="card-body p-4"><h2 class="h4">Find customer by phone</h2>
                <form method="get" class="d-flex gap-2" action="${pageContext.request.contextPath}/staff/counter-booking">
                    <input type="tel" name="phone" value="${fn:escapeXml(param.phone)}" class="form-control" required maxlength="15" minlength="8" aria-label="Customer phone number" placeholder="Enter phone number">
                    <button class="btn btn-primary">Search</button></form>
                <c:if test="${searched}"><c:choose><c:when test="${not empty matches}">
                    <div class="table-responsive mt-4"><table class="table"><thead><tr><th>Name</th><th>Phone</th><th>Email</th><th></th></tr></thead><tbody>
                        <c:forEach items="${matches}" var="customer"><tr><td><c:out value="${customer.fullName}"/></td><td><c:out value="${customer.phone}"/></td><td><c:out value="${customer.email}"/></td>
                            <td><a class="btn btn-outline-primary" href="${pageContext.request.contextPath}/staff/counter-booking?customerID=${fn:escapeXml(customer.customerID)}">Book for this customer</a></td></tr></c:forEach>
                    </tbody></table></div>
                </c:when><c:otherwise>
                    <p class="mt-3">No customer found. Register their information and login account first.</p>
                    <c:url var="registerUrl" value="/customer"><c:param name="action" value="add"/><c:param name="flow" value="counter"/><c:param name="phone" value="${param.phone}"/></c:url>
                    <a class="btn btn-success" href="${fn:escapeXml(registerUrl)}">Register New Customer</a>
                </c:otherwise></c:choose></c:if>
            </div></div>
        </c:otherwise>
    </c:choose>
</div>
</layout:layout>
