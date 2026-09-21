<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Booking Details"
    useBootstrap="true"
    >
    <div class="container py-5">
        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0">
                Booking
                <c:out value="${booking.bookingID}" />
            </h1>
            <a
                href="${pageContext.request.contextPath}/my-bookings"
                class="btn btn-secondary"
                >
                Back
            </a>
        </div>
        <div class="card shadow-sm mb-4">

            <div class="card-body">

                <div class="row g-3">

                    <div class="col-md-4">
                        <strong>Check-in:</strong><br>

                        <fmt:formatDate
                            value="${booking.checkInDate}"
                            pattern="dd/MM/yyyy"
                            />
                    </div>

                    <div class="col-md-4">
                        <strong>Check-out:</strong><br>

                        <fmt:formatDate
                            value="${booking.checkOutDate}"
                            pattern="dd/MM/yyyy"
                            />
                    </div>

                    <div class="col-md-4">
                        <strong>Nights:</strong><br>
                        ${booking.numberOfNights}
                    </div>

                    <div class="col-md-4">
                        <strong>Booking Status:</strong><br>
                        <c:out value="${booking.bookingStatus}" />
                    </div>

                    <div class="col-md-4">
                        <strong>Payment Status:</strong><br>
                        <c:out value="${booking.paymentStatus}" />
                    </div>

                    <div class="col-md-4">
                        <strong>Total Amount:</strong><br>

                        <fmt:formatNumber
                            value="${booking.totalAmount}"
                            type="number"
                            />

                        VND
                    </div>

                </div>

            </div>

        </div>

        <h2 class="h4 mb-3">
            Rooms
        </h2>

        <c:forEach
            var="item"
            items="${booking.items}"
            >

            <div class="card shadow-sm mb-3">

                <div class="card-body">

                    <div class="row">

                        <div class="col-md-5">

                            <h3 class="h5">
                                <c:out value="${item.roomTypeName}" />
                            </h3>

                            <p class="mb-1">
                                Quantity:
                                <strong>${item.quantity}</strong>
                            </p>

                            <p class="mb-1">
                                Guests:
                                <strong>${item.guestCount}</strong>
                            </p>

                            <p class="mb-0">
                                Subtotal:

                                <strong>
                                    <fmt:formatNumber
                                        value="${item.subtotal}"
                                        type="number"
                                        />
                                    VND
                                </strong>
                            </p>

                        </div>

                        <div class="col-md-7">

                            <h4 class="h6">
                                Assigned Rooms
                            </h4>

                            <c:choose>

                                <c:when test="${empty item.roomNumbers}">

                                    <div class="alert alert-warning mb-0">
                                        Room assignment is pending.
                                    </div>

                                </c:when>

                                <c:otherwise>

                                    <ul class="list-group">

                                        <c:forEach
                                            var="roomNumber"
                                            items="${item.roomNumbers}"
                                            >

                                            <li class="list-group-item">
                                                Room
                                                <strong>
                                                    <c:out value="${roomNumber}" />
                                                </strong>
                                            </li>

                                        </c:forEach>

                                    </ul>

                                </c:otherwise>

                            </c:choose>

                        </div>

                    </div>

                </div>

            </div>

        </c:forEach>

        <c:if test="${booking.bookingStatus eq 'PENDING_PAYMENT'}">

            <a
                href="${pageContext.request.contextPath}/payment?bookingID=${booking.bookingID}"
                class="btn btn-success"
                >
                Continue to Payment
            </a>

        </c:if>

    </div>

</layout:layout>