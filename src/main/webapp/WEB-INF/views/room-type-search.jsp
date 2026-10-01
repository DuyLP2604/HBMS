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

        <!-- Page header -->
        <div
            class="d-flex justify-content-between
                   align-items-center mb-4"
        >

            <h1 class="h3 mb-0">
                Search Available Room Types
            </h1>

            <a
                href="${pageContext.request.contextPath}/booking-cart"
                class="btn btn-outline-primary"
            >
                <i class="fa-solid fa-cart-shopping me-1"></i>
                View Booking Cart
            </a>

        </div>

        <!-- Messages -->
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

        <!-- Search form -->
        <div class="card shadow-sm mb-4">

            <div class="card-body">

                <form
                    action="${pageContext.request.contextPath}/room-types"
                    method="get"
                >

                    <div class="row g-3 align-items-end">

                        <div class="col-md-5">

                            <label
                                for="checkInDate"
                                class="form-label"
                            >
                                Check-in Date
                            </label>

                            <input
                                type="date"
                                id="checkInDate"
                                name="checkInDate"
                                value="${checkInDate}"
                                class="form-control"
                                required
                            >

                        </div>

                        <div class="col-md-5">

                            <label
                                for="checkOutDate"
                                class="form-label"
                            >
                                Check-out Date
                            </label>

                            <input
                                type="date"
                                id="checkOutDate"
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
                                <i class="fa-solid fa-magnifying-glass me-1"></i>
                                Search
                            </button>

                        </div>

                    </div>

                </form>

            </div>

        </div>

        <!-- Search results -->
        <c:if test="${searched}">

            <c:choose>

                <c:when test="${not empty availabilityList}">

                    <div class="row g-4">

                        <c:forEach
                            var="roomType"
                            items="${availabilityList}"
                        >

                            <div class="col-md-6">

                                <div
                                    class="card h-100 shadow-sm
                                           overflow-hidden"
                                >

                                    <!-- Room-type image -->
                                    <c:choose>

                                        <c:when test="${not empty roomType.roomTypeImage}">

                                            <c:url
                                                var="roomTypeImageUrl"
                                                value="/assets/images/room-types/${roomType.roomTypeImage}"
                                            />

                                            <img
                                                src="${roomTypeImageUrl}"
                                                class="card-img-top"
                                                alt="<c:out value='${roomType.typeName}' />"
                                                style="height: 240px;
                                                       object-fit: cover;"
                                            >

                                        </c:when>

                                        <c:otherwise>

                                            <div
                                                class="bg-light text-secondary
                                                       d-flex justify-content-center
                                                       align-items-center"
                                                style="height: 240px;"
                                            >
                                                <div class="text-center">

                                                    <i
                                                        class="fa-solid fa-image
                                                               fa-3x mb-2"
                                                    ></i>

                                                    <div>
                                                        No image available
                                                    </div>

                                                </div>
                                            </div>

                                        </c:otherwise>

                                    </c:choose>

                                    <!-- Room-type information -->
                                    <div class="card-body d-flex flex-column">

                                        <h2 class="h5 mb-3">
                                            <c:out value="${roomType.typeName}" />
                                        </h2>

                                        <div class="mb-3">

                                            <p class="mb-2">

                                                <i
                                                    class="fa-solid fa-users
                                                           text-primary me-2"
                                                ></i>

                                                Capacity:

                                                <strong>
                                                    ${roomType.capacity}
                                                    guest(s) per room
                                                </strong>

                                            </p>

                                            <p class="mb-2">

                                                <i
                                                    class="fa-solid fa-money-bill
                                                           text-primary me-2"
                                                ></i>

                                                Price:

                                                <strong class="text-danger">

                                                    <fmt:formatNumber
                                                        value="${roomType.price}"
                                                        type="number"
                                                        groupingUsed="true"
                                                    />

                                                    VND/night

                                                </strong>

                                            </p>

                                            <p class="mb-0">

                                                <i
                                                    class="fa-solid fa-door-open
                                                           text-primary me-2"
                                                ></i>

                                                Available:

                                                <c:choose>

                                                    <c:when test="${roomType.availableRooms > 0}">
                                                        <strong class="text-success">
                                                            ${roomType.availableRooms}
                                                            room(s)
                                                        </strong>
                                                    </c:when>

                                                    <c:otherwise>
                                                        <strong class="text-danger">
                                                            Sold out
                                                        </strong>
                                                    </c:otherwise>

                                                </c:choose>

                                            </p>

                                        </div>

                                        <!-- Booking form or sold-out button -->
                                        <div class="mt-auto">

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

                                                                <label
                                                                    for="quantity_${roomType.roomTypeID}"
                                                                    class="form-label"
                                                                >
                                                                    Number of Rooms
                                                                </label>

                                                                <input
                                                                    type="number"
                                                                    id="quantity_${roomType.roomTypeID}"
                                                                    name="quantity"
                                                                    class="form-control"
                                                                    value="1"
                                                                    min="1"
                                                                    max="${roomType.availableRooms}"
                                                                    required
                                                                >

                                                            </div>

                                                            <div class="col-md-6">

                                                                <label
                                                                    for="guestCount_${roomType.roomTypeID}"
                                                                    class="form-label"
                                                                >
                                                                    Number of Guests
                                                                </label>

                                                                <input
                                                                    type="number"
                                                                    id="guestCount_${roomType.roomTypeID}"
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
                                                            class="btn btn-success
                                                                   w-100 mt-3"
                                                        >
                                                            <i
                                                                class="fa-solid
                                                                       fa-cart-plus me-1"
                                                            ></i>

                                                            Add to Booking Cart
                                                        </button>

                                                    </form>

                                                </c:when>

                                                <c:otherwise>

                                                    <button
                                                        type="button"
                                                        class="btn btn-secondary
                                                               w-100"
                                                        disabled
                                                    >
                                                        Sold Out
                                                    </button>

                                                </c:otherwise>

                                            </c:choose>

                                        </div>

                                    </div>

                                </div>

                            </div>

                        </c:forEach>

                    </div>

                </c:when>

                <c:otherwise>

                    <div class="alert alert-info">
                        <i class="fa-solid fa-circle-info me-1"></i>
                        No room types were found.
                    </div>

                </c:otherwise>

            </c:choose>

        </c:if>

    </div>

</layout:layout>