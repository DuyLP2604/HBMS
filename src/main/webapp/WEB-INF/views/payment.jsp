<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Shows the booking amount due and the available online payment choices. --%>
<fmt:setLocale value="en_US" scope="page" />
<c:url var="paymentUrl" value="/payment" />
<c:url var="bookingUrl" value="/my-bookings"><c:param name="bookingID" value="${booking.bookingID}" /></c:url>
<c:url var="walletUrl" value="/wallet" />
<c:forEach var="method" items="${paymentMethods}">
    <c:if test="${fn:trim(method.methodID) eq 'PT01'}"><c:set var="payOsAvailable" value="${true}" /></c:if>
    <c:if test="${fn:trim(method.methodID) eq 'PT02'}"><c:set var="walletMethodAvailable" value="${true}" /></c:if>
</c:forEach>
<c:set var="initialPayment" value="${paymentType eq 'DEPOSIT' or paymentType eq 'FULL'}" />
<c:set var="canPay" value="${not empty paymentRequestToken and amountToPay gt 0
    and (initialPayment or paymentType eq 'BALANCE') and (payOsAvailable or (walletMethodAvailable and walletCanPay))}" />

<layout:layout title="Booking Payment" pageCss="booking-completion.css" pageJs="payment.js"
               useBootstrap="true" bodyClass="booking-completion-body">
    <div class="booking-completion pay-page">
        <header class="bc-heading">
            <div>
                <p class="bc-eyebrow">BOOKING <c:out value="${booking.bookingID}" /></p>
                <h1>Booking Payment</h1>
                <p>Choose how to pay and review the amount due.</p>
            </div>
            <a class="bc-text-link" href="<c:out value='${bookingUrl}' />">
                <i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to booking
            </a>
        </header>

        <c:if test="${initialPayment}">
            <section class="bp-deadline" id="paymentDeadlineBanner" aria-label="Payment deadline">
                <div class="bp-deadline-details">
                    <span class="bc-icon"><i class="fa-regular fa-clock" aria-hidden="true"></i></span>
                    <div>
                        <span class="bc-small-label">Payment deadline</span>
                        <c:choose>
                            <c:when test="${not empty booking.paymentDeadline}">
                                <strong><fmt:formatDate value="${booking.paymentDeadline}" pattern="dd MMM yyyy, HH:mm" timeZone="Asia/Ho_Chi_Minh" /></strong>
                                <small>Vietnam time</small>
                            </c:when>
                            <c:otherwise><strong>Unavailable</strong></c:otherwise>
                        </c:choose>
                    </div>
                </div>
                <div class="bp-clock">
                    <span class="bc-small-label">Time remaining</span>
                    <strong id="paymentCountdown" data-deadline="<c:out value='${booking.paymentDeadline.time}' />" role="timer">Calculating…</strong>
                </div>
            </section>
        </c:if>

        <c:if test="${not empty errorMessage}">
            <div class="bp-notice bp-notice-error" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>
        <p id="expiredMessage" class="bp-notice bp-notice-error" role="alert" hidden></p>

        <c:choose>
            <c:when test="${canPay}">
                <form id="paymentForm" class="bp-layout" action="<c:out value='${paymentUrl}' />" method="post"
                      data-amount="<c:out value='${amountToPay}' />"
                      data-wallet-contribution="<c:out value='${walletContribution}' />">
                    <input type="hidden" name="bookingID" value="<c:out value='${booking.bookingID}' />">
                    <input type="hidden" name="paymentRequestToken" value="<c:out value='${paymentRequestToken}' />">

                    <section class="bc-panel bp-methods" aria-labelledby="paymentMethodsHeading">
                        <div class="bp-mobile-due">
                            <span class="bc-small-label">Due now</span>
                            <strong><fmt:formatNumber value="${amountToPay}" pattern="#,##0.##" /> VND</strong>
                        </div>
                        <div class="bc-section-heading bp-method-heading">
                            <div>
                                <h2 id="paymentMethodsHeading">Payment method</h2>
                                <p>Choose one of the options below.</p>
                            </div>
                            <a class="bc-text-link" href="<c:out value='${walletUrl}' />">Wallet history</a>
                        </div>

                        <fieldset class="bp-options" aria-labelledby="paymentMethodsHeading">
                            <c:if test="${payOsAvailable}">
                                <label class="bp-option">
                                    <input type="radio" name="methodID" value="PT01" required>
                                    <span class="bp-option-card">
                                        <span class="bc-icon"><i class="fa-solid fa-qrcode" aria-hidden="true"></i></span>
                                        <span class="bp-option-text">
                                            <strong>VietQR via PayOS</strong>
                                            <span>Open PayOS to scan a QR code with your banking app.</span>
                                        </span>
                                        <span class="bp-radio" aria-hidden="true"></span>
                                    </span>
                                </label>
                            </c:if>
                            <c:if test="${walletMethodAvailable}">
                                <label class="bp-option">
                                    <input type="radio" name="methodID" value="PT02" required ${walletCanPay ? '' : 'disabled'}>
                                    <span class="bp-option-card">
                                        <span class="bc-icon"><i class="fa-solid fa-wallet" aria-hidden="true"></i></span>
                                        <span class="bp-option-text">
                                            <strong>My Wallet</strong>
                                            <c:choose>
                                                <c:when test="${walletAvailable}">
                                                    <span>Balance <fmt:formatNumber value="${walletBalance}" pattern="#,##0.##" /> VND</span>
                                                    <c:if test="${not walletCanPay}"><small>Insufficient balance for this payment</small></c:if>
                                                </c:when>
                                                <c:otherwise><span>Wallet balance is unavailable</span></c:otherwise>
                                            </c:choose>
                                        </span>
                                        <span class="bp-radio" aria-hidden="true"></span>
                                    </span>
                                </label>
                            </c:if>
                        </fieldset>

                        <c:if test="${payOsAvailable and walletContribution gt 0 and not walletCanPay}">
                            <label class="bp-wallet-split" id="walletSplitOption" hidden>
                                <input type="checkbox" id="useWallet" name="useWallet" value="true" disabled>
                                <span>
                                    <strong>Use my wallet balance first</strong>
                                    <small>Use <fmt:formatNumber value="${walletContribution}" pattern="#,##0.##" /> VND from your wallet, then pay the rest via PayOS.</small>
                                </span>
                            </label>
                        </c:if>
                    </section>

                    <aside class="bc-panel bp-summary" aria-labelledby="paymentSummaryHeading">
                        <div class="bc-section-heading">
                            <h2 id="paymentSummaryHeading">Booking summary</h2>
                            <span class="bc-icon"><i class="fa-solid fa-receipt" aria-hidden="true"></i></span>
                        </div>
                        <dl class="bc-lines bp-summary-lines">
                            <div><dt>Check-in</dt><dd><fmt:formatDate value="${booking.checkInDate}" pattern="dd MMM yyyy" /></dd></div>
                            <div><dt>Check-out</dt><dd><fmt:formatDate value="${booking.checkOutDate}" pattern="dd MMM yyyy" /></dd></div>
                            <div>
                                <dt>Booking status</dt>
                                <dd><c:choose>
                                    <c:when test="${initialPayment}">Awaiting payment</c:when>
                                    <c:when test="${bookingSummary.bookingStatus eq 'CHECKED_IN'}">Checked in</c:when>
                                    <c:when test="${bookingSummary.bookingStatus eq 'ASSIGNED'}">Rooms assigned</c:when>
                                    <c:otherwise>Confirmed</c:otherwise>
                                </c:choose></dd>
                            </div>
                            <div class="bp-summary-divider"><dt>Booking total</dt><dd><fmt:formatNumber value="${bookingSummary.totalAmount}" pattern="#,##0.##" /> VND</dd></div>
                            <div><dt>Already paid</dt><dd><fmt:formatNumber value="${bookingSummary.totalPaidAmount}" pattern="#,##0.##" /> VND</dd></div>
                        </dl>
                        <div class="bp-due">
                            <div class="bp-due-label">
                                <strong>Due now</strong>
                                <span class="ps-payment-type"><c:choose>
                                    <c:when test="${paymentType eq 'DEPOSIT'}">30% deposit</c:when>
                                    <c:when test="${paymentType eq 'FULL'}">Full payment</c:when>
                                    <c:otherwise>Remaining balance</c:otherwise>
                                </c:choose></span>
                            </div>
                            <strong class="bp-due-amount"><fmt:formatNumber value="${amountToPay}" pattern="#,##0.##" /> <small>VND</small></strong>
                            <c:choose>
                                <c:when test="${paymentType eq 'DEPOSIT' and bookingSummary.roomRemainingAmount gt amountToPay}">
                                    <p><fmt:formatNumber value="${bookingSummary.roomRemainingAmount - amountToPay}" pattern="#,##0.##" /> VND in room charges remains due before check-in.</p>
                                </c:when>
                                <c:when test="${paymentType eq 'DEPOSIT'}"><p>This deposit confirms your booking.</p></c:when>
                                <c:when test="${paymentType eq 'FULL'}"><p>Pay in full to confirm your booking.</p></c:when>
                                <c:when test="${bookingSummary.bookingStatus eq 'CHECKED_IN'}"><p>This payment covers your outstanding charges.</p></c:when>
                                <c:otherwise><p>This payment settles room charges. Extra services are due at checkout.</p></c:otherwise>
                            </c:choose>
                            <c:if test="${paymentType eq 'BALANCE'}">
                                <p>The 24-hour refund window keeps its original deadline.</p>
                            </c:if>
                        </div>
                        <dl id="paymentBreakdown" class="bc-lines bp-breakdown" aria-live="polite" hidden>
                            <div><dt>From wallet</dt><dd id="walletContributionDisplay"></dd></div>
                            <div><dt>Via PayOS</dt><dd id="externalContributionDisplay"></dd></div>
                        </dl>
                        <button id="paymentButton" type="submit" class="bc-button bc-button-primary bp-pay-button">
                            <span id="paymentButtonLabel">Continue to payment</span>
                            <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>
                        </button>
                    </aside>
                </form>
            </c:when>
            <c:otherwise>
                <section class="bc-panel bp-unavailable">
                    <h2>Payment unavailable</h2>
                    <p>Check your booking for its current payment status and available options.</p>
                    <a class="bc-button bc-button-secondary" href="<c:out value='${bookingUrl}' />">View booking</a>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
