<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="isPost" value="${pageContext.request.method eq 'POST'}" />
<c:set var="selectedPosition" value="${isPost ? param.position : employee.position}" />
<c:set var="selectedShift" value="${isPost ? param.shift : employee.shift}" />
<c:set var="emailValue" value="${isPost and param.email ne null ? param.email : employee.email}" />
<layout:layout
    title="Update Employee Information"
    pageCss="employee.css"
    pageJs="employeeForm.js"
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
                                <c:out value="${flashError}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        <form id="employeeForm" data-mode="update" action="${pageContext.request.contextPath}/employee" method="post" novalidate>
                            <input type="hidden" name="action" value="update">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="employeeID" class="form-label fw-semibold text-muted">Employee ID (Read-only)</label>
                                    <input id="employeeID" type="text" name="id" class="form-control bg-light" value="${fn:escapeXml(isPost ? param.id : employee.employeeID)}" maxlength="6" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label for="fullName" class="form-label fw-semibold">Full Name *</label>
                                    <input id="fullName" type="text" name="name" class="form-control${not empty fieldErrors['name'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.name : employee.fullName)}" maxlength="100" autocomplete="name" required>
                                    <div class="invalid-feedback" data-feedback-for="name"><c:out value="${fieldErrors['name']}" /></div>
                                </div>
                                <div class="col-md-6">
                                    <label for="position" class="form-label fw-semibold">Position *</label>
                                    <select id="position" name="position" class="form-select${not empty fieldErrors['position'] ? ' is-invalid' : ''}" required>
                                        <option value="" disabled <c:if test="${empty selectedPosition}">selected</c:if>>Select position</option>
                                        <c:forEach var="pos" items="${positions}">
                                            <option value="${fn:escapeXml(pos)}" <c:if test="${selectedPosition eq pos}">selected</c:if>><c:out value="${pos}" /></option>
                                        </c:forEach>
                                    </select>
                                    <div class="invalid-feedback" data-feedback-for="position"><c:out value="${fieldErrors['position']}" /></div>
                                </div>
                                <div class="col-md-6">
                                    <label for="salary" class="form-label fw-semibold">Base Salary (VND)</label>
                                    <input id="salary" type="number" name="salary" class="form-control${not empty fieldErrors['salary'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.salary : employee.salary)}" min="0" step="0.01">
                                    <div class="invalid-feedback" data-feedback-for="salary"><c:out value="${fieldErrors['salary']}" /></div>
                                </div>
                                <div class="col-md-6">
                                    <label for="shift" class="form-label fw-semibold">Work Shift</label>
                                    <select id="shift" name="shift" class="form-select${not empty fieldErrors['shift'] ? ' is-invalid' : ''}">
                                        <option value="" <c:if test="${empty selectedShift}">selected</c:if>>Select work shift</option>
                                        <option value="Morning" <c:if test="${selectedShift eq 'Morning'}">selected</c:if>>Morning</option>
                                        <option value="Afternoon" <c:if test="${selectedShift eq 'Afternoon'}">selected</c:if>>Afternoon</option>
                                        <option value="Night" <c:if test="${selectedShift eq 'Night'}">selected</c:if>>Night</option>
                                    </select>
                                    <div class="invalid-feedback" data-feedback-for="shift"><c:out value="${fieldErrors['shift']}" /></div>
                                </div>
                                <div class="col-md-6">
                                    <label for="phone" class="form-label fw-semibold">Phone Number *</label>
                                    <input id="phone" type="tel" name="phone" class="form-control${not empty fieldErrors['phone'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.phone : employee.phone)}" maxlength="20" autocomplete="tel" required>
                                    <div class="invalid-feedback" data-feedback-for="phone"><c:out value="${fieldErrors['phone']}" /></div>
                                </div>
                                <div class="col-12">
                                    <label for="email" class="form-label fw-semibold">Email</label>
                                    <input id="email" type="email" name="email" class="form-control${not empty fieldErrors['email'] ? ' is-invalid' : ''}" value="${fn:escapeXml(emailValue)}" placeholder="e.g. employee@gmail.com" maxlength="100" autocomplete="email">
                                    <div class="invalid-feedback" data-feedback-for="email"><c:out value="${fieldErrors['email']}" /></div>
                                </div>
                                <div class="col-12">
                                    <label for="address" class="form-label fw-semibold">Address</label>
                                    <input id="address" type="text" name="address" class="form-control${not empty fieldErrors['address'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.address : employee.address)}" maxlength="200" autocomplete="street-address">
                                    <div class="invalid-feedback" data-feedback-for="address"><c:out value="${fieldErrors['address']}" /></div>
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
