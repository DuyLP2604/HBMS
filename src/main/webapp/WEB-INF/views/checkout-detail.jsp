<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows room and service charges before reception completes checkout.
--%>
<fmt:setLocale value="vi_VN" scope="page" />
<layout:layout title="Checkout Detail" pageCss="checkout.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-checkout-workspace">
        <header class="ui-page-heading">
            <div><p class="ui-eyebrow">FRONT DESK / CHECKOUT</p><h1>Payment &amp; Checkout Details</h1><p class="ui-description">Review booking charges before completing checkout.</p></div>
            <a href="${pageContext.request.contextPath}/CheckoutEmployee" class="ui-button ui-button-secondary"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to Checkout Management</a>
        </header>
        <c:if test="${not empty successMessage}"><div class="ui-notice ui-notice-success" role="alert"><c:out value="${successMessage}" /></div></c:if>
        <c:if test="${not empty errorMessage}"><div class="ui-notice ui-notice-danger" role="alert"><c:out value="${errorMessage}" /></div></c:if>
        <div class="checkout-detail-layout">
            <div class="checkout-detail-main">
                <section class="ui-panel" aria-label="Guest stay information">
                    <div class="ui-panel-body">
                        <div class="checkout-guest"><span class="ui-avatar" aria-hidden="true"><i class="fa-regular fa-user"></i></span><div><p class="checkout-label">Customer name</p><h2><c:out value="${roomDetail.customerName}" /></h2></div></div>
                        <dl class="ui-facts">
                            <div><dt>Room(s)</dt><dd><span class="checkout-room-chip"><c:out value="${roomDetail.roomNumber}" /></span></dd></div>
                            <div><dt>Booking ID</dt><dd class="ui-id"><c:out value="${bookingID}" /></dd></div>
                            <div><dt>Length of stay</dt><dd><c:out value="${roomDetail.checkInDate}" /> <span class="checkout-date-separator" aria-hidden="true">→</span> <c:out value="${roomDetail.checkOutDate}" /><span class="checkout-night-count">(<c:out value="${roomDetail.totalDays}" /> night(s))</span></dd></div>
                        </dl>
                        <div class="checkout-payment-option"><span class="checkout-label">Initial payment option</span>
                            <c:choose><c:when test="${paymentOption eq 'DEPOSIT'}">30% deposit; room balance settled at check-in</c:when><c:when test="${paymentOption eq 'FULL'}">Full payment</c:when><c:otherwise><c:out value="${paymentOption}" /></c:otherwise></c:choose>
                        </div>
                    </div>
                </section>
                <section class="ui-panel" aria-labelledby="bookingChargesTitle">
                    <div class="ui-panel-body"><div class="ui-section-heading checkout-list-heading"><h2 id="bookingChargesTitle">Booking charges</h2></div></div>
                    <div class="ui-table-scroll" role="region" aria-label="Booking charges">
                        <table class="ui-table checkout-charges-table">
                            <thead><tr><th scope="col">Number</th><th scope="col">Name</th><th scope="col">Quantity</th><th scope="col" class="checkout-amount">Unit price</th><th scope="col" class="checkout-amount">Subtotal</th></tr></thead>
                            <tbody>
                                <tr><td>1</td><td>Room charges for <c:out value="${roomDetail.totalDays}" /> night(s)<span class="checkout-charge-note">(settled at check-in)</span></td><td>—</td><td class="checkout-amount">—</td><td class="checkout-amount"><fmt:formatNumber value="${roomDetail.roomTotal}" type="number" maxFractionDigits="2" /> VND</td></tr>
                                <c:forEach var="svc" items="${services}" varStatus="row">
                                    <tr><td>${row.index + 2}</td><td><c:out value="${svc.serviceName}" /></td><td><c:out value="${svc.quantity}" /></td><td class="checkout-amount"><fmt:formatNumber value="${svc.unitPrice}" type="number" maxFractionDigits="2" /> VND</td><td class="checkout-amount"><fmt:formatNumber value="${svc.subtotal}" type="number" maxFractionDigits="2" /> VND</td></tr>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </section>
            </div>
            <aside class="ui-panel checkout-summary" data-outstanding="${bookingSummary.serviceRemainingAmount gt 0 or bookingSummary.roomRemainingAmount gt 0}" aria-labelledby="checkoutSummaryTitle">
                <div class="ui-panel-body">
                    <div class="ui-section-heading"><h2 id="checkoutSummaryTitle">Payment summary</h2></div>
                    <dl class="ui-money-lines checkout-money-lines">
                        <div><dt>Total</dt><dd><fmt:formatNumber value="${totalAmount}" type="number" maxFractionDigits="2" /> VND</dd></div>
                        <div><dt>Amount paid</dt><dd><fmt:formatNumber value="${paidAmount}" type="number" maxFractionDigits="2" /> VND</dd></div>
                    </dl>
                    <div class="checkout-due"><span class="checkout-label">Extra services due at checkout</span><strong class="checkout-due-value"><fmt:formatNumber value="${bookingSummary.serviceRemainingAmount}" type="number" maxFractionDigits="2" /> <small>VND</small></strong></div>
                    <div class="checkout-payment-status"><span class="checkout-label">Payment status</span><span class="ui-status"><c:out value="${bookingSummary.paymentStatus}" /></span></div>
                    <c:if test="${bookingSummary.roomRemainingAmount gt 0}"><div class="ui-notice ui-notice-danger">This existing stay has unpaid room charges. Review and settle its payment history before checkout: <fmt:formatNumber value="${bookingSummary.roomRemainingAmount}" type="number" /> VND.</div></c:if>
                    <c:choose>
                        <c:when test="${bookingSummary.bookingStatus eq 'CHECKED_OUT' and not empty issuedInvoice}">
                            <c:url var="printInvoiceUrl" value="/invoice"><c:param name="action" value="print" /><c:param name="bookingId" value="${bookingID}" /></c:url>
                            <div class="ui-notice ui-notice-success">Checkout is complete. Invoice: <strong><c:out value="${issuedInvoice.invoiceID}" /></strong></div>
                            <a href="<c:out value='${printInvoiceUrl}' />" class="ui-button ui-button-primary ui-button-wide"><i class="fa-solid fa-print" aria-hidden="true"></i> View / Print Invoice</a>
                        </c:when>
                        <c:when test="${canCheckout and (bookingSummary.bookingStatus eq 'CHECKED_IN' or bookingSummary.bookingStatus eq 'CHECKED_OUT')}">
                            <div class="ui-notice ui-notice-success"><span><strong>Payment complete.</strong> You can confirm checkout and issue the invoice.</span></div>
                            <form action="${pageContext.request.contextPath}/CheckoutEmployee" method="post" id="invoiceForm">
                                <input type="hidden" name="action" value="exportInvoice">
                                <input type="hidden" name="bookingID" value="<c:out value='${bookingID}' />">
                                <button type="submit" class="ui-button ui-button-primary ui-button-wide" id="btnInvoice"><i class="fa-solid fa-file-invoice" aria-hidden="true"></i> <c:out value="${bookingSummary.bookingStatus eq 'CHECKED_OUT' ? 'Issue Invoice' : 'Checkout & Issue Invoice'}" /></button>
                            </form>
                        </c:when>
                        <c:otherwise>
                            <div class="ui-notice ui-notice-warning checkout-unavailable" role="alert"><c:choose><c:when test="${remainingAmount gt 0}">Extra services must be paid before checkout. The customer can complete this payment from My Bookings.</c:when><c:otherwise>Checkout is unavailable. Reload this page and verify the booking payment status.</c:otherwise></c:choose></div>
                        </c:otherwise>
                    </c:choose>
                </div>
            </aside>
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
