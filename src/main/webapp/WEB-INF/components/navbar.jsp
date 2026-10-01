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
        <i class="fa-solid fa-hotel"></i>
        Hotel Management
    </a>

    <jsp:include page="/WEB-INF/components/dropdownMenu.jsp" />
</nav>