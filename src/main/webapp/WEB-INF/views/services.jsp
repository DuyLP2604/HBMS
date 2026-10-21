<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Service Management" pageCss="service.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <%-- Header & Điều hướng --%>
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
                <h1 class="h3 mb-1"><i class="fa-solid fa-bell-concierge text-warning me-2"></i>Service Management</h1>
                <p class="text-muted mb-0">Quản lý danh mục dịch vụ của khách sạn: xem danh sách, cập nhật đơn giá, tạo mới hoặc xóa dịch vụ.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-outline-secondary">
                    <i class="fa-solid fa-arrow-left me-1"></i>Dashboard
                </a><c:if test="${sessionScope.user.role == 'Admin'}">
                    <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#createServiceModal">
                        <i class="fa-solid fa-plus me-1"></i>Tạo Service mới
                    </button>
                </c:if>
            </div>
        </div>

        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success alert-dismissible py-2 mb-3 fade show shadow-sm">
                <i class="fa-solid fa-circle-check me-2"></i><c:out value="${sessionScope.successMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="successMessage" scope="session" />
        </c:if>
        <c:if test="${not empty sessionScope.errorMessage}">
            <div class="alert alert-danger alert-dismissible py-2 mb-3 fade show shadow-sm">
                <i class="fa-solid fa-triangle-exclamation me-2"></i><c:out value="${sessionScope.errorMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="errorMessage" scope="session" />
        </c:if>

        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3 d-flex justify-content-between align-items-center border-bottom">
                <h2 class="h5 mb-0 fw-bold">Danh sách dịch vụ (${services.size()})</h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover table-bordered mb-0 align-middle">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 15%" class="text-center">Mã dịch vụ</th>
                                <th style="width: 50%">Tên dịch vụ</th>
                                <th style="width: 20%" class="text-end">Đơn giá</th>
                                    <c:if test="${sessionScope.user.role == 'Admin'}">
                                    <th style="width: 15%" class="text-center">Thao tác</th>
                                    </c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${services}">
                                <tr>
                                    <td class="text-center fw-bold"><code>${s.serviceID}</code></td>
                                    <td><strong>${s.serviceName}</strong></td>
                                    <td class="text-end text-primary fw-bold">
                                        <fmt:formatNumber value="${s.unitPrice}" type="number" groupingUsed="true" /> đ
                                    </td>
                                    <%-- CHỈ ADMIN MỚI CÓ NÚT SỬA VÀ XÓA --%>
                                    <c:if test="${sessionScope.user.role == 'Admin'}">
                                        <td class="text-center">
                                            <button type="button" class="btn btn-outline-primary btn-sm me-1"
                                                    data-bs-toggle="modal" data-bs-target="#editPriceModal_${s.serviceID}">
                                                <i class="fa-solid fa-pen-to-square me-1"></i>Sửa giá
                                            </button>
                                            <button type="button" class="btn btn-outline-danger btn-sm"
                                                    data-bs-toggle="modal" data-bs-target="#deleteServiceModal_${s.serviceID}">
                                                <i class="fa-solid fa-trash-can me-1"></i>Xóa
                                            </button>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="createServiceModal" tabindex="-1" aria-hidden="true">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/service" method="post">
                    <input type="hidden" name="action" value="create">
                    <div class="modal-header bg-primary text-white">
                        <h5 class="modal-title"><i class="fa-solid fa-plus me-2"></i>Tạo dịch vụ mới</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="createServiceName" class="form-label fw-bold">Tên dịch vụ <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="createServiceName" name="serviceName" maxlength="60" required placeholder="VD: Spa chăm sóc da, Thuê xe tự lái...">
                        </div>
                        <div class="mb-3">
                            <label for="createUnitPrice" class="form-label fw-bold">Đơn giá (VND) <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" id="createUnitPrice" name="unitPrice" min="0" step="1000" required placeholder="VD: 150000 (nhập 0 nếu miễn phí)">
                            <div class="form-text">Nhập 0 nếu dịch vụ này là dịch vụ miễn phí kèm theo lưu trú.</div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                        <button type="submit" class="btn btn-primary"><i class="fa-solid fa-check me-1"></i>Lưu dịch vụ</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
</layout:layout>
