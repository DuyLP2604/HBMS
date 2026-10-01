<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Check-out & Billing" pageCss="checkout.css" pageJs="checkout.js" useBootstrap="true" bodyClass="bg-light">

    <div class="container-fluid px-4 py-4">
        <!-- Page Header -->
        <div class="mb-4">
            <h2 class="fw-bold text-dark mb-1">
                Check-out & Billing
            </h2>
            <p class="text-secondary">
                Request additional services, print the invoice,
                and proceed with the check-out process for the guest.
            </p>
        </div>

        <div class="row g-4">
            <!-- LEFT SIDE -->
            <div class="col-lg-8">
                <!-- Occupied Rooms -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">
                        Currently Occupied Rooms
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>Booking ID</th>
                                        <th>Hotel</th>
                                        <th>Customer</th>
                                        <th>Nationality</th>
                                        <th>Room</th>
                                        <th>Room Price</th>
                                        <th>Check-in</th>
                                        <th>Check-out</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>

                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty occupiedRooms}">
                                            <tr>
                                                <td colspan="9"
                                                    class="text-muted py-4">
                                                    No occupied rooms found.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${occupiedRooms}" var="room" >
                                                <tr>
                                                    <td class="fw-bold text-secondary">
                                                        <c:out value="${room.bookingID}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${room.hotelName}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${room.customerName}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${room.nationality}" />
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-secondary fs-6">
                                                            <c:out value="${room.roomNumber}" />
                                                        </span>
                                                    </td>
                                                    <td class="text-danger fw-semibold format-money">
                                                        <c:out value="${room.price}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${room.checkInDate}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${room.checkOutDate}" />
                                                    </td>
                                                    <td>
                                                        <a href="${pageContext.request.contextPath}/invoice?bookingID=${room.bookingID}"
                                                           class="btn btn-sm btn-outline-primary px-3" >
                                                            Select
                                                        </a>
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <!-- Recent Invoices -->
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">
                        10 Most Recent Invoices
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead>
                                    <tr>
                                        <th>Invoice ID</th>
                                        <th>Booking ID</th>
                                        <th>Hotel</th>
                                        <th>Customer</th>
                                        <th>Room</th>
                                        <th>Total Amount</th>
                                        <th>Invoice Date</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:choose>
                                        <c:when test="${empty paidInvoices}">
                                            <tr>
                                                <td colspan="7"
                                                    class="text-muted py-4">
                                                    No invoices have been paid yet.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach items="${paidInvoices}" var="inv" >
                                                <tr>
                                                    <td class="fw-bold text-secondary">
                                                        <c:out value="${inv.invoiceID}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${inv.bookingID}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${inv.hotelName}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${inv.customerName}" />
                                                    </td>
                                                    <td>
                                                        <span class="badge bg-info text-dark">
                                                            <c:out value="${inv.roomNumber}" />
                                                        </span>
                                                    </td>
                                                    <td class="text-success fw-bold format-money">
                                                        <c:out value="${inv.totalAmount}" />
                                                    </td>
                                                    <td>
                                                        <c:out value="${inv.invoiceDate}" />
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- RIGHT SIDE -->
            <div class="col-lg-4">
                <!-- Additional Services -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">
                        Additional Services
                    </div>
                    <div class="card-body">
                        <div class="d-flex gap-2 align-items-center bg-light p-3 rounded border border-secondary border-opacity-25 mb-3">
                            <select id="itemSelect" class="form-select" >

                                <option value="15000" data-name="Bottled Water" >
                                    Bottled Water (15,000 VND)
                                </option>
                                <option value="20000" data-name="Soft Drink" >
                                    Soft Drink (20,000 VND)
                                </option>
                                <option value="30000" data-name="Beer" >
                                    Beer (30,000 VND)
                                </option>
                                <option value="50000" data-name="Fruit" >
                                    Fruit (50,000 VND)
                                </option>
                                <option value="50000" data-name="Temple - Glass" >
                                    Temple - Glass (50,000 VND)
                                </option>
                                <option value="150000" data-name="Temple - Remote" >
                                    Temple - Remote (150,000 VND)
                                </option>
                            </select>
                            <input type="number" id="itemQty" class="form-control text-center service-qty-input" value="1" min="1" placeholder="Qty" >
                            <button type="button" class="btn btn-success text-nowrap px-3" onclick="addServiceItem()" >
                                <i class="fa-solid fa-plus me-1"></i>
                                Add
                            </button>
                        </div>
                        <div class="table-responsive border rounded">
                            <table class="table table-custom text-center align-middle mb-0" id="servicesTable" >
                                <thead>
                                    <tr>
                                        <th>Service Name</th>
                                        <th>Unit Price</th>
                                        <th>Quantity</th>
                                        <th>Total Amount</th>
                                        <th>Delete</th>
                                    </tr>
                                </thead>
                                <tbody id="servicesBody">
                                    <c:forEach items="${bookingServices}" var="srv">
                                        <tr>
                                            <td>
                                                <c:out value="${srv.serviceName}" />
                                            </td>
                                            <td class="format-money">
                                                <c:out value="${srv.unitPrice}" />
                                            </td>
                                            <td>1</td>
                                            <td class="item-total text-danger fw-bold format-money" data-value="${srv.unitPrice}" >
                                                <c:out value="${srv.unitPrice}" />
                                            </td>
                                            <td>
                                                <button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)" >
                                                    <i class="fa-solid fa-xmark"></i>
                                                </button>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Payment -->
                <form action="${pageContext.request.contextPath}/invoice" method="post" >
                    <div class="card shadow-sm border-0 border-top border-success border-4">
                        <div class="card-header bg-white text-success fw-bold text-uppercase py-3">
                            Payment / Check-out
                        </div>
                        <div class="card-body">
                            <c:choose>
                                <c:when test="${not empty selectedRoom}">
                                    <div class="bg-light p-3 rounded mb-4">
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-secondary">
                                                Customer:
                                            </span>
                                            <span class="fw-bold">
                                                <c:out value="${selectedRoom.customerName}" />
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-secondary">
                                                Room:
                                            </span>
                                            <span class="fw-bold">
                                                <c:out value="${selectedRoom.roomNumber}" />
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-secondary">
                                                Check-in:
                                            </span>
                                            <span class="fw-bold">
                                                <c:out value="${selectedRoom.checkInDate}" />
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span class="text-secondary">
                                                Check-out:
                                            </span>
                                            <span class="fw-bold">
                                                <c:out value="${selectedRoom.checkOutDate}" />
                                            </span>
                                        </div>
                                        <hr>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>
                                                Room Price:
                                            </span>
                                            <span class="fw-bold format-money-js">
                                                <c:out value="${selectedRoom.roomTotal}" />
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>
                                                Service Charge:
                                            </span>
                                            <span class="fw-bold text-danger format-money-js">
                                                <c:out value="${serviceTotal}" />
                                            </span>
                                        </div>
                                        <div class="d-flex justify-content-between mb-2">
                                            <span>
                                                VAT (8%):
                                            </span>
                                            <span class="fw-bold format-money-js">
                                                <c:out value="${vat}" />
                                            </span>
                                        </div>
                                        <hr>
                                        <div class="d-flex justify-content-between">
                                            <span class="fw-bold fs-5">
                                                Total Payment:
                                            </span>
                                            <span class="fw-bold fs-4 text-success format-money-js">
                                                <c:out value="${grandTotal}" />
                                            </span>
                                        </div>
                                    </div>
                                    <input type="hidden" name="grandTotal" value="${grandTotal}" >
                                    <input type="hidden" name="action" value="processCheckout" >
                                    <div class="row g-2">
                                        <div class="col-6">
                                            <a href="${pageContext.request.contextPath}/invoice?action=print&bookingID=${selectedRoom.bookingID}"
                                               target="_blank" class="btn btn-warning w-100" >
                                                <i class="fa-solid fa-print me-1"></i>
                                                Print Invoice
                                            </a>
                                        </div>

                                        <div class="col-6">
                                            <button type="submit" class="btn btn-success w-100" >
                                                <i class="fa-solid fa-credit-card me-1"></i>
                                                Check-out
                                            </button>
                                        </div>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="alert alert-info border-0 shadow-sm d-flex align-items-center py-4">
                                        <i class="fa-solid fa-hand-pointer fs-3 me-3 text-info"></i>
                                        <div class="fs-6">
                                            Please select a room from the list
                                            to proceed with payment.
                                        </div>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </div>
                </form>
            </div>
        </div>
    </div>
</layout:layout>
