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
<layout:layout title="Đơn dịch vụ" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-orders-workspace">
        <header class="g5-heading">
            <div>
                <h1>Đơn dịch vụ</h1>
                <p class="ui-description">Theo dõi yêu cầu và cập nhật tiến độ phục vụ.</p>
            </div>
            <div class="g5-actions">
                <a href="${pageContext.request.contextPath}/service?action=manage" class="btn g5-secondary">
                    <i class="fa-solid fa-bell-concierge me-1" aria-hidden="true"></i>Danh mục dịch vụ
                </a>
            </div>
        </header>

        <div class="card g5-panel">
            <div class="card-header g5-section-header">
                <h2 class="h5 mb-0">
                    <i class="fa-solid fa-list-check me-2"></i>Danh sách yêu cầu dịch vụ
                </h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Đơn dịch vụ">
                    <table class="table table-hover align-middle mb-0 g5-orders-table">
                        <colgroup>
                            <col class="g5-order-number">
                            <col class="g5-order-booking">
                            <col class="g5-order-customer">
                            <col>
                            <col class="g5-order-price">
                            <col class="g5-order-quantity">
                            <col class="g5-order-total">
                            <col class="g5-order-status">
                            <col class="g5-order-staff">
                            <c:if test="${canFulfillOrders}"><col class="g5-order-action"></c:if>
                        </colgroup>
                        <thead class="table-light">
                            <tr>
                                <th scope="col" class="text-center">#</th>
                                <th scope="col">Mã booking</th>
                                <th scope="col">Khách hàng</th>
                                <th scope="col">Tên dịch vụ</th>
                                <th scope="col" class="text-end">Đơn giá</th>
                                <th scope="col" class="text-center">Số lượng</th>
                                <th scope="col" class="text-end">Thành tiền</th>
                                <th scope="col" class="text-center">Trạng thái</th>
                                <th scope="col" class="text-center">Nhân viên phục vụ</th>
                                <c:if test="${canFulfillOrders}"><th scope="col" class="text-center">Thao tác</th></c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr>
                                        <td colspan="${canFulfillOrders ? 10 : 9}" class="text-center py-5 text-muted">
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
                                                <div class="g5-order-guest">
                                                    <span class="fw-semibold g5-order-ellipsis" title="<c:out value='${bs.booking.customerID.fullName}' />"><c:out value="${bs.booking.customerID.fullName}" /></span>
                                                    <small class="text-muted"><c:out value="${bs.booking.customerID.phone}" /></small>
                                                </div>
                                            </td>
                                            <td class="fw-semibold">
                                                <span class="g5-order-ellipsis" title="<c:out value='${bs.service.serviceName}' />"><c:out value="${bs.service.serviceName}" /></span>
                                            </td>
                                            <td class="text-end g5-service-amount">
                                                <c:choose>
                                                    <c:when test="${bs.unitPrice == 0}">
                                                        <span class="badge g5-status-success">Miễn phí</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <fmt:formatNumber value="${bs.unitPrice}" pattern="#,##0.##" /> VND
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center">
                                                <span class="badge g5-status-neutral g5-service-badge">
                                                    ×<c:out value="${bs.quantity}" />
                                                </span>
                                            </td>
                                            <td class="text-end g5-service-amount">
                                                <fmt:formatNumber value="${bs.unitPrice * bs.quantity}" pattern="#,##0.##" /> VND
                                            </td>

                                            <td class="text-center">
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

                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${not empty bs.servedByUserID}">
                                                        <span class="badge g5-status-neutral g5-service-badge">
                                                            <i class="fa-solid fa-id-badge me-1"></i>ID #<c:out value="${bs.servedByUserID}" />
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted fst-italic small">
                                                            <i class="fa-solid fa-minus me-1"></i>Chưa ghi nhận
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <c:if test="${canFulfillOrders}">
                                                <td class="text-center g5-order-action-cell">
                                                    <c:choose>
                                                        <c:when test="${bs.serviceStatus eq 'IN_PROGRESS' and bs.booking.bookingStatus ne 'CHECKED_OUT' and sessionScope.user.userID eq bs.servedByUserID}">
                                                            <form action="${pageContext.request.contextPath}/service" method="post" class="m-0 d-inline-block" onsubmit="return confirm('Xác nhận đã phục vụ xong món này?');">
                                                                <input type="hidden" name="action" value="completeOrder">
                                                                <input type="hidden" name="csrfToken" value="${serviceOrderCsrfToken}">
                                                                <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                                <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                                <button type="submit" class="btn btn-sm g5-secondary fw-semibold">
                                                                    <i class="fa-solid fa-check me-1"></i>Hoàn thành
                                                                </button>
                                                            </form>
                                                        </c:when>
                                                        <c:when test="${bs.serviceStatus ne 'COMPLETED' and bs.serviceStatus ne 'IN_PROGRESS' and bs.booking.bookingStatus ne 'CHECKED_OUT'}">
                                                            <form action="${pageContext.request.contextPath}/service" method="post" class="m-0 d-inline-block">
                                                                <input type="hidden" name="action" value="acceptOrder">
                                                                <input type="hidden" name="csrfToken" value="${serviceOrderCsrfToken}">
                                                                <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                                <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                                <button type="submit" class="btn btn-sm g5-primary fw-semibold">
                                                                    <i class="fa-solid fa-hand me-1"></i>Nhận việc
                                                                </button>
                                                            </form>
                                                        </c:when>
                                                        <c:otherwise><span class="text-muted" aria-label="Không có thao tác">—</span></c:otherwise>
                                                    </c:choose>
                                                </td>
                                            </c:if>
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
