<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="My Profile" pageCss="profile.css" useBootstrap="true">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-10 col-xl-9">
                <div class="card profile-card">
                    <div class="profile-header">
                        <div class="profile-cover"></div>
                        <div class="profile-user">
                            <div class="profile-avatar"><i class="fas fa-user"></i></div>
                            <div class="profile-info">
                                <h2><c:out value="${customer.fullName}" /></h2>
                                <p>Customer Profile</p>
                            </div>
                        </div>
                    </div>
                    <div class="profile-body">
                        <div class="info-row">
                            <span class="info-label">Customer ID</span>
                            <span class="info-value"><c:out value="${customer.customerID}" /></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Full Name</span>
                            <span class="info-value"><c:out value="${customer.fullName}" /></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Email</span>
                            <span class="info-value"><c:out value="${empty customer.email ? 'Not provided' : customer.email}" /></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Phone Number</span>
                            <span class="info-value"><c:out value="${empty customer.phone ? 'Not provided' : customer.phone}" /></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Nationality</span>
                            <span class="info-value"><c:out value="${empty customer.nationalityID ? 'Not provided' : customer.nationalityID.nationalityName}" /></span>
                        </div>
                        <div class="info-row">
                            <span class="info-label">Address</span>
                            <span class="info-value"><c:out value="${empty customer.address ? 'Not provided' : customer.address}" /></span>
                        </div>
                    </div>
                    <a href="${pageContext.request.contextPath}/profile?action=update" class="update-btn"><i class="fas fa-pen"></i> Update Profile</a>
                </div>
            </div>
        </div>
    </div>
</layout:layout>