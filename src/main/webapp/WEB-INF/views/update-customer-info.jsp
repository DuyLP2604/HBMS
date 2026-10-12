<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Update Customer Information" pageCss="customer.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm">
                    <div class="card-header bg-warning text-dark py-3">
                        <h2 class="h4 mb-0 fw-semibold">Update Customer Information</h2>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/customer?action=update" method="post">
                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label for="customerID" class="form-label fw-semibold text-muted">Customer ID (Read-only)</label>
                                    <input type="text" id="customerID" name="id" class="form-control bg-light" value="${fn:escapeXml(customer.customerID)}" readonly>
                                </div>
                                <div class="col-md-6">
                                    <label for="name" class="form-label fw-semibold">Full Name *</label>
                                    <input type="text" id="name" name="name" class="form-control" value="${fn:escapeXml(customer.fullName)}" maxlength="100" required>
                                </div>
                                <div class="col-md-6">
                                    <label for="phone" class="form-label fw-semibold">Phone Number</label>
                                    <input type="tel" id="phone" name="phone" class="form-control" value="${fn:escapeXml(customer.phone)}" maxlength="15">
                                </div>
                                <div class="col-md-6">
                                    <label for="email" class="form-label fw-semibold">Email</label>
                                    <input type="email" id="email" name="email" class="form-control" value="${fn:escapeXml(customer.email)}" maxlength="100">
                                </div>
                                <div class="col-12">
                                    <label for="address" class="form-label fw-semibold">Address</label>
                                    <input type="text" id="address" name="address" class="form-control" value="${fn:escapeXml(customer.address)}" maxlength="200">
                                </div>
                                <div class="col-12">
                                    <label for="nation" class="form-label fw-semibold">Nationality *</label>
                                    <select id="nation" name="nation" class="form-select" required>
                                        <option value="" disabled ${empty customer.nationalityID ? 'selected' : ''}>Select nationality</option>
                                        <c:forEach var="nat" items="${listNat}">
                                            <option value="${fn:escapeXml(nat.nationalityID)}" ${nat.nationalityID eq customer.nationalityID.nationalityID ? 'selected' : ''}><c:out value="${nat.nationalityName}" /></option>
                                        </c:forEach>
                                    </select>
                                </div>
                            </div>
                            <div class="d-flex justify-content-end gap-2 mt-4">
                                <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-secondary">Back</a>
                                <button type="submit" class="btn btn-warning px-4">Update Customer</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>