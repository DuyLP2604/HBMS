<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Set New Password" pageCss="forgotPassword.css" bodyClass="forgot-password-page" showNavbar="false" showFooter="false">
    <div class="password-reset-container">
        <div class="password-reset-card">
            <div class="reset-icon"><i class="fa-solid fa-lock"></i></div>
            <h1>Set New Password</h1>
            <p class="reset-description">Welcome, <c:out value="${setupUsername}" />. Set your own password to finish signing in. It must differ from your temporary password.</p>
            <form action="${pageContext.request.contextPath}/set-new-password" method="post" class="password-reset-form">
                <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                <label for="password">New Password</label>
                <input type="password" id="password" name="password" minlength="6" maxlength="50" autocomplete="new-password" aria-describedby="passwordHelp" required>
                <label for="confirmPassword">Confirm New Password</label>
                <input type="password" id="confirmPassword" name="confirmPassword" minlength="6" maxlength="50" autocomplete="new-password" required>
                <p class="reset-description" id="passwordHelp">Use 6–50 characters without spaces.</p>
                <button type="submit" class="reset-primary-btn">Set Password and Sign In</button>
            </form>
            <form action="${pageContext.request.contextPath}/logout" method="post" class="password-reset-form">
                <button type="submit" class="reset-primary-btn">Sign Out</button>
            </form>
        </div>
    </div>
</layout:layout>
