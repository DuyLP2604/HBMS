<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:choose>
    <c:when test="${sessionScope.user.role eq 'Staff'}">
        <c:url var="logoUrl" value="/staff/dashboard" />
    </c:when>
    <c:when test="${sessionScope.user.role eq 'Admin'}">
        <c:url var="logoUrl" value="/dashboard" />
    </c:when>
    <c:otherwise>
        <c:url var="logoUrl" value="/home" />
    </c:otherwise>
</c:choose>

<nav class="navbar">
    <a href="${logoUrl}" class="logo">
        <i class="fa-solid fa-hotel" aria-hidden="true"></i>
        Hotel Management
    </a>

    <div class="navbar-actions">
        <c:if test="${sessionScope.user.role eq 'Customer'}">
            <jsp:include page="/wallet-navbar" />
        </c:if>
        <jsp:include page="/WEB-INF/components/dropdownMenu.jsp" />
    </div>
</nav>
