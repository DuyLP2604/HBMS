document.addEventListener("DOMContentLoaded", function () {

    initializeDateRangePicker();
    initializeRevenueChart();
    initializeCustomerChart();

});


function initializeDateRangePicker() {

    if (typeof $ === "undefined"
            || typeof moment === "undefined"
            || !$.fn.daterangepicker) {
        return;
    }

    const dateRange = $("#dateRange");
    const fromInput = $("#from");
    const toInput = $("#to");

    dateRange.daterangepicker(
        {
            startDate: moment(),
            endDate: moment(),

            locale: {
                format: "DD/MM/YYYY"
            }
        },
        function (start, end) {

            fromInput.val(
                start.format("YYYY-MM-DD")
            );

            toInput.val(
                end.format("YYYY-MM-DD")
            );
        }
    );

    fromInput.val(
        moment().format("YYYY-MM-DD")
    );

    toInput.val(
        moment().format("YYYY-MM-DD")
    );
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
