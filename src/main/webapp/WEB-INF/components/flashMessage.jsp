<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<c:if test="${not empty flashMessage}">

    <div id="flash-message"
         class="flash-message flash-${flashMessage.type.name().toLowerCase()}"
         role="alert">

        <div class="flash-icon">

            <c:choose>

                <c:when test="${flashMessage.type == 'SUCCESS'}">
                    ✓
                </c:when>

                <c:when test="${flashMessage.type == 'ERROR'}">
                    ✕
                </c:when>

                <c:when test="${flashMessage.type == 'WARNING'}">
                    !
                </c:when>

                <c:otherwise>
                    i
                </c:otherwise>

            </c:choose>

        </div>

        <div class="flash-content">
            <c:out value="${flashMessage.message}" />
        </div>

        <button type="button"
                class="flash-close"
                aria-label="Close">
            ×
        </button>

    </div>

</c:if>