<%@ tag language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@ attribute name="title"
              required="true"
              type="java.lang.String" %>

<%@ attribute name="pageCss"
              required="false"
              type="java.lang.String" %>

<%@ attribute name="pageJs"
              required="false"
              type="java.lang.String" %>

<%@ attribute name="useBootstrap"
              required="false"
              type="java.lang.Boolean" %>

<%@ attribute name="bodyClass"
              required="false"
              type="java.lang.String" %>

<%@ attribute name="showNavbar"
              required="false"
              type="java.lang.Boolean" %>

<%@ attribute name="showFooter"
              required="false"
              type="java.lang.Boolean" %>


<!DOCTYPE html>

<html lang="en">

    <head>

        <meta charset="UTF-8">

        <meta name="viewport"
              content="width=device-width, initial-scale=1.0">

        <title>
            <c:out value="${title}" />
        </title>


        <%-- Bootstrap CSS --%>
        <c:if test="${useBootstrap}">
            <link
                href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
                rel="stylesheet">
        </c:if>


        <%-- Font Awesome --%>
        <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/fontawesome/css/all.min.css">


        <%-- Common CSS --%>
        <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/navbar.css">

        <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/dropdown.css">

        <link
            rel="stylesheet"
            href="${pageContext.request.contextPath}/css/flashMessage.css">


        <%-- Page-specific CSS --%>
        <c:if test="${not empty pageCss}">
            <link
                rel="stylesheet"
                href="${pageContext.request.contextPath}/css/${pageCss}">
        </c:if>

    </head>


    <body class="${bodyClass}">


        <%-- Navbar --%>
        <c:if test="${showNavbar == null or showNavbar}">
            <jsp:include page="/WEB-INF/components/navbar.jsp" />
        </c:if>


        <%-- Flash Message --%>
        <jsp:include page="/WEB-INF/components/flashMessage.jsp" />


        <main>
            <jsp:doBody />
        </main>


        <%-- Footer --%>
        <c:if test="${showFooter}">
            <jsp:include page="/WEB-INF/components/footer.jsp" />
        </c:if>


        <%-- Bootstrap JavaScript --%>
        <c:if test="${useBootstrap}">
            <script
                src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js">
            </script>
        </c:if>


        <%-- Common JavaScript --%>
        <script
            src="${pageContext.request.contextPath}/js/flashMessage.js">
        </script>


        <%-- Page-specific JavaScript --%>
        <c:if test="${not empty pageJs}">
            <script
                src="${pageContext.request.contextPath}/js/${pageJs}">
            </script>
        </c:if>

    </body>

</html>