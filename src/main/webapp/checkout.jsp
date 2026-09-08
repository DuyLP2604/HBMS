<%-- 
    Document   : checkout
    Created on : Jul 10, 2026, 2:05:12 PM
    Author     : Admin
--%>

<%-- 
    Document   : checkout
    Created on : Jul 10, 2026, 2:05:12 PM
    Author     : Admin
--%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html lang="vi">
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <title>Hotel Management System - Invoice</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href ="css/checkout.css">
    </head>
    <body>
        <jsp:include page="components/navbar.jsp" />
        <div class="container-fluid px-4 py-4">
            <div class="mb-4">
                <h2 class="fw-bold text-dark mb-1">Check-out & Billing</h2>
                <p class="text-secondary">Request additional services, print the invoice, and proceed with the check-out process for the guest.</p>

                <c:if test="${param.status == 'success'}">
                    <div class="text-success fw-bold mb-3">
                        <i class="fa-solid fa-check me-1"></i> Success payment!
                    </div>
                </c:if>
            </div>

            <div class="row g-4">
                <div class="col-lg-8">
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">
                            List of rooms currently for rent
                        </div>
                        <div class="card-body p-0">
                            <div class="table-responsive">
                                <table class="table table-hover table-custom text-center align-middle mb-0">
                                    <thead>
                                        <tr>
                                            <th>Booking ID</th>
                                            <th>Hotel</th>
                                            <th>Customer</th>
                                            <th>National</th>
                                            <th>Room</th>
                                            <th>Room price</th>
                                            <th>Check-in</th>
                                            <th>Check-out</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty occupiedRooms}">
                                                <tr><td colspan="9" class="text-muted py-4">No rooms are currently available for rent.</td></tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${occupiedRooms}" var="room">
                                                    <tr>
                                                        <td class="fw-bold text-secondary">${room.bookingId}</td>
                                                        <td>${room.hotelName}</td>
                                                        <td>${room.customerName}</td>
                                                        <td>${room.nationality}</td>
                                                        <td><span class="badge bg-secondary fs-6">${room.roomNumber}</span></td>
                                                        <td class="text-danger fw-semibold format-money">${room.price}</td>
                                                        <td>${room.checkInDate}</td>
                                                        <td>${room.checkOutDate}</td>
                                                        <td>
                                                            <a href="invoice?bookingId=${room.bookingId}" class="btn btn-sm btn-outline-primary px-3">Select</a>
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

                    <div class="card shadow-sm border-0">
                        <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">
                            List of the 10 most recent invoices
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
                                            <th>Total amount</th>
                                            <th>Established Date</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:choose>
                                            <c:when test="${empty paidInvoices}">
                                                <tr><td colspan="7" class="text-muted py-4">No invoices have been paid yet.</td></tr>
                                            </c:when>
                                            <c:otherwise>
                                                <c:forEach items="${paidInvoices}" var="inv">
                                                    <tr>
                                                        <td class="fw-bold text-secondary">${inv.invoiceId}</td>
                                                        <td>${inv.bookingId}</td>
                                                        <td>${inv.hotelName}</td>
                                                        <td>${inv.customerName}</td>
                                                        <td><span class="badge bg-info text-dark">${inv.roomNumber}</span></td>
                                                        <td class="text-success fw-bold format-money">${inv.totalAmount}</td>
                                                        <td>${inv.invoiceDate}</td>
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

                <div class="col-lg-4">
                    <div class="card shadow-sm border-0 mb-4">
                        <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">
                            Invoice Lookup & Add Services
                        </div>
                        <div class="card-body">

                            <div class="d-flex gap-2 align-items-center bg-light p-3 rounded border border-secondary border-opacity-25 mb-3">
                                <select id="itemSelect" class="form-select">
                                    <option value="15000" data-name="Bottled Water">Bottled Water (15,000 VND)</option>
                                    <option value="20000" data-name="Soft Drink">Soft Drink (20,000 VND)</option>
                                    <option value="30000" data-name="Beer">Beer (30,000 VND)</option>
                                    <option value="50000" data-name="Fruit">Fruit (50,000 VND)</option>
                                    <option value="50000" data-name="Temple - Glass">Temple - Glass (50,000 VND)</option>
                                    <option value="150000" data-name="Temple - Remote">Temple - Remote (150,000 VND)</option>
                                </select>
                                <input type="number" id="itemQty" class="form-control text-center" value="1" min="1" style="width: 80px;" placeholder="SL">
                                <button type="button" class="btn btn-success text-nowrap px-3" onclick="addServiceItem()">
                                    <i class="fa-solid fa-plus me-1"></i> Add
                                </button>
                            </div>

                            <div class="table-responsive border rounded">
                                <table class="table table-custom text-center align-middle mb-0" id="servicesTable">
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
                                                <td>${srv.serviceName}</td>
                                                <td class="format-money">${srv.unitPrice}</td>
                                                <td>1</td>
                                                <td class="item-total text-danger fw-bold format-money" data-value="${srv.unitPrice}">${srv.unitPrice}</td>
                                                <td><button type="button" class="btn btn-sm btn-danger" onclick="removeRow(this)"><i class="fa-solid fa-xmark"></i></button></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </div>
                    </div>

                    <form action="invoice" method="POST">
                        <div class="card shadow-sm border-0 border-top border-success border-4">
                            <div class="card-header bg-white text-success fw-bold text-uppercase py-3">
                                Payment / Check-out
                            </div>
                            <div class="card-body">
                                <c:choose>
                                    <c:when test="${not empty selectedRoom}">
                                        <div class="bg-light p-3 rounded mb-4">

                                            <div class="d-flex justify-content-between mb-2">
                                                <span class="text-secondary">Customer:</span>
                                                <span class="fw-bold">${selectedRoom.customerName}</span>
                                            </div>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span class="text-secondary">Room:</span>
                                                <span class="fw-bold">${selectedRoom.roomNumber}</span>
                                            </div>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span class="text-secondary">Checkin:</span>
                                                <span class="fw-bold">${selectedRoom.checkInDate}</span>
                                            </div>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span class="text-secondary">Checkout:</span>
                                                <span class="fw-bold">${selectedRoom.checkOutDate}</span>
                                            </div>

                                            <hr>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span>Room Price:</span>

                                                <span class="fw-bold format-money-js">
                                                    ${selectedRoom.roomTotal}
                                                </span>
                                            </div>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span>Service Charge:</span>

                                                <span class="fw-bold text-danger format-money-js">
                                                    ${serviceTotal}
                                                </span>
                                            </div>

                                            <div class="d-flex justify-content-between mb-2">
                                                <span>VAT (8%)</span>

                                                <span class="fw-bold format-money-js">
                                                    ${vat}
                                                </span>
                                            </div>

                                            <hr>

                                            <div class="d-flex justify-content-between">

                                                <span class="fw-bold fs-5">
                                                    Total Payment:
                                                </span>

                                                <span class="fw-bold fs-4 text-success format-money-js">
                                                    ${grandTotal}
                                                </span>
                                            </div>
                                        </div>

                                        <input type="hidden"
                                               name="grandTotal"
                                               value="${grandTotal}">

                                        <input type="hidden"
                                               name="action"
                                               value="processCheckout">
                                        <tbody id="servicesBody">

                                            <c:forEach items="${bookingServices}" var="srv">
                                                <tr>
                                                    <td>${srv.serviceName}</td>
                                                    <td class="format-money">
                                                        ${srv.unitPrice}
                                                    </td>
                                                    <td>1</td>
                                                    <td class="text-danger fw-bold format-money">
                                                        ${srv.unitPrice}
                                                    </td>
                                                    <td>-</td>
                                                </tr>
                                            </c:forEach>
                                        </tbody>
                                        <div class="row g-2">
                                            <div class="col-6">
                                                <a href="invoice?action=print&bookingId=${selectedRoom.bookingId}"
                                                   target="_blank"
                                                   class="btn btn-warning w-100">

                                                    <i class="fa-solid fa-print"></i>
                                                    Print Invoice
                                                </a>
                                            </div>
                                        </div>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-info border-0 shadow-sm d-flex align-items-center py-4">
                                            <i class="fa-solid fa-hand-pointer fs-3 me-3 text-info"></i>
                                            <div class="fs-6">Please select a room from the list on the left to proceed with payment.</div>
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>
                    </form>
                </div>
            </div>
        </div>

        <script>
            function formatVND(amount) {
                let num = parseFloat(amount);

                if (isNaN(num))
                    return amount;

                return parseInt(num) + " đ";
            }

            function formatExistingMoney() {

                document.querySelectorAll('.format-money')
                        .forEach(el => {

                            let value =
                                    parseFloat(el.textContent.trim());

                            if (!isNaN(value)) {
                                el.textContent =
                                        formatVND(value);
                            }
                        });

                document.querySelectorAll('.format-money-js')
                        .forEach(el => {

                            let value =
                                    parseFloat(el.textContent.trim());

                            if (!isNaN(value)) {
                                el.textContent =
                                        formatVND(value);
                            }
                        });
            }


        </script>
    </body>
</html>