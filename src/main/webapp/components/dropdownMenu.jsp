<%-- 
    Document   : navbar
    Created on : Jul 9, 2026, 7:08:56 PM
    Author     : default
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<div class="user-menu">

    <c:choose>

        <c:when test="${not empty sessionScope.user}">

            <div class="dropdown">

                <button type="button"
                        class="dropdown-btn"
                        onclick="toggleMenu()">

                    <i class="fa-solid fa-circle-user"></i>
                    ${sessionScope.user.username}
                    <i class="fa-solid fa-caret-down"></i>

                </button>

                <div class="dropdown-content" id="userDropdown">

                    <c:if test="${sessionScope.user.role eq 'Customer'}">
                        <a href ="index.jsp">Home</a>
                        <a href="profile?action=view">Profile</a>
                        <a href="booking">My Bookings</a>   <!-- wait for implement -->
                        <a href="complaint?action=add">Add complaint</a>
                    </c:if>

                    <c:if test="${sessionScope.user.role eq 'Staff'}">
                        <a href ="index.jsp">Home</a>
                        <a href="customer?action=add">Add customer</a>
                        <a href="booking">Manage Bookings</a>  <!-- wait for implement -->
                        <a href="room">Manage Rooms</a>        <!-- wait for implement -->
                        <a href="invoice">Invoice</a>
                    </c:if>

                    <c:if test="${sessionScope.user.role eq 'Admin'}">
                        <a href ="index.jsp">Home</a>
                        <a href="dashboard">Dashboard</a>               <!-- wait for implement -->
                        <a href="employee?action=add">Add employee</a>
                        <a href ="customer?action=list">View customers</a>
                        <a href ="complaint?action=list">View complaints</a>
                    </c:if>

                    <div class="dropdown-divider"></div>

                    <a href="logout" class="logout-link">
                        <i class="fa-solid fa-right-from-bracket"></i>
                        Logout
                    </a>

                </div>

            </div>

        </c:when>

        <c:otherwise>

            <a href="login.jsp" class="auth-btn">
                <i class="fa-solid fa-right-to-bracket"></i>
                Login
            </a>

        </c:otherwise>

    </c:choose>

</div>
<script>
    function toggleMenu() {
        document.getElementById("userDropdown")
                .classList.toggle("show");
    }

    document.addEventListener("click", function (event) {
        const dropdown = document.querySelector(".dropdown");
        const menu = document.getElementById("userDropdown");

        if (!dropdown.contains(event.target)) {
            menu.classList.remove("show");
        }
    });
</script>