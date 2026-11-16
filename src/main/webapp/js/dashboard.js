// Renders the admin dashboard charts and report controls.
document.addEventListener("DOMContentLoaded", function () {

    initializeDateFilters();
    initializeRevenueChart();
    initializeUtilizationChart();
    initializeServiceChart();
    initializeCustomerChart();

});


function initializeDateFilters() {
    const form = document.getElementById("filterForm");
    if (!form) {
        return;
    }

    const fromInput = form.elements.namedItem("from");
    const toInput = form.elements.namedItem("to");

    if (typeof $ === "undefined"
            || typeof moment === "undefined"
            || !$.fn.daterangepicker) {
        return;
    }

    const dateRange = $("#dateRange");
    const isAllTime = form.dataset.allTime === "true";
    const endDate = moment(toInput.value, "YYYY-MM-DD");
    const startDate = isAllTime
            ? endDate.clone().subtract(29, "days")
            : moment(fromInput.value, "YYYY-MM-DD");

    dateRange.daterangepicker({
        startDate: startDate,
        endDate: endDate,
        autoApply: true,
        autoUpdateInput: false,
        locale: { format: "DD/MM/YYYY" }
    });

    dateRange.on("apply.daterangepicker", function (event, picker) {
        fromInput.value = picker.startDate.format("YYYY-MM-DD");
        toInput.value = picker.endDate.format("YYYY-MM-DD");
        form.requestSubmit();
    });
}


function initializeRevenueChart() {

    const canvas =
            document.getElementById("revenueChart");

    if (!canvas || typeof Chart === "undefined") {
        return;
    }

    const items =
            document.querySelectorAll(
                ".revenue-data-item"
            );

    const labels = [];
    const data = [];

    items.forEach(function (item) {

        labels.push(
            item.dataset.day
        );

        data.push(
            Number(item.dataset.revenue)
        );
    });


    new Chart(canvas, {

        type: "line",

        data: {

            labels: labels,

            datasets: [
                {
                    label: "Revenue",
                    data: data,
                    tension: 0.35,

                    borderColor: "#c8a96b",

                    backgroundColor:
                            "rgba(200, 169, 107, 0.18)",

                    pointBackgroundColor:
                            "#c8a96b",

                    pointBorderColor:
                            "#ffffff",

                    pointRadius: 5,

                    pointHoverRadius: 7,

                    fill: true
                }
            ]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,

            plugins: {

                legend: {

                    position: "top",

                    labels: {
                        color: "#475569",
                        usePointStyle: true,
                        pointStyle: "circle"
                    }
                },

                tooltip: {

                    mode: "index",
                    intersect: false,

                    backgroundColor:
                            "#111827",

                    titleColor:
                            "#ffffff",

                    bodyColor:
                            "#e5e7eb"
                }
            },

            scales: {

                x: {

                    grid: {
                        display: false
                    },

                    ticks: {
                        color: "#64748b"
                    }
                },

                y: {

                    grid: {
                        color:
                                "rgba(148, 163, 184, 0.18)",

                        drawBorder: false
                    },

                    ticks: {
                        color: "#64748b"
                    }
                }
            }
        }
    });
}


function initializeCustomerChart() {

    const canvas =
            document.getElementById("customerChart");

    if (!canvas || typeof Chart === "undefined") {
        return;
    }

    const items =
            document.querySelectorAll(
                ".customer-data-item"
            );

    const labels = [];
    const data = [];

    items.forEach(function (item) {

        labels.push(
            item.dataset.country
        );

        data.push(
            Number(item.dataset.total)
        );
    });


    new Chart(canvas, {

        type: "pie",

        data: {

            labels: labels,

            datasets: [
                {
                    data: data,

                    backgroundColor: [
                        "#c8a96b",
                        "#f59e0b",
                        "#10b981",
                        "#3b82f6",
                        "#8b5cf6",
                        "#ef4444",
                        "#f97316",
                        "#0ea5e9"
                    ],

                    borderColor: "#ffffff",

                    borderWidth: 3,

                    hoverOffset: 14
                }
            ]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,

            plugins: {

                legend: {

                    position: "bottom",

                    labels: {
                        color: "#475569",
                        usePointStyle: true,
                        pointStyle: "circle"
                    }
                },

                tooltip: {

                    backgroundColor:
                            "#111827",

                    titleColor:
                            "#ffffff",

                    bodyColor:
                            "#e5e7eb"
                }
            }
        }
    });
}


