<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Room Details" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="card shadow-lg border-0 rounded-4 overflow-hidden">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3 border-0">
                <h2 class="h4 mb-0 fw-bold"><i class="fa-solid fa-door-open me-2"></i>Room Details: <c:out value="${room.roomNumber}" /></h2>
                <span class="badge bg-white text-primary fs-6 shadow-sm">ID: <c:out value="${room.roomID}" /></span>
            </div>
            <div class="card-body p-5 bg-white">
                <div class="row g-5">
                    <!-- Hình ảnh phòng -->
                    <div class="col-lg-5 text-center">
                        <c:choose>
                            <c:when test="${not empty room.roomImage}">
                                <div class="position-relative">
                                    <img src="${pageContext.request.contextPath}/assets/images/room/${room.roomImage}"
                                         alt="Room ${room.roomNumber}" class="img-fluid rounded-3 shadow" style="width: 100%; max-height: 380px; object-fit: cover;">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="bg-light rounded-3 d-flex align-items-center justify-content-center border shadow-sm" style="height: 380px;">
                                    <i class="fa-solid fa-image fa-5x text-secondary opacity-25"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Thông tin chi tiết -->
                    <div class="col-lg-7 d-flex flex-column justify-content-center">
                        <h3 class="fw-bold text-dark mb-4 border-bottom pb-2">Room Information</h3>
                        <table class="table table-borderless fs-5 mb-0">
                            <tbody>
                                <tr>
                                    <th class="text-muted w-40 py-3"><i class="fa-solid fa-tag me-2"></i>Room Type:</th>
                                    <td class="fw-bold text-dark py-3"><c:out value="${room.roomTypeID.typeName}" /></td>
                                </tr>
                                <tr class="border-top border-light">
                                    <th class="text-muted py-3"><i class="fa-solid fa-users me-2"></i>Capacity:</th>
                                    <td class="py-3"><span class="badge bg-info text-dark fs-6 shadow-sm"><c:out value="${room.roomTypeID.capacity}" /> Guests</span></td>
                                </tr>
                                <tr class="border-top border-light">
                                    <th class="text-muted py-3"><i class="fa-solid fa-money-bill-wave me-2"></i>Price:</th>
                                    <td class="text-danger fw-bold fs-4 py-3">
                                        <fmt:formatNumber value="${room.roomTypeID.price}" type="number" maxFractionDigits="0"/> <span class="fs-6 text-muted fw-normal">VND / night</span>
                                    </td>
                                </tr>
                                <tr class="border-top border-light">
                                    <th class="text-muted py-3"><i class="fa-solid fa-circle-info me-2"></i>Status:</th>
                                    <td class="py-3">
                                        <c:choose>
                                            <c:when test="${room.status eq 'Available' or room.status eq 'ACTIVE'}">
                                                <span class="badge bg-success shadow-sm px-3 py-2 fs-6 text-uppercase"><c:out value="${room.status}"/></span>
                                            </c:when>
                                            <c:when test="${room.status eq 'Occupied'}">
                                                <span class="badge bg-warning text-dark shadow-sm px-3 py-2 fs-6 text-uppercase">Occupied</span>
                                            </c:when>
                                            <c:when test="${room.status eq 'MAINTENANCE'}">
                                                <span class="badge bg-warning text-dark shadow-sm px-3 py-2 fs-6 text-uppercase">Maintenance</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-secondary shadow-sm px-3 py-2 fs-6 text-uppercase"><c:out value="${room.status}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="mt-4 p-4 bg-light rounded-3 shadow-sm border">
                            <h6 class="fw-bold text-primary mb-2"><i class="fa-solid fa-align-left me-2"></i>Description</h6>
                            <p class="text-muted mb-0 lh-lg"><c:out value="${room.roomTypeID.description}" /></p>
                        </div>
                    </div>
                </div>

                <!-- Nhóm Nút Hành động -->
                <div class="d-flex justify-content-end gap-2 mt-5 border-top pt-4">
                    <c:if test="${sessionScope.user.role eq 'Admin'}">
                        <button type="button" class="btn btn-outline-danger px-4 shadow-sm fw-bold" data-bs-toggle="modal" data-bs-target="#deleteDetailModal">
                            <i class="fa-solid fa-trash me-2"></i>Delete Room
                        </button>
                        <a href="${pageContext.request.contextPath}/room?action=update&id=${room.roomID}" class="btn btn-warning px-4 shadow-sm fw-bold text-dark">
                            <i class="fa-solid fa-pen me-2"></i>Edit Room
                        </a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/room?action=list" class="btn btn-secondary px-4 shadow-sm fw-bold">
                        <i class="fa-solid fa-arrow-left me-2"></i>Back to List
                    </a>
                </div>
            </div>
        </div>
    </div>

    <!-- Modal Xác nhận Xóa (Chỉ Render ra HTML cho Admin) -->
    <c:if test="${sessionScope.user.role eq 'Admin'}">
        <div class="modal fade" id="deleteDetailModal" tabindex="-1" aria-hidden="true">
            <div class="modal-dialog modal-dialog-centered">
                <div class="modal-content border-0 shadow-lg">
                    <div class="modal-header bg-danger text-white border-0">
                        <h5 class="modal-title fw-bold"><i class="fa-solid fa-triangle-exclamation me-2"></i>Confirm Delete</h5>
                        <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body text-center py-5">
                        <h5 class="mb-3">Are you sure you want to delete this room?</h5>
                        <h3 class="text-primary fw-bold mb-4">Room: <c:out value="${room.roomNumber}" /></h3>
                        <c:choose>
                            <c:when test="${not empty room.roomImage}">
                                <img src="${pageContext.request.contextPath}/assets/images/room/${room.roomImage}" class="img-thumbnail shadow-sm rounded" style="width: 200px; height: 130px; object-fit: cover;">
                            </c:when>
                            <c:otherwise>
                                <div class="bg-light rounded d-inline-flex align-items-center justify-content-center img-thumbnail shadow-sm" style="width: 200px; height: 130px;">
                                    <i class="fa-solid fa-image fa-3x text-secondary opacity-50"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                        <p class="text-muted mt-4 mb-0 small">This action cannot be undone.</p>
                    </div>
                    <div class="modal-footer justify-content-center bg-light border-0 py-3">
                        <button type="button" class="btn btn-outline-secondary px-4 fw-bold" data-bs-dismiss="modal">Cancel</button>
                        <a href="${pageContext.request.contextPath}/room?action=delete&id=${room.roomID}" class="btn btn-danger px-5 fw-bold shadow-sm">Delete</a>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</layout:layout>