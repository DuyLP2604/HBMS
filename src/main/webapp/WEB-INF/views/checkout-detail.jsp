<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Checkout Detail" pageCss="checkout.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        
        <a href="${pageContext.request.contextPath}/CheckoutEmployee" class="btn btn-secondary mb-4">
            <i class="bi bi-arrow-left"></i> Return home
        </a>

        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white py-3">
                <h3 class="h5 mb-0"><i class="bi bi-receipt me-2"></i> Payment & Checkout Details</h3>
            </div>
            
            <div class="card-body p-4">
                <div class="row mb-4 bg-light p-3 rounded border">
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Customer name</p>
                        <strong class="fs-5">${roomDetail.customerName}</strong>
                    </div>
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Room</p>
                        <strong class="fs-5 text-info">${roomDetail.roomNumber}</strong>
                    </div>
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Booking ID</p>
                        <strong class="fs-5">${roomDetail.bookingID}</strong>
                    </div>
                    <div class="col-md-3">
                        <p class="mb-1 text-muted">Length of stay</p>
                        <strong>${roomDetail.checkInDate} <i class="bi bi-arrow-right mx-1"></i> ${roomDetail.checkOutDate}</strong>
                        <br><small>(${roomDetail.totalDays} night)</small>
                    </div>
                </div>

                <h5 class="fw-bold text-secondary mb-3">List of services</h5>
                <div class="table-responsive mb-4">
                    <table class="table table-bordered align-middle text-center">
                        <thead class="table-secondary">
                            <tr>
                                <th>Number</th>
                                <th class="text-start">Name</th>
                                <th>Quantity</th>
                                <th>Unit price</th>
                                <th class="text-end">Total Amount</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>1</td>
                                <td class="text-start">Room charge (${roomDetail.totalDays} )</td>
                                <td>1</td>
                                <td><fmt:formatNumber value="${roomDetail.roomTotal}" type="number" maxFractionDigits="0"/>đ</td>
                                <td class="text-end fw-bold"><fmt:formatNumber value="${roomDetail.roomTotal}" type="number" maxFractionDigits="0"/>đ</td>
                            </tr>
                            
                            <c:set var="stt" value="2" />
                            <c:forEach var="svc" items="${services}">
                                <tr>
                                    <td>${stt}</td>
                                    <td class="text-start">${svc.serviceName}</td>
                                    <td>${svc.quantity}</td>
                                    <td><fmt:formatNumber value="${svc.unitPrice}" type="number" maxFractionDigits="0"/>đ</td>
                                    <td class="text-end fw-bold"><fmt:formatNumber value="${svc.subtotal}" type="number" maxFractionDigits="0"/>đ</td>
                                </tr>
                                <c:set var="stt" value="${stt + 1}" />
                            </c:forEach>
                        </tbody>
                    </table>
                </div>

                <div class="row justify-content-end mb-4">
                    <div class="col-md-5 text-end border-start">
                        <p class="mb-2 fs-5">Total: <strong class="text-danger"><fmt:formatNumber value="${roomDetail.baseTotal}" type="number" maxFractionDigits="0"/>đ</strong></p>
                        <p class="mb-3 text-muted">Deposit (30%): <strong>- <fmt:formatNumber value="${deposit}" type="number" maxFractionDigits="0"/>đ</strong></p>
                        <hr>
                        <h3 class="text-success mb-0">Payment required: <fmt:formatNumber value="${finalAmount}" type="number" maxFractionDigits="0"/>đ</h3>
                    </div>
                </div>

                <div class="d-flex justify-content-between align-items-center bg-warning-subtle p-3 rounded border border-warning">
                    <div class="form-check fs-5">
                        <input class="form-check-input border-secondary" type="checkbox" id="confirmPaymentCheck">
                        <label class="form-check-label text-dark fw-bold" for="confirmPaymentCheck">
                            The customer has successfully made the payment.
                        </label>
                    </div>

                    <form action="${pageContext.request.contextPath}/CheckoutEmployee" method="POST" id="invoiceForm" class="m-0">
                        <input type="hidden" name="action" value="exportInvoice">
                        <input type="hidden" name="bookingID" value="${roomDetail.bookingID}">
                        
                        <button type="submit" class="btn btn-success btn-lg fw-bold px-4" id="btnInvoice" disabled>
                            <i class="bi bi-printer me-2"></i> Issue invoice
                        </button>
                    </form>
                </div>

            </div>
        </div>
    </div>

    <script>
        document.addEventListener("DOMContentLoaded", function() {
            const paymentCheckbox = document.getElementById("confirmPaymentCheck");
            const btnInvoice = document.getElementById("btnInvoice");

            paymentCheckbox.addEventListener("change", function() {
                if (this.checked) {
                    btnInvoice.removeAttribute("disabled");
                } else {
                    btnInvoice.setAttribute("disabled", "true");
                }
            });
        });
    </script>
</layout:layout>
