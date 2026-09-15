<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Reservations"
    pageCss="booking.css"
    useBootstrap="true"
    bodyClass="bg-light"

    >

    <%-- Staff View --%>
    <c:if test="${sessionScope.user.role eq 'Staff'}">

        <div class="container my-5">

            <div class="card shadow-sm">

                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">

                    <h2 class="h4 mb-0">
                        Reservation List
                    </h2>

                    <a
                        href="${pageContext.request.contextPath}/booking?action=add"
                        class="btn btn-success btn-sm"
                        >
                        + Add New Reservation
                    </a>

                </div>

                <div class="card-body">

                    <div class="row mb-4">

                        <div class="col-md-6">

                            <form
                                action="${pageContext.request.contextPath}/booking"
                                method="get"
                                class="d-flex gap-2"
                                >

                                <input
                                    type="hidden"
                                    name="action"
                                    value="list"
                                    >

                                <div class="input-group">

                                    <span class="input-group-text bg-white">
                                        ID
                                    </span>

                                    <input
                                        type="text"
                                        name="searchId"
                                        class="form-control"
                                        value="<c:out value='${param.searchId}' />"
                                        placeholder="Enter booking ID..."
                                        >

                                    <button
                                        type="submit"
                                        class="btn btn-primary"
                                        >
                                        Search
                                    </button>

                                    <c:if test="${not empty param.searchId}">

                                        <a
                                            href="${pageContext.request.contextPath}/booking?action=list"
                                            class="btn btn-secondary"
                                            >
                                            Clear Filter
                                        </a>

                                    </c:if>

                                </div>

                            </form>

                        </div>

                    </div>


                    <div class="table-responsive">

                        <table class="table table-hover align-middle text-center border">

                            <thead class="table-light">

                                <tr>
                                    <th>Booking ID</th>
                                    <th>Customer</th>
                                    <th>Room Number</th>
                                    <th>Time</th>
                                    <th>Status</th>
                                    <th>Price (VND/night)</th>
                                    <th>Action</th>
                                </tr>

                            </thead>

                            <tbody>

                                <c:forEach
                                    var="b"
                                    items="${bookingList}"
                                    >

                                    <c:if test="${empty param.searchId
                                                  or b.bookingId.trim().equalsIgnoreCase(param.searchId.trim())}">

                                          <tr>

                                              <td>
                                                  <c:out value="${b.bookingId}" />
                                              </td>

                                              <td>

                                                  <c:out value="${b.customer.fullname}" />

                                                  <br>

                                                  Phone:
                                                  <c:out value="${b.customer.phone}" />

                                              </td>

                                              <td>
                                                  <c:out value="${b.room.roomNumber}" />
                                              </td>

                                              <td>
                                                  <c:out value="${b.checkInDate}" />
                                                  -
                                                  <c:out value="${b.checkOutDate}" />
                                              </td>

                                              <td>

                                                  <span class="badge rounded-pill
                                                        ${b.bookingStatus == 'Completed'
                                                          ? 'bg-success'
                                                          : (b.bookingStatus == 'Refused'
                                                          ? 'bg-danger'
                                                          : 'bg-secondary')}">

                                                      <c:out value="${b.bookingStatus}" />

                                                  </span>

                                              </td>

                                              <td>
                                                  <c:out value="${b.room.price}" />
                                                  VND
                                              </td>

                                              <td>

                                                  <a
                                                      href="${pageContext.request.contextPath}/booking?action=detail&id=${b.bookingId}"
                                                      class="btn btn-outline-info btn-sm"
                                                      >
                                                      View
                                                  </a>

                                              </td>

                                          </tr>

                                    </c:if>

                                </c:forEach>

                            </tbody>

                        </table>

                    </div>

                </div>

            </div>

        </div>

    </c:if>


    <%-- Customer View --%>
    <c:if test="${sessionScope.user.role eq 'Customer'}">

        <div class="container my-5">

            <div class="card shadow-sm">

                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">

                    <h2 class="h4 mb-0">
                        Reservation List
                    </h2>

                    <a
                        href="${pageContext.request.contextPath}/booking?action=add"
                        class="btn btn-success btn-sm"
                        >
                        + Add New Reservation
                    </a>

                </div>

                <div class="card-body">

                    <div class="row mb-4">

                        <div class="col-md-6">

                            <form
                                action="${pageContext.request.contextPath}/booking"
                                method="get"
                                class="d-flex gap-2"
                                >

                                <input
                                    type="hidden"
                                    name="action"
                                    value="list"
                                    >

                                <div class="input-group">

                                    <span class="input-group-text bg-white">
                                        ID
                                    </span>

                                    <input
                                        type="text"
                                        name="searchId"
                                        class="form-control"
                                        value="<c:out value='${param.searchId}' />"
                                        placeholder="Enter booking ID..."
                                        >

                                    <button
                                        type="submit"
                                        class="btn btn-primary"
                                        >
                                        Search
                                    </button>

                                    <c:if test="${not empty param.searchId}">

                                        <a
                                            href="${pageContext.request.contextPath}/booking?action=list"
                                            class="btn btn-secondary"
                                            >
                                            Clear Filter
                                        </a>

                                    </c:if>

                                </div>

                            </form>

                        </div>

                    </div>


                    <div class="table-responsive">

                        <table class="table table-hover align-middle text-center border">

                            <thead class="table-light">

                                <tr>
                                    <th>Booking ID</th>
                                    <th>Room Number</th>
                                    <th>Time</th>
                                    <th>Status</th>
                                    <th>Price (VND/night)</th>
                                    <th>Action</th>
                                </tr>

                            </thead>

                            <tbody>

                                <c:forEach
                                    var="b"
                                    items="${bookingList}"
                                    >

                                    <c:if test="${empty param.searchId
                                                  or b.bookingId.trim().equalsIgnoreCase(param.searchId.trim())}">

                                          <tr>

                                              <td>
                                                  <c:out value="${b.bookingId}" />
                                              </td>

                                              <td>
                                                  <c:out value="${b.room.roomNumber}" />
                                              </td>

                                              <td>
                                                  <c:out value="${b.checkInDate}" />
                                                  -
                                                  <c:out value="${b.checkOutDate}" />
                                              </td>

                                              <td>

                                                  <span class="badge rounded-pill
                                                        ${b.bookingStatus == 'Completed'
                                                          ? 'bg-success'
                                                          : (b.bookingStatus == 'Refused'
                                                          ? 'bg-danger'
                                                          : 'bg-secondary')}">

                                                      <c:out value="${b.bookingStatus}" />

                                                  </span>

                                              </td>

                                              <td>
                                                  <c:out value="${b.room.price}" />
                                                  VND
                                              </td>

                                              <td>

                                                  <a
                                                      href="${pageContext.request.contextPath}/booking?action=detail&id=${b.bookingId}"
                                                      class="btn btn-outline-info btn-sm"
                                                      >
                                                      View
                                                  </a>

                                              </td>

                                          </tr>

                                    </c:if>

                                </c:forEach>

                            </tbody>

                        </table>

                    </div>

                </div>

            </div>

        </div>

    </c:if>
</layout:layout>
