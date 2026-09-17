<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Search Rooms"
    useBootstrap="true"
    >

    <div class="container py-5">

        <div class="d-flex justify-content-between align-items-center mb-4">
            <h1 class="h3 mb-0">
                Search Available Room Types
            </h1>

            <a
                href="${pageContext.request.contextPath}/booking-cart"
                class="btn btn-outline-primary"
                >
                View Booking Cart
            </a>
        </div>

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

        <div class="card shadow-sm mb-4">
            <div class="card-body">

                <form
                    action="${pageContext.request.contextPath}/room-types"
                    method="get"
                    >

                    <div class="row g-3 align-items-end">

                        <div class="col-md-5">
                            <label class="form-label">
                                Check-in Date
                            </label>

                            <input
                                type="date"
                                name="checkInDate"
                                value="${checkInDate}"
                                class="form-control"
                                required
                                >
                        </div>

                        <div class="col-md-5">
                            <label class="form-label">
                                Check-out Date
                            </label>

                            <input
                                type="date"
                                name="checkOutDate"
                                value="${checkOutDate}"
                                class="form-control"
                                required
                                >
                        </div>

                        <div class="col-md-2">
                            <button
                                type="submit"
                                class="btn btn-primary w-100"
                                >
                                Search
                            </button>
                        </div>

                    </div>

                </form>

            </div>
        </div>

        <c:if test="${searched}">

            <div class="row g-4">

                <c:forEach
                    var="roomType"
                    items="${availabilityList}"
                    >

                    <div class="col-lg-6">

                        <div class="card h-100 shadow-sm">

                            <div class="card-body">

                                <h2 class="h5">
                                    <c:out value="${roomType.typeName}" />
                                </h2>

                                <p class="mb-1">
                                    Capacity:
                                    <strong>
                                        ${roomType.capacity}
                                        guest(s) per room
                                    </strong>
                                </p>

                                <p class="mb-1">
                                    Price:
                                    <strong>
                                        <fmt:formatNumber
                                            value="${roomType.price}"
                                            type="number"
                                            />
                                        VND/night
                                    </strong>
                                </p>

                                <p class="mb-3">
                                    Available:
                                    <strong>
                                        ${roomType.availableRooms}
                                        room(s)
                                    </strong>
                                </p>

                                <c:choose>

                                    <c:when test="${roomType.availableRooms > 0}">

                                        <form
                                            action="${pageContext.request.contextPath}/booking-cart/add"
                                            method="post"
                                            >

                                            <input
                                                type="hidden"
                                                name="roomTypeID"
                                                value="${roomType.roomTypeID}"
                                                >

                                            <input
                                                type="hidden"
                                                name="checkInDate"
                                                value="${checkInDate}"
                                                >

                                            <input
                                                type="hidden"
                                                name="checkOutDate"
                                                value="${checkOutDate}"
                                                >

                                            <div class="row g-3">

                                                <div class="col-md-6">

                                                    <label class="form-label">
                                                        Number of Rooms
                                                    </label>

                                                    <input
                                                        type="number"
                                                        name="quantity"
                                                        class="form-control"
                                                        value="1"
                                                        min="1"
                                                        max="${roomType.availableRooms}"
                                                        required
                                                        >

                                                </div>

                                                <div class="col-md-6">

                                                    <label class="form-label">
                                                        Number of Guests
                                                    </label>

                                                    <input
                                                        type="number"
                                                        name="guestCount"
                                                        class="form-control"
                                                        value="1"
                                                        min="1"
                                                        required
                                                        >

                                                </div>

                                            </div>

                                            <button
                                                type="submit"
                                                class="btn btn-success w-100 mt-3"
                                                >
                                                Add to Booking Cart
                                            </button>

                                        </form>

                                    </c:when>

                                    <c:otherwise>

                                        <button
                                            type="button"
                                            class="btn btn-secondary w-100"
                                            disabled
                                            >
                                            Sold Out
                                        </button>

                                    </c:otherwise>

                                </c:choose>

                            </div>

                        </div>

                    </div>

                </c:forEach>

            </div>

            <c:if test="${empty availabilityList}">
                <div class="alert alert-info">
                    No room types were found.
                </div>
            </c:if>

        </c:if>

    </div>

</layout:layout>