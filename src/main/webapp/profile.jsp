<%-- 
    Document   : profile
    Created on : Jul 12, 2026, 11:39:57 AM
    Author     : default
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>My Profile</title>
        <link rel ="stylesheet" href ="css/profile.css">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
              rel="stylesheet">
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
    </head>

    <body>

        <jsp:include page="components/navbar.jsp"/>

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
                                    <h2>${customer.fullname}</h2>
                                    <p>Customer Profile</p>
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
                                    ${customer.id}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Full Name
                                </span>

                                <span class="info-value">
                                    ${customer.fullname}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Email
                                </span>

                                <span class="info-value">
                                    ${customer.email}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Phone Number
                                </span>

                                <span class="info-value">
                                    ${customer.phone}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Address
                                </span>

                                <span class="info-value">
                                    ${customer.address}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Identity Number
                                </span>

                                <span class="info-value">
                                    ${customer.cccd}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Passport Number
                                </span>

                                <span class="info-value">
                                    ${customer.passportNumber}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Nationality
                                </span>

                                <span class="info-value">
                                    ${customer.nationality.name}
                                </span>
                            </div>

                        </div>

                        <!-- Footer -->
                        <a href="profile?action=update"
                           class="update-btn">
                            <i class="fas fa-pen"></i>
                            Update Profile
                        </a>

                    </div>

                </div>

            </div>

        </div>

    </body>

</html>

