<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="vi_VN" scope="page" />
<layout:layout title="Invoices Management" pageCss="checkout.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container-fluid px-4 py-4">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
            <div>
                <h2 class="fw-bold text-dark mb-1">Invoice Management</h2>
                <p class="text-secondary mb-0">Review all issued invoices.</p>
            </div>
        </div>
        
        <div class="row">
            <div class="col-12">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-white text-primary fw-bold text-uppercase py-3">List of Issued Invoices</div>
                    <div class="card-body p-0">
                        <div class="table-responsive">
                            <table class="table table-hover table-custom text-center align-middle mb-0">
                                <thead class="table-light">
                                    <tr>
                                        <th>Invoice ID</th>
                                        <th>Customer</th>
                                        <th>Room(s)</th>
                                        <th>Check-in Date</th>
                                        <th>Check-out Date</th>
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
                                            <td><c:out value="${inv.CheckInDate}" /></td>
                                            <td><c:out value="${inv.CheckOutDate}" /></td>
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
        </div>
    </div>
</layout:layout>