<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<c:if test="${sessionScope.user.role eq 'Customer'}">
    <c:url var="navbarWalletUrl" value="/wallet" />
    <a href="${navbarWalletUrl}" class="navbar-wallet" title="View your wallet and transaction history">
        <span class="navbar-wallet-icon"><i class="fa-solid fa-wallet" aria-hidden="true"></i></span>
        <span class="navbar-wallet-info">
            <span class="navbar-wallet-label">My Wallet</span>
            <span class="navbar-wallet-balance">
                <c:choose>
                    <c:when test="${requestScope.navbarWalletAvailable}"><fmt:formatNumber value="${requestScope.navbarWalletBalance}" pattern="#,##0.##" /> <span class="navbar-wallet-currency">VND</span></c:when>
                    <c:otherwise>Unavailable</c:otherwise>
                </c:choose>
            </span>
        </span>
        <i class="fa-solid fa-chevron-right navbar-wallet-arrow" aria-hidden="true"></i>
    </a>
</c:if>
