<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="My Bookings"
    useBootstrap="true"
    pageJs="my-bookings.js"
    >

    <div class="container py-5">

        <div class="d-flex justify-content-between align-items-center mb-4">

            <h1 class="h3 mb-0">
                My Bookings
            </h1>

            <a
                href="${pageContext.request.contextPath}/room-types"
                class="btn btn-primary"
                >
                Book Another Stay
            </a>

        </div>

        <%-- Flash success message --%>
        <c:if test="${not empty successMessage}">

            <div
                class="alert alert-success alert-dismissible fade show"
                role="alert"
                >

                <c:out value="${successMessage}" />

                <button
                    type="button"
                    class="btn-close"
                    data-bs-dismiss="alert"
                    aria-label="Close"
                    >
                </button>

            </div>

        </c:if>

        <%-- Flash error message --%>
        <c:if test="${not empty errorMessage}">

            <div
                class="alert alert-danger alert-dismissible fade show"
                role="alert"
                >

                <c:out value="${errorMessage}" />

                <button
                    type="button"
                    class="btn-close"
                    data-bs-dismiss="alert"
                    aria-label="Close"
                    >
                </button>

            </div>

        </c:if>

        <c:choose>

            <c:when test="${empty bookings}">

                <div class="alert alert-info">
                    You do not have any bookings yet.
                </div>

            </c:when>

            <c:otherwise>

                <div class="row g-4">

                    <c:forEach
                        var="booking"
                        items="${bookings}"
                        >

                        <div class="col-lg-6">

                            <div class="card shadow-sm h-100">

                                <div class="card-body d-flex flex-column">

                                    <div class="d-flex justify-content-between align-items-start">

                                        <h2 class="h5">
                                            Booking
                                            <c:out value="${booking.bookingID}" />
                                        </h2>

                                        <c:choose>

                                            <c:when test="${booking.bookingStatus eq 'CANCELLED'}">

                                                <span class="badge bg-danger">
                                                    CANCELLED
                                                </span>

                                            </c:when>

                                            <c:when test="${booking.bookingStatus eq 'PENDING_PAYMENT'}">

                                                <span class="badge bg-warning text-dark">
                                                    PENDING PAYMENT
                                                </span>

                                            </c:when>

                                            <c:when test="${booking.bookingStatus eq 'CONFIRMED'}">

                                                <span class="badge bg-info text-dark">
                                                    CONFIRMED
                                                </span>

                                            </c:when>

                                            <c:when test="${booking.bookingStatus eq 'ASSIGNED'}">

                                                <span class="badge bg-primary">
                                                    ASSIGNED
                                                </span>

                                            </c:when>

                                            <c:when test="${booking.bookingStatus eq 'CHECKED_IN'}">

                                                <span class="badge bg-success">
                                                    CHECKED IN
                                                </span>

                                            </c:when>

                                            <c:when test="${booking.bookingStatus eq 'CHECKED_OUT'}">

                                                <span class="badge bg-secondary">
                                                    CHECKED OUT
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="badge bg-secondary">
                                                    <c:out value="${booking.bookingStatus}" />
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </div>

                                    <hr>

                                    <p>
                                        <strong>Stay:</strong>

                                        <fmt:formatDate
                                            value="${booking.checkInDate}"
                                            pattern="dd/MM/yyyy"
                                            />

                                        –

                                        <fmt:formatDate
                                            value="${booking.checkOutDate}"
                                            pattern="dd/MM/yyyy"
                                            />
                                    </p>

                                    <p>
                                        <strong>Nights:</strong>
                                        <c:out value="${booking.numberOfNights}" />
                                    </p>

                                    <p>
                                        <strong>Payment:</strong>

                                        <c:choose>

                                            <c:when test="${booking.paymentStatus eq 'PAID'}">

                                                <span class="badge bg-success">
                                                    PAID
                                                </span>

                                            </c:when>

                                            <c:when test="${booking.paymentStatus eq 'REFUNDED'}">

                                                <span class="badge bg-secondary">
                                                    REFUNDED
                                                </span>

                                            </c:when>

                                            <c:otherwise>

                                                <span class="badge bg-warning text-dark">
                                                    <c:out value="${booking.paymentStatus}" />
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </p>

                                    <p>
                                        <strong>Total:</strong>

                                        <fmt:formatNumber
                                            value="${booking.totalAmount}"
                                            type="number"
                                            />

                                        VND
                                    </p>

                                    <div class="d-flex flex-wrap gap-2 mt-auto">

                                        <a
                                            href="${pageContext.request.contextPath}/my-bookings?bookingID=${booking.bookingID}"
                                            class="btn btn-outline-primary"
                                            >
                                            View Details
                                        </a>

                                        <c:if test="${booking.bookingStatus eq 'PENDING_PAYMENT'}">

                                            <p class="text-danger">

                                                <strong>Payment deadline:</strong>

                                                <fmt:formatDate
                                                    value="${booking.paymentDeadline}"
                                                    pattern="dd/MM/yyyy HH:mm:ss"
                                                    />

                                                <span
                                                    class="payment-countdown ms-2"
                                                    data-deadline="${booking.paymentDeadline.time}"
                                                    >
                                                </span>

                                            </p>

                                            <a
                                                href="${pageContext.request.contextPath}/payment?bookingID=${booking.bookingID}"
                                                class="btn btn-success"
                                                >
                                                Pay Now
                                            </a>

                                        </c:if>

                                        <c:if test="${booking.bookingStatus eq 'PENDING_PAYMENT'
                                                      or booking.bookingStatus eq 'CONFIRMED'
                                                      or booking.bookingStatus eq 'ASSIGNED'}">

                                              <form
                                                  action="${pageContext.request.contextPath}/my-bookings/cancel"
                                                  method="post"
                                                  class="d-inline"
                                                  onsubmit="return confirm('Are you sure you want to cancel this booking?');"
                                                  >

                                                  <input
                                                      type="hidden"
                                                      name="bookingID"
                                                      value="${booking.bookingID}"
                                                      >

                                                  <button
                                                      type="submit"
                                                      class="btn btn-outline-danger"
                                                      >
                                                      Cancel Booking
                                                  </button>

                                              </form>

                                        </c:if>

                                    </div>

                                </div>

                            </div>

                        </div>

                    </c:forEach>

                </div>

            </c:otherwise>

        </c:choose>

    </div>

</layout:layout>