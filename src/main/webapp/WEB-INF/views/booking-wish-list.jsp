<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Booking Wishlist" useBootstrap="true" >
    <div class="container py-5">
        <h1 class="h3 mb-4">
            Booking Wish list
        </h1>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success">
                <c:out value="${successMessage}" />
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>
        <c:choose>
            <c:when test="${wishList == null or empty wishList.items}">
                <div class="alert alert-info">
                    Your booking wish list is empty.
                </div>
                <a href="${pageContext.request.contextPath}/room-types" class="btn btn-primary" >
                    Search Rooms
                </a>
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
                                ${wishList.numberOfNights}
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
                            <c:forEach var="item" items="${wishList.items}" >
                                <tr>
                                    <td>
                                        <c:out value="${item.typeName}" />
                                    </td>
                                    <td>
                                        ${item.quantity}
                                    </td>
                                    <td>
                                        ${item.guestCount}
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${item.unitPrice}" type="number" />
                                        VND
                                    </td>
                                    <td>
                                        <fmt:formatNumber value="${subtotals[item.roomTypeID]}" type="number" />
                                        VND
                                    </td>
                                    <td>
                                        <form action="${pageContext.request.contextPath}/booking-wish-list" method="post" >
                                            <input type="hidden" name="action" value="remove" >
                                            <input type="hidden" name="roomTypeID" value="${item.roomTypeID}" >
                                            <button type="submit" class="btn btn-sm btn-danger">
                                                Remove
                                            </button>
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
                            <strong>${wishList.totalRooms}</strong>
                        </div>
                        <div class="d-flex justify-content-between mb-2">
                            <span>Total Guests:</span>
                            <strong>${wishList.totalGuests}</strong>
                        </div>
                        <div class="d-flex justify-content-between">
                            <span>Total Amount:</span>
                            <strong class="text-success">
                                <fmt:formatNumber value="${wishList.totalAmount}"  type="number" />
                                VND
                            </strong>
                        </div>
                    </div>
                </div>
                <div class="d-flex justify-content-between mt-4">
                    <form action="${pageContext.request.contextPath}/booking-wish-list" method="post" >
                        <input type="hidden" name="action" value="clear" >
                        <button type="submit" class="btn btn-outline-danger" >
                            Clear Wish list
                        </button>
                    </form>
                    <div>
                        <a href="${pageContext.request.contextPath}/room-types?checkInDate=${wishList.checkInDate}&checkOutDate=${wishList.checkOutDate}" class="btn btn-secondary">
                            Add More Rooms
                        </a>
                        <form action="${pageContext.request.contextPath}/checkout" method="post" class="d-inline" >
                            <button type="submit" class="btn btn-success">
                                Proceed to Payment
                            </button>
                        </form>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>