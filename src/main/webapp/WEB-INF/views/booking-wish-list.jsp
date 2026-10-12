<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Booking Wishlist" useBootstrap="true">
    <div class="container py-5">
        <h1 class="h3 mb-4">Booking Wish List</h1>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:set var="checkoutError" value="${not empty errorMessage ? errorMessage : sessionScope.bookingError}" />
        <c:if test="${not empty checkoutError}">
            <div class="alert alert-danger"><c:out value="${checkoutError}" /></div>
            <c:remove var="bookingError" scope="session" />
        </c:if>
        <c:choose>
            <c:when test="${wishList == null or empty wishList.items}">
                <div class="alert alert-info">Your booking wish list is empty.</div>
                <a href="${pageContext.request.contextPath}/room-types" class="btn btn-primary">Search Rooms</a>
            </c:when>
            <c:otherwise>
                <div class="card shadow-sm mb-4">
                    <div class="card-body">
                        <div class="row">
                            <div class="col-md-4">
                                <strong>Check-in:</strong>
                                <c:out value="${wishList.checkInDate}" />
                            </div>
                            <div class="col-md-4">
                                <strong>Check-out:</strong>
                                <c:out value="${wishList.checkOutDate}" />
                            </div>
                            <div class="col-md-4">
                                <strong>Nights:</strong>
                                <c:out value="${wishList.numberOfNights}" />
                            </div>
                        </div>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="table table-bordered align-middle">
                        <thead class="table-light">
                            <tr>
                                <th>Room Type</th>
                                <th>Rooms</th>
                                <th>Guests</th>
                                <th>Price/Night</th>
                                <th>Subtotal</th>
                                <th>Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="item" items="${wishList.items}">
                                <tr>
                                    <td><c:out value="${item.typeName}" /></td>
                                    <td><c:out value="${item.quantity}" /></td>
                                    <td><c:out value="${item.guestCount}" /></td>
                                    <td><fmt:formatNumber value="${item.unitPrice}" type="number" /> VND</td>
                                    <td><fmt:formatNumber value="${subtotals[item.roomTypeID]}" type="number" /> VND</td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/booking-wish-list" method="post">
                                            <input type="hidden" name="action" value="remove">
                                            <input type="hidden" name="roomTypeID" value="<c:out value='${item.roomTypeID}' />">
                                            <button type="submit" class="btn btn-sm btn-danger">Remove</button>
                                        </form>
                                    </td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="card shadow-sm">
                    <div class="card-body">
                        <div class="d-flex justify-content-between mb-2">
                            <span>Total Rooms:</span>
                            <strong><c:out value="${wishList.totalRooms}" /></strong>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span>Total Guests:</span>
                            <strong><c:out value="${wishList.totalGuests}" /></strong>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span>Total Amount:</span>
                            <strong class="text-success"><fmt:formatNumber value="${wishList.totalAmount}" type="number" /> VND</strong>
                        </div>
                    </div>
                </div>
                <form id="checkoutForm" action="${pageContext.request.contextPath}/checkout" method="post" class="mt-4">
                    <div class="card shadow-sm">
                        <div class="card-body">
                            <fieldset>
                                <legend class="h5 mb-3">Choose Your Payment Option</legend>
                                <div class="form-check mb-3">
                                    <input class="form-check-input" type="radio" id="paymentDeposit" name="paymentOption" value="DEPOSIT" required>
                                    <label class="form-check-label" for="paymentDeposit">
                                        <strong>Pay 30% deposit</strong>
                                        <span class="d-block text-muted">Pay 30% to secure your booking, then pay the remaining 70% later.</span>
                                    </label>
                                </div>
                                <div class="form-check">
                                    <input class="form-check-input" type="radio" id="paymentFull" name="paymentOption" value="FULL" required>
                                    <label class="form-check-label" for="paymentFull">
                                        <strong>Pay full amount</strong>
                                        <span class="d-block text-muted">Pay 100% of your booking amount.</span>
                                    </label>
                                </div>
                            </fieldset>
                        </div>
                    </div>
                </form>
                <c:url var="addMoreRoomsUrl" value="/room-types">
                    <c:param name="checkInDate" value="${wishList.checkInDate}" />
                    <c:param name="checkOutDate" value="${wishList.checkOutDate}" />
                </c:url>
                <div class="d-flex flex-wrap justify-content-between gap-3 mt-4">
                    <form action="${pageContext.request.contextPath}/booking-wish-list" method="post">
                        <input type="hidden" name="action" value="clear">
                        <button type="submit" class="btn btn-outline-danger">Clear Wish List</button>
                    </form>
                    <div class="d-flex flex-wrap gap-2">
                        <a href="<c:out value='${addMoreRoomsUrl}' />" class="btn btn-secondary">Add More Rooms</a>
                        <button type="submit" form="checkoutForm" class="btn btn-success">Proceed to Payment</button>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>