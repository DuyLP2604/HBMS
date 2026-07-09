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
        <style>
            .navbar{
                height:70px;
                background:#1f2937;
                color:white;

                display:flex;
                justify-content:space-between;
                align-items:center;

                padding:0 40px;
            }

            .logo{
                font-size:24px;
                font-weight:bold;
            }

            .logo i{
                color:#c8a96b;
                margin-right:8px;
            }

            .nav-links{
                display:flex;
                gap:25px;
                list-style:none;
            }

            .nav-links a{
                color:white;
                text-decoration:none;
                transition:0.3s;
            }

            .nav-links a:hover{
                color:#c8a96b;
            }

            .user-menu{
                display:flex;
                align-items:center;
                gap:15px;
            }

            .logout-btn{
                background:#c8a96b;
                color:white;
                padding:8px 15px;
                border-radius:6px;
                text-decoration:none;
            }
            .auth-btn{
                text-decoration:none;
                background:#c8a96b;
                color:white;
                padding:10px 18px;
                border-radius:8px;
                font-weight:600;
                transition:0.3s;
            }

            .auth-btn:hover{
                background:#b08f4f;
            }

        </style>
    </head>
    <body>
        <nav class="navbar">
            <div class="logo">
                <i class="fa-solid fa-hotel"></i>
                Hotel Management
            </div>

            <ul class="nav-links">
                <li><a href="home">Home</a></li>
                <li><a href="rooms">Rooms</a></li>
                <li><a href="booking">Booking</a></li>
                <li><a href="about">About</a></li>
            </ul>

            <div class="user-menu">

                <c:choose>

                    <c:when test="${not empty sessionScope.user}">
                        <span>
                            <i class="fa-solid fa-user"></i>
                            Hi, ${sessionScope.user.username}
                        </span>

                        <a href="logout" class="auth-btn">
                            <i class="fa-solid fa-right-from-bracket"></i>
                            Logout
                        </a>
                    </c:when>

                    <c:otherwise>
                        <a href="login.jsp" class="auth-btn">
                            <i class="fa-solid fa-right-to-bracket"></i>
                            Login
                        </a>
                    </c:otherwise>

                </c:choose>

            </div>
        </nav>
    </body>
</html>
