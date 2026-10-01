<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Forgot Password"
    pageCss="forgotPassword.css"
    bodyClass="forgot-password-page"
    showNavbar="false"
    showFooter="false"
>

    <div class="password-reset-container">

        <div class="password-reset-card">

            <div class="reset-icon">
                <i class="fa-solid fa-key"></i>
            </div>

            <h1>
                Forgot Password?
            </h1>

            <p class="reset-description">
                Enter your email address or phone number
                to find your account.
            </p>


            <form
                action="${pageContext.request.contextPath}/forgot-password"
                method="post"
                class="password-reset-form"
            >

                <label for="identifier">
                    Email or Phone Number
                </label>

                <input
                    type="text"
                    id="identifier"
                    name="identifier"
                    placeholder="Enter email or phone number"
                    autocomplete="username"
                    required
                >


                <button
                    type="submit"
                    class="reset-primary-btn"
                >
                    Send Verification Code
                </button>

            </form>


            <a
                href="${pageContext.request.contextPath}/login"
                class="back-login-link"
            >
                <i class="fa-solid fa-arrow-left"></i>
                Back to Login
            </a>

        </div>

    </div>

</layout:layout>