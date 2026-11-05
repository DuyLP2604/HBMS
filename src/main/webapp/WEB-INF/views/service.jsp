<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Lets a guest choose a hotel service and quantity for a booking. --%>
<layout:layout title="Dịch vụ khách sạn" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace">
        <header class="g5-heading">
            <div>
                <h1>Dịch vụ khách sạn</h1>
                <p class="ui-description">Đặt dịch vụ bổ sung và theo dõi trạng thái phục vụ.</p>
            </div>
        </header>

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

        <div class="card g5-panel mb-4">
            <div class="card-body p-3 bg-white rounded">
                <div class="row align-items-center">
                    <div class="col-md-8">
                        <c:choose>
                            <c:when test="${not empty currentBooking}">
                                <div class="d-flex flex-wrap align-items-center gap-2">
                                    <span class="badge g5-status-neutral px-3 py-2"><i class="fa-solid fa-hotel me-1"></i>Đơn đặt phòng #${currentBooking.bookingID}</span>
                                    <span class="badge g5-status-neutral py-2 px-3">${currentBooking.bookingStatus}</span>
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
                            <a href="${pageContext.request.contextPath}/" class="btn g5-primary btn-sm px-3">
                                <i class="fa-solid fa-calendar-plus me-1"></i>Đặt phòng ngay
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>


        <div class="g5-service-notice mb-4">
            <i class="fa-solid fa-utensils" aria-hidden="true"></i>
            <div>
                <strong>Đặc quyền lưu trú.</strong>
                <span>Mọi đơn đặt phòng tại HBMS mặc định đã được tặng kèm <strong>Buffet sáng miễn phí mỗi ngày</strong> cho toàn bộ khách lưu trú.</span>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-12">
                <div class="card g5-panel">
                    <div class="card-header g5-section-header">
                        <h2 class="h5 mb-0"><i class="fa-solid fa-cart-plus me-2"></i>Chọn dịch vụ khách sạn</h2>
                    </div>
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/service" method="post">
                            <c:if test="${not empty currentBooking}">
                                <input type="hidden" name="bookingID" value="${currentBooking.bookingID}">
                            </c:if>

                            <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Danh mục dịch vụ">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
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
                                                            <span class="badge g5-status-success">Miễn phí</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="g5-service-amount">
                                                                <fmt:formatNumber value="${service.unitPrice}" pattern="#,##0.##" /> VND
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
                                <div class="g5-service-quantity mt-3">
                                    <label for="quantity" class="form-label fw-semibold mb-0">Số lượng</label>
                                    <input id="quantity" class="form-control text-center" type="number" name="quantity" value="1" min="1" max="99" required ${empty currentBooking ? 'disabled' : ''}>
                                </div>
                            </c:if>

                            <div class="g5-actions justify-content-between mt-3">
                                <a href="${pageContext.request.contextPath}/" class="btn g5-secondary px-4">Quay lại</a>
                                <button type="submit" class="btn g5-primary px-4" ${empty list || empty currentBooking ? "disabled" : ""}>
                                    <i class="fa-solid fa-plus me-1"></i>Thêm dịch vụ
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-12">
                <div class="card g5-panel">
                    <div class="card-header g5-section-header">
                        <h2 class="h5 mb-0"><i class="fa-solid fa-clipboard-list me-2"></i>Dịch vụ đã đặt</h2>
                        <c:if test="${not empty currentBooking}">
                            <span class="badge g5-status-neutral g5-service-badge">Booking #<c:out value="${currentBooking.bookingID}" /></span>
                        </c:if>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Dịch vụ đã đặt">
                            <table class="table table-hover mb-0 align-middle text-center">
                                <thead class="table-light">
                                    <tr>
                                        <th class="text-start">Tên dịch vụ</th>
                                        <th>Đơn giá</th>
                                        <th>Số lượng</th>
                                        <th>Trạng thái</th>
                                        <th class="text-end">Thành tiền</th>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:set var="totalServiceCharges" value="0" />
                                    <c:choose>
                                        <c:when test="${empty myServices}">
                                            <tr>
                                                <td colspan="5" class="text-center text-muted py-5">
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
                                                        <c:out value="${bs.service.serviceName}" />
                                                    </td>
                                                    <td class="g5-service-amount">
                                                        <c:choose>
                                                            <c:when test="${bs.unitPrice == 0}">
                                                                <span class="badge g5-status-success g5-service-badge">Miễn phí</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <fmt:formatNumber value="${bs.unitPrice}" pattern="#,##0.##" /> VND
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td><span class="badge g5-status-neutral g5-service-badge">×<c:out value="${bs.quantity}" /></span></td>
                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${bs.serviceStatus eq 'COMPLETED'}">
                                                                <span class="badge g5-status-success g5-service-badge">Đã phục vụ</span>
                                                            </c:when>
                                                            <c:when test="${bs.serviceStatus eq 'IN_PROGRESS'}">
                                                                <span class="badge g5-status-progress g5-service-badge">Đang thực hiện</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge g5-status-pending g5-service-badge">Chờ phục vụ</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>
                                                    <td class="text-end g5-service-amount">
                                                        <fmt:formatNumber value="${subtotal}" pattern="#,##0.##" /> VND
                                                    </td>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                                <c:if test="${not empty myServices}">
                                    <tfoot class="table-light">
                                        <tr>
                                            <td colspan="4" class="text-end fw-semibold">Tổng tiền dịch vụ</td>
                                            <td class="text-end g5-service-amount">
                                                <fmt:formatNumber value="${totalServiceCharges}" pattern="#,##0.##" /> VND
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
