<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="profileName" value="${empty fn:trim(employee.fullName) ? sessionScope.user.username : employee.fullName}" />
<c:url var="dashboardUrl" value="/staff/dashboard" />
<c:url var="changePasswordUrl" value="/change-password" />
<layout:layout title="Staff Profile" pageCss="account-profile.css" bodyClass="account-profile-page">
    <fmt:setLocale value="vi_VN" scope="page" />
    <div class="account-profile">
        <header class="account-page-heading">
            <div>
                <p class="account-eyebrow">STAFF ACCOUNT</p>
                <h1>My profile</h1>
            </div>
            <a href="${dashboardUrl}" class="account-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to dashboard</a>
        </header>

        <div class="account-profile-grid">
            <aside class="account-sidebar" aria-label="Account overview">
                <section class="account-identity" aria-labelledby="accountName">
                    <div class="account-identity-top">
                        <div class="account-identity-mark">
                            <div class="account-avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(profileName, 0, 1))}" /></div>
                            <span class="account-role"><c:out value="${empty fn:trim(employee.position) ? 'Employee' : employee.position}" /></span>
                        </div>
                        <h2 id="accountName"><c:out value="${profileName}" /></h2>
                    </div>
                    <dl class="account-identity-meta">
                        <div><dt>Username</dt><dd><c:out value="${sessionScope.user.username}" /></dd></div>
                        <div><dt>Employee ID</dt><dd><c:out value="${employee.employeeID}" /></dd></div>
                    </dl>
                    <div class="account-identity-actions">
                        <a href="${changePasswordUrl}" class="account-button account-password-button"><i class="fa-solid fa-lock" aria-hidden="true"></i> Change password <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a>
                    </div>
                </section>
            </aside>

            <div class="account-content">
                <section class="account-panel" aria-labelledby="personalInfoTitle">
                    <header class="account-panel-heading">
                        <div class="account-section-title">
                            <span class="account-section-icon" aria-hidden="true"><i class="fa-regular fa-address-card"></i></span>
                            <h2 id="personalInfoTitle">Personal information</h2>
                        </div>
                    </header>
                    <dl class="account-detail-grid">
                        <div class="account-detail"><dt>Full name</dt><dd><c:out value="${empty fn:trim(employee.fullName) ? 'Not provided' : employee.fullName}" /></dd></div>
                        <div class="account-detail"><dt>Phone number</dt><dd><c:out value="${empty fn:trim(employee.phone) ? 'Not provided' : employee.phone}" /></dd></div>
                        <div class="account-detail"><dt>Email address</dt><dd><c:out value="${empty fn:trim(employee.email) ? 'Not provided' : employee.email}" /></dd></div>
                        <div class="account-detail"><dt>Address</dt><dd><c:out value="${empty fn:trim(employee.address) ? 'Not provided' : employee.address}" /></dd></div>
                    </dl>
                </section>

                <section class="account-panel" aria-labelledby="workInfoTitle">
                    <header class="account-panel-heading">
                        <div class="account-section-title">
                            <span class="account-section-icon" aria-hidden="true"><i class="fa-solid fa-briefcase"></i></span>
                            <h2 id="workInfoTitle">Work information</h2>
                        </div>
                    </header>
                    <dl class="account-detail-grid account-work-grid">
                        <div class="account-detail"><dt>Position</dt><dd><c:out value="${empty fn:trim(employee.position) ? 'Not provided' : employee.position}" /></dd></div>
                        <div class="account-detail"><dt>Shift</dt><dd><c:out value="${empty fn:trim(employee.shift) ? 'Not provided' : employee.shift}" /></dd></div>
                        <div class="account-detail"><dt>Salary</dt><dd><c:choose><c:when test="${not empty employee.salary}"><fmt:formatNumber value="${employee.salary}" type="number" groupingUsed="true" maxFractionDigits="0" /> <span class="account-currency">₫</span></c:when><c:otherwise>Not provided</c:otherwise></c:choose></dd></div>
                    </dl>
                </section>
            </div>
        </div>
    </div>
</layout:layout>
