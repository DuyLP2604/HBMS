<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Staff Dashboard" useBootstrap="true">
    <div class="container py-5">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-5">
            <div>
                <h1 class="h2 mb-2">Staff Dashboard</h1>
                <p class="text-muted mb-0">
                    Welcome, <strong><c:out value="${employee.fullName}" /></strong>
                    <c:if test="${not empty employee.position}">
                        <span class="badge bg-primary ms-2"><c:out value="${employee.position}" /></span>
                    </c:if>
                </p>
            </div>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
        </c:if>

        <section class="mb-5" aria-labelledby="general-menu-title">
            <h2 id="general-menu-title" class="h4 mb-4">General Menu</h2>

            <div class="row g-4">
                <div class="col-md-6">
                    <div class="card h-100 shadow-sm">
                        <div class="card-body d-flex flex-column">
                            <div class="mb-3">
                                <i class="fa-solid fa-circle-user fa-2x text-primary"></i>
                            </div>
                            <h3 class="h5">View Profile</h3>
                            <p class="text-muted flex-grow-1">
                                View your staff profile and personal information.
                            </p>
                            <a href="${pageContext.request.contextPath}/staff/profile"
                               class="btn btn-primary">
                                View Profile
                            </a>
                        </div>
                    </div>
                </div>

                <div class="col-md-6">
                    <div class="card h-100 shadow-sm">
                        <div class="card-body d-flex flex-column">
                            <div class="mb-3">
                                <i class="fa-solid fa-file-invoice-dollar fa-2x text-primary"></i>
                            </div>
                            <h3 class="h5">Invoice</h3>
                            <p class="text-muted flex-grow-1">
                                View and manage customer invoices.
                            </p>
                            <a href="${pageContext.request.contextPath}/invoice"
                               class="btn btn-primary">
                                Manage Invoices
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <c:if test="${isReceptionist}">
            <section class="mb-4" aria-labelledby="receptionist-menu-title">
                <div class="d-flex align-items-center justify-content-between mb-4">
                    <h2 id="receptionist-menu-title" class="h4 mb-0">Receptionist Menu</h2>
                    <span class="badge bg-success">Receptionist Only</span>
                </div>
                <div class="row g-4">
                    <div class="col-md-6 col-xl-4">
                        <div class="card h-100 shadow-sm border-success">
                            <div class="card-body d-flex flex-column">
                                <div class="mb-3"><i class="fa-solid fa-key fa-2x text-success"></i></div>
                                <h3 class="h5">Room Assignment</h3>
                                <p class="text-muted flex-grow-1">Select available rooms and assign them to confirmed bookings.</p>
                                <a href="${pageContext.request.contextPath}/staff/bookings" class="btn btn-success">
                                    Assign Rooms
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="card h-100 shadow-sm border-success">
                            <div class="card-body d-flex flex-column">
                                <div class="mb-3"><i class="fa-solid fa-hotel fa-2x text-success"></i></div>
                                <h3 class="h5">Guest Stays</h3>
                                <p class="text-muted flex-grow-1">Manage guest check-in, stay information and check-out.</p>
                                <a href="${pageContext.request.contextPath}/staff/stays" class="btn btn-success">
                                    Manage Guest Stays
                                </a>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-6 col-xl-4">
                        <div class="card h-100 shadow-sm border-success">
                            <div class="card-body d-flex flex-column">
                                <div class="mb-3"><i class="fa-solid fa-users fa-2x text-success"></i></div>
                                <h3 class="h5">Customer List</h3>
                                <p class="text-muted flex-grow-1">View the list of customers and their information.</p>
                                <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-success">
                                    View Customer List
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </c:if>
    </div>
</layout:layout>
