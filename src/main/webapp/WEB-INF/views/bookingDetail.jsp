<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Booking Detail"
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

                            <div class="profile-info">

                                <h2>
                                    Booking Details
                                </h2>

                                <p>
                                    <strong>Booking ID:</strong>
                                    <c:out value="${booking.bookingID}" />
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
                                <c:out value="${booking.customerID.customerID}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Customer
                            </span>

                            <span class="info-value">
                                <c:out value="${booking.customerID.fullName}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Room ID
                            </span>

                            <span class="info-value">
                                <c:out value="${booking.roomID.roomID}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Booking Date
                            </span>

                            <span class="info-value">
                                <c:out value="${booking.bookingDate}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Check-in Date
                            </span>

                            <span class="info-value">
                                <c:out value="${booking.checkInDate}" />
                            </span>

                        </div>


                        <div class="info-row">

                            <span class="info-label">
                                Check-out Date
                            </span>

                            <span class="info-value">
                                <c:out value="${booking.checkOutDate}" />
                            </span>

                        </div>

                    </div>


                    <!-- Footer -->
                    <div class="profile-footer text-center p-3">

                        <c:if test="${booking.bookingStatus == 'Pending'}">

                            <a
                                href="${pageContext.request.contextPath}/booking?action=update&status=Confirmed&id=${booking.bookingID}"
                                class="btn btn-success"
                                >
                                Confirm Booking
                            </a>

                            <a
                                href="${pageContext.request.contextPath}/booking?action=update&status=Cancelled&id=${booking.bookingID}"
                                class="btn btn-danger"
                                >
                                Cancel Booking
                            </a>

                        </c:if>


                        <c:if test="${booking.bookingStatus != 'Pending'}">

                            <div class="alert alert-info">

                                Status:

                                <strong>
                                    <c:out value="${booking.bookingStatus}" />
                                </strong>

                            </div>

                        </c:if>


                        <a
                            href="${pageContext.request.contextPath}/booking?action=list"
                            class="btn btn-secondary mt-2"
                            >
                            Back
                        </a>

                    </div>

                </div>

            </div>

        </div>

    </div>
</layout:layout>
