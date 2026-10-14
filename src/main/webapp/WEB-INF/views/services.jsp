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
                </a>
                <button type="button" class="btn btn-primary" data-bs-toggle="modal" data-bs-target="#createServiceModal">
                    <i class="fa-solid fa-plus me-1"></i>Tạo Service mới
                </button>
            </div>
        </div>

        <%-- Thông báo kết quả --%>
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

        <%-- Bảng danh sách dịch vụ --%>
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
                                <th style="width: 45%">Tên dịch vụ</th>
                                <th style="width: 20%" class="text-end">Đơn giá</th>
                                <th style="width: 20%" class="text-center">Thao tác</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="s" items="${services}">
                                <tr>
                                    <td class="text-center fw-bold text-secondary">
                                        <code><c:out value="${s.serviceID}" /></code>
                                    </td>
                                    <td>
                                        <strong class="text-dark"><c:out value="${s.serviceName}" /></strong>
                                    </td>
                                    <td class="text-end">
                                        <c:choose>
                                            <c:when test="${s.unitPrice == 0}">
                                                <span class="badge bg-success">Miễn phí (0 đ)</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="fw-bold text-primary">
                                                    <fmt:formatNumber value="${s.unitPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <%-- Chức năng 1: Sửa / Update giá --%>
                                        <button type="button" 
                                                class="btn btn-outline-primary btn-sm me-1"
                                                data-bs-toggle="modal" 
                                                data-bs-target="#editPriceModal_${s.serviceID}"
                                                title="Cập nhật giá">
                                            <i class="fa-solid fa-pen-to-square me-1"></i>Sửa giá
                                        </button>

                                        <%-- Chức năng 2: Xóa dịch vụ --%>
                                        <button type="button" 
                                                class="btn btn-outline-danger btn-sm"
                                                data-bs-toggle="modal" 
                                                data-bs-target="#deleteServiceModal_${s.serviceID}"
                                                title="Xóa dịch vụ">
                                            <i class="fa-solid fa-trash-can me-1"></i>Xóa
                                        </button>
                                    </td>
                                </tr>

                                <%-- MODAL SỬA GIÁ TỪNG DÒNG (Không cần JavaScript) --%>
                                <div class="modal fade" id="editPriceModal_${s.serviceID}" tabindex="-1" aria-hidden="true">
                                    <div class="modal-dialog">
                                        <div class="modal-content">
                                            <form action="${pageContext.request.contextPath}/service" method="post">
                                                <input type="hidden" name="action" value="updatePrice">
                                                <input type="hidden" name="serviceID" value="${s.serviceID}">
                                                <div class="modal-header bg-warning text-dark">
                                                    <h5 class="modal-title"><i class="fa-solid fa-pen-to-square me-2"></i>Cập nhật đơn giá</h5>
                                                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                                </div>
                                                <div class="modal-body text-start">
                                                    <div class="mb-3">
                                                        <label class="form-label fw-bold">Mã dịch vụ</label>
                                                        <input type="text" class="form-control bg-light" value="${s.serviceID}" readonly>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label fw-bold">Tên dịch vụ</label>
                                                        <input type="text" class="form-control bg-light" value="${s.serviceName}" readonly>
                                                    </div>
                                                    <div class="mb-3">
                                                        <label class="form-label fw-bold">Đơn giá mới (VND) <span class="text-danger">*</span></label>
                                                        <input type="number" class="form-control" name="unitPrice" value="${s.unitPrice}" min="0" step="1000" required>
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                                                    <button type="submit" class="btn btn-warning fw-semibold"><i class="fa-solid fa-check me-1"></i>Cập nhật giá</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>

                                <%-- MODAL XÓA DỊCH VỤ TỪNG DÒNG (Không cần JavaScript) --%>
                                <div class="modal fade" id="deleteServiceModal_${s.serviceID}" tabindex="-1" aria-hidden="true">
                                    <div class="modal-dialog">
                                        <div class="modal-content">
                                            <form action="${pageContext.request.contextPath}/service" method="post">
                                                <input type="hidden" name="action" value="delete">
                                                <input type="hidden" name="serviceID" value="${s.serviceID}">
                                                <div class="modal-header bg-danger text-white">
                                                    <h5 class="modal-title"><i class="fa-solid fa-triangle-exclamation me-2"></i>Xác nhận xóa</h5>
                                                    <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                                </div>
                                                <div class="modal-body text-start">
                                                    <p>Bạn có chắc chắn muốn xóa dịch vụ: <strong class="text-danger"><c:out value="${s.serviceName}" /> (<code><c:out value="${s.serviceID}" /></code>)</strong>?</p>
                                                    <div class="alert alert-warning py-2 mb-0 small">
                                                        <i class="fa-solid fa-circle-exclamation me-1"></i>
                                                        Lưu ý: Chỉ dịch vụ chưa từng phát sinh trong đơn đặt phòng nào mới có thể xóa.
                                                    </div>
                                                </div>
                                                <div class="modal-footer">
                                                    <button type="button" class="btn btn-secondary" data-bs-dismiss="modal">Hủy</button>
                                                    <button type="submit" class="btn btn-danger"><i class="fa-solid fa-trash-can me-1"></i>Xóa dịch vụ</button>
                                                </div>
                                            </form>
                                        </div>
                                    </div>
                                </div>
                            </c:forEach>

                            <c:if test="${empty services}">
                                <tr>
                                    <td colspan="4" class="text-center text-muted py-5">
                                        <i class="fa-solid fa-inbox fa-3x mb-3 text-secondary opacity-50"></i><br>
                                        Chưa có dịch vụ nào trong hệ thống. Hãy bấm <strong>"Tạo Service mới"</strong> để thêm dịch vụ.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <%-- MODAL: TẠO SERVICE MỚI (Không cần JavaScript) --%>
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
