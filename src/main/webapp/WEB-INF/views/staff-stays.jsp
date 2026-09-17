<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Guest Stays"
    useBootstrap="true"
    >

    <div class="container py-5">

        <h1 class="h3 mb-4">
            Guest Stay Management
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

        <div class="table-responsive">

            <table class="table table-bordered table-hover align-middle">

                <thead class="table-light">

                    <tr>
                        <th>Booking</th>
                        <th>Customer</th>
                        <th>Check-in</th>
                        <th>Check-out</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>

                </thead>

                <tbody>

                    <c:forEach
                        var="booking"
                        items="${bookings}"
                        >

                        <tr>

                            <td>
                                <c:out value="${booking.bookingID}" />
                            </td>

                            <td>
                                <c:out value="${booking.customerID.fullName}" />
                            </td>

                            <td>
                                <fmt:formatDate
                                    value="${booking.checkInDate}"
                                    pattern="dd/MM/yyyy"
                                    />
                            </td>

                            <td>
                                <fmt:formatDate
                                    value="${booking.checkOutDate}"
                                    pattern="dd/MM/yyyy"
                                    />
                            </td>

                            <td>
                                <span class="badge bg-primary">
                                    <c:out value="${booking.bookingStatus}" />
                                </span>
                            </td>

                            <td>

                                <c:choose>

                                    <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">

                                        <a
                                            href="${pageContext.request.contextPath}/staff/bookings?bookingID=${booking.bookingID}"
                                            class="btn btn-primary btn-sm"
                                            >
                                            Assign Rooms
                                        </a>

                                    </c:when>

                                    <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">

                                        <form
                                            action="${pageContext.request.contextPath}/staff/stays"
                                            method="post"
                                            class="d-inline"
                                            >

                                            <input
                                                type="hidden"
                                                name="bookingID"
                                                value="${booking.bookingID}"
                                                >

                                            <input
                                                type="hidden"
                                                name="action"
                                                value="checkIn"
                                                >

                                            <button
                                                type="submit"
                                                class="btn btn-success btn-sm"
                                                >
                                                Check In
                                            </button>

                                        </form>

                                    </c:when>

                                    <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}">

                                        <form
                                            action="${pageContext.request.contextPath}/staff/stays"
                                            method="post"
                                            class="d-inline"
                                            >

                                            <input
                                                type="hidden"
                                                name="bookingID"
                                                value="${booking.bookingID}"
                                                >

                                            <input
                                                type="hidden"
                                                name="action"
                                                value="checkOut"
                                                >

                                            <button
                                                type="submit"
                                                class="btn btn-warning btn-sm"
                                                >
                                                Check Out
                                            </button>

                                        </form>

                                    </c:when>

                                </c:choose>

                            </td>

                        </tr>

                    </c:forEach>

                    <c:if test="${empty bookings}">

                        <tr>
                            <td
                                colspan="6"
                                class="text-center text-muted py-4"
                                >
                                No active stays were found.
                            </td>
                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</layout:layout>