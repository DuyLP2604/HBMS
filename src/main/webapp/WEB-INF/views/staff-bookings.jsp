<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Bookings Awaiting Assignment"
    useBootstrap="true"
    >

    <div class="container py-5">

        <h1 class="h3 mb-4">
            Bookings Awaiting Room Assignment
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
                        <th>Total</th>
                        <th>Status</th>
                        <th>Action</th>
                    </tr>

                </thead>

                <tbody>

                    <c:forEach var="booking" items="${bookings}">

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
                                <fmt:formatNumber
                                    value="${booking.totalAmount}"
                                    type="number"
                                    />
                                VND
                            </td>

                            <td>
                                <span class="badge bg-success">
                                    <c:out value="${booking.bookingStatus}" />
                                </span>
                            </td>

                            <td>

                                <a
                                    href="${pageContext.request.contextPath}/staff/bookings?bookingID=${booking.bookingID}"
                                    class="btn btn-primary btn-sm"
                                    >
                                    Assign Rooms
                                </a>

                            </td>

                        </tr>

                    </c:forEach>

                    <c:if test="${empty bookings}">

                        <tr>

                            <td
                                colspan="7"
                                class="text-center text-muted py-4"
                                >
                                No bookings are awaiting room assignment.
                            </td>

                        </tr>

                    </c:if>

                </tbody>

            </table>

        </div>

    </div>

</layout:layout>