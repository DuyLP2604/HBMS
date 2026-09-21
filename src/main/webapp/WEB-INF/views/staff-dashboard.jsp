<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Staff Dashboard"
    useBootstrap="true"
>

    <div class="container py-5">

        <!-- Dashboard header -->
        <div
            class="d-flex flex-column flex-md-row
                   justify-content-between
                   align-items-md-center
                   gap-3 mb-5"
        >

            <div>
                <h1 class="h2 mb-2">
                    Staff Dashboard
                </h1>

                <p class="text-muted mb-0">
                    Welcome,
                    <strong>
                        <c:out value="${employee.fullName}" />
                    </strong>

                    <c:if test="${not empty employee.position}">
                        <span class="badge bg-primary ms-2">
                            <c:out value="${employee.position}" />
                        </span>
                    </c:if>
                </p>
            </div>

        </div>

        <!-- Flash messages -->
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

        <!-- General staff menu -->
        <div class="mb-5">

            <h2 class="h4 mb-4">
                General Menu
            </h2>

            <div class="row g-4">

                <!-- Profile -->
                <div class="col-md-6 col-xl-4">

                    <div class="card h-100 shadow-sm">

                        <div class="card-body d-flex flex-column">

                            <div class="mb-3">
                                <i
                                    class="fa-solid fa-circle-user
                                           fa-2x text-primary"
                                ></i>
                            </div>

                            <h3 class="h5">
                                Profile
                            </h3>

                            <p class="text-muted flex-grow-1">
                                View and update your personal information.
                            </p>

                            <a
                                href="${pageContext.request.contextPath}/profile?action=view"
                                class="btn btn-primary"
                            >
                                Open Profile
                            </a>

                        </div>

                    </div>

                </div>

                <!-- Add customer -->
                <div class="col-md-6 col-xl-4">

                    <div class="card h-100 shadow-sm">

                        <div class="card-body d-flex flex-column">

                            <div class="mb-3">
                                <i
                                    class="fa-solid fa-user-plus
                                           fa-2x text-primary"
                                ></i>
                            </div>

                            <h3 class="h5">
                                Add Customer
                            </h3>

                            <p class="text-muted flex-grow-1">
                                Create customer information for walk-in guests.
                            </p>

                            <a
                                href="${pageContext.request.contextPath}/customer?action=add"
                                class="btn btn-primary"
                            >
                                Add Customer
                            </a>

                        </div>

                    </div>

                </div>

                <!-- Manage bookings -->
                <div class="col-md-6 col-xl-4">

                    <div class="card h-100 shadow-sm">

                        <div class="card-body d-flex flex-column">

                            <div class="mb-3">
                                <i
                                    class="fa-solid fa-calendar-check
                                           fa-2x text-primary"
                                ></i>
                            </div>

                            <h3 class="h5">
                                Manage Bookings
                            </h3>

                            <p class="text-muted flex-grow-1">
                                View and manage customer bookings.
                            </p>

                            <a
                                href="${pageContext.request.contextPath}/booking?action=list"
                                class="btn btn-primary"
                            >
                                Manage Bookings
                            </a>

                        </div>

                    </div>

                </div>

                <!-- Manage rooms -->
                <div class="col-md-6 col-xl-4">

                    <div class="card h-100 shadow-sm">

                        <div class="card-body d-flex flex-column">

                            <div class="mb-3">
                                <i
                                    class="fa-solid fa-bed
                                           fa-2x text-primary"
                                ></i>
                            </div>

                            <h3 class="h5">
                                Manage Rooms
                            </h3>

                            <p class="text-muted flex-grow-1">
                                View and manage hotel rooms.
                            </p>

                            <a
                                href="${pageContext.request.contextPath}/room"
                                class="btn btn-primary"
                            >
                                Manage Rooms
                            </a>

                        </div>

                    </div>

                </div>

                <!-- Services -->
                <div class="col-md-6 col-xl-4">

                    <div class="card h-100 shadow-sm">

                        <div class="card-body d-flex flex-column">

                            <div class="mb-3">
                                <i
                                    class="fa-solid fa-bell-concierge
                                           fa-2x text-primary"
                                ></i>
                            </div>

                            <h3 class="h5">
                                Services
                            </h3>

                            <p class="text-muted flex-grow-1">
                                View and manage hotel services.
                            </p>

                            <a
                                href="${pageContext.request.contextPath}/service"
                                class="btn btn-primary"
                            >
                                Manage Services
                            </a>

                        </div>

                    </div>

                </div>

                <!-- Invoice -->
                <div class="col-md-6 col-xl-4">

                    <div class="card h-100 shadow-sm">

                        <div class="card-body d-flex flex-column">

                            <div class="mb-3">
                                <i
                                    class="fa-solid fa-file-invoice-dollar
                                           fa-2x text-primary"
                                ></i>
                            </div>

                            <h3 class="h5">
                                Invoice
                            </h3>

                            <p class="text-muted flex-grow-1">
                                View and manage customer invoices.
                            </p>

                            <a
                                href="${pageContext.request.contextPath}/invoice"
                                class="btn btn-primary"
                            >
                                Manage Invoices
                            </a>

                        </div>

                    </div>

                </div>

            </div>

        </div>

        <!-- Receptionist-only menu -->
        <c:if test="${isReceptionist}">

            <div class="mb-4">

                <div
                    class="d-flex align-items-center
                           justify-content-between mb-4"
                >
                    <h2 class="h4 mb-0">
                        Receptionist Menu
                    </h2>

                    <span class="badge bg-success">
                        Receptionist Only
                    </span>
                </div>

                <div class="row g-4">

                    <!-- Room assignment -->
                    <div class="col-md-6">

                        <div
                            class="card h-100 shadow-sm
                                   border-success"
                        >

                            <div class="card-body d-flex flex-column">

                                <div class="mb-3">
                                    <i
                                        class="fa-solid fa-key
                                               fa-2x text-success"
                                    ></i>
                                </div>

                                <h3 class="h5">
                                    Room Assignment
                                </h3>

                                <p class="text-muted flex-grow-1">
                                    Select available rooms and assign them
                                    to confirmed bookings.
                                </p>

                                <a
                                    href="${pageContext.request.contextPath}/staff/bookings"
                                    class="btn btn-success"
                                >
                                    Assign Rooms
                                </a>

                            </div>

                        </div>

                    </div>

                    <!-- Guest stays -->
                    <div class="col-md-6">

                        <div
                            class="card h-100 shadow-sm
                                   border-success"
                        >

                            <div class="card-body d-flex flex-column">

                                <div class="mb-3">
                                    <i
                                        class="fa-solid fa-hotel
                                               fa-2x text-success"
                                    ></i>
                                </div>

                                <h3 class="h5">
                                    Guest Stays
                                </h3>

                                <p class="text-muted flex-grow-1">
                                    Manage guest check-in, stay information
                                    and check-out.
                                </p>

                                <a
                                    href="${pageContext.request.contextPath}/staff/stays"
                                    class="btn btn-success"
                                >
                                    Manage Guest Stays
                                </a>

                            </div>

                        </div>

                    </div>

                </div>

            </div>

        </c:if>

        <!-- Message for non-receptionist staff -->
        <c:if test="${not isReceptionist}">

            <div class="alert alert-info mt-4">

                <i class="fa-solid fa-circle-info me-1"></i>

                You are logged in as

                <strong>
                    <c:out value="${employee.position}" />
                </strong>.

                Receptionist functions such as room assignment
                and guest stays are not available for this position.

            </div>

        </c:if>

    </div>

</layout:layout>