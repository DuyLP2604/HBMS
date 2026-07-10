<%-- 
    Document   : errorMessage
    Created on : Jul 9, 2026, 9:12:56 PM
    Author     : default
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Error</title>
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
        <style>
            .error-message{
                margin-bottom:15px;
                padding:12px;

                background:rgba(255,0,0,0.15);
                border:1px solid rgba(255,255,255,0.15);
                border-left:4px solid #ff6b6b;

                border-radius:8px;

                color:#ffffff;
            }

            .error-message i{
                color:#ff6b6b;
                margin-right:6px;
            }
        </style>
    </head>
    <body>
    <c:if test="${not empty error}">
        <div class="error-message">
            <i class="fa-solid fa-circle-exclamation"></i>
            ${error}
        </div>
    </c:if>
</body>
</html>
