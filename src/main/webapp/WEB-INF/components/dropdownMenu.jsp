<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>

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
                        </c:when>
                    </c:choose>

                    <c:if test="${sessionScope.user.role eq 'Staff' or sessionScope.user.role eq 'Admin'}">
                        <a href="${pageContext.request.contextPath}/booking-overview">Booking Overview</a>
                    </c:if>

                    <div class="dropdown-divider"></div>
                    <a href="${pageContext.request.contextPath}/logout" class="logout-link">
                        <i class="fa-solid fa-right-from-bracket"></i> Logout
                    </a>
                </div>
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
</script>
