<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Payment Successful"
    useBootstrap="true"
    >

    <div class="container py-5">

        <div class="row justify-content-center">

            <div class="col-lg-7">

                <div class="card shadow-sm">

                    <div class="card-body text-center p-5">

                        <h1 class="h3 text-success mb-3">
                            Payment Successful
                        </h1>

                        <p>
                            Booking:
                            <strong>
                                <c:out value="${bookingID}" />
                            </strong>
                        </p>

                        <p>
                            Payment:
                            <strong>
                                <c:out value="${paymentID}" />
                            </strong>
                        </p>

                        <p>
                            Transaction:
                            <strong>
                                <c:out value="${transactionCode}" />
                            </strong>
                        </p>

                        <div class="alert alert-success">
                            Your booking is now confirmed and awaiting
                            room assignment.
                        </div>

                        <a
                            href="${pageContext.request.contextPath}/my-bookings"
                            class="btn btn-primary"
                            >
                            View My Bookings
                        </a>

                    </div>

                </div>

            </div>

        </div>

    </div>

</layout:layout>