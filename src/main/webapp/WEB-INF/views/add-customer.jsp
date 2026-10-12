<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Add New Customer" pageCss="customer.css" pageJs="addCustomer.js" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header bg-success text-white py-3">
                        <h2 class="h4 mb-0">Add New Customer</h2>
                    </div>
                    <div class="card-body p-4">
                        <form id="addCustomerForm" action="${pageContext.request.contextPath}/customer?action=add" method="post">
                            <input type="hidden" name="createAccount" value="true">
                            <div class="row g-3">
                                <div class="col-12">
                                    <label for="name" class="form-label fw-semibold">Full Name *</label>
                                    <input type="text" id="name" name="name" class="form-control" maxlength="100" placeholder="Enter full name..." required>
                                </div>
                                <div class="col-md-6">
                                    <label for="phone" class="form-label fw-semibold">Phone Number</label>
                                    <input type="tel" id="phone" name="phone" class="form-control" maxlength="15" placeholder="Enter phone number...">
                                </div>
                                <div class="col-md-6">
                                    <label for="email" class="form-label fw-semibold">Email</label>
                                    <input type="email" id="email" name="email" class="form-control" maxlength="100" placeholder="Enter email...">
                                </div>
                                <div class="col-12">
                                    <label for="address" class="form-label fw-semibold">Address</label>
                                    <input type="text" id="address" name="address" class="form-control" maxlength="200" placeholder="Enter address...">
                                </div>
                                <div class="col-12">
                                    <label for="nation" class="form-label fw-semibold">Nationality *</label>
                                    <select id="nation" name="nation" class="form-select" required>
                                        <option value="" disabled selected>Select nationality</option>
                                        <c:forEach items="${listNat}" var="n">
                                            <option value="${fn:escapeXml(n.nationalityID)}"><c:out value="${n.nationalityName}" /></option>
                                        </c:forEach>
                                    </select>
                                </div>
                                <div class="col-12">
                                    <hr class="my-2">
                                    <h3 class="h6 fw-bold mb-1">Login Account</h3>
                                    <p class="text-muted small mb-0">Create login credentials for this customer.</p>
                                </div>
                                <div class="col-md-6">
                                    <label for="username" class="form-label fw-semibold">Username *</label>
                                    <input type="text" id="username" name="username" class="form-control" maxlength="50" autocomplete="off" placeholder="Enter username..." required>
                                </div>
                                <div class="col-md-6">
                                    <label for="password" class="form-label fw-semibold">Password *</label>
                                    <input type="password" id="password" name="password" class="form-control" autocomplete="new-password" placeholder="Enter password..." required>
                                </div>
                            </div>
                            <div class="d-flex justify-content-end gap-2 mt-4">
                                <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-secondary">Cancel</a>
                                <button type="submit" class="btn btn-primary px-4">Save Customer</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>