function initializeUtilizationChart() {

    const canvas =
            document.getElementById(
                "utilizationChart"
            );

    if (!canvas || typeof Chart === "undefined") {
        return;
    }

    const items =
            document.querySelectorAll(
                ".utilization-data-item"
            );

    const labels = [];
    const rates = [];
    const used = [];
    const available = [];

    items.forEach(function (item) {

        labels.push(item.dataset.type);
        rates.push(Number(item.dataset.rate));
        used.push(Number(item.dataset.used));
        available.push(Number(item.dataset.available));
    });


    new Chart(canvas, {

        type: "bar",

        data: {

            labels: labels,

            datasets: [
                {
                    label: "Occupancy (%)",
                    data: rates,

                    backgroundColor:
                            "rgba(200, 169, 107, 0.75)",

                    borderColor: "#c8a96b",

                    borderWidth: 1,

                    borderRadius: 8
                }
            ]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,

            plugins: {

                legend: {
                    display: false
                },

                tooltip: {

                    backgroundColor: "#111827",
                    titleColor: "#ffffff",
                    bodyColor: "#e5e7eb",

                    callbacks: {

                        label: function (context) {

                            const i = context.dataIndex;

                            return context.parsed.y + "% ("
                                    + used[i] + " / "
                                    + available[i] + " room-days)";
                        }
                    }
                }
            },

            scales: {

                x: {

                    grid: {
                        display: false
                    },

                    ticks: {
                        color: "#64748b"
                    }
                },

                y: {

                    beginAtZero: true,

                    suggestedMax: 100,

                    grid: {
                        color:
                                "rgba(148, 163, 184, 0.18)",

                        drawBorder: false
                    },

                    ticks: {

                        color: "#64748b",

                        callback: function (value) {
                            return value + "%";
                        }
                    }
                }
            }
        }
    });
}


function initializeServiceChart() {

    const canvas = document.getElementById("serviceChart");

    if (!canvas || typeof Chart === "undefined") {
        return;
    }

    const labels = [];
    const used = [];
    const cancelled = [];

    document.querySelectorAll(".service-data-item").forEach(function (item) {

        labels.push(item.dataset.name);
        used.push(Number(item.dataset.used));
        cancelled.push(Number(item.dataset.cancelled));
    });

    new Chart(canvas, {

        type: "bar",

        data: {

            labels: labels,

            datasets: [
                {
                    label: "Used",
                    data: used,
                    backgroundColor: "rgba(16, 185, 129, 0.75)",
                    borderColor: "#10b981",
                    borderWidth: 1,
                    borderRadius: 6
                },
                {
                    label: "Cancelled",
                    data: cancelled,
                    backgroundColor: "rgba(239, 68, 68, 0.75)",
                    borderColor: "#ef4444",
                    borderWidth: 1,
                    borderRadius: 6
                }
            ]
        },

        options: {

            responsive: true,
            maintainAspectRatio: false,

            plugins: {

                legend: {

                    position: "top",

                    labels: {
                        color: "#475569",
                        usePointStyle: true,
                        pointStyle: "circle"
                    }
                },

                tooltip: {

                    mode: "index",
                    intersect: false,

                    backgroundColor: "#111827",
                    titleColor: "#ffffff",
                    bodyColor: "#e5e7eb"
                }
            },

            scales: {

                x: {

                    grid: {
                        display: false
                    },

                    ticks: {
                        color: "#64748b"
                    }
                },

                y: {

                    beginAtZero: true,

                    grid: {
                        color: "rgba(148, 163, 184, 0.18)",
                        drawBorder: false
                    },

                    ticks: {
                        color: "#64748b",
                        precision: 0
                    }
                }
            }
        }
    });
}
