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
        
        <div class="row g-4">
            <div class="col-lg-7">
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
                                        <c:url var="viewInvoiceUrl" value="/invoice">
                                            <c:param name="bookingID" value="${inv.bookingID}" />
                                        </c:url>
                                        <c:url var="printInvoiceUrl" value="/invoice">
                                            <c:param name="action" value="print" />
                                            <c:param name="bookingID" value="${inv.bookingID}" />
                                        </c:url>
                                        <tr>
                                            <td class="fw-bold text-secondary"><c:out value="${inv.invoiceID}" /></td>
                                            <td><c:out value="${inv.customerName}" /></td>
                                            <td><span class="badge bg-info text-dark"><c:out value="${inv.roomNumber}" /></span></td>
                                            <td><c:out value="${inv.checkInDate}" /></td>
                                            <td><c:out value="${inv.checkOutDate}" /></td>
                                            <td class="text-success fw-bold"><fmt:formatNumber value="${inv.totalAmount}" type="number" maxFractionDigits="2" /></td>
                                            <td>
                                                <div class="d-flex justify-content-center gap-2">
                                                    <a href="<c:out value='${viewInvoiceUrl}' />" class="btn btn-sm btn-outline-info fw-bold">View</a>
                                                    <a href="<c:out value='${printInvoiceUrl}' />" target="_blank" rel="noopener" class="btn btn-sm btn-outline-primary">Print</a>
                                                </div>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                    <c:if test="${empty paidInvoices}">
                                        <tr><td colspan="7" class="text-muted py-4">No completed invoices found.</td></tr>
                                    </c:if>
                                </tbody>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

            <div class="col-lg-5">
                <c:choose>
                    <c:when test="${not empty invoiceDTO}">
                        <div class="card shadow-sm border-0 border-top border-info border-4">
                            <div class="card-header bg-white text-info fw-bold text-uppercase py-3">
                                Invoice Details
                            </div>
                            <div class="card-body">
                                <div class="bg-light p-4 rounded">
                                    
                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span class="text-muted">Invoice ID:</span>
                                        <strong class="text-primary fs-5"><c:out value="${invoice.invoiceID}" default="N/A" /></strong>
                                    </div>
                                    <div class="d-flex justify-content-between gap-2 mb-3">
                                        <span class="text-muted">Invoice Date:</span>
                                        <strong><c:out value="${invoice.invoiceDate}" default="N/A" /></strong>
                                    </div>
                                    <hr class="border-secondary opacity-25">

                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span class="text-muted">Customer:</span>
                                        <strong><c:out value="${invoiceDTO.customerName}" /></strong>
                                    </div>
                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span class="text-muted">Phone:</span>
                                        <strong><c:out value="${not empty roomDetail.phone ? roomDetail.phone : 'N/A'}" /></strong>
                                    </div>
                                    <div class="d-flex justify-content-between gap-2 mb-3">
                                        <span class="text-muted">Email:</span>
                                        <strong><c:out value="${not empty roomDetail.email ? roomDetail.email : 'N/A'}" /></strong>
                                    </div>
                                    <hr class="border-secondary opacity-25">

                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span class="text-muted">Room Type(s):</span>
                                        <strong><c:out value="${not empty roomDetail.roomType ? roomDetail.roomType : 'N/A'}" /></strong>
                                    </div>
                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span class="text-muted">Room(s):</span>
                                        <strong><c:out value="${invoiceDTO.roomNumber}" /></strong>
                                    </div>
                                    <div class="d-flex justify-content-between gap-2 mb-2">
                                        <span class="text-muted">Check-in Date:</span>
                                        <strong><c:out value="${invoiceDTO.checkInDate}" /></strong>
                                    </div>
                                    <div class="d-flex justify-content-between gap-2 mb-3">
                                        <span class="text-muted">Check-out Date:</span>
                                        <strong><c:out value="${invoiceDTO.checkOutDate}" /></strong>
                                    </div>
                                    <hr class="border-secondary opacity-25">

                                    <div class="mb-3">
                                        <span class="text-muted d-block mb-1">Services Used:</span>
                                        <strong class="text-dark"><c:out value="${invoiceDTO.usedServices}" /></strong>
                                    </div>
                                    <hr class="border-secondary opacity-25">

                                    <div class="d-flex justify-content-between align-items-center mt-3">
                                        <span class="text-muted fs-5">Total Amount:</span>
                                        <strong class="text-success fs-4"><fmt:formatNumber value="${invoiceDTO.grandTotal}" type="number" maxFractionDigits="2" /> VND</strong>
                                    </div>
                                </div>
                                
                                <div class="mt-4 text-center">
                                    <a href="${pageContext.request.contextPath}/invoice?action=print&bookingID=${invoiceDTO.bookingID}" 
                                       class="btn btn-primary px-5 fw-bold shadow-sm" target="_blank">
                                        Print Invoice
                                    </a>
                                </div>
                            </div>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-info shadow-sm border-0 d-flex align-items-center">
                            <i class="fa-solid fa-hand-pointer fs-4 me-3 text-info"></i> 
                            <div>Select <strong>View</strong> on an invoice from the list to display its full details here.</div>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
</layout:layout>