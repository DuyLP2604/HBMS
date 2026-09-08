<%-- 
    Document   : bookingDetail
    Created on : Jul 20, 2026, 11:41:12 AM
    Author     : Lenovo
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Booking Detail</title>
        <link rel ="stylesheet" href ="css/profile.css">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
              rel="stylesheet">
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
    </head>
        <body>
        <jsp:include page="components/navbar.jsp" />
        <div class="container py-5">

            <div class="row justify-content-center">

                <div class="col-lg-8 col-xl-7">

                    <div class="card profile-card">

                        <!-- Header -->
                        <div class="profile-header">

                            <div class="profile-cover"></div>

                            <div class="profile-user">

                                <div class="profile-info">
                                    <h2>Detail Information</h2>
                                    <p><b>Booking ID: </b>${booking.bookingId}</p>
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
                                    ${booking.customer.id}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Customer
                                </span>

                                <span class="info-value">
                                    ${booking.customer.fullname}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Room ID
                                </span>

                                <span class="info-value">
                                    ${booking.room.roomId}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Booking Date
                                </span>

                                <span class="info-value">
                                    ${booking.bookingDate}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Check-in Date
                                </span>

                                <span class="info-value">
                                    ${booking.checkInDate}
                                </span>
                            </div>

                            <div class="info-row">
                                <span class="info-label">
                                    Check-out Date
                                </span>

                                <span class="info-value">
                                    ${booking.checkOutDate}
                                </span>
                            </div>
                        </div>

                        <!-- Footer -->
                        <div class="profile-footer text-center p-3">
                            <c:if test="${booking.bookingStatus == 'Pending'}">
                                <a href="booking?update&status=Confirmed&id=${booking.bookingId}" class="btn btn-success">Confirm Booking</a>
                                <a href="booking?update&status=Cancelled&id=${booking.bookingId}" class="btn btn-danger">Cancel Booking</a>
                            </c:if>

                            <c:if test="${booking.bookingStatus != 'Pending'}">
                                <div class="alert alert-info">Status: <strong>${booking.bookingStatus}</strong></div>
                            </c:if>

                            <a href="booking?action=list" class="btn btn-secondary mt-2">Back</a>
                        </div>

                    </div>

                </div>

            </div>

        </div>
    </body>
</html>
