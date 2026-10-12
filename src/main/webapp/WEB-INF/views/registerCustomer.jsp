<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Register" pageCss="registerCustomer.css" pageJs="register.js" showNavbar="false" bodyClass="register-page">
    <form action="${pageContext.request.contextPath}/register" method="post" class="register-form">
        <h2>Create Account</h2>
        <div id="step1">
            <div class="step-indicator">
                <span class="active">1</span>
                <span>2</span>
            </div>
            <h3>Account Information</h3>
            <label for="username">Username</label>
            <input type="text" id="username" name="username" value="${fn:escapeXml(username)}" maxlength="50" autocomplete="username" required>
            <label for="password">Password</label>
            <input type="password" id="password" name="password" minlength="6" autocomplete="new-password" required>
            <label for="confirmPassword">Confirm Password</label>
            <input type="password" id="confirmPassword" name="confirmPassword" minlength="6" autocomplete="new-password" required>
            <button type="button" class="next-btn" onclick="nextStep()">Next</button>
        </div>
        <div id="step2" class="register-step-hidden">
            <div class="step-indicator">
                <span class="done">✓</span>
                <span class="active">2</span>
            </div>
            <h3>Personal Information</h3>
            <label for="nationalityID">Nationality</label>
            <select id="nationalityID" name="nationalityID" onchange="handleNationalityChange()" required>
                <option value="">-- Select Nationality --</option>
                <c:forEach var="n" items="${nationalities}">
                    <option value="${fn:escapeXml(n.nationalityID)}" data-nationality-name="${fn:escapeXml(n.nationalityName)}" ${nationalityID eq n.nationalityID ? 'selected' : ''}><c:out value="${n.nationalityName}" /></option>
                </c:forEach>
            </select>
            <div class="form-grid">
                <label for="fullname">Full Name</label>
                <input type="text" id="fullname" name="fullname" value="${fn:escapeXml(fullname)}" maxlength="100" autocomplete="name" required>
                <label for="email">Email</label>
                <input type="email" id="email" name="email" value="${fn:escapeXml(email)}" maxlength="100" autocomplete="email" required>
                <div id="phoneGroup">
                    <label for="phone">Phone Number <span id="phoneRequiredMark" class="${nationalityID eq 'N01' ? '' : 'register-step-hidden'}">*</span></label>
                    <input type="tel" id="phone" name="phone" value="${fn:escapeXml(phone)}" maxlength="15" autocomplete="tel" ${nationalityID eq 'N01' ? 'required' : ''}>
                </div>
            </div>
            <label for="address">Address</label>
            <input type="text" id="address" name="address" value="${fn:escapeXml(address)}" maxlength="200" autocomplete="street-address" required>
            <div class="btn-group">
                <button type="button" class="back-btn" onclick="backStep()">Back</button>
                <button type="submit" class="register-btn">Register</button>
            </div>
        </div>
        <a href="${pageContext.request.contextPath}/login" class="back-login-btn">
            <i class="fa-solid fa-arrow-left" aria-hidden="true"></i>
            Back to Login
        </a>
    </form>
</layout:layout>