<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Assign Rooms"
    useBootstrap="true"
    >

    <div class="container py-5">

        <div class="d-flex justify-content-between mb-4">

            <h1 class="h3">
                Assign Rooms:
                <c:out value="${booking.bookingID}" />
            </h1>

            <a
                href="${pageContext.request.contextPath}/staff/bookings"
                class="btn btn-secondary"
                >
                Back
            </a>

        </div>

        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger">
                <c:out value="${errorMessage}" />
            </div>
        </c:if>

        <div class="card shadow-sm mb-4">

            <div class="card-body">

                <p>
                    <strong>Customer:</strong>
                    <c:out value="${booking.customerID.fullName}" />
                </p>

                <p>
                    <strong>Stay:</strong>

                    <fmt:formatDate
                        value="${booking.checkInDate}"
                        pattern="dd/MM/yyyy"
                        />

                    –

                    <fmt:formatDate
                        value="${booking.checkOutDate}"
                        pattern="dd/MM/yyyy"
                        />
                </p>

            </div>

        </div>

        <form
            action="${pageContext.request.contextPath}/staff/room-assignments"
            method="post"
            >

            <input
                type="hidden"
                name="bookingID"
                value="${booking.bookingID}"
                >

            <c:forEach var="detail" items="${details}">

                <div class="card shadow-sm mb-4">

                    <div class="card-body">

                        <h2 class="h5">
                            <c:out value="${detail.roomTypeID.typeName}" />
                        </h2>

                        <p>
                            Required rooms:
                            <strong>${detail.quantity}</strong>
                        </p>

                        <p>
                            Guests:
                            <strong>${detail.guestCount}</strong>
                        </p>

                        <label class="form-label fw-semibold">
                            Select exactly ${detail.quantity} room(s)
                        </label>

                        <select
                            name="rooms_${detail.bookingDetailID}"
                            class="form-select"
                            multiple
                            size="6"
                            required
                            >

                            <c:forEach
                                var="room"
                                items="${availableRoomMap[detail.bookingDetailID]}"
                                >

                                <option value="${room.roomID}">
                                    Room
                                    <c:out value="${room.roomNumber}" />
                                </option>

                            </c:forEach>

                        </select>

                        <div class="form-text">
                            Hold Ctrl to select multiple rooms.
                        </div>

                        <c:if test="${empty availableRoomMap[detail.bookingDetailID]}">

                            <div class="alert alert-danger mt-3">
                                No matching rooms are currently available.
                            </div>

                        </c:if>

                    </div>

                </div>

            </c:forEach>

            <button
                type="submit"
                class="btn btn-success"
                >
                Confirm Room Assignment
            </button>

        </form>

    </div>

</layout:layout>