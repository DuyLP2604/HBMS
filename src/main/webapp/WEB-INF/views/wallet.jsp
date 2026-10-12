<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="My Wallet" pageCss="wallet.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container py-5 wallet-page">
        <div class="d-flex flex-wrap justify-content-between align-items-center gap-3 mb-4">
            <div><h1 class="h3 mb-1">My Wallet</h1><p class="text-muted mb-0">Use your refund balance to pay for a booking.</p></div>
            <a href="${pageContext.request.contextPath}/my-bookings" class="btn btn-outline-secondary">My Bookings</a>
        </div>
        <c:choose>
            <c:when test="${not walletAvailable}">
                <div class="alert alert-warning" role="alert">Your wallet is not available yet. Please contact hotel staff.</div>
            </c:when>
            <c:otherwise>
                <div class="card wallet-balance-card shadow-sm mb-4">
                    <div class="card-body p-4">
                        <div class="wallet-balance-label mb-2">Available Balance</div>
                        <div class="wallet-balance mb-3"><fmt:formatNumber value="${wallet.balance}" pattern="#,##0.##" /> <small>VND</small></div>
                        <div class="wallet-meta">Customer: <c:out value="${wallet.customerID}" /> · Wallet #<c:out value="${wallet.walletID}" /></div>
                    </div>
                </div>
                <div class="alert alert-info">Eligible booking refunds are credited here. You can use your wallet for a 30% deposit, full payment or remaining balance when it covers the entire amount due for that payment.</div>
                <div class="card wallet-history-card shadow-sm">
                    <div class="card-header bg-white py-3"><h2 class="h5 mb-0">Transaction History</h2></div>
                    <c:choose>
                        <c:when test="${empty walletTransactions}">
                            <div class="card-body text-center text-muted py-5"><c:out value="${walletPage eq 1 ? 'You do not have any wallet transactions yet.' : 'There are no transactions on this page.'}" /></div>
                        </c:when>
                        <c:otherwise>
                            <div class="table-responsive">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light"><tr><th scope="col">Date</th><th scope="col">Type</th><th scope="col">Booking</th><th scope="col">Reference</th><th scope="col" class="text-end">Amount (VND)</th></tr></thead>
                                    <tbody>
                                        <c:forEach var="item" items="${walletTransactions}">
                                            <tr>
                                                <td><fmt:formatDate value="${item.transactionTime}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                                <td><c:choose><c:when test="${item.credit}"><span class="badge bg-success">Refund</span></c:when><c:otherwise><span class="badge bg-primary">Booking Payment</span></c:otherwise></c:choose></td>
                                                <td>
                                                    <c:choose>
                                                        <c:when test="${not empty item.bookingID}"><c:url var="walletBookingUrl" value="/my-bookings"><c:param name="bookingID" value="${item.bookingID}" /></c:url><a href="<c:out value='${walletBookingUrl}' />"><c:out value="${item.bookingID}" /></a></c:when>
                                                        <c:otherwise>—</c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td><c:choose><c:when test="${not empty item.paymentID}"><c:out value="${item.paymentID}" /></c:when><c:otherwise>Wallet #<c:out value="${item.walletTransactionID}" /></c:otherwise></c:choose></td>
                                                <td class="text-end fw-semibold ${item.credit ? 'wallet-credit' : 'wallet-debit'}"><c:out value="${item.credit ? '+' : '-'}" /><fmt:formatNumber value="${item.amount}" pattern="#,##0.##" /></td>
                                            </tr>
                                        </c:forEach>
                                    </tbody>
                                </table>
                            </div>
                        </c:otherwise>
                    </c:choose>
                    <div class="card-footer bg-white d-flex justify-content-between align-items-center gap-2 py-3">
                        <c:choose><c:when test="${walletPage gt 1}"><c:url var="walletPreviousUrl" value="/wallet"><c:param name="page" value="${walletPage - 1}" /></c:url><a class="btn btn-outline-secondary btn-sm" href="<c:out value='${walletPreviousUrl}' />">Previous</a></c:when><c:otherwise><span></span></c:otherwise></c:choose>
                        <span class="text-muted small">Page <c:out value="${walletPage}" /></span>
                        <c:choose><c:when test="${walletHasNext}"><c:url var="walletNextUrl" value="/wallet"><c:param name="page" value="${walletPage + 1}" /></c:url><a class="btn btn-outline-secondary btn-sm" href="<c:out value='${walletNextUrl}' />">Next</a></c:when><c:otherwise><span></span></c:otherwise></c:choose>
                    </div>
                </div>
            </c:otherwise>
        </c:choose>
    </div>
</layout:layout>
