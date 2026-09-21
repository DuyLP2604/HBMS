<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Register"
    pageCss="registerCustomer.css"
    pageJs="register.js"
    showNavbar="false"
    bodyClass="register-page"
    >

    <form
        action="${pageContext.request.contextPath}/register"
        method="post"
        class="register-form"
        >

        <h2>
            Create Account
        </h2>


        <!-- Step 1 -->
        <div id="step1">

            <div class="step-indicator">

                <span class="active">
                    1
                </span>

                <span>
                    2
                </span>

            </div>


            <h3>
                Account Information
            </h3>


            <label for="username">
                Username
            </label>

            <input
                type="text"
                id="username"
                name="username"
                required
                >


            <label for="password">
                Password
            </label>

            <input
                type="password"
                id="password"
                name="password"
                required
                >


            <label for="confirmPassword">
                Confirm Password
            </label>

            <input
                type="password"
                id="confirmPassword"
                required
                >


            <button
                type="button"
                class="next-btn"
                onclick="nextStep()"
                >
                Next
            </button>

        </div>

        <!-- Step 2 -->
        <div id="step2" class="register-step-hidden">

            <div class="step-indicator">
                <span class="done">✓</span>
                <span class="active">2</span>
            </div>

            <h3>Personal Information</h3>

            <!-- Select nationality first -->
            <label for="nationalityID">Nationality</label>

            <select
                id="nationalityID"
                name="nationalityID"
                onchange="handleNationalityChange()"
                required
                >
                <option value="">-- Select Nationality --</option>

                <c:forEach var="n" items="${nationalities}">
                    <option
                        value="${n.nationalityID}"
                        data-nationality-name="${n.nationalityName}"
                        ${nationalityID == n.nationalityID ? 'selected' : ''}
                        >
                        <c:out value="${n.nationalityName}" />
                    </option>
                </c:forEach>
            </select>

            <div class="form-grid">

                <label for="fullname">Full Name</label>
                <input
                    type="text"
                    id="fullname"
                    name="fullname"
                    required
                    >

                <label for="email">Email</label>
                <input
                    type="email"
                    id="email"
                    name="email"
                    required
                    >

                <div id="phoneGroup">
                    <label for="phone">
                        Phone Number
                        <span id="phoneRequiredMark">*</span>
                    </label>

                    <input
                        type="text"
                        id="phone"
                        name="phone"
                        >
                </div>

            </div>

            <label for="address">Address</label>
            <input
                type="text"
                id="address"
                name="address"
                required
                >

            <!-- Shown only for Vietnamese users -->
            <div id="cccdGroup" class="register-step-hidden">
                <label for="cccd">Identity Number (CCCD)</label>

                <input
                    type="text"
                    id="cccd"
                    name="cccd"
                    >
            </div>

            <!-- Shown only for foreign users -->
            <div id="passportGroup" class="register-step-hidden">
                <label for="passportNumber">Passport Number</label>

                <input
                    type="text"
                    id="passportNumber"
                    name="passportNumber"
                    >
            </div>

            <div class="btn-group">
                <button
                    type="button"
                    class="back-btn"
                    onclick="backStep()"
                    >
                    Back
                </button>

                <button
                    type="submit"
                    class="register-btn"
                    >
                    Register
                </button>
            </div>

        </div>

    </form>
</layout:layout>
