<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:url var="changePasswordUrl" value="/change-password" />
<c:url var="forgotPasswordUrl" value="/forgot-password">
    <c:param name="accountType" value="${staffPassword ? 'Staff' : 'Customer'}" />
</c:url>
<c:url var="profileUrl" value="${staffPassword ? '/staff/profile' : '/profile'}" />
<layout:layout title="Change Password" pageCss="change-password.css" bodyClass="change-password-page">
    <section class="password-settings" aria-labelledby="changePasswordTitle">
        <a class="password-back" href="${profileUrl}"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to profile</a>
        <div class="password-settings-card">
            <div class="password-settings-heading">
                <span class="password-settings-icon"><i class="fa-solid fa-lock" aria-hidden="true"></i></span>
                <p class="password-eyebrow">ACCOUNT SECURITY</p>
                <h1 id="changePasswordTitle">Change password</h1>
                <p>Enter your current password, then choose a new one.</p>
            </div>
            <c:if test="${not empty passwordError}">
                <div class="password-error" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i> <c:out value="${passwordError}" /></div>
            </c:if>
            <form action="${changePasswordUrl}" method="post" class="password-settings-form">
                <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                <div class="password-field">
                    <label for="currentPassword">Current password</label>
                    <input type="password" id="currentPassword" name="currentPassword" autocomplete="current-password" required>
                </div>
                <div class="password-field">
                    <label for="newPassword">New password</label>
                    <c:choose>
                        <c:when test="${staffPassword}">
                            <input type="password" id="newPassword" name="newPassword" minlength="6" maxlength="50" pattern="\S{6,50}" autocomplete="new-password" aria-describedby="passwordRules" required>
                        </c:when>
                        <c:otherwise>
                            <input type="password" id="newPassword" name="newPassword" minlength="6" autocomplete="new-password" aria-describedby="passwordRules" required>
                        </c:otherwise>
                    </c:choose>
                    <p id="passwordRules" class="password-help">${staffPassword ? 'Use 6–50 characters without spaces.' : 'Use at least 6 characters.'}</p>
                </div>
                <div class="password-field">
                    <label for="confirmPassword">Confirm new password</label>
                    <input type="password" id="confirmPassword" name="confirmPassword" minlength="6" autocomplete="new-password" required>
                </div>
                <p class="password-session-note"><i class="fa-solid fa-shield-halved" aria-hidden="true"></i> After saving, sign in again with your new password.</p>
                <button type="submit" class="password-save">Save new password <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></button>
            </form>
            <div class="password-recovery">
                <div><strong>Forgot your current password?</strong><p>Recover your account with an email verification code.</p></div>
                <a href="${forgotPasswordUrl}" class="password-recovery-button">Forgot password <i class="fa-solid fa-arrow-up-right-from-square" aria-hidden="true"></i></a>
            </div>
        </div>
    </section>
</layout:layout>
