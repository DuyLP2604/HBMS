<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Add New Employee"
    pageCss="employee.css"
    pageJs="employeeForm.js"
    useBootstrap="true"
    bodyClass="bg-light">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-lg-9 col-xl-8">
                <div class="card border-0 shadow-lg rounded-4 overflow-hidden">
                    <div class="card-header employee-header bg-gradient text-white p-4">
                        <div class="d-flex align-items-center">
                            <div class="bg-white bg-opacity-25 p-3 rounded-3 me-3">
                                <i class="bi bi-person-plus-fill fs-3"></i>
                            </div>
                            <div>
                                <h2 class="h4 fw-bold mb-1">Add New Employee</h2>
                                <p class="mb-0 text-white-50 small">Enter staff details. Login credentials will be sent to the staff email address.</p>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-4 p-md-5 bg-white">
                        <c:if test="${not empty flashError}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                <c:out value="${flashError}" />
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        <form id="employeeForm" data-mode="add" action="${pageContext.request.contextPath}/employee" method="post" class="needs-validation" novalidate>
                            <input type="hidden" name="action" value="add">
                            <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                            
                            <!-- Section: Personal Information -->
                            <div class="mb-4">
                                <h5 class="text-secondary fw-bold text-uppercase fs-7 mb-3 pb-2 border-bottom">
                                    <i class="bi bi-person-badge me-2"></i>Personal Information
                                </h5>
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label for="employeeID" class="form-label fw-semibold text-muted">Employee ID</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-hash text-muted"></i></span>
                                            <input id="employeeID" type="text" value="${fn:escapeXml(nextEmployeeID)}" class="form-control bg-light border-start-0 ps-0" readonly tabindex="-1" aria-describedby="employeeIDHelp">
                                        </div>
                                        <div id="employeeIDHelp" class="form-text">Assigned automatically.</div>
                                    </div>
                                    <div class="col-md-8">
                                        <label for="fullName" class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-person text-muted"></i></span>
                                            <input id="fullName" type="text" name="name" value="${fn:escapeXml(param.name)}" class="form-control border-start-0 ps-0${not empty fieldErrors['name'] ? ' is-invalid' : ''}" placeholder="e.g. Nguyen Van A" maxlength="100" autocomplete="name" required>
                                        <div class="invalid-feedback" data-feedback-for="name"><c:out value="${fieldErrors['name']}" /></div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="phone" class="form-label fw-semibold">Phone Number <span class="text-danger">*</span></label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-telephone text-muted"></i></span>
                                            <input id="phone" type="tel" name="phone" value="${fn:escapeXml(param.phone)}" class="form-control border-start-0 ps-0${not empty fieldErrors['phone'] ? ' is-invalid' : ''}" placeholder="e.g. 0901234567" maxlength="20" autocomplete="tel" required>
                                        <div class="invalid-feedback" data-feedback-for="phone"><c:out value="${fieldErrors['phone']}" /></div>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="email" class="form-label fw-semibold">Email <span class="text-danger">*</span></label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-envelope text-muted"></i></span>
                                            <input id="email" type="email" name="email" value="${fn:escapeXml(param.email)}" class="form-control border-start-0 ps-0${not empty fieldErrors['email'] ? ' is-invalid' : ''}" placeholder="e.g. employee@gmail.com" maxlength="100" autocomplete="email" required>
                                        <div class="invalid-feedback" data-feedback-for="email"><c:out value="${fieldErrors['email']}" /></div>
                                        </div>
                                    </div>
                                    <div class="col-12">
                                        <label for="address" class="form-label fw-semibold">Address</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-geo-alt text-muted"></i></span>
                                            <input id="address" type="text" name="address" value="${fn:escapeXml(param.address)}" class="form-control border-start-0 ps-0${not empty fieldErrors['address'] ? ' is-invalid' : ''}" placeholder="e.g. Can Tho City" maxlength="200" autocomplete="street-address">
                                        <div class="invalid-feedback" data-feedback-for="address"><c:out value="${fieldErrors['address']}" /></div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Section: Employment Details -->
                            <div class="mb-4">
                                <h5 class="text-secondary fw-bold text-uppercase fs-7 mb-3 pb-2 border-bottom">
                                    <i class="bi bi-briefcase me-2"></i>Employment Details
                                </h5>
                                <div class="row g-3">
                                    <div class="col-md-4">
                                        <label for="position" class="form-label fw-semibold">Position <span class="text-danger">*</span></label>
                                        <select id="position" name="position" class="form-select${not empty fieldErrors['position'] ? ' is-invalid' : ''}" required>
                                            <option value="" disabled <c:if test="${empty param.position}">selected</c:if>>Select position</option>
                                            <c:forEach var="pos" items="${positions}">
                                                <option value="${fn:escapeXml(pos)}" <c:if test="${param.position == pos}">selected</c:if>><c:out value="${pos}" /></option>
                                            </c:forEach>
                                        </select>
                                        <div class="invalid-feedback" data-feedback-for="position"><c:out value="${fieldErrors['position']}" /></div>
                                    </div>
                                    <div class="col-md-4">
                                        <label for="salary" class="form-label fw-semibold">Base Salary (VND)</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0">₫</span>
                                            <input id="salary" type="number" name="salary" value="${fn:escapeXml(param.salary)}" class="form-control border-start-0 ps-0${not empty fieldErrors['salary'] ? ' is-invalid' : ''}" placeholder="0.00" min="0" step="0.01">
                                        <div class="invalid-feedback" data-feedback-for="salary"><c:out value="${fieldErrors['salary']}" /></div>
                                        </div>
                                    </div>
                                    <div class="col-md-4">
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
                            <div class="mb-4">
                                <h5 class="text-secondary fw-bold text-uppercase fs-7 mb-3 pb-2 border-bottom">
                                    <i class="bi bi-shield-lock me-2"></i>Login Credentials
                                </h5>
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label for="username" class="form-label fw-semibold text-muted">Username</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-at text-muted"></i></span>
                                            <input id="username" type="text" value="${fn:escapeXml(nextUsername)}" class="form-control bg-light border-start-0 ps-0" readonly aria-describedby="usernameHelp">
                                        </div>
                                        <div id="usernameHelp" class="form-text">Generated from the Employee ID.</div>
                                    </div>
                                    <div class="col-md-6">
                                        <div class="form-label fw-semibold">Temporary Password</div>
                                        <div class="alert alert-info mb-0 small">
                                            Generated automatically and sent to the staff email address.
                                            The staff member must set a new password on first login.
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <!-- Form Actions -->
                            <div class="d-flex align-items-center justify-content-end gap-3 mt-4 pt-3 border-top">
                                <a href="${pageContext.request.contextPath}/employee?action=list" class="btn btn-light px-4 py-2 border">Cancel</a>
                                <button type="submit" class="btn employee-btn text-white px-5 py-2 fw-semibold shadow-sm">Save Employee</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>