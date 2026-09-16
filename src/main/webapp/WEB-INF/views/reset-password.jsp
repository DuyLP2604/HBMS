<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Reset Password"
    pageCss="forgotPassword.css"
    bodyClass="forgot-password-page"
    showNavbar="false"
    showFooter="false"
>

    <div class="password-reset-container">

        <div class="password-reset-card">

            <div class="reset-icon">
                <i class="fa-solid fa-lock"></i>
            </div>


            <h1>
                Create New Password
            </h1>


            <p class="reset-description">
                Enter a new password for your account.
            </p>


            <form
                action="${pageContext.request.contextPath}/reset-password"
                method="post"
                class="password-reset-form"
            >

                <label for="password">
                    New Password
                </label>

                <input
                    type="password"
                    id="password"
                    name="password"
                    minlength="6"
                    autocomplete="new-password"
                    required
                >


                <label for="confirmPassword">
                    Confirm New Password
                </label>

                <input
                    type="password"
                    id="confirmPassword"
                    name="confirmPassword"
                    minlength="6"
                    autocomplete="new-password"
                    required
                >


                <button
                    type="submit"
                    class="reset-primary-btn"
                >
                    Reset Password
                </button>

            </form>

        </div>

    </div>

</layout:layout>