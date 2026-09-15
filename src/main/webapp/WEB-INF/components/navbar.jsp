<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<nav class="navbar">

    <a href="${pageContext.request.contextPath}/home"
       class="logo">

        <i class="fa-solid fa-hotel"></i>
        Hotel Management

    </a>

    <jsp:include page="/WEB-INF/components/dropdownMenu.jsp" />

</nav>

