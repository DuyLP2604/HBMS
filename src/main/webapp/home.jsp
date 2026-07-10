<%-- 
    Document   : home
    Created on : Jul 10, 2026, 10:52:30 PM
    Author     : ADMIN
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Home</title>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css">
        <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.css">
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
            }

            body {
                min-height: 100vh;
                background: linear-gradient(180deg, #eff4ff 0%, #f7f9fc 100%);
                font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif;
                color: #1f2937;
            }

            .dashboard-container {
                padding: 32px 24px 48px;
                max-width: 1240px;
                margin: 0 auto;
            }

            .dashboard-header {
                display: grid;
                grid-template-columns: 1fr auto;
                gap: 18px;
                align-items: center;
                margin-bottom: 30px;
            }

            .dashboard-intro {
                padding: 28px 30px;
                background: rgba(255, 255, 255, 0.82);
                border: 1px solid rgba(148, 163, 184, 0.18);
                border-radius: 24px;
                box-shadow: 0 20px 60px rgba(148, 163, 184, 0.08);
            }

            .dashboard-welcome {
                font-size: 32px;
                font-weight: 700;
                line-height: 1.1;
                color: #111827;
                margin-bottom: 10px;
            }

            .dashboard-subtitle {
                font-size: 16px;
                color: #475569;
                max-width: 680px;
                line-height: 1.75;
            }

            .date-filter {
                display: flex;
                gap: 12px;
                align-items: center;
                flex-wrap: wrap;
                justify-content: flex-end;
            }

            .date-filter input[type='text'] {
                padding: 12px 16px;
                border: 1px solid rgba(148, 163, 184, 0.32);
                border-radius: 14px;
                font-size: 14px;
                min-width: 280px;
                background: #ffffff;
                cursor: pointer;
                transition: border-color 0.2s ease, box-shadow 0.2s ease;
            }

            .date-filter input[type='text']:focus {
                outline: none;
                border-color: #8b5cf6;
                box-shadow: 0 0 0 4px rgba(139, 92, 246, 0.12);
            }

            .btn-filter {
                padding: 12px 24px;
                background: linear-gradient(135deg, #c8a96b 0%, #a67d3a 100%);
                color: white;
                border: none;
                border-radius: 14px;
                cursor: pointer;
                font-weight: 700;
                transition: transform 0.25s ease, box-shadow 0.25s ease;
                box-shadow: 0 12px 30px rgba(200, 169, 107, 0.18);
            }

            .btn-filter:hover {
                transform: translateY(-2px);
                box-shadow: 0 18px 36px rgba(200, 169, 107, 0.24);
            }

            .kpi-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
                gap: 22px;
                margin-bottom: 30px;
            }

            .kpi-card {
                background: linear-gradient(180deg, #ffffff 0%, #f8fafc 100%);
                padding: 28px 26px;
                border-radius: 22px;
                border: 1px solid rgba(148, 163, 184, 0.18);
                box-shadow: 0 18px 40px rgba(15, 23, 42, 0.06);
                position: relative;
                overflow: hidden;
                transition: transform 0.3s ease, box-shadow 0.3s ease;
            }

            .kpi-card::before {
                content: '';
                position: absolute;
                top: -12px;
                right: -12px;
                width: 72px;
                height: 72px;
                background: rgba(200, 169, 107, 0.12);
                border-radius: 50%;
                z-index: 0;
            }

            .kpi-card:hover {
                transform: translateY(-5px);
                box-shadow: 0 24px 52px rgba(15, 23, 42, 0.12);
            }

            .kpi-header {
                display: flex;
                justify-content: space-between;
                align-items: start;
                margin-bottom: 18px;
                position: relative;
                z-index: 1;
            }

            .kpi-title {
                font-size: 13px;
                color: #64748b;
                font-weight: 700;
                text-transform: uppercase;
                letter-spacing: 0.14em;
            }

            .kpi-icon {
                font-size: 28px;
                color: #c8a96b;
                opacity: 0.92;
            }

            .kpi-value {
                font-size: 38px;
                font-weight: 800;
                color: #111827;
                margin-bottom: 10px;
                position: relative;
                z-index: 1;
            }

            .kpi-subtitle {
                font-size: 13px;
                color: #64748b;
                line-height: 1.7;
                position: relative;
                z-index: 1;
            }

            .chart-card {
                background: white;
                padding: 30px;
                border-radius: 22px;
                border: 1px solid rgba(148, 163, 184, 0.18);
                box-shadow: 0 20px 50px rgba(15, 23, 42, 0.06);
                margin-bottom: 24px;
            }

            .chart-card canvas {
                display: block;
                width: 100% !important;
                max-height: 340px;
                margin: 0 auto;
            }

            .chart-title {
                font-size: 18px;
                font-weight: 800;
                color: #111827;
                margin-bottom: 20px;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .chart-title i {
                color: #c8a96b;
            }

            .chart-container {
                position: relative;
                height: 360px;
            }

            .chart-card:last-of-type {
                margin-bottom: 0;
            }

            @media (max-width: 992px) {
                .dashboard-header {
                    grid-template-columns: 1fr;
                }

                .date-filter {
                    justify-content: flex-start;
                }
            }

            @media (max-width: 768px) {
                .date-filter {
                    flex-direction: column;
                    width: 100%;
                }

                .date-filter input[type='text'] {
                    width: 100%;
                    min-width: unset;
                }

                .kpi-grid {
                    grid-template-columns: 1fr;
                }

                .chart-container {
                    height: 300px;
                }
            }
        </style>
    </head>
    <body>
        <jsp:include page="components/navbar.jsp" />

        <div class="dashboard-container">
            <div class="dashboard-header">
                <div class="dashboard-title">
                    <i class="fas fa-chart-line" style="color: #c8a96b; margin-right: 10px;"></i>
                    Dashboard
                </div>
                <form class="date-filter" action="home" method="get">
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
