<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Checkout Detail" pageCss="checkout.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <a href="${pageContext.request.contextPath}/CheckoutEmployee" class="btn btn-secondary mb-4"><i class="bi bi-arrow-left"></i> Back to Checkout Management</a>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success" role="alert"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white py-3">
                <h3 class="h5 mb-0"><i class="bi bi-receipt me-2"></i> Payment &amp; Checkout Details</h3>
            </div>
            <div class="card-body p-4">
                <div class="row mb-4 bg-light p-3 rounded border g-3">
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Customer name</p>
                        <strong class="fs-5"><c:out value="${roomDetail.customerName}" /></strong>
                    </div>
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Room(s)</p>
                        <strong class="fs-5 text-info"><c:out value="${roomDetail.roomNumber}" /></strong>
                    </div>
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Booking ID</p>
                        <strong class="fs-5"><c:out value="${bookingID}" /></strong>
                    </div>
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Length of stay</p>
                        <strong><c:out value="${roomDetail.checkInDate}" /> <i class="bi bi-arrow-right mx-1"></i> <c:out value="${roomDetail.checkOutDate}" /></strong>
                        <br><small>(<c:out value="${roomDetail.totalDays}" /> night(s))</small>
                    </div>
                </div>
                <div class="mb-4">
                    <strong>Initial payment option:</strong>
                    <c:choose>
                        <c:when test="${paymentOption eq 'DEPOSIT'}">30% deposit, then pay the remaining balance</c:when>
                        <c:when test="${paymentOption eq 'FULL'}">Full payment</c:when>
                        <c:otherwise><c:out value="${paymentOption}" /></c:otherwise>
                    </c:choose>
                    <span class="ms-3"><strong>Payment status:</strong> <c:out value="${bookingSummary.paymentStatus}" /></span>
                </div>
                <h5 class="fw-bold text-secondary mb-3">Booking charges</h5>
                <div class="table-responsive mb-4">
                    <table class="table table-bordered align-middle text-center">
                        <thead class="table-secondary">
                            <tr>
                                <th>Number</th>
                                <th class="text-start">Name</th>
                                <th>Quantity</th>
                                <th>Unit price</th>
                                <th class="text-end">Subtotal</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>1</td>
                                <td class="text-start">Room charges for <c:out value="${roomDetail.totalDays}" /> night(s)</td>
                                <td>—</td>
                                <td>—</td>
                                <td class="text-end fw-bold"><fmt:formatNumber value="${roomDetail.roomTotal}" type="number" maxFractionDigits="2" /> VND</td>
                            </tr>
                            <c:forEach var="svc" items="${services}" varStatus="row">
                                <tr>
                                    <td>${row.index + 2}</td>
                                    <td class="text-start"><c:out value="${svc.serviceName}" /></td>
                                    <td><c:out value="${svc.quantity}" /></td>
                                    <td><fmt:formatNumber value="${svc.unitPrice}" type="number" maxFractionDigits="2" /> VND</td>
                                    <td class="text-end fw-bold"><fmt:formatNumber value="${svc.subtotal}" type="number" maxFractionDigits="2" /> VND</td>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
                <div class="row justify-content-end mb-4">
                    <div class="col-md-6 text-end border-start">
                        <p class="mb-2 fs-5">Total: <strong><fmt:formatNumber value="${totalAmount}" type="number" maxFractionDigits="2" /> VND</strong></p>
                        <p class="mb-2 text-muted">Amount paid: <strong><fmt:formatNumber value="${paidAmount}" type="number" maxFractionDigits="2" /> VND</strong></p>
                        <hr>
                        <h3 class="${remainingAmount gt 0 ? 'text-danger' : 'text-success'} mb-0">Remaining balance: <fmt:formatNumber value="${remainingAmount}" type="number" maxFractionDigits="2" /> VND</h3>
                    </div>
                </div>
                <c:choose>
                    <c:when test="${bookingSummary.bookingStatus eq 'CHECKED_OUT' and not empty issuedInvoice}">
                        <c:url var="printInvoiceUrl" value="/invoice">
                            <c:param name="action" value="print" />
                            <c:param name="bookingId" value="${bookingID}" />
                        </c:url>
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 bg-success-subtle p-3 rounded border border-success">
                            <div>Checkout is complete. Invoice: <strong><c:out value="${issuedInvoice.invoiceID}" /></strong></div>
                            <a href="<c:out value='${printInvoiceUrl}' />" class="btn btn-primary">View / Print Invoice</a>
                        </div>
                    </c:when>
                    <c:when test="${canCheckout and (bookingSummary.bookingStatus eq 'CHECKED_IN' or bookingSummary.bookingStatus eq 'CHECKED_OUT')}">
                        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 bg-success-subtle p-3 rounded border border-success">
                            <div><strong>Payment complete.</strong> You can confirm checkout and issue the invoice.</div>
                            <form action="${pageContext.request.contextPath}/CheckoutEmployee" method="post" id="invoiceForm" class="m-0">
                                <input type="hidden" name="action" value="exportInvoice">
                                <input type="hidden" name="bookingID" value="<c:out value='${bookingID}' />">
                                <button type="submit" class="btn btn-success btn-lg fw-bold px-4" id="btnInvoice"><i class="bi bi-printer me-2"></i> <c:out value="${bookingSummary.bookingStatus eq 'CHECKED_OUT' ? 'Issue Invoice' : 'Checkout & Issue Invoice'}" /></button>
                            </form>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="alert alert-warning mb-0" role="alert">
                            <c:choose>
                                <c:when test="${remainingAmount gt 0}">The booking has an unpaid balance. Ask the customer to complete payment from My Bookings before checkout.</c:when>
                                <c:otherwise>Checkout is unavailable. Reload this page and verify the booking payment status.</c:otherwise>
                            </c:choose>
                        </div>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </div>
    <script>
        document.addEventListener("DOMContentLoaded", function ()
        {
            const form = document.getElementById("invoiceForm");
            const button = document.getElementById("btnInvoice");
            if (!form || !button)
            {
                return;
            }
            const originalLabel = button.innerHTML;
            let submitted = false;
            form.addEventListener("submit", function (event)
            {
                if (submitted)
                {
                    event.preventDefault();
                    return;
                }
                submitted = true;
                button.disabled = true;
                button.textContent = "Processing checkout...";
            });
            window.addEventListener("pageshow", function (event)
            {
                if (event.persisted)
                {
                    submitted = false;
                    button.disabled = false;
                    button.innerHTML = originalLabel;
                }
            });
        });
    </script>
</layout:layout>