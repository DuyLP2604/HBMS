<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Booking Created"
    useBootstrap="true"
    >

    <div class="container py-5">

        <div class="row justify-content-center">

            <div class="col-lg-7">

                <div class="card shadow-sm">

                    <div class="card-body text-center p-5">

                        <h1 class="h3 text-success mb-3">
                            Booking Created Successfully
                        </h1>

                        <p class="mb-2">
                            Your booking reference is:
                        </p>

                        <p class="fs-3 fw-bold">
                            <c:out value="${bookingID}" />
                        </p>

                        <p class="text-muted">
                            The booking is currently awaiting payment.
                        </p>

                        <div class="alert alert-warning">
                            Booking status:
                            <strong>PENDING_PAYMENT</strong>
                        </div>

                        <a
                            href="${pageContext.request.contextPath}/payment?bookingID=${bookingID}"
                            class="btn btn-success"
                            >
                            Continue to Payment
                        </a>
                    </div>

                </div>

            </div>

        </div>

    </div>

</layout:layout>