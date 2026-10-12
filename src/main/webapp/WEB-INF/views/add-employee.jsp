<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Add New Employee"
    pageCss="employee.css"
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
                                <p class="mb-0 text-white-50 small">Enter staff member details and login credentials below.</p>
                            </div>
                        </div>
                    </div>
                    <div class="card-body p-4 p-md-5 bg-white">
                        <c:if test="${not empty flashError}">
                            <div class="alert alert-danger alert-dismissible fade show" role="alert">
                                ${flashError}
                                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
                            </div>
                        </c:if>
                        <form action="${pageContext.request.contextPath}/employee" method="post" class="needs-validation" novalidate>
                            <input type="hidden" name="action" value="add">
                            
                            <!-- Section: Personal Information -->
                            <div class="mb-4">
                                <h5 class="text-secondary fw-bold text-uppercase fs-7 mb-3 pb-2 border-bottom">
                                    <i class="bi bi-person-badge me-2"></i>Personal Information
                                </h5>
                                <div class="row g-3">
                                    <div class="col-12">
                                        <label for="fullName" class="form-label fw-semibold">Full Name <span class="text-danger">*</span></label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-person text-muted"></i></span>
                                            <input id="fullName" type="text" name="name" value="${param.name}" class="form-control border-start-0 ps-0" placeholder="e.g. Nguyen Van A" maxlength="100" autocomplete="name" required>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="phone" class="form-label fw-semibold">Phone Number</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-telephone text-muted"></i></span>
                                            <input id="phone" type="tel" name="phone" value="${param.phone}" class="form-control border-start-0 ps-0" placeholder="e.g. 0901234567" maxlength="20" autocomplete="tel">
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="address" class="form-label fw-semibold">Address</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-geo-alt text-muted"></i></span>
                                            <input id="address" type="text" name="address" value="${param.address}" class="form-control border-start-0 ps-0" placeholder="e.g. Can Tho City" maxlength="200" autocomplete="street-address">
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
                                        <select id="position" name="position" class="form-select" required>
                                            <option value="" disabled <c:if test="${empty param.position}">selected</c:if>>Select position</option>
                                            <c:forEach var="pos" items="${positions}">
                                                <option value="${pos}" <c:if test="${param.position == pos}">selected</c:if>>${pos}</option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <div class="col-md-4">
                                        <label for="salary" class="form-label fw-semibold">Base Salary (VND)</label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0">₫</span>
                                            <input id="salary" type="number" name="salary" value="${param.salary}" class="form-control border-start-0 ps-0" placeholder="0.00" min="0" step="0.01">
                                        </div>
                                    </div>
                                    <div class="col-md-4">
                                        <label for="shift" class="form-label fw-semibold">Work Shift</label>
                                        <select id="shift" name="shift" class="form-select">
                                            <option value="" <c:if test="${empty param.shift}">selected</c:if>>Select work shift</option>
                                            <option value="Morning" <c:if test="${param.shift == 'Morning'}">selected</c:if>>Morning</option>
                                            <option value="Afternoon" <c:if test="${param.shift == 'Afternoon'}">selected</c:if>>Afternoon</option>
                                            <option value="Night" <c:if test="${param.shift == 'Night'}">selected</c:if>>Night</option>
                                        </select>
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
                                        <label for="username" class="form-label fw-semibold">Username <span class="text-danger">*</span></label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-at text-muted"></i></span>
                                            <input id="username" type="text" name="username" value="${param.username}" class="form-control border-start-0 ps-0" placeholder="Choose a username" maxlength="50" autocomplete="username" required>
                                        </div>
                                    </div>
                                    <div class="col-md-6">
                                        <label for="password" class="form-label fw-semibold">Password <span class="text-danger">*</span></label>
                                        <div class="input-group">
                                            <span class="input-group-text bg-light border-end-0"><i class="bi bi-key text-muted"></i></span>
                                            <input id="password" type="password" name="password" value="${param.password}" class="form-control border-start-0 ps-0" placeholder="At least 6 characters" minlength="6" maxlength="255" autocomplete="new-password" required>
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