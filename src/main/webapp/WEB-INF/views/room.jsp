<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Room List"
    pageCss="room.css"
    useBootstrap="true"
    bodyClass="bg-light"

    >
    <div class="container my-5">

        <div class="card shadow-sm">

            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">

                <h2 class="h4 mb-0">
                    Room List
                </h2>

                <a
                    href="${pageContext.request.contextPath}/room?action=add"
                    class="btn btn-success btn-sm"
                    >
                    + Add New Room
                </a>

            </div>


            <div class="card-body">

                <!-- Search -->
                <div class="row mb-4">

                    <div class="col-md-6">

                        <form
                            action="${pageContext.request.contextPath}/room"
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
                                    placeholder="Enter room ID..."
                                    >

                                <button
                                    type="submit"
                                    class="btn btn-primary"
                                    >
                                    Search
                                </button>

                                <c:if test="${not empty param.searchId}">

                                    <a
                                        href="${pageContext.request.contextPath}/room?action=list"
                                        class="btn btn-secondary"
                                        >
                                        Clear Filter
                                    </a>

                                </c:if>

                            </div>

                        </form>

                    </div>

                </div>


                <!-- Room Table -->
                <div class="table-responsive">

                    <table class="table table-hover align-middle border">

                        <thead class="table-light">

                            <tr>
                                <th>Room ID</th>
                                <th>Room Number</th>
                                <th>Image</th>
                                <th>Price (VND/night)</th>
                                <th>Hotel Name</th>
                                <th>Status</th>
                                <th class="text-center">
                                    Action
                                </th>
                            </tr>

                        </thead>


                        <tbody>

                            <c:forEach
                                items="${roomList}"
                                var="c"
                                >

                                <c:if test="${empty param.searchId
                                              or c.roomId.trim().equalsIgnoreCase(param.searchId.trim())}">

                                      <tr>

                                          <td class="fw-bold text-secondary">
                                              <c:out value="${c.roomId}" />
                                          </td>


                                          <td>
                                              <c:out value="${c.roomNumber}" />
                                          </td>


                                          <td>

                                              <img
                                                  class="room-thumbnail"
                                                  src="${pageContext.request.contextPath}/assets/images/room/${c.roomImage}"
                                                  alt="Room ${c.roomNumber}"
                                                  >

                                          </td>


                                          <td>
                                              <c:out value="${c.price}" />
                                              VND
                                          </td>


                                          <td>
                                              <c:out value="${c.hotel.name}" />
                                          </td>


                                          <td>

                                              <c:choose>

                                                  <c:when test="${c.status eq 'Available'}">

                                                      <span class="badge rounded-pill bg-success">
                                                          Available
                                                      </span>

                                                  </c:when>


                                                  <c:when test="${c.status eq 'Occupied'}">

                                                      <span class="badge rounded-pill bg-warning text-dark">
                                                          Occupied
                                                      </span>

                                                  </c:when>


                                                  <c:otherwise>

                                                      <span class="badge rounded-pill bg-danger">
                                                          <c:out value="${c.status}" />
                                                      </span>

                                                  </c:otherwise>

                                              </c:choose>

                                          </td>


                                          <td class="text-center">

                                              <a
                                                  href="${pageContext.request.contextPath}/room?action=viewDetail&id=${c.roomId}"
                                                  class="btn btn-outline-info btn-sm me-1"
                                                  >
                                                  View Details
                                              </a>

                                              <a
                                                  href="${pageContext.request.contextPath}/room?action=update&id=${c.roomId}"
                                                  class="btn btn-outline-warning btn-sm"
                                                  >
                                                  Edit
                                              </a>

                                          </td>

                                      </tr>

                                </c:if>

                            </c:forEach>


                            <c:if test="${empty roomList}">

                                <tr>

                                    <td
                                        colspan="7"
                                        class="text-center text-muted py-4"
                                        >
                                        No rooms found.
                                    </td>

                                </tr>

                            </c:if>

                        </tbody>

                    </table>

                </div>

            </div>

        </div>

    </div>
</layout:layout>
