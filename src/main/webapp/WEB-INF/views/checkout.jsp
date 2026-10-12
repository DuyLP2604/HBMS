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
            <a href="${pageContext.request.contextPath}/CheckoutEmployee" class="btn btn-primary">Checkout Management</a>
        </div>
        <div class="row g-4">
            <div class="col-lg-8">
                <div class="card shadow-sm border-0 mb-4">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">Currently Checked-in Bookings</div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Booking ID</th>
                                        <th>Hotel</th>
                                        <th>Customer</th>
                                        <th>Nationality</th>
                                        <th>Room(s)</th>
                                        <th>Room Type(s)</th>
                                        <th>Check-in</th>
                                        <th>Check-out</th>
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
                                            <td><c:out value="${room.hotelName}" /></td>
                                            <td><c:out value="${room.customerName}" /></td>
                                            <td><c:out value="${room.nationality}" /></td>
                                            <td><span class="badge bg-secondary fs-6"><c:out value="${room.roomNumber}" /></span></td>
                                            <td><c:out value="${room.roomType}" /></td>
                                            <td><c:out value="${room.checkInDate}" /></td>
                                            <td><c:out value="${room.checkOutDate}" /></td>
                                            <td><a href="<c:out value='${selectBookingUrl}' />" class="btn btn-sm btn-outline-primary px-3">Select</a></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty occupiedRooms}">
                                        <tr><td colspan="9" class="text-muted py-4">No checked-in bookings found.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">10 Most Recent Issued Invoices</div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Invoice ID</th>
                                        <th>Booking ID</th>
                                        <th>Hotel</th>
                                        <th>Customer</th>
                                        <th>Room(s)</th>
                                        <th>Total (VND)</th>
                                        <th>Invoice Date</th>
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
                                            <td><c:out value="${inv.bookingID}" /></td>
                                            <td><c:out value="${inv.hotelName}" /></td>
                                            <td><c:out value="${inv.customerName}" /></td>
                                            <td><span class="badge bg-info text-dark"><c:out value="${inv.roomNumber}" /></span></td>
                                            <td class="text-success fw-bold"><fmt:formatNumber value="${inv.totalAmount}" type="number" maxFractionDigits="2" /></td>
                                            <td><c:out value="${inv.invoiceDate}" /></td>
                                            <td><a href="<c:out value='${printInvoiceUrl}' />" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">View / Print</a></td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty paidInvoices}">
                                        <tr><td colspan="8" class="text-muted py-4">No completed invoices found.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>
            <div class="col-lg-4">
                <c:choose>
                    <c:when test="${not empty selectedRoom}">
                        <div class="card shadow-sm border-0 mb-4">
                            <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">Recorded Services</div>
                            <div class="card-body p-0">
                                <div class="table-responsive">
                                    <table class="table text-center align-middle mb-0">
                                        <thead class="table-light">
                                            <tr><th>Service</th><th>Unit Price</th><th>Quantity</th><th>Subtotal</th></tr>
                                        </thead>
                                        <tbody>
                                            <c:forEach var="srv" items="${bookingServices}">
                                                <tr>
                                                    <td><c:out value="${srv.serviceName}" /></td>
                                                    <td><fmt:formatNumber value="${srv.unitPrice}" type="number" maxFractionDigits="2" /></td>
                                                    <td><c:out value="${srv.quantity}" /></td>
                                                    <td class="fw-bold"><fmt:formatNumber value="${srv.subtotal}" type="number" maxFractionDigits="2" /></td>
                                                </tr>
                                            </c:forEach>
                                            <c:if test="${empty bookingServices}">
                                                <tr><td colspan="4" class="text-muted py-3">No services recorded for this booking.</td></tr>
                                            </c:if>
                                        </tbody>
                                    </table>
                                </div>
                                <p class="small text-muted px-3 py-2 mb-0">Amounts are shown in VND.</p>
                            </div>
                        </div>
                        <div class="card shadow-sm border-0 border-top border-success border-4">
                            <div class="card-header bg-white text-success fw-bold text-uppercase py-3">Booking Charges &amp; Checkout</div>
                            <div class="card-body">
                                <div class="bg-light p-3 rounded mb-3">
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Booking:</span><strong><c:out value="${selectedRoom.bookingID}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Customer:</span><strong><c:out value="${selectedRoom.customerName}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Room(s):</span><strong><c:out value="${selectedRoom.roomNumber}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Check-in:</span><strong><c:out value="${selectedRoom.checkInDate}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Check-out:</span><strong><c:out value="${selectedRoom.checkOutDate}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Booking status:</span><strong><c:out value="${bookingSummary.bookingStatus}" /></strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Payment status:</span><strong><c:out value="${bookingSummary.paymentStatus}" /></strong></div>
                                    <hr>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Room charges:</span><strong><fmt:formatNumber value="${roomTotal}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Service charges:</span><strong><fmt:formatNumber value="${serviceTotal}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Total:</span><strong><fmt:formatNumber value="${grandTotal}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <div class="d-flex justify-content-between gap-2 mb-2"><span>Amount paid:</span><strong><fmt:formatNumber value="${paidAmount}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                    <hr>
                                    <div class="d-flex justify-content-between gap-2"><strong>Remaining:</strong><strong class="${remainingAmount gt 0 ? 'text-danger' : 'text-success'}"><fmt:formatNumber value="${remainingAmount}" type="number" maxFractionDigits="2" /> VND</strong></div>
                                </div>
                                <c:if test="${remainingAmount gt 0}">
                                    <div class="alert alert-warning">Complete the remaining payment before checkout.</div>
                                </c:if>
                                <c:choose>
                                    <c:when test="${bookingSummary.bookingStatus eq 'CHECKED_IN' or bookingSummary.bookingStatus eq 'CHECKED_OUT'}">
                                        <c:url var="checkoutDetailsUrl" value="/CheckoutEmployee">
                                            <c:param name="action" value="detail" />
                                            <c:param name="bookingID" value="${selectedRoom.bookingID}" />
                                        </c:url>
                                        <a href="<c:out value='${checkoutDetailsUrl}' />" class="btn btn-success w-100">View Checkout Details</a>
                                    </c:when>
                                    <c:otherwise>
                                        <div class="alert alert-info mb-0">This booking is not ready for checkout.</div>
                                    </c:otherwise>
                                </c:choose>
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
</layout:layout>