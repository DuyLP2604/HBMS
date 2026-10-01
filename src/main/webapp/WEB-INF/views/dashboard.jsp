<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Dashboard"
    pageCss="dashboard.css"
    pageJs="dashboard.js"
    useBootstrap="true"
    >

    <div class="dashboard-container">

        <!-- Header -->
        <div class="dashboard-header">

            <div class="dashboard-title">
                <i class="fas fa-chart-line dashboard-accent-icon"></i>
                Dashboard
            </div>

            <form
                class="date-filter"
                action="${pageContext.request.contextPath}/dashboard"
                method="get"
                >

                <i class="fas fa-calendar dashboard-muted-icon"></i>

                <input
                    type="text"
                    id="dateRange"
                    name="dateRange"
                    readonly
                    >

                <input
                    type="hidden"
                    id="from"
                    name="from"
                    >

                <input
                    type="hidden"
                    id="to"
                    name="to"
                    >

                <button
                    type="submit"
                    class="btn-filter"
                    >
                    <i class="fas fa-sync-alt"></i>
                    Apply
                </button>

            </form>

        </div>


        <!-- KPI Cards -->
        <div class="kpi-grid">

            <div class="kpi-card">

                <div class="kpi-header">

                    <div class="kpi-title">
                        Revenue
                    </div>

                    <div class="kpi-icon">
                        <i class="fas fa-coins"></i>
                    </div>

                </div>

                <div
                    class="kpi-value"
                    id="todayRevenue"
                    >
                    <c:out value="${todayRevenue}" />
                </div>

                <div class="kpi-subtitle">
                    Revenue
                </div>

            </div>


            <div class="kpi-card">

                <div class="kpi-header">

                    <div class="kpi-title">
                        Occupied Rooms
                    </div>

                    <div class="kpi-icon">
                        <i class="fas fa-door-open"></i>
                    </div>

                </div>

                <div
                    class="kpi-value"
                    id="occupiedRooms"
                    >
                    <c:out value="${occupiedRooms}" />
                    /
                    <c:out value="${totalRooms}" />
                </div>

                <div class="kpi-subtitle">
                    Rooms currently occupied
                </div>

            </div>


            <div class="kpi-card">

                <div class="kpi-header">

                    <div class="kpi-title">
                        Total Guests
                    </div>

                    <div class="kpi-icon">
                        <i class="fas fa-users"></i>
                    </div>

                </div>

                <div
                    class="kpi-value"
                    id="totalCustomers"
                    >
                    <c:out value="${totalCustomers}" />
                </div>

                <div class="kpi-subtitle">
                    Total guests
                </div>

            </div>

        </div>


        <!-- Revenue Chart -->
        <div class="chart-card">

            <div class="chart-title">
                <i class="fas fa-chart-line dashboard-accent-icon"></i>
                Revenue
            </div>

            <canvas id="revenueChart"></canvas>

        </div>


        <!-- Customer Source Chart -->
        <div class="chart-card">

            <div class="chart-title">
                Customer Sources
            </div>

            <canvas id="customerChart"></canvas>

        </div>

    </div>


    <!--
        Server data for dashboard.js.
        This avoids putting JSTL code inside the external JS file.
    -->
    <div id="dashboardData" hidden>

        <div id="revenueData">

            <c:forEach
                items="${revenueChart}"
                var="r"
                >
                <span
                    class="revenue-data-item"
                    data-day="${r.day}"
                    data-revenue="${r.revenue}"
                    ></span>
            </c:forEach>

        </div>


        <div id="customerSourceData">

            <c:forEach
                items="${customerSource}"
                var="c"
                >
                <span
                    class="customer-data-item"
                    data-country="${c.country}"
                    data-total="${c.total}"
                    ></span>
            </c:forEach>

        </div>

    </div>


    <!-- Dashboard dependencies -->
    <script
        src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js">
    </script>

    <script
        src="https://cdn.jsdelivr.net/npm/moment@2.29.4/moment.min.js">
    </script>

    <script
        src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js">
    </script>

    <script
        src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js">
    </script>

</layout:layout>
