<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Add New Employee" pageCss="employee.css" pageJs="employeeForm.js" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-shell-form ui-employee-workspace">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">STAFF DIRECTORY</p><h1>Add New Employee</h1><p class="ui-description">Enter staff details. Login credentials will be sent to the staff email address.</p></div></header>
<c:if test="${not empty flashError}">
                            <div class="alert alert-dismissible fade show ui-notice ui-notice-danger" role="alert">
                                <c:out value="${flashError}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
        <section class="ui-panel" aria-label="New employee information">
<form id="employeeForm" data-mode="add" action="${pageContext.request.contextPath}/employee" method="post" class="needs-validation" novalidate>
                <div class="ui-panel-body">
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">

                            <!-- Section: Personal Information -->
                            <div class="employee-form-section">
                                <h2 class="employee-section-title">
                                    <i class="fa-regular fa-user" aria-hidden="true"></i>Personal Information
                                </h2>
                                <div class="ui-form-grid">
                                    <div class="ui-field">
                                        <label for="employeeID" class="form-label fw-semibold text-muted">Employee ID</label>
                                        <div class="employee-control">

                                            <input id="employeeID" type="text" value="${fn:escapeXml(nextEmployeeID)}" class="form-control bg-light" readonly tabindex="-1" aria-describedby="employeeIDHelp">
                                        </div>
                                        <div id="employeeIDHelp" class="form-text">Assigned automatically.</div>
                                    </div>
                                    <div class="ui-field">
                                        <label for="fullName" class="form-label fw-semibold">Full Name <span class="ui-required">*</span></label>
                                        <div class="employee-control">

                                            <input id="fullName" type="text" name="name" value="${fn:escapeXml(param.name)}" class="form-control${not empty fieldErrors['name'] ? ' is-invalid' : ''}" placeholder="e.g. Nguyen Van A" maxlength="100" autocomplete="name" required>
                                        <div class="invalid-feedback" data-feedback-for="name"><c:out value="${fieldErrors['name']}" /></div>
                                        </div>
                                    </div>
                                    <div class="ui-field">
                                        <label for="phone" class="form-label fw-semibold">Phone Number <span class="ui-required">*</span></label>
                                        <div class="employee-control">

                                            <input id="phone" type="tel" name="phone" value="${fn:escapeXml(param.phone)}" class="form-control${not empty fieldErrors['phone'] ? ' is-invalid' : ''}" placeholder="e.g. 0901234567" maxlength="20" autocomplete="tel" required>
                                        <div class="invalid-feedback" data-feedback-for="phone"><c:out value="${fieldErrors['phone']}" /></div>
                                        </div>
                                    </div>
                                    <div class="ui-field">
                                        <label for="email" class="form-label fw-semibold">Email <span class="ui-required">*</span></label>
                                        <div class="employee-control">

                                            <input id="email" type="email" name="email" value="${fn:escapeXml(param.email)}" class="form-control${not empty fieldErrors['email'] ? ' is-invalid' : ''}" placeholder="e.g. employee@gmail.com" maxlength="100" autocomplete="email" required>
                                        <div class="invalid-feedback" data-feedback-for="email"><c:out value="${fieldErrors['email']}" /></div>
                                        </div>
                                    </div>
                                    <div class="ui-field ui-field-full">
                                        <label for="address" class="form-label fw-semibold">Address</label>
                                        <div class="employee-control">

                                            <input id="address" type="text" name="address" value="${fn:escapeXml(param.address)}" class="form-control${not empty fieldErrors['address'] ? ' is-invalid' : ''}" placeholder="e.g. Can Tho City" maxlength="200" autocomplete="street-address">
                                        <div class="invalid-feedback" data-feedback-for="address"><c:out value="${fieldErrors['address']}" /></div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Section: Employment Details -->
                            <div class="employee-form-section">
                                <h2 class="employee-section-title">
                                    <i class="fa-solid fa-briefcase" aria-hidden="true"></i>Employment Details
                                </h2>
                                <div class="ui-form-grid">
                                    <div class="ui-field">
                                        <label for="position" class="form-label fw-semibold">Position <span class="ui-required">*</span></label>
                                        <select id="position" name="position" class="form-select${not empty fieldErrors['position'] ? ' is-invalid' : ''}" required>
                                            <option value="" disabled <c:if test="${empty param.position}">selected</c:if>>Select position</option>
                                            <c:forEach var="pos" items="${positions}">
                                                <option value="${fn:escapeXml(pos)}" <c:if test="${param.position == pos}">selected</c:if>><c:out value="${pos}" /></option>
                                            </c:forEach>
                                        </select>
                                        <div class="invalid-feedback" data-feedback-for="position"><c:out value="${fieldErrors['position']}" /></div>
                                    </div>
                                    <div class="ui-field">
                                        <label for="salary" class="form-label fw-semibold">Base Salary (VND)</label>
                                        <div class="employee-control">

                                            <input id="salary" type="number" name="salary" value="${fn:escapeXml(param.salary)}" class="form-control${not empty fieldErrors['salary'] ? ' is-invalid' : ''}" placeholder="0.00" min="0" step="0.01">
                                        <div class="invalid-feedback" data-feedback-for="salary"><c:out value="${fieldErrors['salary']}" /></div>
                                        </div>
                                    </div>
                                    <div class="ui-field">
                                        <label for="shift" class="form-label fw-semibold">Work Shift</label>
                                        <select id="shift" name="shift" class="form-select${not empty fieldErrors['shift'] ? ' is-invalid' : ''}">
                                            <option value="" <c:if test="${empty param.shift}">selected</c:if>>Select work shift</option>
                                            <option value="Morning" <c:if test="${param.shift == 'Morning'}">selected</c:if>>Morning</option>
                                            <option value="Afternoon" <c:if test="${param.shift == 'Afternoon'}">selected</c:if>>Afternoon</option>
                                            <option value="Night" <c:if test="${param.shift == 'Night'}">selected</c:if>>Night</option>
                                        </select>
                                        <div class="invalid-feedback" data-feedback-for="shift"><c:out value="${fieldErrors['shift']}" /></div>
                                    </div>
                                </div>
                            </div>

                            <!-- Section: Account Credentials -->
                            <div class="employee-form-section">
                                <h2 class="employee-section-title">
                                    <i class="fa-solid fa-shield-halved" aria-hidden="true"></i>Login Credentials
                                </h2>
                                <div class="ui-form-grid">
                                    <div class="ui-field">
                                        <label for="username" class="form-label fw-semibold text-muted">Username</label>
                                        <div class="employee-control">

                                            <input id="username" type="text" value="${fn:escapeXml(nextUsername)}" class="form-control bg-light" readonly aria-describedby="usernameHelp">
                                        </div>
                                        <div id="usernameHelp" class="form-text">Generated from the Employee ID.</div>
                                    </div>
                                    <div class="ui-field">
                                        <div class="form-label fw-semibold">Temporary Password</div>
                                        <div class="employee-credentials-notice">
                                            Generated automatically and sent to the staff email address.
                                            The staff member must set a new password on first login.
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Form Actions -->
                            </div>
                <div class="ui-panel-footer ui-form-actions">
                                <a href="${pageContext.request.contextPath}/employee?action=list" class="ui-button ui-button-secondary">Cancel</a>
                                <button type="submit" class="ui-button ui-button-primary">Save Employee</button>
                            </div>
                        </form>
        </section>
    </div>
</layout:layout>
