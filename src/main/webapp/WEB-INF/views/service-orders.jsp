<%--
    Document   : service-order
    Created on : Oct 21, 2026, 9:14:35 PM
    Author     : thanh
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Shows service orders and the action to mark them complete. --%>
<layout:layout title="Booked Services" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace">
        <div class="g5-heading">
            <div>
                <h1 class="h3 mb-1">
                    <i class="fa-solid fa-clipboard-list text-warning me-2"></i>Booked Services by Room
                </h1>
                <p class="text-muted mb-0">Danh sách các dịch vụ khách đang lưu trú đã yêu cầu theo từng phòng.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/service?action=manage" class="btn g5-secondary text-dark">
                    <i class="fa-solid fa-bell-concierge me-1"></i>Service Catalog
                </a>
            </div>
        </div>

        <div class="card g5-panel">
            <div class="card-header g5-section-header">
                <h2 class="h5 mb-0 text-secondary">
                    <i class="fa-solid fa-list-check me-2"></i>Danh sách yêu cầu dịch vụ
                </h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Đơn dịch vụ">
                    <table class="table table-hover align-middle mb-0 g5-orders-table">
                        <thead class="table-light">
                            <tr>
                                <th class="text-center" style="width: 60px;">#</th>
                                <th>Mã đơn (Booking)</th>
                                <th>Khách hàng</th>
                                <th>Tên dịch vụ</th>
                                <th class="text-end">Đơn giá</th>
                                <th class="text-center">Số lượng</th>
                                <th class="text-end">Thành tiền</th>
                                <th class="text-center" style="width: 15%">Trạng thái</th>
                                <th class="text-center" style="width: 20%">Nhân viên phục vụ</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr>
                                        <td colspan="9" class="text-center py-5 text-muted">
                                            <i class="fa-solid fa-inbox fa-3x mb-3 text-secondary opacity-50 d-block"></i>
                                            Hiện chưa có yêu cầu dịch vụ nào từ các phòng lưu trú.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${orders}" var="bs" varStatus="loop">
                                        <tr>
                                            <td class="text-center text-muted"><c:out value="${loop.count}" /></td>
                                            <td>
                                                <span class="badge g5-status-neutral">
                                                    <c:out value="${bs.booking.bookingID}" />
                                                </span>
                                            </td>
                                            <td>
                                                <div class="fw-semibold">
                                                    <c:out value="${bs.booking.customerID.fullName}" />
                                                </div>
                                                <small class="text-muted"><c:out value="${bs.booking.customerID.phone}" /></small>
                                            </td>
                                            <td class="fw-bold text-dark">
                                                <c:out value="${bs.service.serviceName}" />
                                            </td>
                                            <td class="text-end">
                                                <c:choose>
                                                    <c:when test="${bs.unitPrice == 0}">
                                                        <span class="badge g5-status-success">Miễn phí</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <fmt:formatNumber value="${bs.unitPrice}" type="number" groupingUsed="true" maxFractionDigits="0" /> VND
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <span class="badge bg-light text-dark border px-2 py-1 fs-6">
                                                    x<c:out value="${bs.quantity}" />
                                                </span>
                                            </td>
                                            <td class="text-end fw-bold text-danger">
                                                <fmt:formatNumber value="${bs.unitPrice * bs.quantity}" type="number" groupingUsed="true" maxFractionDigits="0" /> VND
                                            </td>

                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${bs.serviceStatus eq 'COMPLETED'}">
                                                        <span class="badge g5-status-success px-2 py-1">
                                                            <i class="fa-solid fa-circle-check me-1"></i>Đã phục vụ
                                                        </span>
                                                    </c:when>

                                                    <c:when test="${bs.serviceStatus eq 'IN_PROGRESS'}">
                                                        <c:choose>
                                                            <c:when test="${canFulfillOrders and bs.booking.bookingStatus ne 'CHECKED_OUT' and sessionScope.user.userID eq bs.servedByUserID}">
                                                                <form action="${pageContext.request.contextPath}/service" method="post" class="m-0" onsubmit="return confirm('Xác nhận đã phục vụ xong món này?');">
                                                                    <input type="hidden" name="action" value="completeOrder">
                                                                    <input type="hidden" name="csrfToken" value="${serviceOrderCsrfToken}">
                                                                    <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                                    <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                                    <button type="submit" class="btn btn-sm g5-primary fw-bold shadow-sm">
                                                                        <i class="fa-solid fa-check me-1"></i>Hoàn thành
                                                                    </button>
                                                                </form>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-info text-dark px-2 py-1">
                                                                    <i class="fa-solid fa-spinner fa-spin me-1"></i>Đang thực hiện
                                                                </span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </c:when>

                                                    <c:when test="${canFulfillOrders and bs.booking.bookingStatus ne 'CHECKED_OUT'}">
                                                        <form action="${pageContext.request.contextPath}/service" method="post" class="m-0">
                                                            <input type="hidden" name="action" value="acceptOrder">
                                                            <input type="hidden" name="csrfToken" value="${serviceOrderCsrfToken}">
                                                            <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                            <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                            <button type="submit" class="btn btn-sm g5-primary fw-bold shadow-sm">
                                                                <i class="fa-solid fa-hand me-1"></i>Nhận việc
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge g5-status-pending px-2 py-1">Chờ phục vụ</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${not empty bs.servedByUserID}">
                                                        <span class="badge bg-light text-primary border border-primary px-2 py-1 fs-6">
                                                            <i class="fa-solid fa-id-badge me-1"></i>User ID #<c:out value="${bs.servedByUserID}" />
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted fst-italic small">
                                                            <i class="fa-solid fa-minus me-1"></i>Chưa ghi nhận
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                        </tr>
                                    </c:forEach>
                                </c:otherwise>
                            </c:choose>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
