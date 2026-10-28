 <%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<fmt:setLocale value="en_US" />
<c:url var="walletRefreshUrl" value="/wallet"><c:param name="page" value="${walletPage}" /></c:url>
<layout:layout title="My Wallet" pageCss="wallet.css" useBootstrap="true" bodyClass="wallet-body">
    <div class="wallet-page">
        <header class="wlt-heading">
            <div><p class="wlt-eyebrow">YOUR ACCOUNT</p><h1>My wallet</h1><p class="wlt-description">Keep track of your refunds and booking payments.</p></div>
            <div class="wlt-actions">
                <a class="wlt-button" href="${pageContext.request.contextPath}/my-bookings"><i class="fa-solid fa-calendar-check" aria-hidden="true"></i> My bookings</a>
                <a class="wlt-button wlt-button-quiet" href="<c:out value='${walletRefreshUrl}' />"><i class="fa-solid fa-rotate-right" aria-hidden="true"></i> Refresh</a>
            </div>
        </header>
        <c:choose>
            <c:when test="${not walletAvailable}">
                <section class="wlt-empty wlt-unavailable" aria-labelledby="walletUnavailableTitle">
                    <span class="wlt-empty-icon"><i class="fa-solid fa-wallet" aria-hidden="true"></i></span>
                    <h2 id="walletUnavailableTitle">Your wallet is not available yet</h2>
                    <p>Please contact hotel staff for help with your wallet.</p>
                    <a class="wlt-button" href="<c:out value='${walletRefreshUrl}' />"><i class="fa-solid fa-rotate-right" aria-hidden="true"></i> Try again</a>
                </section>
            </c:when>
            <c:otherwise>
                <div class="wlt-overview">
                    <section class="wlt-balance-card" aria-labelledby="walletBalanceTitle">
                        <div class="wlt-balance-top"><span id="walletBalanceTitle">Available balance</span><span class="wlt-wallet-icon"><i class="fa-solid fa-wallet" aria-hidden="true"></i></span></div>
                        <p class="wlt-balance"><fmt:formatNumber value="${wallet.balance}" pattern="#,##0.##" /><span>VND</span></p>
                        <p class="wlt-balance-note"><c:choose><c:when test="${wallet.balance eq 0}">Eligible refunds will appear here, ready for a future booking.</c:when><c:otherwise>Ready to use for your next booking payment.</c:otherwise></c:choose></p>
                        <div class="wlt-wallet-details"><span>Customer <strong><c:out value="${wallet.customerID}" /></strong></span><span>Wallet <strong>#<c:out value="${wallet.walletID}" /></strong></span></div>
                    </section>
                    <aside class="wlt-how-it-works" aria-labelledby="walletHelpTitle">
                        <span class="wlt-help-icon"><i class="fa-solid fa-arrows-rotate" aria-hidden="true"></i></span>
                        <h2 id="walletHelpTitle">From refunds to your next stay</h2>
                        <p>Eligible booking refunds are credited to this wallet.</p>
                        <p>Use your balance for a 30% deposit, full payment or remaining balance when it covers the entire amount due for that payment.</p>
                        <a href="${pageContext.request.contextPath}/room-types">Explore rooms <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                    </aside>
                </div>
                <section class="wlt-history" aria-labelledby="walletHistoryTitle">
                    <header class="wlt-history-heading"><div><h2 id="walletHistoryTitle">Transaction history</h2><p>Your wallet activity, most recent first.</p></div><span class="wlt-timezone"><i class="fa-regular fa-clock" aria-hidden="true"></i> Vietnam time</span></header>
                    <c:choose>
                        <c:when test="${empty walletTransactions}">
                            <div class="wlt-empty">
                                <span class="wlt-empty-icon"><i class="fa-solid fa-receipt" aria-hidden="true"></i></span>
                                <h3><c:out value="${walletPage eq 1 ? 'No transactions yet' : 'No transactions on this page'}" /></h3>
                                <p><c:out value="${walletPage eq 1 ? 'Your refunds and wallet payments will appear here.' : 'Return to an earlier page to view your wallet activity.'}" /></p>
                                <c:if test="${walletPage eq 1}"><a class="wlt-button" href="${pageContext.request.contextPath}/my-bookings">View my bookings <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></c:if>
                            </div>
                        </c:when>
                        <c:otherwise>
                            <table class="wlt-table">
                                <caption class="visually-hidden">Refunds and booking payments in your wallet. All times are in Vietnam time.</caption>
                                <thead><tr><th scope="col">Activity</th><th scope="col">Booking</th><th scope="col">Date</th><th scope="col" class="wlt-amount-heading">Amount</th></tr></thead>
                                <tbody>
                                    <c:forEach var="item" items="${walletTransactions}">
                                        <tr>
                                            <td class="wlt-activity-cell"><div class="wlt-activity">
                                                <span class="wlt-transaction-icon ${item.credit ? 'wlt-credit-icon' : 'wlt-debit-icon'}"><i class="fa-solid ${item.credit ? 'fa-arrow-down' : 'fa-arrow-up'}" aria-hidden="true"></i></span>
                                                <div><strong><c:out value="${item.credit ? 'Booking refund' : 'Booking payment'}" /></strong><span class="wlt-reference"><c:choose><c:when test="${not empty item.paymentID}">Payment <c:out value="${item.paymentID}" /></c:when><c:otherwise>Transaction #<c:out value="${item.walletTransactionID}" /></c:otherwise></c:choose></span></div>
                                            </div></td>
                                            <td class="wlt-booking-cell"><span class="wlt-mobile-label">Booking</span><c:choose><c:when test="${not empty item.bookingID}"><c:url var="walletBookingUrl" value="/my-bookings"><c:param name="bookingID" value="${item.bookingID}" /></c:url><a class="wlt-booking-link" href="<c:out value='${walletBookingUrl}' />"><c:out value="${item.bookingID}" /> <i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i></a></c:when><c:otherwise><span class="wlt-muted">—</span></c:otherwise></c:choose></td>
                                            <td class="wlt-date-cell"><span class="wlt-mobile-label">Date</span><div><span class="wlt-date"><fmt:formatDate value="${item.transactionTime}" pattern="dd MMM yyyy" timeZone="Asia/Ho_Chi_Minh" /></span><span class="wlt-time"><fmt:formatDate value="${item.transactionTime}" pattern="HH:mm" timeZone="Asia/Ho_Chi_Minh" /></span></div></td>
                                            <td class="wlt-amount-cell ${item.credit ? 'wallet-credit' : 'wallet-debit'}"><span class="visually-hidden"><c:out value="${item.credit ? 'Credited: ' : 'Debited: '}" /></span><span><c:out value="${item.credit ? '+' : '-'}" /><fmt:formatNumber value="${item.amount}" pattern="#,##0.##" /></span><small>VND</small></td>
                                        </tr>
                                    </c:forEach>
                                </tbody>
                            </table>
                        </c:otherwise>
                    </c:choose>
                    <nav class="wlt-pagination" aria-label="Wallet transaction pages">
                        <c:choose><c:when test="${walletPage gt 1}"><c:url var="walletPreviousUrl" value="/wallet"><c:param name="page" value="${walletPage - 1}" /></c:url><a class="wlt-button" href="<c:out value='${walletPreviousUrl}' />"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Previous</a></c:when><c:otherwise><span class="wlt-button wlt-button-disabled" aria-disabled="true"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Previous</span></c:otherwise></c:choose>
                        <span class="wlt-page-number">Page <c:out value="${walletPage}" /></span>
                        <c:choose><c:when test="${walletHasNext}"><c:url var="walletNextUrl" value="/wallet"><c:param name="page" value="${walletPage + 1}" /></c:url><a class="wlt-button" href="<c:out value='${walletNextUrl}' />">Next <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></c:when><c:otherwise><span class="wlt-button wlt-button-disabled" aria-disabled="true">Next <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></span></c:otherwise></c:choose>
                    </nav>
                </section>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
