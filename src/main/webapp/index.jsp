<%-- 
    Document   : index
    Created on : Jul 9, 2026, 5:00:13 PM
    Author     : default
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Simple Bear Hotel</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
        <link href="https://fonts.googleapis.com/css2?family=Poppins:wght@300;400;500;600;700&display=swap" rel="stylesheet">
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
        <jsp:include page="components/navbar.jsp" />

        <nav class="navbar navbar-expand-lg" style = "background-color: white">
            <div class="container">
                <ul class="navbar-nav mx-auto">
                    <li class="nav-item">
                        <a class="nav-link active">Home</a>
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
                   
                </ul>
            </div>
        </nav>
        <section class="hero">
            <img src="assets/images/hotel/hotel2.jpg">
            <div class="overlay"></div>
            <div class="hero-content">
                <h1>Simple Bear Hotel</h1>
                <p>
                    Luxury • Comfort • Experience
                </p>
                <a href="booking"
                   class="btn btn-primary btn-lg btn-book mt-3">
                    Book Now
                </a>
            </div>
        </section>

        <div class="container">
            <div class="card hotel-card p-5">
                <div class="row align-items-center">
                    <div class="col-lg-8">
                        <h2 class="fw-bold mb-4">
                            Simple Bear Hotel
                        </h2>
                        <div class="info-item">
                            <i class="bi bi-geo-alt-fill"></i>
                            Số 600, đường Nguyễn Văn Cừ (nối dài),
                            Phường An Bình, TP Cần Thơ
                        </div>
                        <div class="info-item">
                            <i class="bi bi-geo-alt-fill"></i>
                            Lô E2a-7, Đường D1, Khu Công nghệ cao, Phường Tăng Nhơn Phú, TP. Hồ Chí Minh, Việt Nam
                        </div>
                        <div class="info-item">
                            <i class="bi bi-geo-alt-fill"></i>
                            Khu K, FU, Khu đô thị Công nghệ FPT Đà Nẵng, Phường Ngũ Hành Sơn, TP Đà Nẵng, Việt Nam
                        </div>
                    </div>
                    <div class="col-lg-4 text-lg-end mt-4 mt-lg-0">
                        <a href="booking"
                           class="btn btn-primary btn-lg btn-book">
                            Booking
                        </a>
                    </div>
                </div>
            </div>
        </div>

        <section id="overview" class="py-5">
            <div class="container">
                <div class="text-center mb-5">
                    <h2 class="fw-bold">
                        Overview
                    </h2>
                    <p class="text-muted">
                        Enjoy a luxurious stay with modern facilities.
                    </p>
                </div>

                <div class="row g-4">
                    <div class="col-md-4">
                        <div class="card border-0 shadow-sm h-100">
                            <img src="assets/images/hotel/hotel3.jpg" class="card-img-top">
                            <div class="card-body">
                                <h5>Luxury Rooms</h5>
                                <p>
                                    Spacious rooms with elegant interiors.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="card border-0 shadow-sm h-100">
                            <img src="assets/images/hotel/hotel.jpg" class="card-img-top">
                            <div class="card-body">
                                <h5>Swimming Pool</h5>
                                <p>
                                    Relax in our outdoor swimming pool.
                                </p>
                            </div>
                        </div>
                    </div>

                    <div class="col-md-4">
                        <div class="card border-0 shadow-sm h-100">
                            <img src="assets/images/hotel/hotel4.jpg" class="card-img-top">
                            <div class="card-body">
                                <h5>Restaurant</h5>
                                <p>
                                    Enjoy delicious dishes from top chefs.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </section>

        <footer class="bg-dark text-white text-center py-4">
            <h5>Simple Bear Hotel</h5>
            <p class="mb-0">
                Luxury • Comfort • Experience
            </p>
        </footer>

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
