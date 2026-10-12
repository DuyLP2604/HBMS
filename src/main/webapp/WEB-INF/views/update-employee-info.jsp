<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Update Employee Information"
    pageCss="employee.css"
    useBootstrap="true"
    bodyClass="bg-light">
    <div class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header bg-warning text-dark py-3">
                        <h2 class="h4 mb-0 fw-semibold">Update Employee Information</h2>
                    </div>
                    <div class="card-body p-4">
                        <c:if test="${not empty flashError}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                ${flashError}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        <form action="${pageContext.request.contextPath}/employee" method="post">
                            <input type="hidden" name="action" value="update">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="employeeID" class="form-label fw-semibold text-muted">Employee ID (Read-only)</label>
                                    <input id="employeeID" type="text" name="id" class="form-control bg-light" value="${fn:escapeXml(employee.employeeID)}" maxlength="6" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label for="fullName" class="form-label fw-semibold">Full Name *</label>
                                    <input id="fullName" type="text" name="name" class="form-control" value="${fn:escapeXml(employee.fullName)}" maxlength="100" autocomplete="name" required>
                                </div>
                                <div class="col-md-6">
                                    <label for="position" class="form-label fw-semibold">Position *</label>
                                    <select id="position" name="position" class="form-select" required>
                                        <option value="" disabled>Select position</option>
                                        <c:forEach var="pos" items="${positions}">
                                            <option value="${pos}" <c:if test="${employee.position == pos}">selected</c:if>>${pos}</option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label for="salary" class="form-label fw-semibold">Base Salary (VND)</label>
                                    <input id="salary" type="number" name="salary" class="form-control" value="${employee.salary}" min="0" step="0.01">
                                </div>
                                <div class="col-md-6">
                                    <label for="shift" class="form-label fw-semibold">Work Shift</label>
                                    <select id="shift" name="shift" class="form-select">
                                        <option value="">Select work shift</option>
                                        <option value="Morning" <c:if test="${employee.shift == 'Morning'}">selected</c:if>>Morning</option>
                                        <option value="Afternoon" <c:if test="${employee.shift == 'Afternoon'}">selected</c:if>>Afternoon</option>
                                        <option value="Night" <c:if test="${employee.shift == 'Night'}">selected</c:if>>Night</option>
                                    </select>
                                </div>
                                <div class="col-md-6">
                                    <label for="phone" class="form-label fw-semibold">Phone Number</label>
                                    <input id="phone" type="tel" name="phone" class="form-control" value="${fn:escapeXml(employee.phone)}" maxlength="20" autocomplete="tel">
                                </div>
                                <div class="col-12">
                                    <label for="address" class="form-label fw-semibold">Address</label>
                                    <input id="address" type="text" name="address" class="form-control" value="${fn:escapeXml(employee.address)}" maxlength="200" autocomplete="street-address">
                                </div>
                            </div>
                            <div class="d-flex justify-content-end gap-2 mt-4">
                                <a href="${pageContext.request.contextPath}/employee?action=list" class="btn btn-secondary">Back</a>
                                <button type="submit" class="btn btn-warning px-4 fw-semibold">Update Employee</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>