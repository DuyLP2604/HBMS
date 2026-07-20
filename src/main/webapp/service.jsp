<%-- 
    Document   : service
    Created on : Jul 19, 2026, 11:07:22 PM
    Author     : ADMIN
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Service</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <style>
            *{
                font-family: 'Poppins', sans-serif;
            }
            body{
                background:#f8f9fa;
            }
            .top-header{
                background:white;
                border-bottom:1px solid #e9ecef;
            }
            .logo{
                font-size:32px;
                font-weight:700;
            }
            .navbar{
                background:white;
                box-shadow:0 2px 10px rgba(0,0,0,.05);
            }
            .nav-link{
                font-weight:600;
                color:#333;
                position:relative;
                margin:0 10px;
            }
            .nav-link::after{
                content:"";
                position:absolute;
                width:0;
                height:2px;
                background:#0d6efd;
                left:50%;
                bottom:0;
                transform:translateX(-50%);
                transition:.3s;
            }
            .nav-link:hover::after{
                width:70%;
            }
            .hero{
                position:relative;
                height:650px;
                overflow:hidden;
            }
            .hero img{
                width:100%;
                height:100%;
                object-fit:cover;
            }
            .overlay{
                position:absolute;
                inset:0;
                background:rgba(0,0,0,.45);
            }
            .hero-content{
                position:absolute;
                top:50%;
                left:50%;
                transform:translate(-50%,-50%);
                text-align:center;
                color:white;
                animation:fadeUp .8s ease;
            }
            .hero-content h1{
                font-size:4rem;
                font-weight:700;
            }
            .hero-content p{
                font-size:1.3rem;
            }
            @keyframes fadeUp{
                from{
                    opacity:0;
                    transform:translate(-50%, -20%);
                }
                to{
                    opacity:1;
                    transform:translate(-50%, -50%);
                }
            }
            .hotel-card{
                margin-top:-100px;
                position:relative;
                z-index:10;
                border:none;
                border-radius:20px;
                box-shadow:
                    0 15px 40px rgba(0,0,0,.12);
            }
            .hotel-card:hover{
                transform:translateY(-5px);
                transition:.3s;
            }
            .btn-book{
                border-radius:50px;
                padding:12px 30px;
                font-weight:600;
            }
            .btn-book:hover{
                transform:translateY(-2px);
                transition:.3s;
            }
            .info-item{
                margin-bottom:15px;
            }
            .info-item i{
                color:#0d6efd;
                margin-right:10px;
            }
            .user-menu{
                position:relative;
            }
            .dropdown-btn{
                background:white;
                border:1px solid #0d6efd;
                color:#0d6efd;
                border-radius:50px;
                padding:10px 22px;
                font-weight:600;
                transition:.3s;
            }
            .dropdown-btn:hover{
                background:#0d6efd;
                color:white;
            }
            .dropdown-content{
                display:none;
                position:absolute;
                top:60px;
                right:0;
                width:230px;
                background:white;
                border-radius:15px;
                overflow:hidden;
                box-shadow:0 10px 30px rgba(0,0,0,.15);
                z-index:999;
            }
            .dropdown-content.show{
                display:block;
            }
            .dropdown-content a{
                display:block;
                padding:13px 18px;
                color:#333;
                text-decoration:none;
                transition:.2s;
                font-weight:500;
            }
            .dropdown-content a:hover{
                background:#f5f5f5;
                padding-left:24px;
            }
            .dropdown-divider{
                margin:0;
            }
            .logout-link{
                color:#dc3545 !important;
            }
        </style>
    </head>
    <body>
        <div class="top-header py-3">
            <div class="container">
                <div class="d-flex justify-content-between align-items-center">
                    <div class="logo">
                        Simple Bear Hotel
                    </div>
                    <div class="user-menu">
                        <c:choose>
                            <c:when test="${not empty sessionScope.user}">
                                <div class="dropdown position-relative">
                                    <button type="button"
                                            class="dropdown-btn"
                                            onclick="toggleMenu()">
                                        <i class="bi bi-person-circle me-1"></i>
                                        Hi, ${sessionScope.user.username}
                                        <i class="bi bi-caret-down-fill ms-1"></i>
                                    </button>
                                    <div class="dropdown-content shadow"
                                         id="userDropdown">
                                        <c:if test="${sessionScope.user.role eq 'Customer'}">
                                            <a href="profile?action=view">
                                                Profile
                                            </a>
                                            <a href="my-bookings">
                                                My Bookings
                                            </a>
                                            <a href="add-complaint">
                                                Add Complaint
                                            </a>
                                        </c:if>
                                        <c:if test="${sessionScope.user.role eq 'Staff'}">
                                            <a href="customer?action=add">
                                                Add Customer
                                            </a>
                                            <a href="manage-bookings">
                                                Manage Bookings
                                            </a>
                                            <a href="manage-rooms">
                                                Manage Rooms
                                            </a>
                                        </c:if>
                                        <c:if test="${sessionScope.user.role eq 'Admin'}">
                                            <a href="dashboard">
                                                Dashboard
                                            </a>
                                            <a href="employee?action=add">
                                                Add Employee
                                            </a>
                                            <a href="customer?action=list">
                                                View Customers
                                            </a>
                                            <a href="complaint?action=list">
                                                View Complaints
                                            </a>
                                        </c:if>
                                        <div class="dropdown-divider"></div>
                                        <a href="logout" class="logout-link">
                                            <i class="bi bi-box-arrow-right me-2"></i>
                                            Logout
                                        </a>
                                    </div>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <a href="login"
                                   class="btn btn-outline-primary rounded-pill px-4">
                                    Login
                                </a>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>
        </div>

        <nav class="navbar navbar-expand-lg">
            <div class="container">
                <ul class="navbar-nav mx-auto">
                    <li class="nav-item">
                        <a class="nav-link active" href="index.jsp">Home</a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="#overview">
                            Overview
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="booking">
                            Booking
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="service">
                            Service
                        </a>
                    </li>
                    <li class="nav-item">
                        <a class="nav-link" href="rate">
                            Reviews
                        </a>
                    </li>
                </ul>
            </div>
        </nav>
        <div class="container mt-5">
            <div class="card shadow">
                <div class="card-header bg-primary text-white">
                    <h3 class="mb-0">Select Hotel Services</h3>
                </div>
                <div class="card-body">
                    <form action="service" method="post">
                        <input type="hidden"
                               name="bookingID"
                               value="${bookingID}">

                        <table class="table table-bordered table-hover align-middle">

                            <thead class="table-light">
                                <tr>
                                    <th width="10%" class="text-center">
                                        Select
                                    </th>
                                    <th>
                                        Service Name
                                    </th>
                                    <th width="20%">
                                        Price
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${list}" var="s">
                                    <tr>
                                        <td class="text-center">
                                            <input
                                                class="form-check-input"
                                                type="checkbox"
                                                name="serviceID"
                                                value="${s.serviceID}">
                                        </td>

                                        <td>
                                            ${s.serviceName}
                                        </td>
                                        <td>
                                            ${s.unitPrice}
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <div class="text-end">

                            <a href="index.jsp" class="btn btn-secondary">
                                Back
                            </a>
                            <button type="submit" class="btn btn-success" >
                                Submit
                            </button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <script>
            function toggleMenu() {
                document.getElementById("userDropdown")
                        .classList.toggle("show");
            }
            document.addEventListener("click", function (e) {
                const dropdown = document.querySelector(".dropdown");
                if (!dropdown)
                    return;
                const menu = document.getElementById("userDropdown");
                if (!dropdown.contains(e.target)) {
                    menu.classList.remove("show");
                }
            });
        </script>
    </body>
</html>
