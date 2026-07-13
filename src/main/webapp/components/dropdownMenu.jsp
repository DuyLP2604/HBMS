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
                        <a href="profile?action=view">Profile</a>
                        <a href="my-bookings">My Bookings</a>   <!-- wait for implement -->
                        <a href="add-complaint">Add complaint</a>
                    </c:if>

                    <c:if test="${sessionScope.user.role eq 'Staff'}">
                        <a href="customer?action=add">Add customer</a>
                        <a href="manage-bookings">Manage Bookings</a>  <!-- wait for implement -->
                        <a href="manage-rooms">Manage Rooms</a>        <!-- wait for implement -->
                    </c:if>

                    <c:if test="${sessionScope.user.role eq 'Admin'}">
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