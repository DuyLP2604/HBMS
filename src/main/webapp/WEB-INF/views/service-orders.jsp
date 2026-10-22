<%--
    Document   : service-order
    Created on : Oct 21, 2026, 9:14:35 PM
    Author     : thanh
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Booked Services" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center gap-3 mb-4">
            <div>
                <h1 class="h3 mb-1">
                    <i class="fa-solid fa-clipboard-list text-warning me-2"></i>Booked Services by Room
                </h1>
                <p class="text-muted mb-0">Danh sách các dịch vụ khách đang lưu trú đã yêu cầu theo từng phòng.</p>
            </div>
            <div class="d-flex gap-2">
                <a href="${pageContext.request.contextPath}/service?action=manage" class="btn btn-outline-warning text-dark">
                    <i class="fa-solid fa-bell-concierge me-1"></i>Service Catalog
                </a>
            </div>
        </div>

        <div class="card shadow-sm border-0">
            <div class="card-header bg-white py-3 border-0">
                <h2 class="h5 mb-0 text-secondary">
                    <i class="fa-solid fa-list-check me-2"></i>Danh sách yêu cầu dịch vụ
                </h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th class="text-center" style="width: 60px;">#</th>
                                <th>Mã đơn (Booking)</th>
                                <th>Khách hàng</th>
                                <th>Tên dịch vụ</th>
                                <th class="text-end">Đơn giá</th>
                                <th class="text-center">Số lượng</th>
                                <th class="text-end">Thành tiền</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr>
                                        <td colspan="8" class="text-center py-5 text-muted">
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
                                                <span class="badge bg-primary fs-6">
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
                                                        <span class="badge bg-success">Miễn phí</span>
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
