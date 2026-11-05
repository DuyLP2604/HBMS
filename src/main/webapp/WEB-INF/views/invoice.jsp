<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Prints the invoice with accommodation and service charges.
--%>
<fmt:setLocale value="vi_VN" scope="page" />
<layout:layout title="Hotel Invoice" pageCss="invoice.css" pageJs="invoice.js" useBootstrap="true" showNavbar="false" bodyClass="hbms-ui-page invoice-page">
    <div class="hbms-ui ui-shell ui-invoice-workspace">
        <c:choose>
            <c:when test="${not empty invoiceDTO and not empty invoice}">
                <div class="print-controls">
                    <a href="${pageContext.request.contextPath}/invoice" class="ui-button ui-button-secondary"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to Billing</a>
                    <button type="button" id="printInvoiceButton" class="ui-button ui-button-primary"><i class="fa-solid fa-print" aria-hidden="true"></i> Print / Save PDF</button>
                </div>
                <article class="invoice-container">
                    <header class="invoice-heading">
                        <img class="invoice-logo" src="${pageContext.request.contextPath}/assets/images/invoice/simple-bear-hotel-logo.png"
                             width="812" height="812" alt="Simple Bear Hotel">
                        <h1>Invoice</h1>
                    </header>
                    <div class="invoice-stay">
                        <section>
                            <h2>Customer Info</h2>
                            <p class="invoice-guest-name"><c:out value="${invoiceDTO.customerName}" /></p>
                            <dl class="invoice-facts">
                                <div><dt>Booking</dt><dd><c:out value="${invoiceDTO.bookingID}" /></dd></div>
                                <div><dt>Room(s)</dt><dd><c:out value="${invoiceDTO.roomNumber}" /></dd></div>
                                <div><dt>Check-in</dt><dd><c:out value="${invoiceDTO.checkInDate}" /></dd></div>
                                <div><dt>Check-out</dt><dd><c:out value="${invoiceDTO.checkOutDate}" /></dd></div>
                                <div><dt>Nights</dt><dd><c:out value="${invoiceDTO.daysOfNight}" /></dd></div>
                            </dl>
                        </section>
                        <div class="invoice-reference">
                            <p>Invoice No. <strong><c:out value="${invoice.invoiceID}" /></strong></p>
                            <p>Issued <c:out value="${invoice.invoiceDate}" /></p>
                            <c:if test="${not empty invoice.employeeID}">
                                <p>Issued by <c:out value="${invoice.employeeID}" /></p>
                            </c:if>
                        </div>
                    </div>
                    <div class="invoice-table-scroll" role="region" aria-label="Invoice charges" tabindex="0">
                        <table class="invoice-table">
                            <colgroup>
                                <col class="invoice-description">
                                <col class="invoice-quantity">
                                <col class="invoice-price">
                                <col class="invoice-price">
                            </colgroup>
                            <thead>
                                <tr>
                                    <th scope="col">Item</th>
                                    <th scope="col">Quantity</th>
                                    <th scope="col" class="invoice-amount">Unit price (VND)</th>
                                    <th scope="col" class="invoice-amount">Total (VND)</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="roomLine" items="${accommodationLines}">
                                    <tr>
                                        <td>Accommodation &ndash; <c:out value="${roomLine.roomType}" /></td>
                                        <td>
                                            <c:out value="${roomLine.roomNights}" />
                                            <c:if test="${roomLine.roomCount gt 1}">
                                                <div class="small"><c:out value="${roomLine.roomCount}" /> rooms &times; <c:out value="${invoiceDTO.daysOfNight}" /> nights</div>
                                            </c:if>
                                        </td>
                                        <td class="invoice-amount"><fmt:formatNumber value="${roomLine.unitPrice}" type="number" maxFractionDigits="2" /></td>
                                        <td class="invoice-amount"><fmt:formatNumber value="${roomLine.subtotal}" type="number" maxFractionDigits="2" /></td>
                                    </tr>
                                </c:forEach>
                                <c:forEach var="srv" items="${bookingServices}">
                                    <tr>
                                        <td><c:out value="${srv.serviceName}" /></td>
                                        <td><c:out value="${srv.quantity}" /></td>
                                        <td class="invoice-amount"><fmt:formatNumber value="${srv.unitPrice}" type="number" maxFractionDigits="2" /></td>
                                        <td class="invoice-amount"><fmt:formatNumber value="${srv.subtotal}" type="number" maxFractionDigits="2" /></td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                    <section class="invoice-payment-summary" aria-label="Payment summary">
                        <c:set var="hasRemainingBalance" value="${invoiceDTO.remainingAmount gt 0 or invoiceDTO.remainingAmount lt 0}" />
                        <c:set var="paidInFull" value="${not empty invoiceDTO.remainingAmount and not hasRemainingBalance and invoiceDTO.paidAmount ge invoiceDTO.grandTotal}" />
                        <dl class="invoice-payment-amounts">
                            <c:choose>
                                <c:when test="${paidInFull and invoiceDTO.paidAmount le invoiceDTO.grandTotal}">
                                    <div class="invoice-total">
                                        <dt>Total paid (VND)</dt>
                                        <dd><fmt:formatNumber value="${invoiceDTO.paidAmount}" type="number" maxFractionDigits="2" /></dd>
                                    </div>
                                </c:when>
                                <c:otherwise>
                                    <div class="invoice-total">
                                        <dt>Invoice total (VND)</dt>
                                        <dd><fmt:formatNumber value="${invoiceDTO.grandTotal}" type="number" maxFractionDigits="2" /></dd>
                                    </div>
                                    <div>
                                        <dt>Amount paid (VND)</dt>
                                        <dd><fmt:formatNumber value="${invoiceDTO.paidAmount}" type="number" maxFractionDigits="2" /></dd>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                            <c:if test="${hasRemainingBalance}">
                                <div>
                                    <dt>Remaining balance (VND)</dt>
                                    <dd><fmt:formatNumber value="${invoiceDTO.remainingAmount}" type="number" maxFractionDigits="2" /></dd>
                                </div>
                            </c:if>
                        </dl>
                        <c:if test="${paidInFull}">
                            <p class="invoice-payment-status"><strong>Paid in full</strong></p>
                        </c:if>
                    </section>
                    <footer class="invoice-footer">
                        <p class="invoice-thanks">Thank you!</p>
                        <div class="invoice-issuer">
                            <p class="invoice-hotel"><c:out value="${empty hotelName ? selectedRoom.hotelName : hotelName}" /></p>
                            <c:if test="${not empty invoice.hotelAddress}">
                                <p><c:out value="${invoice.hotelAddress}" /></p>
                            </c:if>
                        </div>
                    </footer>
                </article>
            </c:when>
            <c:otherwise>
                <div class="ui-notice ui-notice-warning">The invoice is unavailable. Open an issued invoice from Billing.</div>
                <a href="${pageContext.request.contextPath}/invoice" class="ui-button ui-button-secondary">Back to Billing</a>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
