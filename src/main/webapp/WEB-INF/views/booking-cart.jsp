<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Booking Cart"
    useBootstrap="true"
    >

    <div class="container py-5">

        <h1 class="h3 mb-4">
            Booking Cart
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

            <c:when test="${cart == null or empty cart.items}">

                <div class="alert alert-info">
                    Your booking cart is empty.
                </div>

                <a
                    href="${pageContext.request.contextPath}/room-types"
                    class="btn btn-primary"
                    >
                    Search Rooms
                </a>

            </c:when>

            <c:otherwise>

                <div class="card shadow-sm mb-4">

                    <div class="card-body">

                        <div class="row">

                            <div class="col-md-4">
                                <strong>Check-in:</strong>
                                <c:out value="${cart.checkInDate}" />
                            </div>

                            <div class="col-md-4">
                                <strong>Check-out:</strong>
                                <c:out value="${cart.checkOutDate}" />
                            </div>

                            <div class="col-md-4">
                                <strong>Nights:</strong>
                                ${cart.numberOfNights}
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

                            <c:forEach
                                var="item"
                                items="${cart.items}"
                                >

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
                                        <fmt:formatNumber
                                            value="${item.unitPrice}"
                                            type="number"
                                            />
                                        VND
                                    </td>

                                    <td>
                                        <fmt:formatNumber
                                            value="${subtotals[item.roomTypeID]}"
                                            type="number"
                                            />
                                        VND
                                    </td>

                                    <td>

                                        <form
                                            action="${pageContext.request.contextPath}/booking-cart"
                                            method="post"
                                            >

                                            <input
                                                type="hidden"
                                                name="action"
                                                value="remove"
                                                >

                                            <input
                                                type="hidden"
                                                name="roomTypeID"
                                                value="${item.roomTypeID}"
                                                >

                                            <button
                                                type="submit"
                                                class="btn btn-sm btn-danger"
                                                >
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
                            <strong>${cart.totalRooms}</strong>
                        </div>

                        <div class="d-flex justify-content-between mb-2">
                            <span>Total Guests:</span>
                            <strong>${cart.totalGuests}</strong>
                        </div>

                        <div class="d-flex justify-content-between">
                            <span>Total Amount:</span>

                            <strong class="text-success">
                                <fmt:formatNumber
                                    value="${cart.totalAmount}"
                                    type="number"
                                    />
                                VND
                            </strong>
                        </div>

                    </div>

                </div>

                <div class="d-flex justify-content-between mt-4">

                    <form
                        action="${pageContext.request.contextPath}/booking-cart"
                        method="post"
                        >

                        <input
                            type="hidden"
                            name="action"
                            value="clear"
                            >

                        <button
                            type="submit"
                            class="btn btn-outline-danger"
                            >
                            Clear Cart
                        </button>

                    </form>

                    <div>

                        <a
                            href="${pageContext.request.contextPath}/room-types?checkInDate=${cart.checkInDate}&checkOutDate=${cart.checkOutDate}"
                            class="btn btn-secondary"
                            >
                            Add More Rooms
                        </a>

                        <form
                            action="${pageContext.request.contextPath}/checkout"
                            method="post"
                            class="d-inline"
                            >

                            <button
                                type="submit"
                                class="btn btn-success"
                                >
                                Proceed to Payment
                            </button>

                        </form>

                    </div>

                </div>

            </c:otherwise>

        </c:choose>

    </div>

</layout:layout>