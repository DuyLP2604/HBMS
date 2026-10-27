<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="profileName" value="${empty fn:trim(customer.fullName) ? sessionScope.user.username : customer.fullName}" />
<c:url var="homeUrl" value="/home" />
<c:url var="editProfileUrl" value="/profile"><c:param name="action" value="update" /></c:url>
<c:url var="changePasswordUrl" value="/change-password" />
<layout:layout title="My Profile" pageCss="account-profile.css" bodyClass="account-profile-page">
    <div class="account-profile">
        <header class="account-page-heading">
            <div>
                <p class="account-eyebrow">YOUR ACCOUNT</p>
                <h1>My profile</h1>
            </div>
            <a href="${homeUrl}" class="account-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to home</a>
        </header>

        <div class="account-profile-grid">
            <aside class="account-sidebar" aria-label="Account overview">
                <section class="account-identity" aria-labelledby="accountName">
                    <div class="account-identity-top">
                        <div class="account-identity-mark">
                            <div class="account-avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(profileName, 0, 1))}" /></div>
                            <span class="account-role">Customer</span>
                        </div>
                        <h2 id="accountName"><c:out value="${profileName}" /></h2>
                    </div>
                    <dl class="account-identity-meta">
                        <div><dt>Username</dt><dd><c:out value="${sessionScope.user.username}" /></dd></div>
                        <div><dt>Customer ID</dt><dd><c:out value="${customer.customerID}" /></dd></div>
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
                        <a href="${editProfileUrl}" class="account-button account-button-secondary"><i class="fa-solid fa-pen" aria-hidden="true"></i> Edit profile</a>
                    </header>
                    <dl class="account-detail-grid">
                        <div class="account-detail"><dt>Full name</dt><dd><c:out value="${empty fn:trim(customer.fullName) ? 'Not provided' : customer.fullName}" /></dd></div>
                        <div class="account-detail"><dt>Phone number</dt><dd><c:out value="${empty fn:trim(customer.phone) ? 'Not provided' : customer.phone}" /></dd></div>
                        <div class="account-detail"><dt>Email address</dt><dd><c:out value="${empty fn:trim(customer.email) ? 'Not provided' : customer.email}" /></dd></div>
                        <div class="account-detail"><dt>Nationality</dt><dd><c:out value="${empty customer.nationalityID or empty fn:trim(customer.nationalityID.nationalityName) ? 'Not provided' : customer.nationalityID.nationalityName}" /></dd></div>
                        <div class="account-detail account-detail-wide"><dt>Address</dt><dd><c:out value="${empty fn:trim(customer.address) ? 'Not provided' : customer.address}" /></dd></div>
                    </dl>
                </section>
            </div>
        </div>
    </div>
</layout:layout>
