<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Staff Dashboard" useBootstrap="true"
               pageCss="staff-dashboard.css" bodyClass="staff-dashboard-page">
    <div class="container staff-dashboard">
        <header class="staff-dashboard-header">
            <div class="staff-header-content">
                <p class="staff-eyebrow">Hotel operations</p>
                <h1>Staff Dashboard</h1>
                <p class="staff-welcome">
                    Welcome back, <strong><c:out value="${employee.fullName}" /></strong>.
                </p>
                <p class="staff-header-description">Everything you need to take care of your guests, in one place.</p>
            </div>
            <c:if test="${not empty employee.position}">
                <div class="staff-role-badge">
                    <i class="fa-solid fa-id-badge" aria-hidden="true"></i>
                    <c:out value="${employee.position}" />
                </div>
            </c:if>
        </header>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger"><c:out value="${errorMessage}" /></div>
        </c:if>
        <section aria-labelledby="staff-functions-title">
            <div class="staff-section-heading">
                <div>
                    <h2 id="staff-functions-title">Staff Functions</h2>
                    <p>Choose a function to get started.</p>
                </div>
            </div>

            <div class="staff-functions-grid">
                <c:if test="${isReceptionist}">

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-blue">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-user-plus" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Book for Customer</h3>
                                <p class="staff-card-description">Find or register a guest, book at the counter and collect payment.</p>
                                <a href="${pageContext.request.contextPath}/staff/counter-booking" class="btn staff-function-link"><span>Book for Customer</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-blue">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-calendar-check" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Booking Overview</h3>
                                <p class="staff-card-description">Browse all bookings and view the customer account for each booking.</p>
                                <a href="${pageContext.request.contextPath}/booking-overview" class="btn staff-function-link"><span>View Bookings</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-blue">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-file-invoice-dollar" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Invoice</h3>
                                <p class="staff-card-description">View and manage customer invoices.</p>
                                <a href="${pageContext.request.contextPath}/invoice" class="btn staff-function-link"><span>Manage Invoices</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-teal">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-key" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Room Assignment</h3>
                                <p class="staff-card-description">Select available rooms and assign them to confirmed bookings.</p>
                                <a href="${pageContext.request.contextPath}/staff/bookings" class="btn staff-function-link"><span>Assign Rooms</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-teal">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-hotel" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Guest Stays</h3>
                                <p class="staff-card-description">Manage guest stays and bookings awaiting check-in.</p>
                                <a href="${pageContext.request.contextPath}/staff/stays" class="btn staff-function-link"><span>Manage Guest Stays</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-violet">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-users" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Customer List</h3>
                                <p class="staff-card-description">View the list of customers and their information.</p>
                                <a href="${pageContext.request.contextPath}/customer?action=list" class="btn staff-function-link"><span>View Customer List</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-amber">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-money-check-dollar" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Check Out</h3>
                                <p class="staff-card-description">Manage guest check-out and invoices.</p>
                                <a href="${pageContext.request.contextPath}/CheckoutEmployee" class="btn staff-function-link"><span>Checkout Manager</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>

                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-teal">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-bell-concierge" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Check In</h3>
                                <p class="staff-card-description">Manage guest check-in.</p>
                                <a href="${pageContext.request.contextPath}/checkin" class="btn staff-function-link"><span>Checkin Manager</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>
                </c:if>

                <!-- 2. CUSTOMER SERVICE STAFF MENU -->
                <c:if test="${employee.position eq 'Customer service staff'}">
                    <div class="staff-function-item">
                        <div class="card staff-function-card staff-card-violet">
                            <div class="card-body staff-card-body">
                                <div class="staff-function-icon"><i class="fa-solid fa-comments" aria-hidden="true"></i></div>
                                <h3 class="staff-card-title">Review & Reply</h3>
                                <p class="staff-card-description">View customer information and reply to their complaints.</p>
                                <a href="${pageContext.request.contextPath}/complaint?action=list" class="btn staff-function-link"><span>Manage Complaints</span><i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                            </div>
                        </div>
                    </div>
                </c:if>

            </div> <!-- Đóng staff-functions-grid -->
        </section> <!-- Đóng section -->

        <!-- 3. SERVICE STAFF MENU (2 Cards riêng biệt) -->
        <c:if test="${employee.position == 'Service staff'}">
            <section class="mb-4" aria-labelledby="service-menu-title">
                <div class="d-flex align-items-center justify-content-between mb-4">
                    <h2 id="service-menu-title" class="h4 mb-0">Service Staff Menu</h2>
                    <span class="badge bg-warning text-dark">Service Staff</span>
                </div>
                <div class="row g-4">
                    <div class="col-md-6 col-xl-4">
                        <div class="card h-100 shadow-sm border-warning">
                            <div class="card-body d-flex flex-column">
                                <div class="mb-3"><i class="fa-solid fa-clipboard-list fa-2x text-warning"></i></div>
                                <h3 class="h5">Booked Services</h3>
                                <p class="text-muted flex-grow-1">Xem danh sách các dịch vụ khách đang lưu trú đã yêu cầu theo từng phòng.</p>
                                <a href="${pageContext.request.contextPath}/service?action=orders" class="btn btn-warning text-dark fw-semibold">
                                    View Booked Services
                                </a>
                            </div>
                        </div>
                    </div>
                    <div class="col-md-6 col-xl-4">
                        <div class="card h-100 shadow-sm border-warning">
                            <div class="card-body d-flex flex-column">
                                <div class="mb-3"><i class="fa-solid fa-bell-concierge fa-2x text-warning"></i></div>
                                <h3 class="h5">Service Catalog</h3>
                                <p class="text-muted flex-grow-1">Xem bảng giá và danh mục dịch vụ của khách sạn.</p>
                                <a href="${pageContext.request.contextPath}/service?action=manage" class="btn btn-outline-warning text-dark fw-semibold">
                                    View Service List
                                </a>
                            </div>
                        </div>
                    </div>
                </div>
            </section>
        </c:if>
    </div>
</layout:layout>