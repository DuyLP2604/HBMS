<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Payment"
    useBootstrap="true"
    pageJs="payment.js"
    >

    <div class="container py-5">

        <div class="row justify-content-center">

            <div class="col-lg-7">

                <div class="card shadow-sm">

                    <div class="card-header bg-primary text-white">

                        <h1 class="h4 mb-0">
                            Booking Payment
                        </h1>

                    </div>

                    <div class="card-body p-4">

                        <c:if test="${not empty errorMessage}">

                            <div class="alert alert-danger">

                                <c:out value="${errorMessage}" />

                            </div>

                        </c:if>


                        <dl class="row">

                            <dt class="col-sm-5">
                                Booking Reference
                            </dt>

                            <dd class="col-sm-7">
                                <c:out value="${booking.bookingID}" />
                            </dd>


                            <dt class="col-sm-5">
                                Check-in
                            </dt>

                            <dd class="col-sm-7">

                                <fmt:formatDate
                                    value="${booking.checkInDate}"
                                    pattern="dd/MM/yyyy"
                                    />

                            </dd>


                            <dt class="col-sm-5">
                                Check-out
                            </dt>

                            <dd class="col-sm-7">

                                <fmt:formatDate
                                    value="${booking.checkOutDate}"
                                    pattern="dd/MM/yyyy"
                                    />

                            </dd>


                            <dt class="col-sm-5">
                                Payment Deadline
                            </dt>

                            <dd class="col-sm-7">

                                <fmt:formatDate
                                    value="${booking.paymentDeadline}"
                                    pattern="dd/MM/yyyy HH:mm:ss"
                                    />

                            </dd>


                            <dt class="col-sm-5">
                                Time Remaining
                            </dt>

                            <dd class="col-sm-7">

                                <span
                                    id="paymentCountdown"
                                    class="fw-semibold text-danger"
                                    data-deadline="${booking.paymentDeadline.time}"
                                    >
                                    Calculating...
                                </span>

                            </dd>


                            <dt class="col-sm-5">
                                Amount
                            </dt>

                            <dd class="col-sm-7 fw-bold text-success">

                                <fmt:formatNumber
                                    value="${booking.totalAmount}"
                                    type="number"
                                    />

                                VND

                            </dd>

                        </dl>


                        <c:choose>

                            <c:when test="${empty paymentMethods}">

                                <div class="alert alert-warning">
                                    No payment methods are currently available.
                                </div>

                                <a
                                    href="${pageContext.request.contextPath}/my-bookings"
                                    class="btn btn-secondary w-100"
                                    >
                                    Back to My Bookings
                                </a>

                            </c:when>


                            <c:otherwise>

                                <form
                                    id="paymentForm"
                                    action="${pageContext.request.contextPath}/payment"
                                    method="post"
                                    >

                                    <input
                                        type="hidden"
                                        name="bookingID"
                                        value="<c:out value='${booking.bookingID}' />"
                                        >


                                    <div class="mb-4">

                                        <label
                                            for="methodID"
                                            class="form-label fw-semibold"
                                            >
                                            Payment Method
                                        </label>

                                        <select
                                            id="methodID"
                                            name="methodID"
                                            class="form-select"
                                            required
                                            >

                                            <option
                                                value=""
                                                disabled
                                                selected
                                                >
                                                Select a payment method
                                            </option>

                                            <c:forEach
                                                var="method"
                                                items="${paymentMethods}"
                                                >

                                                <option value="${method.methodID}">

                                                    <c:out
                                                        value="${method.methodName}"
                                                        />

                                                </option>

                                            </c:forEach>

                                        </select>

                                    </div>


                                    <div
                                        id="expiredMessage"
                                        class="alert alert-danger d-none"
                                        >
                                        The payment time limit has expired.
                                    </div>


                                    <div class="d-flex gap-2">

                                        <a
                                            href="${pageContext.request.contextPath}/my-bookings"
                                            class="btn btn-outline-secondary"
                                            >
                                            Back
                                        </a>

                                        <button
                                            id="paymentButton"
                                            type="submit"
                                            class="btn btn-success flex-grow-1"
                                            >
                                            Confirm Payment
                                        </button>

                                    </div>

                                </form>

                            </c:otherwise>

                        </c:choose>

                    </div>

                </div>

            </div>

        </div>

    </div>
</layout:layout>