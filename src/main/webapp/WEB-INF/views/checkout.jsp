<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="vi_VN" scope="page" />
<layout:layout title="Checkout & Billing" pageCss="checkout.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container-fluid px-4 py-4">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
            <div>
                <h2 class="fw-bold text-dark mb-1">Checkout &amp; Billing</h2>
                <p class="text-secondary mb-0">Review booking charges, complete checkout, and print issued invoices.</p>
            </div>
        </div>
        
        <div class="row g-4">
            <!-- CỘT TRÁI -->
            <div class="col-lg-7">
                <!-- Bảng 1: Currently Checked-in -->
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">Currently Checked-in Bookings</div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <!-- Đã bỏ: Hotel, Nationality, Check-in, Check-out -->
                                        <th>Booking ID</th>
                                        <th>Customer</th>
                                        <th>Room(s)</th>
                                        <th>Room Type(s)</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="room" items="${occupiedRooms}">
                                        <c:url var="selectBookingUrl" value="/invoice">
                                            <c:param name="bookingID" value="${room.bookingID}" />
                                        </c:url>
                                        <tr>
                                            <td class="fw-bold text-secondary"><c:out value="${room.bookingID}" /></td>
                                            <td><c:out value="${room.customerName}" /></td>
                                            <td><span class="badge bg-secondary fs-6"><c:out value="${room.roomNumber}" /></span></td>
                                            <td><c:out value="${room.roomType}" /></td>
                                            <td><a href="<c:out value='${selectBookingUrl}' />" class="btn btn-sm btn-outline-primary px-3">Select</a></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty occupiedRooms}">
                                        <tr><td colspan="5" class="text-muted py-4">No checked-in bookings found.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>

                <!-- Bảng 2: Top 10 Invoices -->
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">10 Most Recent Issued Invoices</div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <!-- Đã bỏ: Booking ID, Hotel, Invoice Date -->
                                        <th>Invoice ID</th>
                                        <th>Customer</th>
                                        <th>Room(s)</th>
                                        <th>Total (VND)</th>
                                        <th>Action</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:forEach var="inv" items="${paidInvoices}">
                                        <c:url var="printInvoiceUrl" value="/invoice">
                                            <c:param name="action" value="print" />
                                            <c:param name="bookingId" value="${inv.bookingID}" />
                                        </c:url>
                                        <tr>
                                            <td class="fw-bold text-secondary"><c:out value="${inv.invoiceID}" /></td>
                                            <td><c:out value="${inv.customerName}" /></td>
                                            <td><span class="badge bg-info text-dark"><c:out value="${inv.roomNumber}" /></span></td>
                                            <td class="text-success fw-bold"><fmt:formatNumber value="${inv.totalAmount}" type="number" maxFractionDigits="2" /></td>
                                            <td><a href="<c:out value='${printInvoiceUrl}' />" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">View / Print</a></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty paidInvoices}">
                                        <tr><td colspan="5" class="text-muted py-4">No completed invoices found.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <!-- CỘT PHẢI (Booking Charges & Checkout) -->
            <div class="col-lg-5">
                <c:choose>
                    <c:when test="${not empty invoiceDTO}">
                        <div class="card shadow-sm border-0 border-top border-success border-4">
                            <div class="card-header bg-white text-success fw-bold text-uppercase py-3">Booking Charges &amp; Checkout</div>
                            <div class="card-body">
                                <div class="bg-light p-3 rounded mb-3">
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Booking:</span><strong><c:out value="${invoiceDTO.bookingID}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Customer:</span><strong><c:out value="${invoiceDTO.customerName}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Room(s):</span><strong><c:out value="${invoiceDTO.roomNumber}" /></strong></div>
                                    
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Days of night:</span><strong><c:out value="${invoiceDTO.daysOfNight}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span>Services used:</span>
                                        <strong class="text-end"><c:out value="${invoiceDTO.usedServices}" /></strong>
                                    </div>
                                    <hr>
                                    
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Room charges:</span><strong><fmt:formatNumber value="${invoiceDTO.roomCharges}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Service charges:</span><strong><fmt:formatNumber value="${invoiceDTO.serviceCharges}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Total:</span><strong><fmt:formatNumber value="${invoiceDTO.grandTotal}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Amount paid:</span><strong><fmt:formatNumber value="${invoiceDTO.paidAmount}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <hr>
                                    <div class="d-flex justify-content-between gap-2">
                                        <strong>Remaining:</strong>
                                        <strong class="${invoiceDTO.remainingAmount gt 0 ? 'text-danger' : 'text-success'}">
                                            <fmt:formatNumber value="${invoiceDTO.remainingAmount}" type="number" maxFractionDigits="2" /> VND
                                        </strong>
                                    </div>
                                </div>

                                <form action="${pageContext.request.contextPath}/invoice" method="post" id="checkoutForm">
                                    <input type="hidden" name="action" value="print" />
                                    <input type="hidden" name="bookingID" value="${invoiceDTO.bookingID}" />
                                    
                                    <div class="form-check mb-3 mt-3">
                                        <input class="form-check-input border-secondary" type="checkbox" id="paymentConfirmed" onchange="toggleButtons()">
                                        <label class="form-check-label fw-bold" for="paymentConfirmed">
                                            KH đã thanh toán thành công
                                        </label>
                                    </div>
                                    
                                    <div class="d-flex gap-2">
                                        <button type="submit" id="printBtn" class="btn btn-success flex-grow-1" disabled>Print Invoice</button>
                                        <a href="${pageContext.request.contextPath}/invoice" id="exitBtn" class="btn btn-secondary flex-grow-1 disabled-link" aria-disabled="true">Exit</a>
                                    </div>
                                </form>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-info">Select a booking to review its recorded charges and payment status.</div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>

    <!-- Khai báo style nhỏ để vô hiệu hóa thẻ <a> -->
    <style>
        .disabled-link {
            pointer-events: none;
            opacity: 0.6;
        }
    </style>

    <!-- Script điều khiển bật/tắt 2 nút bằng Checkbox -->
    <script>
        function toggleButtons() {
            const isChecked = document.getElementById('paymentConfirmed').checked;
            const printBtn = document.getElementById('printBtn');
            const exitBtn = document.getElementById('exitBtn');
            
            printBtn.disabled = !isChecked;
            
            if (isChecked) {
                exitBtn.classList.remove('disabled-link');
                exitBtn.removeAttribute('aria-disabled');
            } else {
                exitBtn.classList.add('disabled-link');
                exitBtn.setAttribute('aria-disabled', 'true');
            }
        }
    </script>
</layout:layout>