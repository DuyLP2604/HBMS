<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%-- Shows account and workspace links based on the user's role. --%>
<div class="user-menu">
    <c:choose>
        <c:when test="${not empty sessionScope.user}">
            <div class="dropdown">
                <button type="button" id="userDropdownButton" class="dropdown-btn" onclick="toggleMenu()" aria-controls="userDropdown" aria-expanded="false">
                    <i class="fa-solid fa-circle-user"></i>
                    <c:out value="${sessionScope.user.username}" />
                    <i class="fa-solid fa-caret-down"></i>
                </button>

                <div class="dropdown-content" id="userDropdown">
                    <c:choose>
                        <c:when test="${sessionScope.user.role eq 'Customer'}">
                            <a href="${pageContext.request.contextPath}/home">Home</a>
                            <a href="${pageContext.request.contextPath}/profile?action=view">Profile</a>
                            <a href="${pageContext.request.contextPath}/my-bookings">My Bookings</a>
                            <a href="${pageContext.request.contextPath}/complaint?action=add">Add complaint</a>
                        </c:when>

                        <c:when test="${sessionScope.user.role eq 'Staff'}">
                            <a href="${pageContext.request.contextPath}/staff/dashboard">Dashboard</a>
                            <a href="${pageContext.request.contextPath}/staff/profile">Staff Profile</a>
                            <a href="${pageContext.request.contextPath}/room">View rooms</a>
                        </c:when>

                        <c:when test="${sessionScope.user.role eq 'Admin'}">
                            <a href="${pageContext.request.contextPath}/dashboard">Dashboard</a>
                            <a href="${pageContext.request.contextPath}/employee?action=list">View employee</a>
                            <a href="${pageContext.request.contextPath}/customer?action=list">View customers</a>
                            <a href="${pageContext.request.contextPath}/room?action=list">View rooms</a>
                            <a href="${pageContext.request.contextPath}/complaint?action=list">View complaints</a>
                            <a href="${pageContext.request.contextPath}/hotel">Hotel information</a>
                            <a href="${pageContext.request.contextPath}/room-types?action=list">Manage room type</a>
                            <a href="${pageContext.request.contextPath}/service?action=manage">Manage services</a>
                            <a href="#" onclick="openExportModal(event)">Export report</a>
                        </c:when>
                    </c:choose>

                    <div class="dropdown-divider"></div>
                    <a href="${pageContext.request.contextPath}/logout" class="logout-link">
                        <i class="fa-solid fa-right-from-bracket"></i> Logout
                    </a>
                </div>
                <%-- Export Report modal (Admin only) --%>
                <c:if test="${sessionScope.user.role eq 'Admin'}">
                    <div class="modal fade" id="exportModal" tabindex="-1" aria-labelledby="exportModalTitle" aria-hidden="true">
                        <div class="modal-dialog modal-dialog-centered">
                            <div class="modal-content">
                                <div class="modal-header border-0 pb-0">
                                    <h5 class="modal-title w-100 text-center" id="exportModalTitle">
                                        <i class="fa-solid fa-file-export me-1"></i> Export Report
                                    </h5>
                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                </div>

                                <div class="modal-body">
                                    <form id="exportForm" action="${pageContext.request.contextPath}/data" method="post" onsubmit="return validateExportForm()">
                                        <input type="hidden" name="action" value="report">

                                        <div class="row g-3">
                                            <div class="col-6">
                                                <label for="exportBeginDate" class="form-label fw-medium">Begin date</label>
                                                <input type="date" class="form-control form-control-sm" id="exportBeginDate" name="from" required>
                                            </div>
                                            <div class="col-6">
                                                <label for="exportEndDate" class="form-label fw-medium">End date</label>
                                                <input type="date" class="form-control form-control-sm" id="exportEndDate" name="to" required>
                                            </div>
                                        </div>

                                        <div class="form-text mt-2">The date range must not exceed 60 days.</div>
                                        <div class="text-danger small mt-2 d-none" id="exportError"></div>

                                        <div class="d-flex justify-content-center mt-4">
                                            <button type="submit" class="btn btn-primary btn-sm px-4">
                                                <i class="fas fa-download"></i> Export
                                            </button>
                                        </div>
                                    </form>

                                    <div class="text-end mt-3">
                                        <a class="small text-reset" href="${pageContext.request.contextPath}/data">All data</a>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>
                </c:if>
            </div>
        </c:when>

        <c:otherwise>
            <a href="${pageContext.request.contextPath}/login" class="auth-btn">
                <i class="fa-solid fa-right-to-bracket"></i> Login
            </a>
        </c:otherwise>
    </c:choose>
