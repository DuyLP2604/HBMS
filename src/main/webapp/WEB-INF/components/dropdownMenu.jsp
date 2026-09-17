<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<div class="user-menu">

    <c:choose>

        <c:when test="${not empty sessionScope.user}">

            <div class="dropdown">

                <button type="button"
                        class="dropdown-btn"
                        onclick="toggleMenu()">

                    <i class="fa-solid fa-circle-user"></i>

                    <c:out value="${sessionScope.user.username}" />

                    <i class="fa-solid fa-caret-down"></i>

                </button>

                <div class="dropdown-content" id="userDropdown">

                    <c:if test="${sessionScope.user.role eq 'Customer'}">

                        <a href="${pageContext.request.contextPath}/home">
                            Home
                        </a>

                        <a href="${pageContext.request.contextPath}/profile?action=view">
                            Profile
                        </a>

                        <a href="${pageContext.request.contextPath}/my-bookings">                   
                            My Bookings
                        </a>

                        <a href="${pageContext.request.contextPath}/complaint?action=add">
                            Add complaint
                        </a>

                    </c:if>


                    <c:if test="${sessionScope.user.role eq 'Staff'}">

                        <a href="${pageContext.request.contextPath}/home">
                            Home
                        </a>

                        <a href="${pageContext.request.contextPath}/customer?action=add">
                            Add customer
                        </a>

                        <a href="${pageContext.request.contextPath}/staff/bookings" >
                            Room Assignment
                        </a>

                        <a href="${pageContext.request.contextPath}/staff/stays" >
                            Guest Stays
                        </a>

                        <a href="${pageContext.request.contextPath}/booking?action=list">
                            Manage Bookings
                        </a>

                        <a href="${pageContext.request.contextPath}/room">
                            Manage Rooms
                        </a>

                        <a href="${pageContext.request.contextPath}/invoice">
                            Invoice
                        </a>

                    </c:if>


                    <c:if test="${sessionScope.user.role eq 'Admin'}">

                        <a href="${pageContext.request.contextPath}/home">
                            Home
                        </a>

                        <a href="${pageContext.request.contextPath}/dashboard">
                            Dashboard
                        </a>

                        <a href="${pageContext.request.contextPath}/employee?action=add">
                            Add employee
                        </a>

                        <a href="${pageContext.request.contextPath}/customer?action=list">
                            View customers
                        </a>

                        <a href="${pageContext.request.contextPath}/complaint?action=list">
                            View complaints
                        </a>

                    </c:if>

                    <div class="dropdown-divider"></div>

                    <a href="${pageContext.request.contextPath}/logout"
                       class="logout-link">

                        <i class="fa-solid fa-right-from-bracket"></i>
                        Logout

                    </a>

                </div>

            </div>

        </c:when>


        <c:otherwise>

            <a href="${pageContext.request.contextPath}/login"
               class="auth-btn">

                <i class="fa-solid fa-right-to-bracket"></i>
                Login

            </a>

        </c:otherwise>

    </c:choose>

</div>

<script>

    function toggleMenu() {

        const menu = document.getElementById("userDropdown");

        if (menu) {
            menu.classList.toggle("show");
        }
    }


    document.addEventListener("click", function (event) {

        const dropdown = document.querySelector(".dropdown");
        const menu = document.getElementById("userDropdown");

        if (!dropdown || !menu) {
            return;
        }

        if (!dropdown.contains(event.target)) {
            menu.classList.remove("show");
        }

    });

</script>