<%-- 
    Document   : navbar
    Created on : Jul 9, 2026, 7:08:56 PM
    Author     : default
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Nav</title>
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
        <link rel ="stylesheet" href ="css/dropdown.css">
        <link rel ="stylesheet" href ="css/navbar.css">
    </head>
    <body>
        <nav class="navbar">

            <div class="logo">
                <i class="fa-solid fa-hotel"></i>
                Hotel Management
            </div>

            <jsp:include page="dropdownMenu.jsp" />

        </nav>
    </body>
</html>
