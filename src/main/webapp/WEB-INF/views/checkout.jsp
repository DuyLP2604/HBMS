<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="vi_VN" scope="page" />
<layout:layout title="Invoices Management" pageCss="checkout.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-checkout-workspace">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">BILLING</p><h1>Invoice Management</h1><p class="ui-description">Review all issued invoices.</p></div></header>
        <section class="ui-panel" aria-labelledby="issuedInvoicesTitle">
            <div class="ui-panel-body"><div class="ui-section-heading checkout-list-heading"><h2 id="issuedInvoicesTitle">List of Issued Invoices</h2></div></div>
            <div class="ui-table-scroll" role="region" aria-label="Issued invoices">
                <table class="ui-table checkout-invoice-table">
                    <thead><tr><th scope="col">Invoice ID</th><th scope="col">Customer</th><th scope="col">Room(s)</th><th scope="col">Check-in Date</th><th scope="col">Check-out Date</th><th scope="col" class="checkout-amount">Total (VND)</th><th scope="col">Action</th></tr></thead>
                    <tbody>
                        <c:forEach var="inv" items="${paidInvoices}">
                            <c:url var="printInvoiceUrl" value="/invoice"><c:param name="action" value="print" /><c:param name="bookingId" value="${inv.bookingID}" /></c:url>
                            <tr>
                                <td class="ui-id"><c:out value="${inv.invoiceID}" /></td>
                                <td class="ui-name"><c:out value="${inv.customerName}" /></td>
                                <td><span class="checkout-room-chip"><c:out value="${inv.roomNumber}" /></span></td>
                                <td><c:out value="${inv.checkInDate}" /></td><td><c:out value="${inv.checkOutDate}" /></td>
                                <td class="checkout-amount checkout-invoice-amount"><fmt:formatNumber value="${inv.totalAmount}" type="number" maxFractionDigits="2" /></td>
                                <td><a href="<c:out value='${printInvoiceUrl}' />" target="_blank" rel="noopener" class="ui-button ui-button-soft ui-button-small"><i class="fa-solid fa-print" aria-hidden="true"></i> View / Print</a></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty paidInvoices}"><tr><td colspan="7" class="ui-empty-state"><i class="fa-solid fa-file-invoice" aria-hidden="true"></i><p>No completed invoices found.</p></td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</layout:layout>
