<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Shows details about one physical room. --%>
<layout:layout title="Room Details" pageCss="room.css" useBootstrap="true" bodyClass="hbms-ui-page room-management-page">
    <div class="hbms-ui ui-shell room-workspace">
        <div class="room-detail">
            <div class="room-page-header">
                <h1><i class="fa-solid fa-door-open me-2"></i>Room Details: <c:out value="${room.roomNumber}" /></h1>
                <div class="room-detail-identifiers">
                    <span class="room-id-label">ID: <c:out value="${room.roomID}" /></span>
                    <c:choose>
                        <c:when test="${room.status eq 'Available' or room.status eq 'ACTIVE'}">
                            <span class="room-status room-status-active"><c:out value="${room.status}"/></span>
                        </c:when>
                        <c:when test="${room.status eq 'Occupied'}">
                            <span class="room-status room-status-warning">Occupied</span>
                        </c:when>
                        <c:when test="${room.status eq 'MAINTENANCE'}">
                            <span class="room-status room-status-warning">Maintenance</span>
                        </c:when>
                        <c:otherwise>
                            <span class="room-status"><c:out value="${room.status}" /></span>
                        </c:otherwise>
                    </c:choose>
                </div>
            </div>
            <div class="room-panel room-detail-panel">
                <div class="room-detail-grid">
                    <!-- Hình ảnh phòng -->
                    <div class="room-detail-media">
                        <c:choose>
                            <c:when test="${not empty room.roomImage}">
                                <div class="position-relative">
                                    <img src="${pageContext.request.contextPath}/assets/images/room/${room.roomImage}"
                                         alt="Room ${room.roomNumber}" class="room-detail-image">
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="room-detail-image room-detail-placeholder">
                                    <i class="fa-solid fa-image fa-5x text-secondary opacity-25"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Thông tin chi tiết -->
                    <div class="room-detail-content">
                        <h3 class="room-detail-subtitle">Room Information</h3>
                        <table class="table room-detail-facts mb-0">
                            <tbody>
                                <tr>
                                    <th class="text-muted py-3"><i class="fa-solid fa-tag me-2"></i>Room Type:</th>
                                    <td class="fw-bold text-dark py-3"><c:out value="${room.roomTypeID.typeName}" /></td>
                                </tr>
                                <tr class="border-top border-light">
                                    <th class="text-muted py-3"><i class="fa-solid fa-users me-2"></i>Capacity:</th>
                                    <td class="py-3"><span class="room-status"><c:out value="${room.roomTypeID.capacity}" /> Guests</span></td>
                                </tr>
                                <tr class="border-top border-light">
                                    <th class="text-muted py-3"><i class="fa-solid fa-money-bill-wave me-2"></i>Price:</th>
                                    <td class="room-detail-price py-3">
                                        <fmt:formatNumber value="${room.roomTypeID.price}" type="number" maxFractionDigits="0"/> <span class="fs-6 text-muted fw-normal">VND / night</span>
                                    </td>
                                </tr>
                            </tbody>
                        </table>

                        <div class="room-description-panel">
                            <h6 class="fw-bold text-primary mb-2"><i class="fa-solid fa-align-left me-2"></i>Description</h6>
                            <p class="text-muted mb-0 lh-lg"><c:out value="${room.roomTypeID.description}" /></p>
                        </div>
                    </div>
                </div>

                <!-- Nhóm Nút Hành động -->
                <div class="room-form-actions">
                    <c:if test="${sessionScope.user.role eq 'Admin'}">
                        <button type="button" class="btn btn-outline-danger px-4 shadow-sm fw-bold" data-bs-toggle="modal" data-bs-target="#deleteDetailModal">
                            <i class="fa-solid fa-trash me-2"></i>Delete Room
                        </button>
                        <a href="${pageContext.request.contextPath}/room?action=update&id=${room.roomID}" class="btn room-button-primary px-4 shadow-sm fw-bold text-dark">
                            <i class="fa-solid fa-pen me-2"></i>Edit Room
                        </a>
                    </c:if>
                    <a href="${pageContext.request.contextPath}/room?action=list" class="btn room-button-secondary px-4 shadow-sm fw-bold">
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
                        <button type="button" class="btn room-button-secondary px-4 fw-bold" data-bs-dismiss="modal">Cancel</button>
                        <a href="${pageContext.request.contextPath}/room?action=delete&id=${room.roomID}" class="btn btn-danger px-5 fw-bold shadow-sm">Delete</a>
                    </div>
                </div>
            </div>
        </div>
    </c:if>
</layout:layout>
