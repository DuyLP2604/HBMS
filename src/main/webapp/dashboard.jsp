<%-- 
    Document   : dashboard
    Created on : Jul 13, 2026, 4:26:27 PM
    Author     : ADMIN
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Dashboard</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css">
        <link rel="stylesheet" href="css/dashboard.css">
    </head>
    <body>
        <jsp:include page="components/navbar.jsp" />

        <div class="dashboard-container">
            <div class="dashboard-header">
                <div class="dashboard-title">
                    <i class="fas fa-chart-line" style="color: #c8a96b; margin-right: 10px;"></i>
                    Dashboard
                </div>
                <form class="date-filter" action="dashboard" method="get">
                    <i class="fas fa-calendar" style="color:#6b7280;"></i>

                    <input type="text"
                           id="dateRange"
                           name="dateRange"
                           readonly>

                    <input type="hidden" id="from" name="from">
                    <input type="hidden" id="to" name="to">

                    <button type="submit" class="btn-filter">
                        <i class="fas fa-sync-alt"></i>
                        Apply
                    </button>
                </form>
            </div>

            <!-- KPI Cards -->
            <div class="kpi-grid">
                <div class="kpi-card">
                    <div class="kpi-header">
                        <div class="kpi-title">Revenue</div>
                        <div class="kpi-icon"><i class="fas fa-coins"></i></div>
                    </div>
                    <div class="kpi-value" id="todayRevenue">${todayRevenue}</div>
                    <div class="kpi-subtitle">Revenue</div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-header">
                        <div class="kpi-title">Occupied Rooms</div>
                        <div class="kpi-icon"><i class="fas fa-door-open"></i></div>
                    </div>
                    <div class="kpi-value" id="occupiedRooms">${occupiedRooms}/${totalRooms}</div>
                    <div class="kpi-subtitle">Rooms currently occupied</div>
                </div>

                <div class="kpi-card">
                    <div class="kpi-header">
                        <div class="kpi-title">Total Guests</div>
                        <div class="kpi-icon"><i class="fas fa-users"></i></div>
                    </div>
                    <div class="kpi-value" id="totalCustomers">${totalCustomers}</div>
                    <div class="kpi-subtitle">Total guests</div>
                </div>
            </div>

            <!-- Chart Section -->
            <div class="chart-card">
                <div class="chart-title">
                    <i class="fas fa-chart-line" style="color: #c8a96b; margin-right: 10px;"></i>
                    Revenue (Last 10 Days)
                </div>
                <canvas id="revenueChart"></canvas>
            </div>

            <div class="chart-card">
                <div class="chart-title">
                    Customer Sources
                </div>
                <canvas id="customerChart"></canvas>
            </div>
        </div>

        <!-- Scripts -->
        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/moment@2.29.4/moment.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js"></script>
        <script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>

        <script>
            // Initialize Date Range Picker
            $(function () {

                $('#dateRange').daterangepicker({

                    startDate: moment(),
                    endDate: moment(),

                    locale: {
                        format: 'DD/MM/YYYY'
                    }

                }, function (start, end) {

                    $('#from').val(start.format('YYYY-MM-DD'));
                    $('#to').val(end.format('YYYY-MM-DD'));

                });

                $('#from').val(moment().format('YYYY-MM-DD'));
                $('#to').val(moment().format('YYYY-MM-DD'));

            });

            const revenueLabels = [
            <c:forEach items="${revenueChart}" var="r">
                '${r.day}',
            </c:forEach>
            ];
            const revenueData = [
            <c:forEach items="${revenueChart}" var="r">
                ${r.revenue},
            </c:forEach>
            ];
            new Chart(document.getElementById("revenueChart"), {
                type: 'line',
                data: {
                    labels: revenueLabels,
                    datasets: [{
                            label: 'Revenue',
                            data: revenueData,
                            tension: 0.35,
                            borderColor: '#c8a96b',
                            backgroundColor: 'rgba(200, 169, 107, 0.18)',
                            pointBackgroundColor: '#c8a96b',
                            pointBorderColor: '#ffffff',
                            pointRadius: 5,
                            pointHoverRadius: 7,
                            fill: true
                        }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'top',
                            labels: {
                                color: '#475569',
                                usePointStyle: true,
                                pointStyle: 'circle'
                            }
                        },
                        tooltip: {
                            mode: 'index',
                            intersect: false,
                            backgroundColor: '#111827',
                            titleColor: '#ffffff',
                            bodyColor: '#e5e7eb'
                        }
                    },
                    scales: {
                        x: {
                            grid: {
                                display: false
                            },
                            ticks: {
                                color: '#64748b'
                            }
                        },
                        y: {
                            grid: {
                                color: 'rgba(148, 163, 184, 0.18)',
                                drawBorder: false
                            },
                            ticks: {
                                color: '#64748b'
                            }
                        }
                    }
                }
            });

            const customerLabels = [
            <c:forEach items="${customerSource}" var="c">
                '${c.country}',
            </c:forEach>
            ];
            const customerData = [
            <c:forEach items="${customerSource}" var="c">
                ${c.total},
            </c:forEach>
            ];
            new Chart(document.getElementById("customerChart"), {
                type: 'pie',
                data: {
                    labels: customerLabels,
                    datasets: [{
                            data: customerData,
                            backgroundColor: [
                                '#c8a96b',
                                '#f59e0b',
                                '#10b981',
                                '#3b82f6',
                                '#8b5cf6',
                                '#ef4444',
                                '#f97316',
                                '#0ea5e9'
                            ],
                            borderColor: '#ffffff',
                            borderWidth: 3,
                            hoverOffset: 14
                        }]
                },
                options: {
                    responsive: true,
                    maintainAspectRatio: false,
                    plugins: {
                        legend: {
                            position: 'bottom',
                            labels: {
                                color: '#475569',
                                usePointStyle: true,
                                pointStyle: 'circle'
                            }
                        },
                        tooltip: {
                            backgroundColor: '#111827',
                            titleColor: '#ffffff',
                            bodyColor: '#e5e7eb'
                        }
                    }
                }
            });
        </script>
    </body>
</html>