</div>

<script>
    function toggleMenu() {
        const menu = document.getElementById("userDropdown");
        const button = document.getElementById("userDropdownButton");
        if (menu) {
            const opened = menu.classList.toggle("show");
            if (button) {
                button.setAttribute("aria-expanded", String(opened));
            }
        }
    }

    function closeUserMenu() {
        const menu = document.getElementById("userDropdown");
        const button = document.getElementById("userDropdownButton");
        if (menu) {
            menu.classList.remove("show");
        }
        if (button) {
            button.setAttribute("aria-expanded", "false");
        }
    }

    document.addEventListener("click", function (event) {
        const menu = document.getElementById("userDropdown");
        const userMenu = menu ? menu.closest(".user-menu") : null;
        if (userMenu && !userMenu.contains(event.target)) {
            closeUserMenu();
        }
    });

    document.addEventListener("keydown", function (event) {
        if (event.key === "Escape") {
            closeUserMenu();
        }
    });
// ----- Export Report modal (Bootstrap) -----
    function openExportModal(event) {
        if (event) {
            event.preventDefault();
        }
        closeUserMenu();
        const modal = document.getElementById("exportModal");
        if (!modal) {
            return;
        }
        // Keep the modal as a direct child of <body> so parent styles cannot offset it
        if (modal.parentElement !== document.body) {
            document.body.appendChild(modal);
        }
        document.getElementById("exportError").classList.add("d-none");
        bootstrap.Modal.getOrCreateInstance(modal).show();
    }

// Limit the end date to at most 60 days after the begin date
    function limitExportRange() {
        const from = document.getElementById("exportBeginDate");
        const to = document.getElementById("exportEndDate");
        if (!from || !to) {
            return;
        }
        if (!from.value) {
            to.removeAttribute("min");
            to.removeAttribute("max");
            return;
        }
        const max = new Date(from.value);
        max.setUTCDate(max.getUTCDate() + 59); // 60 days including the begin date
        to.min = from.value;
        to.max = max.toISOString().slice(0, 10);
    }

    function validateExportForm() {
        const from = document.getElementById("exportBeginDate").value;
        const to = document.getElementById("exportEndDate").value;
        const error = document.getElementById("exportError");
        let message = "";

        if (!from || !to) {
            message = "Please choose both begin date and end date.";
        } else if (to < from) {
            message = "End date must not be before begin date.";
        } else if ((new Date(to) - new Date(from)) / 86400000 + 1 > 60) {
            message = "The date range must not exceed 60 days.";
        }

        if (message) {
            error.textContent = message;
            error.classList.remove("d-none");
            return false;
        }
        error.classList.add("d-none");
        // The response is a file download, so the page stays open; close the modal afterwards
        setTimeout(function () {
            bootstrap.Modal.getOrCreateInstance(document.getElementById("exportModal")).hide();
        }, 300);
        return true;
    }

    (function () {
        const from = document.getElementById("exportBeginDate");
        if (from) {
            from.addEventListener("change", limitExportRange);
            limitExportRange();
        }
        const modal = document.getElementById("exportModal");
        if (modal) {
            modal.addEventListener("shown.bs.modal", function () {
                document.getElementById("exportBeginDate").focus();
            });
        }
    })();
</script>
