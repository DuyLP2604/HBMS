<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Complaint List"
    pageCss="complaint.css"
    useBootstrap="true"
    bodyClass="bg-light"

    >

    <div class="container my-5">

        <div class="card shadow-sm">

            <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center py-3">

                <h2 class="h4 mb-0">
                    Complaint List
                </h2>

            </div>


            <div class="card-body">

                <div class="table-responsive">

                    <table class="table table-hover align-middle border">

                        <thead class="table-light">

                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Customer</th>
                                <th>Created At</th>
                                <th>Status</th>
                                <th class="text-center">
                                    Action
                                </th>
                            </tr>

                        </thead>


                        <tbody>

                            <c:forEach
                                var="cp"
                                items="${complaints}"
                                >

                                <tr>

                                    <td class="fw-bold text-secondary">
                                        <c:out value="${cp.id}" />
                                    </td>


                                    <td>
                                        <c:out value="${cp.title}" />
                                    </td>


                                    <td>

                                        <c:choose>

                                            <c:when test="${empty cp.customerName}">
                                                Anonymous
                                            </c:when>

                                            <c:otherwise>
                                                <c:out value="${cp.customerName}" />
                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <td>

                                        <fmt:formatDate
                                            value="${cp.createdAt}"
                                            pattern="dd/MM/yyyy HH:mm"
                                            />

                                    </td>


                                    <td>

                                        <c:choose>

                                            <c:when test="${cp.status eq 'Đã xử lý'}">

                                                <span class="badge bg-success">
                                                    Resolved
                                                </span>

                                            </c:when>


                                            <c:when test="${cp.status eq 'Đang xử lý'}">

                                                <span class="badge bg-warning text-dark">
                                                    In Progress
                                                </span>

                                            </c:when>


                                            <c:otherwise>

                                                <span class="badge bg-secondary">
                                                    Pending
                                                </span>

                                            </c:otherwise>

                                        </c:choose>

                                    </td>


                                    <td class="text-center">

                                        <a
                                            href="${pageContext.request.contextPath}/complaint?action=viewDetail&id=${cp.id}"
                                            class="btn btn-outline-info btn-sm"
                                            >
                                            View Details
                                        </a>

                                    </td>

                                </tr>

                            </c:forEach>


                            <c:if test="${empty complaints}">

                                <tr>

                                    <td
                                        colspan="6"
                                        class="text-center text-muted py-4"
                                        >
                                        No complaints found.
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
