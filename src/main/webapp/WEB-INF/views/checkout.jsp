<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<fmt:setLocale value="vi_VN" scope="page" />

<layout:layout title="Invoices Management"
               pageCss="checkout.css"
               useBootstrap="true"
               bodyClass="hbms-ui-page">

    <style>
        .hbms-ui.ui-shell.invoice-management {
            width: calc(100% - clamp(24px, 3vw, 48px));
            max-width: none;
            padding-top: 28px;
        }
        .invoice-management-grid { display: grid; gap: 18px; align-items: start; }
        .invoice-management-grid > * { min-width: 0; }
        .invoice-management .ui-panel-body { padding: 18px; }
        .invoice-management .checkout-invoice-table { min-width: 760px; }
        .invoice-management .checkout-invoice-table th,
        .invoice-management .checkout-invoice-table td {
            padding: 10px 9px;
            vertical-align: middle;
        }
        .invoice-management .checkout-invoice-table :is(th, td):nth-child(n+4) {
            white-space: nowrap;
        }
        .invoice-management .checkout-invoice-table .ui-empty-state {
            padding: 24px 12px;
        }
        .invoice-management #invoiceDetails dd { overflow-wrap: anywhere; }
        @media (min-width: 1200px) {
            .invoice-management-grid {
                grid-template-columns: minmax(0, 1fr) clamp(320px, 28%, 460px);
            }
        }
    </style>

    <div class="hbms-ui ui-shell ui-checkout-workspace invoice-management">
        <header class="ui-page-heading">
            <div>
                <p class="ui-eyebrow">BILLING</p>
                <h1>Invoice Management</h1>
                <p class="ui-description">Review all issued invoices.</p>
            </div>
        </header>

        <div class="invoice-management-grid">
        <section class="ui-panel" aria-labelledby="issuedInvoicesTitle">
            <div class="ui-panel-body">
                <div class="ui-section-heading checkout-list-heading">
                    <h2 id="issuedInvoicesTitle">List of Issued Invoices</h2>
                </div>
            </div>

            <div class="ui-table-scroll" role="region" aria-label="Issued invoices">
                <table class="ui-table checkout-invoice-table">
                    <thead>
                        <tr>
                            <th scope="col">Invoice ID</th>
                            <th scope="col">Customer</th>
                            <th scope="col">Room(s)</th>
                            <th scope="col">Check-in Date</th>
                            <th scope="col">Check-out Date</th>
                            <th scope="col" class="checkout-amount">Total (VND)</th>
                            <th scope="col">Action</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:forEach var="inv" items="${paidInvoices}">
                            <c:url var="viewInvoiceUrl" value="/invoice">
                                <c:param name="bookingId" value="${inv.bookingID}" />
                            </c:url>
                            <c:url var="printInvoiceUrl" value="/invoice">
                                <c:param name="action" value="print" />
                                <c:param name="bookingId" value="${inv.bookingID}" />
                            </c:url>

                            <tr>
                                <td class="ui-id"><c:out value="${inv.invoiceID}" /></td>
                                <td class="ui-name"><c:out value="${inv.customerName}" /></td>
                                <td>
                                    <span class="checkout-room-chip">
                                        <c:out value="${inv.roomNumber}" />
                                    </span>
                                </td>
                                <td><c:out value="${inv.checkInDate}" /></td>
                                <td><c:out value="${inv.checkOutDate}" /></td>
                                <td class="checkout-amount checkout-invoice-amount">
                                    <fmt:formatNumber value="${inv.totalAmount}"
                                                      type="number"
                                                      maxFractionDigits="2" />
                                </td>
                                <td>
                                    <div class="d-flex flex-wrap gap-2">
                                        <a href="<c:out value='${viewInvoiceUrl}' />#invoiceDetails"
                                           class="ui-button ui-button-secondary ui-button-small">
                                            View
                                        </a>
                                        <a href="<c:out value='${printInvoiceUrl}' />"
                                           target="_blank"
                                           rel="noopener"
                                           class="ui-button ui-button-soft ui-button-small">
                                            <i class="fa-solid fa-print" aria-hidden="true"></i>
                                            Print
                                        </a>
                                    </div>
                                </td>
                            </tr>
                        </c:forEach>

                        <c:if test="${empty paidInvoices}">
                            <tr>
                                <td colspan="7" class="ui-empty-state">
                                    <i class="fa-solid fa-file-invoice" aria-hidden="true"></i>
                                    <p>No completed invoices found.</p>
                                </td>
                            </tr>
                        </c:if>
                    </tbody>
                </table>
            </div>
        </section>

        <aside aria-label="Invoice details">
            <c:if test="${empty invoiceDTO or empty invoice}">
                <div class="ui-panel">
                    <div class="ui-panel-body">
                        Select <strong>View</strong> on an invoice to see its details here.
                    </div>
                </div>
            </c:if>
        <c:if test="${not empty invoiceDTO and not empty invoice}">
            <c:url var="selectedPrintUrl" value="/invoice">
                <c:param name="action" value="print" />
                <c:param name="bookingId" value="${invoiceDTO.bookingID}" />
            </c:url>

            <section id="invoiceDetails" class="ui-panel"
                     aria-labelledby="invoiceDetailsTitle">
                <div class="ui-panel-body">
                    <div class="ui-section-heading checkout-list-heading">
                        <h2 id="invoiceDetailsTitle">Invoice Details</h2>
                    </div>

                    <dl class="row gy-2 mt-2 mb-3">
                        <dt class="col-sm-4">Invoice ID</dt>
                        <dd class="col-sm-8"><c:out value="${invoice.invoiceID}" /></dd>

                        <dt class="col-sm-4">Invoice Date</dt>
                        <dd class="col-sm-8"><c:out value="${invoice.invoiceDate}" /></dd>

                        <dt class="col-sm-4">Customer</dt>
                        <dd class="col-sm-8"><c:out value="${invoiceDTO.customerName}" /></dd>

                        <dt class="col-sm-4">Phone</dt>
                        <dd class="col-sm-8">
                            <c:out value="${not empty roomDetail.phone ? roomDetail.phone : 'N/A'}" />
                        </dd>

                        <dt class="col-sm-4">Email</dt>
                        <dd class="col-sm-8">
                            <c:out value="${not empty roomDetail.email ? roomDetail.email : 'N/A'}" />
                        </dd>

                        <dt class="col-sm-4">Room Type(s)</dt>
                        <dd class="col-sm-8">
                            <c:out value="${not empty roomDetail.roomType ? roomDetail.roomType : 'N/A'}" />
                        </dd>

                        <dt class="col-sm-4">Room(s)</dt>
                        <dd class="col-sm-8"><c:out value="${invoiceDTO.roomNumber}" /></dd>

                        <dt class="col-sm-4">Check-in Date</dt>
                        <dd class="col-sm-8"><c:out value="${invoiceDTO.checkInDate}" /></dd>

                        <dt class="col-sm-4">Check-out Date</dt>
                        <dd class="col-sm-8"><c:out value="${invoiceDTO.checkOutDate}" /></dd>

                        <dt class="col-sm-4">Services Used</dt>
                        <dd class="col-sm-8"><c:out value="${invoiceDTO.usedServices}" /></dd>

                        <dt class="col-sm-4">Total Amount</dt>
                        <dd class="col-sm-8 fw-bold">
                            <fmt:formatNumber value="${invoice.totalAmount}"
                                              type="number"
                                              maxFractionDigits="2" /> VND
                        </dd>
                    </dl>

                    <a href="<c:out value='${selectedPrintUrl}' />"
                       target="_blank"
                       rel="noopener"
                       class="ui-button ui-button-primary">
                        <i class="fa-solid fa-print" aria-hidden="true"></i>
                        Print Invoice
                    </a>
                </div>
            </section>
        </c:if>
        </aside>
        </div>
    </div>
</layout:layout>