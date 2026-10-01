<%@ page contentType="text/html" pageEncoding="UTF-8" %>

<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>


<layout:layout
    title="Verify Code"
    pageCss="forgotPassword.css"
    pageJs="verifyOtp.js"
    bodyClass="forgot-password-page"
    showNavbar="false"
    showFooter="false"
>

    <div class="password-reset-container">

        <div class="password-reset-card">

            <div class="reset-icon">
                <i class="fa-solid fa-envelope"></i>
            </div>


            <h1>
                Check Your Email
            </h1>


            <p class="reset-description">
                We sent a 6-digit verification code
                to your registered email address.
            </p>


            <!-- ========================= -->
            <!-- VERIFY OTP -->
            <!-- ========================= -->

            <form
                action="${pageContext.request.contextPath}/verify-otp"
                method="post"
                class="password-reset-form"
            >

                <input
                    type="hidden"
                    name="action"
                    value="verify"
                >


                <label for="otp">
                    Verification Code
                </label>


                <input
                    type="text"
                    id="otp"
                    name="otp"
                    class="otp-input"
                    maxlength="6"
                    minlength="6"
                    inputmode="numeric"
                    pattern="[0-9]{6}"
                    autocomplete="one-time-code"
                    placeholder="000000"
                    required
                >


                <button
                    type="submit"
                    class="reset-primary-btn"
                >
                    Verify Code
                </button>

            </form>


            <p class="otp-note">
                The verification code expires in 5 minutes.
            </p>


            <!-- ========================= -->
            <!-- RESEND OTP -->
            <!-- ========================= -->

            <div class="resend-section">

                <span class="resend-text">
                    Didn't receive the code?
                </span>


                <form
                    action="${pageContext.request.contextPath}/verify-otp"
                    method="post"
                    class="resend-form"
                >

                    <input
                        type="hidden"
                        name="action"
                        value="resend"
                    >


                    <button
                        type="submit"
                        id="resendOtpBtn"
                        class="resend-btn"
                        data-remaining="${resendRemainingSeconds}"
                    >
                        Resend Code
                    </button>

                </form>

            </div>


            <a
                href="${pageContext.request.contextPath}/forgot-password"
                class="back-login-link"
            >
                Use a different email or phone number
            </a>

        </div>

    </div>

</layout:layout>