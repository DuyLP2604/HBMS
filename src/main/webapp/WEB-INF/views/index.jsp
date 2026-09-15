<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Simple Bear Hotel"
    pageCss="index.css"
    useBootstrap="true"
    showFooter="true"
    >

    <!-- Home Navigation -->
    <nav class="home-nav">
        <a href="${pageContext.request.contextPath}/home"
           class="home-nav-link active">
            Home
        </a>

        <a href="#overview"
           class="home-nav-link">
            Overview
        </a>

        <a href="${pageContext.request.contextPath}/booking?action=list"
           class="home-nav-link">
            Booking
        </a>

        <a href="${pageContext.request.contextPath}/service"
           class="home-nav-link">
            Service
        </a>
    </nav>


    <!-- Hero -->
    <section class="hero">

        <img
            src="${pageContext.request.contextPath}/assets/images/hotel/hotel2.jpg"
            alt="Simple Bear Hotel"
            >

        <div class="overlay"></div>

        <div class="hero-content">

            <h1>
                Simple Bear Hotel
            </h1>

            <p>
                Luxury • Comfort • Experience
            </p>

            <a
                href="${pageContext.request.contextPath}/booking?action=add"     
                class="btn btn-primary btn-lg btn-book mt-3"
                >
                Book Now
            </a>

        </div>

    </section>


    <!-- Hotel Information -->
    <div class="container">

        <div class="card hotel-card p-5">

            <div class="row align-items-center">

                <div class="col-lg-8">

                    <h2 class="fw-bold mb-4">
                        Simple Bear Hotel
                    </h2>


                    <div class="info-item">

                        <i class="bi bi-geo-alt-fill"></i>

                        600 Nguyen Van Cu Street (Extended),
                        An Binh Ward, Can Tho City

                    </div>


                    <div class="info-item">

                        <i class="bi bi-geo-alt-fill"></i>

                        Lot E2a-7, D1 Street, High-Tech Park,
                        Tang Nhon Phu Ward, Ho Chi Minh City,
                        Vietnam

                    </div>


                    <div class="info-item">

                        <i class="bi bi-geo-alt-fill"></i>

                        Area K, FPT University,
                        FPT Technology Urban Area,
                        Ngu Hanh Son Ward, Da Nang City,
                        Vietnam

                    </div>

                </div>


                <div class="col-lg-4 text-lg-end mt-4 mt-lg-0">

                    <a
                        href="${pageContext.request.contextPath}/booking?action=add"  
                        class="btn btn-primary btn-lg btn-book"
                        >
                        Booking
                    </a>

                </div>

            </div>

        </div>

    </div>


    <!-- Overview -->
    <section
        id="overview"
        class="py-5"
        >

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

                <!-- Luxury Rooms -->
                <div class="col-md-4">

                    <div class="card border-0 shadow-sm h-100">

                        <img
                            src="${pageContext.request.contextPath}/assets/images/hotel/hotel3.jpg"
                            class="card-img-top"
                            alt="Luxury hotel room"
                            >

                        <div class="card-body">

                            <h5>
                                Luxury Rooms
                            </h5>

                            <p>
                                Spacious rooms with elegant interiors.
                            </p>

                        </div>

                    </div>

                </div>


                <!-- Swimming Pool -->
                <div class="col-md-4">

                    <div class="card border-0 shadow-sm h-100">

                        <img
                            src="${pageContext.request.contextPath}/assets/images/hotel/hotel.jpg"
                            class="card-img-top"
                            alt="Hotel swimming pool"
                            >

                        <div class="card-body">

                            <h5>
                                Swimming Pool
                            </h5>

                            <p>
                                Relax in our outdoor swimming pool.
                            </p>

                        </div>

                    </div>

                </div>


                <!-- Restaurant -->
                <div class="col-md-4">

                    <div class="card border-0 shadow-sm h-100">

                        <img
                            src="${pageContext.request.contextPath}/assets/images/hotel/hotel4.jpg"
                            class="card-img-top"
                            alt="Hotel restaurant"
                            >

                        <div class="card-body">

                            <h5>
                                Restaurant
                            </h5>

                            <p>
                                Enjoy delicious dishes from top chefs.
                            </p>

                        </div>

                    </div>

                </div>

            </div>

        </div>

    </section>
</layout:layout>
