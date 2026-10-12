<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="vi_VN" scope="page" />
<jsp:useBean id="printedAt" class="java.util.Date" scope="request" />
<layout:layout title="Booking Invoice" pageCss="invoice.css" pageJs="invoice.js" useBootstrap="true" showNavbar="false" bodyClass="invoice-page">
    <div class="container">
        <c:choose>
            <c:when test="${not empty invoice and not empty invoiceID and not empty selectedRoom and bookingSummary.bookingStatus eq 'CHECKED_OUT' and bookingSummary.paymentStatus eq 'FULLY_PAID'}">
                <div class="invoice-container shadow-sm p-4 p-md-5 border rounded">
                    <div class="print-controls d-flex flex-wrap justify-content-between gap-2 mb-4">
                        <a href="${pageContext.request.contextPath}/invoice" class="btn btn-secondary">Back to Billing</a>
                        <button type="button" id="printInvoiceButton" class="btn btn-primary">Print / Save PDF</button>
                    </div>
                    <h1 class="text-center fs-4 mb-1 fw-bold"><c:out value="${selectedRoom.hotelName}" /></h1>
                    <h2 class="text-center text-uppercase fs-5 mb-4">Booking Invoice</h2>
                    <div class="row border-bottom pb-3 mb-4 g-2">
                        <div class="col-sm-6">Invoice ID: <strong><c:out value="${invoiceID}" /></strong></div>
                        <div class="col-sm-6 text-sm-end">Booking ID: <strong><c:out value="${selectedRoom.bookingID}" /></strong></div>
                        <div class="col-sm-6">Invoice date: <strong><c:out value="${invoiceDate}" /></strong></div>
                        <div class="col-sm-6 text-sm-end">Printed at: <strong><fmt:formatDate value="${printedAt}" pattern="dd/MM/yyyy HH:mm:ss" timeZone="Asia/Ho_Chi_Minh" /></strong></div>
                    </div>
                    <div class="row mb-4 g-3">
                        <div class="col-md-6">
                            <div class="mb-2"><span class="text-secondary">Customer:</span> <strong><c:out value="${selectedRoom.customerName}" /></strong></div>
                            <div class="mb-2"><span class="text-secondary">Check-in:</span> <strong><c:out value="${selectedRoom.checkInDate}" /></strong></div>
                            <div class="mb-2"><span class="text-secondary">Check-out:</span> <strong><c:out value="${selectedRoom.checkOutDate}" /></strong></div>
                        </div>
                        <div class="col-md-6">
                            <div class="mb-2"><span class="text-secondary">Room(s):</span> <strong><c:out value="${selectedRoom.roomNumber}" /></strong></div>
                            <div class="mb-2"><span class="text-secondary">Nights:</span> <strong><c:out value="${selectedRoom.totalDays}" /></strong></div>
                            <div class="mb-2"><span class="text-secondary">Issued by employee:</span> <strong><c:out value="${empty invoice.employeeID ? 'Not recorded' : invoice.employeeID}" /></strong></div>
                        </div>
                    </div>
                    <div class="table-responsive">
                        <table class="table table-bordered align-middle text-center mb-0">
                            <thead class="table-light">
                                <tr>
                                    <th scope="col">#</th>
                                    <th scope="col" class="text-start">Description</th>
                                    <th scope="col">Quantity</th>
                                    <th scope="col">Unit price (VND)</th>
                                    <th scope="col" class="text-end">Subtotal (VND)</th>
                                </tr>
                            </thead>
                            <tbody>
                                <tr>
                                    <td>1</td>
                                    <td class="text-start">Room charges for all booked rooms, <c:out value="${selectedRoom.totalDays}" /> night(s)</td>
                                    <td>—</td>
                                    <td>—</td>
                                    <td class="text-end fw-bold"><fmt:formatNumber value="${roomTotal}" type="number" maxFractionDigits="2" /></td>
                                </tr>
                                <c:forEach var="srv" items="${bookingServices}" varStatus="row">
                                    <tr>
                                        <td>${row.index + 2}</td>
                                        <td class="text-start"><c:out value="${srv.serviceName}" /></td>
                                        <td><c:out value="${srv.quantity}" /></td>
                                        <td><fmt:formatNumber value="${srv.unitPrice}" type="number" maxFractionDigits="2" /></td>
                                        <td class="text-end fw-bold"><fmt:formatNumber value="${srv.subtotal}" type="number" maxFractionDigits="2" /></td>
                                    </tr>
                                </c:forEach>
                                <tr>
                                    <td colspan="4" class="text-end fw-bold">Invoice total</td>
                                    <td class="text-end fw-bold fs-5"><fmt:formatNumber value="${grandTotal}" type="number" maxFractionDigits="2" /></td>
                                </tr>
                                <tr>
                                    <td colspan="4" class="text-end">Amount paid</td>
                                    <td class="text-end"><fmt:formatNumber value="${paidAmount}" type="number" maxFractionDigits="2" /></td>
                                </tr>
                                <tr>
                                    <td colspan="4" class="text-end fw-bold">Remaining balance</td>
                                    <td class="text-end fw-bold"><fmt:formatNumber value="${remainingAmount}" type="number" maxFractionDigits="2" /></td>
                                </tr>
                            </tbody>
                        </table>
                    </div>
                    <div class="d-flex flex-wrap justify-content-between align-items-center gap-2 mt-3">
                        <div>
                            Initial payment option:
                            <c:choose>
                                <c:when test="${bookingSummary.paymentOption eq 'DEPOSIT'}">30% deposit</c:when>
                                <c:when test="${bookingSummary.paymentOption eq 'FULL'}">Full payment</c:when>
                                <c:otherwise><c:out value="${bookingSummary.paymentOption}" /></c:otherwise>
                            </c:choose>
                        </div>
                        <strong class="text-success">FULLY PAID</strong>
                    </div>
                    <div class="row text-center mt-5 invoice-signatures">
                        <div class="col-6">
                            <p class="fw-bold mb-0">Receptionist</p>
                            <div class="signature-space"></div>
                            <p class="small text-secondary">Signature</p>
                        </div>
                        <div class="col-6">
                            <p class="fw-bold mb-0">Customer</p>
                            <div class="signature-space"></div>
                            <p class="fw-bold fst-italic"><c:out value="${selectedRoom.customerName}" /></p>
                        </div>
                    </div>
                </div>
            </c:when>
            <c:otherwise>
                <div class="alert alert-warning mt-4">The invoice is unavailable. Open an issued invoice from Billing.</div>
                <a href="${pageContext.request.contextPath}/invoice" class="btn btn-secondary">Back to Billing</a>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>