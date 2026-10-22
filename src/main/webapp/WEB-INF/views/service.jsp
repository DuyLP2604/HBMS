<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Hotel Services" pageCss="service.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container-fluid px-lg-5 my-4">

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

        <%-- Booking Status Banner --%>
        <div class="card shadow-sm mb-4 border-0">
            <div class="card-body p-3 bg-white rounded">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <c:choose>
                            <c:when test="${not empty currentBooking}">
                                <div class="d-flex flex-wrap align-items-center gap-2">
                                    <span class="badge bg-primary fs-6 px-3 py-2"><i class="fa-solid fa-hotel me-1"></i>Đơn đặt phòng #${currentBooking.bookingID}</span>
                                    <span class="badge bg-secondary py-2 px-3">${currentBooking.bookingStatus}</span>
                                    <span class="text-muted ms-2">
                                        <i class="fa-regular fa-calendar-days me-1"></i>
                                        <fmt:formatDate value="${currentBooking.checkInDate}" pattern="dd/MM/yyyy" /> - <fmt:formatDate value="${currentBooking.checkOutDate}" pattern="dd/MM/yyyy" />
                                    </span>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <div class="text-warning-emphasis">
                                    <i class="fa-solid fa-circle-info me-2 text-warning fs-5"></i>
                                    <strong>Quý khách chưa có đơn đặt phòng nào đang hoạt động.</strong>
                                    <span class="text-muted ms-1">Vui lòng đặt phòng trước để sử dụng dịch vụ tại khách sạn.</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${empty currentBooking}">
                        <div class="col-md-4 text-md-end mt-2 mt-md-0">
                            <a href="${pageContext.request.contextPath}/" class="btn btn-primary btn-sm px-3">
                                <i class="fa-solid fa-calendar-plus me-1"></i>Đặt phòng ngay
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>


        <div class="alert alert-success border-success-subtle d-flex align-items-center mb-4 shadow-sm" role="alert">
            <i class="fa-solid fa-utensils fs-3 text-success me-3"></i>
            <div>
                <strong class="text-success">Đặc quyền lưu trú:</strong>
                <span>Mọi đơn đặt phòng tại HBMS mặc định đã được tặng kèm <strong>Buffet sáng miễn phí mỗi ngày</strong> cho toàn bộ khách lưu trú.</span>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-lg-5 col-md-6">
                <div class="card shadow-sm sticky-top" style="top: 20px; z-index: 1;">
                    <div class="card-header bg-primary text-white py-3">
                        <h2 class="h5 mb-0"><i class="fa-solid fa-cart-plus me-2"></i>Chọn dịch vụ khách sạn</h2>
                    </div>
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/service" method="post">
                            <c:if test="${not empty currentBooking}">
                                <input type="hidden" name="bookingID" value="${currentBooking.bookingID}">
                            </c:if>

                            <div class="table-responsive" style="max-height: 50vh; overflow-y: auto;">
                                <table class="table table-bordered table-hover align-middle mb-0">
                                    <thead class="table-light sticky-top" style="z-index: 0;">
                                        <tr>
                                            <th class="text-center" style="width: 15%">Chọn</th>
                                            <th style="width: 50%">Dịch vụ</th>
                                            <th style="width: 35%" class="text-end">Đơn giá</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        <c:forEach items="${list}" var="service">
                                            <c:set var="lowerName" value="${fn:toLowerCase(service.serviceName)}" />
                                            <c:set var="isBuffet" value="${fn:contains(lowerName, 'buffet') || fn:contains(lowerName, 'buffer') || fn:contains(lowerName, 'breakfast') || fn:contains(lowerName, 'sáng')}" />
                                            <c:set var="isFree" value="${service.unitPrice == 0 || isBuffet}" />

                                            <tr class="${isFree ? 'table-success bg-opacity-25' : ''}">
                                                <td class="text-center">
                                                    <c:choose>
                                                        <c:when test="${isFree}">
                                                            <span class="text-success fw-bold fs-5" title="Đã được tặng kèm theo phòng">✓</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <input class="form-check-input" type="radio" name="selectedService" value="${service.serviceID}" required ${empty currentBooking ? 'disabled' : ''}>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <strong class="${isFree ? 'text-success' : 'text-dark'}"><c:out value="${service.serviceName}" /></strong>
                                                    <c:if test="${isFree}">
                                                        <br><small class="text-success fst-italic"><i class="fa-solid fa-gift me-1"></i>Tặng kèm theo phòng</small>
                                                    </c:if>
                                                </td>
                                                <td class="text-end">
                                                    <c:choose>
                                                        <c:when test="${isFree}">
                                                            <span class="badge bg-success">Miễn phí</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="fw-semibold text-primary">
                                                                <fmt:formatNumber value="${service.unitPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                                                            </span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </tr>
                                        </c:forEach>
                                        <c:if test="${empty list}">
                                            <tr>
                                                <td colspan="3" class="text-center text-muted py-4">Hiện không có dịch vụ nào trong danh mục.</td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>

                            <c:if test="${not empty list}">
                                <div class="d-flex align-items-center justify-content-between mt-3 p-3 bg-light border rounded">
                                    <label for="quantity" class="form-label fw-bold mb-0">Số lượng:</label>
                                    <input id="quantity" class="form-control text-center w-25" type="number" name="quantity" value="1" min="1" max="99" required ${empty currentBooking ? 'disabled' : ''}>
                                </div>
                            </c:if>

                            <div class="d-flex justify-content-between mt-3">
                                <a href="${pageContext.request.contextPath}/" class="btn btn-secondary px-4">Quay lại</a>
                                <button type="submit" class="btn btn-primary px-4" ${empty list || empty currentBooking ? "disabled" : ""}>
                                    <i class="fa-solid fa-plus me-1"></i>Thêm dịch vụ
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-7 col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-dark text-white py-3 d-flex justify-content-between align-items-center">
                        <h2 class="h5 mb-0"><i class="fa-solid fa-clipboard-list me-2"></i>Dịch vụ đã đặt</h2>
                        <c:if test="${not empty currentBooking}">
                            <span class="badge bg-light text-dark">Phòng đơn #${currentBooking.bookingID}</span>
                        </c:if>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive" style="max-height: 80vh; overflow-y: auto;">
                            <table class="table table-hover table-bordered mb-0 align-middle text-center">
                                <thead class="table-light sticky-top" style="z-index: 0;">
                                    <tr>
                                        <th class="text-start">Tên dịch vụ</th>
                                        <th>Đơn giá</th>
                                        <th>Số lượng</th>
                                        <th class="text-end">Thành tiền</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:set var="totalServiceCharges" value="0" />
                                    <c:choose>
                                        <c:when test="${empty myServices}">
                                            <tr>
                                                <td colspan="4" class="text-center text-muted py-5">
                                                    <i class="fa-solid fa-bell-concierge fa-3x mb-3 text-secondary opacity-50"></i><br>
                                                    Chưa có dịch vụ bổ sung nào được thêm vào đơn đặt phòng này.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="bs" items="${myServices}">
                                                <c:set var="subtotal" value="${bs.unitPrice * bs.quantity}" />
                                                <c:set var="totalServiceCharges" value="${totalServiceCharges + subtotal}" />
                                                <tr>
                                                    <td class="text-start fw-bold">
                                                        <i class="fa-solid fa-check text-success me-1"></i>
                                                        <c:out value="${bs.service.serviceName}" />
                                                    </td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${bs.unitPrice == 0}">
                                                                <span class="text-success fw-bold">0 đ</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <fmt:formatNumber value="${bs.unitPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td><span class="badge bg-secondary-subtle text-dark px-2 py-1">x<c:out value="${bs.quantity}" /></span></td>
                                                    <td class="fw-bold text-danger text-end">
                                                        <fmt:formatNumber value="${subtotal}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                                <c:if test="${not empty myServices}">
                                    <tfoot class="table-light">
                                        <tr>
                                            <td colspan="3" class="text-end fw-bold">Tổng tiền dịch vụ:</td>
                                            <td class="fw-bold text-danger fs-6 text-end">
                                                <fmt:formatNumber value="${totalServiceCharges}" type="number" groupingUsed="true" maxFractionDigits="0" /> đ
                                            </td>
                                        </tr>
                                    </tfoot>
                                </c:if>
                            </table>
                        </div>
                    </div>
                </div>
            </div>

        </div>
    </div>
</layout:layout>
