<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Service Orders" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-orders-workspace">
        <header class="g5-heading">
            <div>
                <h1>Service Orders</h1>
                <p class="ui-description">Track service requests and update fulfillment progress.</p>
            </div>
            <div class="g5-actions">
                <a href="${pageContext.request.contextPath}/service?action=manage" class="btn g5-secondary">
                    <i class="fa-solid fa-bell-concierge me-1" aria-hidden="true"></i>Service Catalog
                </a>
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

        <div class="card g5-panel">
            <div class="card-header g5-section-header">
                <h2 class="h5 mb-0">
                    <i class="fa-solid fa-list-check me-2"></i>Service Request List
                </h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Service orders">
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
                            <col class="g5-order-action">
                        </colgroup>
                        <thead class="table-light">
                            <tr>
                                <th scope="col" class="text-center">#</th>
                                <th scope="col">Booking ID</th>
                                <th scope="col">Customer</th>
                                <th scope="col">Service Name</th>
                                <th scope="col" class="text-end">Unit Price</th>
                                <th scope="col" class="text-center">Quantity</th>
                                <th scope="col" class="text-end">Total</th>
                                <th scope="col" class="text-center">Status</th>
                                <th scope="col" class="text-center">Serving Staff</th>
                                <th scope="col" class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty orders}">
                                    <tr>
                                        <td colspan="10" class="text-center py-5 text-muted">
                                            <i class="fa-solid fa-inbox fa-3x mb-3 text-secondary opacity-50 d-block"></i>
                                            No service requests found from guest stays.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach items="${orders}" var="bs" varStatus="loop">
                                        <tr>
                                            <td class="text-center text-muted"><c:out value="${(currentPage - 1) * pageSize + loop.count}" /></td>
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
                                                        <span class="badge g5-status-success">Free</span>
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
                                                        <span class="badge g5-status-success g5-service-badge">Completed</span>
                                                    </c:when>
                                                    <c:when test="${bs.serviceStatus eq 'IN_PROGRESS'}">
                                                        <span class="badge g5-status-progress g5-service-badge">In Progress</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge g5-status-pending g5-service-badge">Pending</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>

                                            <td class="text-center">
                                                <c:choose>
                                                    <c:when test="${not empty bs.servedByUserID}">
                                                        <span class="badge g5-status-neutral g5-service-badge">
                                                            <i class="fa-solid fa-id-badge me-1"></i>Staff #<c:out value="${bs.servedByUserID}" />
                                                        </span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted fst-italic small">
                                                            <i class="fa-solid fa-minus me-1"></i>Unassigned
                                                        </span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td class="text-center g5-order-action-cell">
                                                <c:choose>
                                                    <c:when test="${bs.serviceStatus eq 'IN_PROGRESS' and bs.booking.bookingStatus ne 'CHECKED_OUT' and sessionScope.user.userID eq bs.servedByUserID}">
                                                        <form action="${pageContext.request.contextPath}/service" method="post" class="m-0 d-inline-block" onsubmit="return confirm('Confirm completion of this service?');">
                                                            <input type="hidden" name="action" value="completeOrder">
                                                            <input type="hidden" name="csrfToken" value="${serviceOrderCsrfToken}">
                                                            <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                            <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                            <button type="submit" class="btn btn-sm g5-secondary fw-semibold">
                                                                <i class="fa-solid fa-check me-1"></i>Complete
                                                            </button>
                                                        </form>
                                                    </c:when>
                                                    <c:when test="${bs.serviceStatus ne 'COMPLETED' and bs.serviceStatus ne 'IN_PROGRESS' and bs.booking.bookingStatus ne 'CHECKED_OUT'}">
                                                        <c:if test="${canCancelService}">
                                                            <form action="${pageContext.request.contextPath}/service" method="post" class="m-0 d-inline-block me-1" onsubmit="return confirm('Cancel this service request?');">
                                                                <input type="hidden" name="action" value="cancelOrder">
                                                                <input type="hidden" name="from" value="orders">
                                                                <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                                <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                                <button type="submit" class="btn btn-sm btn-outline-danger fw-semibold">
                                                                    <i class="fa-solid fa-xmark me-1"></i>Cancel
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                        <c:if test="${canFulfillOrders}">
                                                            <form action="${pageContext.request.contextPath}/service" method="post" class="m-0 d-inline-block">
                                                                <input type="hidden" name="action" value="acceptOrder">
                                                                <input type="hidden" name="csrfToken" value="${serviceOrderCsrfToken}">
                                                                <input type="hidden" name="bookingID" value="${bs.booking.bookingID.trim()}">
                                                                <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                                <button type="submit" class="btn btn-sm g5-primary fw-semibold">
                                                                    <i class="fa-solid fa-hand me-1"></i>Accept
                                                                </button>
                                                            </form>
                                                        </c:if>
                                                        <c:if test="${not canCancelService and not canFulfillOrders}">
                                                            <span class="text-muted" aria-label="No action">—</span>
                                                        </c:if>
                                                    </c:when>
                                                    <c:otherwise><span class="text-muted" aria-label="No action">—</span></c:otherwise>
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
            <c:if test="${not empty orders}">
                <div class="card-footer bg-white d-flex justify-content-between align-items-center py-2 px-3">
                    <div class="small text-muted">
                        Showing <strong><c:out value="${startIndex}" />–<c:out value="${endIndex}" /></strong> of <strong><c:out value="${totalOrders}" /></strong> requests
                    </div>
                    <c:if test="${totalPages gt 1}">
                        <nav aria-label="Service orders pagination">
                            <ul class="pagination pagination-sm mb-0">
                                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/service?action=orders&amp;page=${currentPage - 1}">Previous</a>
                                </li>
                                <c:forEach var="p" begin="${pageStart}" end="${pageEnd}">
                                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/service?action=orders&amp;page=${p}">${p}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/service?action=orders&amp;page=${currentPage + 1}">Next</a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>
</layout:layout>
