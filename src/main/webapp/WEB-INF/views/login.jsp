<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Hotel Management - Login"
    pageCss="login.css"
    pageJs="togglePassword.js"
    bodyClass="login-page"
    showNavbar="false"
    showFooter="false"
>

    <div class="login-container">

        <div class="login-logo">
            <i class="fa-solid fa-hotel"></i>
        </div>

        <h2 class="login-title">
            Hotel Management System
        </h2>

        <p class="login-subtitle">
            Welcome Back
        </p>


        <form
            action="${pageContext.request.contextPath}/login"
            method="post"
            class="login-form"
        >

            <div class="form-group">

                <label for="username">
                    <i class="fa-solid fa-user"></i>
                    Username
                </label>

                <input
                    type="text"
                    id="username"
                    name="username"
                    autocomplete="username"
                    required
                >

            </div>


            <div class="form-group">

                <label for="password">
                    <i class="fa-solid fa-lock"></i>
                    Password
                </label>

                <div class="password-container">

                    <input
                        type="password"
                        id="password"
                        name="password"
                        autocomplete="current-password"
                        required
                    >

                    <i
                        class="fa-solid fa-eye toggle-password"
                        id="togglePassword"
                    ></i>

                </div>

            </div>


            <!-- Forgot Password -->
            <div class="forgot-password-wrapper">

                <a
                    href="${pageContext.request.contextPath}/forgot-password"
                    class="forgot-password-link"
                >
                    Forgot Password?
                </a>

            </div>


            <button
                type="submit"
                class="login-btn"
            >
                Sign In
            </button>


            <div class="register-link">

                <span>
                    Don't have an account?
                </span>

                <a href="${pageContext.request.contextPath}/register">
                    Register
                </a>

            </div>

        </form>

    </div>

</layout:layout>