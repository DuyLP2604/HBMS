<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Staff Profile"
    useBootstrap="true"
    bodyClass="bg-light">

    <fmt:setLocale value="vi_VN" scope="page" />

    <div class="container py-5">
        <div class="d-flex flex-column flex-sm-row
                    justify-content-between align-items-sm-center
                    gap-3 mb-4">
            <div>
                <h1 class="h2 mb-1">My Profile</h1>
                <p class="text-muted mb-0">
                    View your staff information.
                </p>
            </div>

            <a href="${pageContext.request.contextPath}/staff/dashboard"
               class="btn btn-outline-secondary">
                <i class="fa-solid fa-arrow-left me-1"></i>
                Back to Dashboard
            </a>
        </div>

        <div class="card shadow-sm">
            <div class="card-body p-4 p-md-5">

                <div class="d-flex align-items-center gap-3 mb-4">
                    <div class="text-primary">
                        <i class="fa-solid fa-circle-user fa-3x"></i>
                    </div>

                    <div>
                        <h2 class="h4 mb-1">
                            <c:out value="${employee.fullName}" />
                        </h2>
                        <span class="badge bg-primary">
                            <c:out value="${employee.position}" />
                        </span>
                    </div>
                </div>

                <hr class="mb-4">

                <div class="row g-4">
                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Employee ID</div>
                        <div class="fw-semibold">
                            <c:out value="${employee.employeeID}" />
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Username</div>
                        <div class="fw-semibold">
                            <c:out value="${sessionScope.user.username}" />
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Full Name</div>
                        <div class="fw-semibold">
                            <c:out value="${employee.fullName}" />
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Position</div>
                        <div class="fw-semibold">
                            <c:out value="${employee.position}" />
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Shift</div>
                        <div class="fw-semibold">
                            <c:out value="${employee.shift}" default="—" />
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Phone Number</div>
                        <div class="fw-semibold">
                            <c:out value="${employee.phone}" default="—" />
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Salary</div>
                        <div class="fw-semibold">
                            <c:choose>
                                <c:when test="${not empty employee.salary}">
                                    <fmt:formatNumber
                                        value="${employee.salary}"
                                        type="number"
                                        groupingUsed="true"
                                        maxFractionDigits="0" />
                                    ₫
                                </c:when>
                                <c:otherwise>—</c:otherwise>
                            </c:choose>
                        </div>
                    </div>

                    <div class="col-md-6">
                        <div class="text-muted small mb-1">Address</div>
                        <div class="fw-semibold">
                            <c:out value="${employee.address}" default="—" />
                        </div>
                    </div>
                </div>

            </div>
        </div>
    </div>

</layout:layout>