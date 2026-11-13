<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows room and service charges before reception completes checkout.
--%>
<fmt:setLocale value="vi_VN" scope="page" />
<layout:layout title="Checkout Detail" pageCss="checkout.css" useBootstrap="true" bodyClass="hbms-ui-page">

    <style>
        .checkout-collection {
            margin-top:22px;
            display:grid;
            gap:12px;
        }
        .checkout-collection p {
            margin:0;
            font-size:13px;
            line-height:1.7;
        }
        .checkout-collection select {
            width:100%;
            padding:11px;
            border:1px solid var(--ui-line);
            border-radius:8px;
            background:#fff;
        }
        .checkout-cash-confirm {
            display:flex;
            gap:10px;
            align-items:flex-start;
            font-size:13px;
            line-height:1.6;
        }
        .checkout-cash-confirm input {
            margin-top:4px;
            flex-shrink:0;
        }
        .checkout-qr {
            display:grid;
            gap:12px;
            justify-items:center;
        }
        .checkout-qr img {
            max-width:100%;
            height:auto;
        }
        .checkout-payment-help {
            color:var(--ui-muted);
        }
        .checkout-collection [hidden] {
            display:none !important;
        }
    </style>
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
                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                <input type="hidden" name="bookingID" value="<c:out value='${bookingID}' />">
                                <button type="submit" class="ui-button ui-button-primary ui-button-wide" id="btnInvoice"><i class="fa-solid fa-file-invoice" aria-hidden="true"></i> <c:out value="${bookingSummary.bookingStatus eq 'CHECKED_OUT' ? 'Issue Invoice' : 'Checkout & Issue Invoice'}" /></button>
                            </form>
                        </c:when>
                        <c:when test="${not empty checkoutPaymentToken}">
                            <form action="${pageContext.request.contextPath}/CheckoutEmployee" method="post" id="checkoutPaymentForm" class="checkout-collection">
                                <input type="hidden" name="action" value="collectCash">
                                <input type="hidden" name="bookingID" value="<c:out value='${bookingID}' />">
                                <input type="hidden" name="csrfToken" value="<c:out value='${csrfToken}' />">
                                <input type="hidden" name="checkoutPaymentToken" value="<c:out value='${checkoutPaymentToken}' />">
                                <p>Collect outstanding balance: <strong><fmt:formatNumber value="${remainingAmount}" type="number" /> VND</strong></p>
                                <label for="checkoutMethod">Payment method</label>
                                <select name="methodID" id="checkoutMethod" required>
                                    <option value="">Select Cash or QR</option>
                                    <c:forEach var="method" items="${paymentMethods}">
                                        <option value="<c:out value='${method.methodID}' />"><c:out value="${method.methodID eq 'PT03' ? 'Cash' : 'VietQR (PayOS)'}" /></option>
                                    </c:forEach>
                                </select>
                                <label class="checkout-cash-confirm" id="checkoutCashGroup" hidden>
                                    <input type="checkbox" name="collectionConfirmed" id="checkoutCashConfirmed" value="true" disabled>
                                    <span>I confirm receipt of <fmt:formatNumber value="${remainingAmount}" type="number" /> VND in cash.</span>
                                </label>
                                <section id="checkoutPayOs" class="checkout-qr" aria-label="VietQR payment" hidden>
                                    <p>Scan the QR to pay the outstanding balance. Payment is confirmed automatically by PayOS.</p>
                                    <button type="button" id="checkoutPayOsCreate" class="ui-button ui-button-secondary ui-button-wide">Generate VietQR</button>
                                    <img id="checkoutPayOsImage" width="240" height="240" alt="VietQR for the outstanding checkout balance" hidden>
                                    <a id="checkoutPayOsLink" class="ui-button ui-button-secondary ui-button-wide" target="_blank" rel="noopener noreferrer" hidden>Open PayOS</a>
                                </section>
                                <p id="checkoutPaymentStatus" class="checkout-payment-help" role="status" aria-live="polite">Checking payment status…</p>
                                <button type="submit" id="checkoutCashSubmit" class="ui-button ui-button-primary ui-button-wide" disabled>Confirm cash received</button>
                                <p class="checkout-payment-help">After payment is recorded, confirm checkout and issue the invoice.</p>
                                <c:if test="${empty paymentMethods}"><div class="ui-notice ui-notice-danger">No collection methods are available. Contact the administrator.</div></c:if>
                                    <noscript><p>Enable JavaScript to collect cash or display the payment QR.</p></noscript>
                                </form>
                        </c:when>
                        <c:when test="${checkoutPaymentDue}">
                            <div class="ui-notice ui-notice-warning">A receptionist can collect the outstanding balance.</div>
                        </c:when>
                        <c:otherwise>
                            <div class="ui-notice ui-notice-warning checkout-unavailable" role="alert">Checkout is unavailable. Reload this page and verify the booking payment status.</div>
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
    <script>
        // Wait for a verified payment before enabling checkout.
        (function () {
            'use strict';
            const form = document.getElementById('checkoutPaymentForm');
            if (!form)
                return;
            // A hidden input named action shadows form.action in the browser.
            const endpoint = new URL(form.getAttribute('action'), document.baseURI);
            const method = document.getElementById('checkoutMethod');
            const cashGroup = document.getElementById('checkoutCashGroup');
            const confirmation = document.getElementById('checkoutCashConfirmed');
            const submit = document.getElementById('checkoutCashSubmit');
            const qrPanel = document.getElementById('checkoutPayOs');
            const create = document.getElementById('checkoutPayOsCreate');
            const image = document.getElementById('checkoutPayOsImage');
            const link = document.getElementById('checkoutPayOsLink');
            const status = document.getElementById('checkoutPaymentStatus');
            let state = 'UNKNOWN';
            let orderCode = '';
            let busy = false;
            let submitting = false;
            let timer;

            function sync() {
                const locked = ['UNKNOWN', 'PENDING', 'PAID', 'FAILED'].includes(state);
                if (['PENDING', 'PAID', 'FAILED'].includes(state))
                    method.value = 'PT01';
                for (const option of method.options)
                    option.disabled = locked && option.value !== 'PT01';
                const cash = method.value === 'PT03' && !locked && !busy;
                cashGroup.hidden = !cash;
                confirmation.disabled = !cash;
                confirmation.required = cash;
                if (!cash)
                    confirmation.checked = false;
                submit.hidden = method.value === 'PT01';
                submit.disabled = submitting || !cash || !confirmation.checked;
                qrPanel.hidden = method.value !== 'PT01';
                create.hidden = ['PENDING', 'PAID', 'FAILED'].includes(state);
                create.disabled = busy || state === 'UNKNOWN';
                image.hidden = link.hidden = state !== 'PENDING';
                if (image.hidden) {
                    image.removeAttribute('src');
                    link.removeAttribute('href');
                }
            }

            function render(order) {
                state = order.status;
                orderCode = order.orderCode || '';
                if (state === 'PENDING') {
                    if (image.getAttribute('src') !== order.imageUrl)
                        image.src = order.imageUrl;
                    link.href = order.checkoutUrl;
                }
                status.textContent = {
                    NONE: 'Choose Cash or QR to collect the outstanding balance.',
                    PENDING: 'Waiting for PayOS to confirm payment. Do not collect cash for this QR.',
                    PAID: 'Payment received. Refreshing the checkout balance…',
                    FAILED: 'This QR payment needs review. Check the PayOS order before collecting again.',
                    EXPIRED: 'This QR expired. Verify any transfer before collecting again.'
                }[state] || 'Reload checkout to verify the payment status.';
                sync();
                if (state === 'PAID')
                    window.location.reload();
            }

            async function read(response) {
                if (!response.ok || response.redirected) {
                    let message = 'Unable to verify payment. Reload checkout and review payment history.';
                    try {
                        message = (await response.json()).message || message;
                    } catch (_) { /* non-JSON error */
                    }
                    throw new Error(message);
                }
                return response.json();
            }

            async function poll() {
                clearTimeout(timer);
                const url = new URL(endpoint);
                url.searchParams.set('action', 'payOsStatus');
                url.searchParams.set('bookingID', form.elements.bookingID.value);
                if (orderCode)
                    url.searchParams.set('orderCode', orderCode);
                try {
                    render(await read(await fetch(url, {cache: 'no-store'})));
                } catch (error) {
                    status.textContent = error.message;
                    // Keep collection disabled while the order status is unknown.
                    state = 'UNKNOWN';
                    sync();
                }
                if (state === 'PENDING' || state === 'UNKNOWN')
                    timer = setTimeout(poll, 3000);
            }

            create.addEventListener('click', async function () {
                if (busy || ['UNKNOWN', 'PENDING', 'PAID', 'FAILED'].includes(state))
                    return;
                clearTimeout(timer);
                busy = true;
                sync();
                status.textContent = 'Generating VietQR…';
                const body = new URLSearchParams({
                    action: 'createPayOs', bookingID: form.elements.bookingID.value,
                    csrfToken: form.elements.csrfToken.value,
                    checkoutPaymentToken: form.elements.checkoutPaymentToken.value
                });
                try {
                    render(await read(await fetch(endpoint, {method: 'POST', body})));
                    if (state === 'PENDING')
                        timer = setTimeout(poll, 3000);
                } catch (error) {
                    status.textContent = error.message;
                    // A lost response may still have created an order. Check before accepting cash.
                    state = 'UNKNOWN';
                    timer = setTimeout(poll, 3000);
                } finally {
                    busy = false;
                    sync();
                }
            });
            method.addEventListener('change', sync);
            confirmation.addEventListener('change', sync);
            form.addEventListener('submit', function (event) {
                if (submitting || submit.disabled || method.value !== 'PT03' || !form.reportValidity()) {
                    event.preventDefault();
                    return;
                }
                submitting = true;
                submit.disabled = true;
                submit.textContent = 'Recording cash payment…';
            });
            window.addEventListener('pagehide', () => clearTimeout(timer));
            window.addEventListener('pageshow', event => {
                if (!event.persisted)
                    return;
                submitting = false;
                orderCode = '';
                state = 'UNKNOWN';
                submit.textContent = 'Confirm cash received';
                sync();
                poll();
            });
            sync();
            poll();
        }());
    </script>
</layout:layout>
