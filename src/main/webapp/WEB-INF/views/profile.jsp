<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="My Profile"
    pageCss="profile.css"
    useBootstrap="true"
    >
    <div class="container py-5">

        <div class="row justify-content-center">

            <div class="col-lg-8 col-xl-7">

                <div class="card profile-card">

                    <!-- Header -->
                    <div class="profile-header">

                        <div class="profile-cover"></div>

                        <div class="profile-user">

                            <div class="profile-avatar">
                                <i class="fas fa-user"></i>
                            </div>

                            <div class="profile-info">

                                <h2>
                                    <c:out value="${customer.fullName}" />
                                </h2>

                                <p>
                                    Customer Profile
                                </p>

                            </div>

                        </div>

                    </div>


                    <!-- Body -->
                    <div class="profile-body">

                        <div class="info-row">

                            <span class="info-label">
                                Customer ID
                            </span>

                            <span class="info-value">
                                <c:out value="${customer.id}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Full Name
                            </span>

                            <span class="info-value">
                                <c:out value="${customer.fullname}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Email
                            </span>

                            <span class="info-value">

                                <c:choose>

                                    <c:when test="${not empty customer.email}">
                                        <c:out value="${customer.email}" />
                                    </c:when>

                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Phone Number
                            </span>

                            <span class="info-value">

                                <c:choose>

                                    <c:when test="${not empty customer.phone}">
                                        <c:out value="${customer.phone}" />
                                    </c:when>

                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Address
                            </span>

                            <span class="info-value">

                                <c:choose>

                                    <c:when test="${not empty customer.address}">
                                        <c:out value="${customer.address}" />
                                    </c:when>

                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Identity Number
                            </span>

                            <span class="info-value">

                                <c:choose>

                                    <c:when test="${not empty customer.cccd}">
                                        <c:out value="${customer.cccd}" />
                                    </c:when>

                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Passport Number
                            </span>

                            <span class="info-value">

                                <c:choose>

                                    <c:when test="${not empty customer.passportNumber}">
                                        <c:out value="${customer.passportNumber}" />
                                    </c:when>

                                    <c:otherwise>
                                        Not provided
                                    </c:otherwise>

                                </c:choose>

                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Nationality
                            </span>

                            <span class="info-value">
                                <c:out value="${customer.nationality.name}" />
                            </span>

                        </div>

                    </div>


                    <!-- Footer -->
                    <a
                        href="${pageContext.request.contextPath}/profile?action=update"
                        class="update-btn"
                        >
                        <i class="fas fa-pen"></i>
                        Update Profile
                    </a>

                </div>

            </div>

        </div>

    </div>
</layout:layout>
