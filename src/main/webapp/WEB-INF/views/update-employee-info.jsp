<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="isPost" value="${pageContext.request.method eq 'POST'}" />
<c:set var="selectedPosition" value="${isPost ? param.position : employee.position}" />
<c:set var="selectedShift" value="${isPost ? param.shift : employee.shift}" />
<c:set var="emailValue" value="${isPost and param.email ne null ? param.email : employee.email}" />
<layout:layout title="Update Employee Information" pageCss="employee.css" pageJs="employeeForm.js" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-shell-form ui-employee-workspace">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">STAFF DIRECTORY</p><h1>Update Employee Information</h1><p class="ui-description">Review the employee's personal and employment details.</p></div></header>
<c:if test="${not empty flashError}">
                            <div class="alert alert-dismissible fade show ui-notice ui-notice-danger" role="alert">
                                <c:out value="${flashError}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
        <section class="ui-panel" aria-label="Update employee information">
<form id="employeeForm" data-mode="update" action="${pageContext.request.contextPath}/employee" method="post" novalidate>
                <div class="ui-panel-body">
                            <input type="hidden" name="action" value="update">
                            <div class="employee-form-section"><h2 class="employee-section-title"><i class="fa-regular fa-user" aria-hidden="true"></i> Personal Information</h2><div class="ui-form-grid"><div class="ui-field">
                                    <label for="employeeID" class="form-label fw-semibold text-muted">Employee ID (Read-only)</label>
                                    <input id="employeeID" type="text" name="id" class="form-control bg-light" value="${fn:escapeXml(isPost ? param.id : employee.employeeID)}" maxlength="6" readonly>
                                </div>
<div class="ui-field">
                                    <label for="fullName" class="form-label fw-semibold">Full Name <span class="ui-required">*</span></label>
                                    <input id="fullName" type="text" name="name" class="form-control${not empty fieldErrors['name'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.name : employee.fullName)}" maxlength="100" autocomplete="name" required>
                                    <div class="invalid-feedback" data-feedback-for="name"><c:out value="${fieldErrors['name']}" /></div>
                                </div>
<div class="ui-field">
                                    <label for="phone" class="form-label fw-semibold">Phone Number <span class="ui-required">*</span></label>
                                    <input id="phone" type="tel" name="phone" class="form-control${not empty fieldErrors['phone'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.phone : employee.phone)}" maxlength="20" autocomplete="tel" required>
                                    <div class="invalid-feedback" data-feedback-for="phone"><c:out value="${fieldErrors['phone']}" /></div>
                                </div>
<div class="ui-field">
                                    <label for="email" class="form-label fw-semibold">Email</label>
                                    <input id="email" type="email" name="email" class="form-control${not empty fieldErrors['email'] ? ' is-invalid' : ''}" value="${fn:escapeXml(emailValue)}" placeholder="e.g. employee@gmail.com" maxlength="100" autocomplete="email">
                                    <div class="invalid-feedback" data-feedback-for="email"><c:out value="${fieldErrors['email']}" /></div>
                                </div>
<div class="ui-field ui-field-full">
                                    <label for="address" class="form-label fw-semibold">Address</label>
                                    <input id="address" type="text" name="address" class="form-control${not empty fieldErrors['address'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.address : employee.address)}" maxlength="200" autocomplete="street-address">
                                    <div class="invalid-feedback" data-feedback-for="address"><c:out value="${fieldErrors['address']}" /></div>
                                </div></div></div><div class="employee-form-section"><h2 class="employee-section-title"><i class="fa-solid fa-briefcase" aria-hidden="true"></i> Employment Details</h2><div class="ui-form-grid"><div class="ui-field">
                                    <label for="position" class="form-label fw-semibold">Position <span class="ui-required">*</span></label>
                                    <select id="position" name="position" class="form-select${not empty fieldErrors['position'] ? ' is-invalid' : ''}" required>
                                        <option value="" disabled <c:if test="${empty selectedPosition}">selected</c:if>>Select position</option>
                                        <c:forEach var="pos" items="${positions}">
                                            <option value="${fn:escapeXml(pos)}" <c:if test="${selectedPosition eq pos}">selected</c:if>><c:out value="${pos}" /></option>
                                        </c:forEach>
                                    </select>
                                    <div class="invalid-feedback" data-feedback-for="position"><c:out value="${fieldErrors['position']}" /></div>
                                </div>
<div class="ui-field">
                                    <label for="salary" class="form-label fw-semibold">Base Salary (VND)</label>
                                    <input id="salary" type="number" name="salary" class="form-control${not empty fieldErrors['salary'] ? ' is-invalid' : ''}" value="${fn:escapeXml(isPost ? param.salary : employee.salary)}" min="0" step="0.01">
                                    <div class="invalid-feedback" data-feedback-for="salary"><c:out value="${fieldErrors['salary']}" /></div>
                                </div>
<div class="ui-field">
                                    <label for="shift" class="form-label fw-semibold">Work Shift</label>
                                    <select id="shift" name="shift" class="form-select${not empty fieldErrors['shift'] ? ' is-invalid' : ''}">
                                        <option value="" <c:if test="${empty selectedShift}">selected</c:if>>Select work shift</option>
                                        <option value="Morning" <c:if test="${selectedShift eq 'Morning'}">selected</c:if>>Morning</option>
                                        <option value="Afternoon" <c:if test="${selectedShift eq 'Afternoon'}">selected</c:if>>Afternoon</option>
                                        <option value="Night" <c:if test="${selectedShift eq 'Night'}">selected</c:if>>Night</option>
                                    </select>
                                    <div class="invalid-feedback" data-feedback-for="shift"><c:out value="${fieldErrors['shift']}" /></div>
                                </div></div></div>
                            </div>
                <div class="ui-panel-footer ui-form-actions">
                                <a href="${pageContext.request.contextPath}/employee?action=list" class="ui-button ui-button-secondary">Back</a>
                                <button type="submit" class="ui-button ui-button-primary">Update Employee</button>
                            </div>
                        </form>
        </section>
    </div>
</layout:layout>
