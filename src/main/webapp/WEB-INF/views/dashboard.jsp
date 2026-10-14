<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Dashboard"
    pageCss="dashboard.css"
    pageJs="dashboard.js"
    useBootstrap="true"
    >
    <div class="dashboard-container">
        <!-- Header -->
        <div class="dashboard-header d-flex justify-content-between align-items-center mb-4">
            <div class="dashboard-title">
                <i class="fas fa-chart-line dashboard-accent-icon"></i> Dashboard
            </div>

            <!-- Thanh Filter Bootstrap -->
            <form id="filterForm" class="date-filter d-flex align-items-center gap-2" action="${pageContext.request.contextPath}/dashboard" method="get">
                <!-- Date Picker Input -->
                <div class="position-relative d-flex align-items-center">
                    <i class="fas fa-calendar dashboard-muted-icon position-absolute" style="left: 12px; z-index: 10;"></i>
                    <input type="text" id="dateRange" name="dateRange" class="form-control form-control-sm ps-5" readonly style="cursor: pointer; min-width: 220px; background-color: #fff;">
                </div>

                <input type="hidden" id="from" name="from">
                <input type="hidden" id="to" name="to">

                <!-- Quick Filter Dropdown -->
                <div class="dropdown">
                    <button class="btn btn-outline-secondary btn-sm dropdown-toggle" type="button" data-bs-toggle="dropdown" style="border-radius: 6px; font-weight: 500;">
                        <i class="fas fa-filter"></i> Filter
                    </button>
                    <ul class="dropdown-menu shadow-sm">
                        <li><a class="dropdown-item" href="#" onclick="applyQuickFilter('week')">This Week</a></li>
                        <li><a class="dropdown-item" href="#" onclick="applyQuickFilter('month')">This Month</a></li>
                        <li><a class="dropdown-item" href="#" onclick="applyQuickFilter('year')">This Year</a></li>
                    </ul>
                </div>

                <!-- Reset Button -->
                <button type="button" class="btn btn-secondary btn-sm text-nowrap" onclick="resetFilter()" style="border-radius: 6px; font-weight: 500;">
                    <i class="fas fa-undo"></i> Reset
                </button>
            </form>
        </div>

        <!-- KPI Cards -->
        <div class="kpi-grid">
            <!-- Card 1: Revenue -->
            <div class="kpi-card">
                <div class="kpi-header">
                    <div class="kpi-title">Revenue</div>
                    <div class="kpi-icon"><i class="fas fa-coins"></i></div>
                </div>
                <div class="kpi-value" id="todayRevenue">
                    <fmt:formatNumber value="${todayRevenue}" type="number" pattern="#,##0" /> VND
                </div>
                <div class="kpi-subtitle">Revenue</div>
            </div>

            <!-- Card 2: Total Rooms Booked -->
            <div class="kpi-card">
                <div class="kpi-header">
                    <div class="kpi-title">Total Rooms Booked</div>
                    <div class="kpi-icon"><i class="fas fa-door-open"></i></div>
                </div>
                <div class="kpi-value" id="occupiedRooms">
                    <c:out value="${occupiedRooms}" />
                </div>
                <div class="kpi-subtitle">Total rooms reserved / occupied</div>
            </div>

            <!-- Card 3: Room-days used -->
            <div class="kpi-card">
                <div class="kpi-header">
                    <div class="kpi-title">Room-Days Used</div>
                    <div class="kpi-icon"><i class="fas fa-calendar-check"></i></div>
                </div>
                <div class="kpi-value" id="usedRoomDays">
                    <fmt:formatNumber value="${roomUtilization.usedRoomDays}" type="number" pattern="#,##0" />
                </div>
                <div class="kpi-subtitle">
                    Nights sold out of
                    <fmt:formatNumber value="${roomUtilization.availableRoomDays}" type="number" pattern="#,##0" />
                    available (${roomUtilization.activeRooms} rooms &times; ${roomUtilization.totalDays} days)
                </div>
            </div>

            <!-- Card 4: Occupancy rate -->
            <div class="kpi-card">
                <div class="kpi-header">
                    <div class="kpi-title">Occupancy Rate</div>
                    <div class="kpi-icon"><i class="fas fa-percent"></i></div>
                </div>
                <div class="kpi-value" id="occupancyRate">
                    <fmt:formatNumber value="${roomUtilization.occupancyRate}" type="number" minFractionDigits="1" maxFractionDigits="1" />%
                </div>
                <div class="kpi-subtitle">Room-days used / room-days available</div>
            </div>

            <!-- Card 5: Total Guests -->
            <div class="kpi-card">
                <div class="kpi-header">
                    <div class="kpi-title">Total Guests</div>
                    <div class="kpi-icon"><i class="fas fa-users"></i></div>
                </div>
                <div class="kpi-value" id="totalCustomers">
                    <c:out value="${totalCustomers}" />
                </div>
                <div class="kpi-subtitle">Total guests</div>
            </div>
        </div>

        <!-- Revenue Chart -->
        <div class="chart-card">
            <div class="chart-title">
                <i class="fas fa-chart-line dashboard-accent-icon"></i> Revenue
            </div>
            <canvas id="revenueChart"></canvas>
        </div>

        <!-- Room utilization report -->
        <div class="chart-card">
            <div class="chart-title">
                <i class="fas fa-bed"></i> Room Utilization
                <span class="utilization-period"><c:out value="${roomUtilization.periodLabel}" /></span>
            </div>
            <c:choose>
                <c:when test="${empty roomUtilization.byRoomType}">
                    <p class="text-muted mb-0">No room usage data for this period.</p>
                </c:when>
                <c:otherwise>
                    <div class="utilization-chart">
                        <canvas id="utilizationChart"></canvas>
                    </div>
                    <div class="table-responsive mt-4">
                        <table class="table table-hover align-middle utilization-table mb-0">
                            <thead>
                                <tr>
                                    <th>Room type</th>
                                    <th class="text-end">Active rooms</th>
                                    <th class="text-end">Room-days used</th>
                                    <th class="text-end">Room-days available</th>
                                    <th style="min-width: 180px;">Occupancy</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${roomUtilization.byRoomType}" var="u">
                                    <tr>
                                        <td class="fw-semibold"><c:out value="${u.typeName}" /></td>
                                        <td class="text-end">${u.roomCount}</td>
                                        <td class="text-end"><fmt:formatNumber value="${u.usedRoomDays}" type="number" pattern="#,##0" /></td>
                                        <td class="text-end"><fmt:formatNumber value="${u.availableRoomDays}" type="number" pattern="#,##0" /></td>
                                        <td>
                                            <div class="d-flex align-items-center gap-2">
                                                <div class="progress flex-grow-1" style="height: 8px;">
                                                    <div class="progress-bar utilization-bar" role="progressbar"
                                                         style="width: ${u.occupancyRate > 100 ? 100 : u.occupancyRate}%;"
                                                         aria-valuenow="${u.occupancyRate}" aria-valuemin="0" aria-valuemax="100"></div>
                                                </div>
                                                <span class="fw-semibold utilization-rate"><fmt:formatNumber value="${u.occupancyRate}" type="number" minFractionDigits="1" maxFractionDigits="1" />%</span>
                                            </div>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                            <tfoot>
                                <tr class="fw-bold">
                                    <td>Total</td>
                                    <td class="text-end">${roomUtilization.activeRooms}</td>
                                    <td class="text-end"><fmt:formatNumber value="${roomUtilization.usedRoomDays}" type="number" pattern="#,##0" /></td>
                                    <td class="text-end"><fmt:formatNumber value="${roomUtilization.availableRoomDays}" type="number" pattern="#,##0" /></td>
                                    <td><fmt:formatNumber value="${roomUtilization.occupancyRate}" type="number" minFractionDigits="1" maxFractionDigits="1" />%</td>
                                </tr>
                            </tfoot>
                        </table>
                    </div>
                    <p class="utilization-note">
                        Counts confirmed, assigned, checked-in and checked-out bookings (unpaid and cancelled bookings are excluded).
                        One room-day = one room occupied for one night; the check-out day is not counted.
                    </p>
                </c:otherwise>
            </c:choose>
        </div>

        <!-- Customer Source Chart -->
        <div class="chart-card">
            <div class="chart-title">Customer Sources</div>
            <canvas id="customerChart"></canvas>
        </div>
    </div>

    <!-- Server data for dashboard.js -->
    <div id="dashboardData" hidden>
        <div id="revenueData">
            <c:forEach items="${revenueChart}" var="r">
                <span class="revenue-data-item" data-day="${r.day}" data-revenue="${r.revenue}"></span>
            </c:forEach>
        </div>
        <div id="utilizationData">
            <c:forEach items="${roomUtilization.byRoomType}" var="u">
                <span class="utilization-data-item" data-type="${fn:escapeXml(u.typeName)}" data-rate="${u.occupancyRate}" data-used="${u.usedRoomDays}" data-available="${u.availableRoomDays}"></span>
            </c:forEach>
        </div>
        <div id="customerSourceData">
            <c:forEach items="${customerSource}" var="c">
                <span class="customer-data-item" data-country="${c.country}" data-total="${c.total}"></span>
            </c:forEach>
        </div>
    </div>

    <!-- Dashboard dependencies -->
    <script src="https://cdn.jsdelivr.net/npm/jquery@3.6.0/dist/jquery.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/moment@2.29.4/moment.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/daterangepicker/daterangepicker.min.js"></script>
    <script src="https://cdn.jsdelivr.net/npm/chart.js@3.9.1/dist/chart.min.js"></script>

    <script>
                    $(document).ready(function () {
                        const urlParams = new URLSearchParams(window.location.search);
                        let fromDate = urlParams.get('from');
                        let toDate = urlParams.get('to');

                        let start, end;
                        let isResetState = false;

                        if (fromDate && toDate) {
                            start = moment(fromDate, "YYYY-MM-DD");
                            end = moment(toDate, "YYYY-MM-DD");
                            if (fromDate === "2010-01-01") {
                                isResetState = true;
                            }
                        } else {
                            start = moment().subtract(29, 'days');
                            end = moment();
                        }

                        $('#from').val(start.format('YYYY-MM-DD'));
                        $('#to').val(end.format('YYYY-MM-DD'));

                        $('#dateRange').daterangepicker({
                            startDate: start,
                            endDate: end,
                            autoApply: true,
                            locale: {
                                format: 'DD/MM/YYYY'
                            }
                        });
                        if (isResetState || (!fromDate && !toDate)) {
                            $('#dateRange').val('All Time');
                        }

                        $('#dateRange').on('apply.daterangepicker', function (ev, picker) {
                            $('#from').val(picker.startDate.format('YYYY-MM-DD'));
                            $('#to').val(picker.endDate.format('YYYY-MM-DD'));
                            $('#filterForm').submit();
                        });
                    });

                    function applyQuickFilter(type) {
                        let start, end = moment();
                        if (type === 'week') {
                            start = moment().subtract(6, 'days');
                        } else if (type === 'month') {
                            start = moment().startOf('month');
                        } else if (type === 'year') {
                            start = moment().startOf('year');
                        }

                        $('#from').val(start.format('YYYY-MM-DD'));
                        $('#to').val(end.format('YYYY-MM-DD'));

                        $('#dateRange').data('daterangepicker').setStartDate(start);
                        $('#dateRange').data('daterangepicker').setEndDate(end);
                        $('#filterForm').submit();
                    }

                    // Nút Reset
                    function resetFilter() {
                        $('#from').val('2010-01-01');
                        $('#to').val(moment().format('YYYY-MM-DD'));
                        $('#filterForm').submit();
                    }
    </script>
</layout:layout>