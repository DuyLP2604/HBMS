<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Hotel Services" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace">
        <header class="g5-heading">
            <div>
                <h1>Hotel Services</h1>
                <p class="ui-description">Request additional amenities and monitor fulfillment status.</p>
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
                                    <span class="badge g5-status-neutral px-3 py-2"><i class="fa-solid fa-hotel me-1"></i>Booking #${currentBooking.bookingID}</span>
                                    <span class="badge g5-status-neutral py-2 px-3">${currentBooking.bookingStatus}</span>
                                    <span class="text-muted ms-2">
                                        <i class="fa-regular fa-calendar-days me-1"></i>
                                        <fmt:formatDate value="${currentBooking.checkInDate}" pattern="dd/MM/yyyy" /> - <fmt:formatDate value="${currentBooking.checkOutDate}" pattern="dd/MM/yyyy" />
                                    </span>
                                </div>
                                <c:set var="canOrder" value="false" />
                                <c:if test="${not empty currentBooking}">
                                    <c:choose>
                                        <c:when test="${sessionScope.user.role eq 'Customer'}">
                                            <c:set var="canOrder" value="${currentBooking.bookingStatus eq 'CONFIRMED' or currentBooking.bookingStatus eq 'ASSIGNED'}" />
                                        </c:when>
                                        <c:otherwise>
                                            <c:set var="canOrder" value="${currentBooking.bookingStatus eq 'CHECKED_IN'}" />
                                        </c:otherwise>
                                    </c:choose>
                                </c:if>
                                <c:if test="${sessionScope.user.role eq 'Customer' and currentBooking.bookingStatus eq 'CHECKED_IN'}">
                                    <div class="alert alert-warning mt-3 mb-0 py-2">
                                        <i class="fa-solid fa-bell me-2"></i>
                                        <strong>You have checked in.</strong> To order in-room services, please contact hotel reception or on-duty staff.
                                    </div>
                                </c:if>
                                <c:if test="${sessionScope.user.role ne 'Customer' and currentBooking.bookingStatus ne 'CHECKED_IN'}">
                                    <div class="alert alert-warning mt-3 mb-0 py-2">
                                        <i class="fa-solid fa-circle-exclamation me-2"></i>
                                        Services can only be added for in-house guests currently staying at the hotel.
                                    </div>
                                </c:if>
                            </c:when>
                            <c:otherwise>
                                <div class="text-warning-emphasis">
                                    <i class="fa-solid fa-circle-info me-2 text-warning fs-5"></i>
                                    <strong>No active room reservations found.</strong>
                                    <span class="text-muted ms-1">Please make a room booking to order hotel services.</span>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>
                    <c:if test="${empty currentBooking}">
                        <div class="col-md-4 text-md-end mt-2 mt-md-0">
                            <a href="${pageContext.request.contextPath}/" class="btn g5-primary btn-sm px-3">
                                <i class="fa-solid fa-calendar-plus me-1"></i>Book Now
                            </a>
                        </div>
                    </c:if>
                </div>
            </div>
        </div>

        <div class="g5-service-notice mb-4">
            <i class="fa-solid fa-utensils" aria-hidden="true"></i>
            <div>
                <strong>Stay Privilege.</strong>
                <span>Every room reservation at HBMS includes complimentary <strong>daily dinner buffet</strong> for all staying guests.</span>
            </div>
        </div>

        <div class="row g-4">
            <div class="col-12">
                <div class="card g5-panel">
                    <div class="card-header g5-section-header">
                        <h2 class="h5 mb-0"><i class="fa-solid fa-cart-plus me-2"></i>Select Hotel Service</h2>
                    </div>
                    <div class="card-body p-3">
                        <form action="${pageContext.request.contextPath}/service" method="post">
                            <c:if test="${not empty currentBooking}">
                                <input type="hidden" name="bookingID" value="${currentBooking.bookingID}">
                            </c:if>

                            <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Service Catalog">
                                <table class="table table-hover align-middle mb-0">
                                    <thead class="table-light">
                                        <tr>
                                            <th class="text-center" style="width: 15%">Select</th>
                                            <th style="width: 50%">Service</th>
                                            <th style="width: 35%" class="text-end">Unit Price</th>
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
                                                            <span class="text-success fw-bold fs-5" title="Included with room">✓</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <input class="form-check-input" type="radio" name="selectedService" value="${service.serviceID}" required ${not canOrder ? 'disabled' : ''}>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </td>
                                                <td>
                                                    <strong class="${isFree ? 'text-success' : 'text-dark'}"><c:out value="${service.serviceName}" /></strong>
                                                    <c:if test="${isFree}">
                                                        <br><small class="text-success fst-italic"><i class="fa-solid fa-gift me-1"></i>Included with room</small>
                                                    </c:if>
                                                </td>
                                                <td class="text-end">
                                                    <c:choose>
                                                        <c:when test="${isFree}">
                                                            <span class="badge g5-status-success">Free</span>
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
                                                <td colspan="3" class="text-center text-muted py-4">No services are currently available in the catalog.</td>
                                            </tr>
                                        </c:if>
                                    </tbody>
                                </table>
                            </div>

                            <c:if test="${not empty list}">
                                <div class="g5-service-quantity mt-3">
                                    <label for="quantity" class="form-label fw-semibold mb-0">Quantity</label>
                                    <input id="quantity" class="form-control text-center" type="number" name="quantity" value="1" min="1" max="99" required ${not canOrder ? 'disabled' : ''}>
                                </div>
                            </c:if>
                            <div class="g5-actions justify-content-between mt-3">
                                <a href="${pageContext.request.contextPath}/" class="btn g5-secondary px-4">Back</a>
                                <button type="submit" class="btn g5-primary px-4" ${empty list or not canOrder ? "disabled" : ""}>
                                    <i class="fa-solid fa-plus me-1"></i>Add Service
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>

            <div class="col-12">
                <div class="card g5-panel">
                    <div class="card-header g5-section-header">
                        <h2 class="h5 mb-0"><i class="fa-solid fa-clipboard-list me-2"></i>Booked Services</h2>
                        <c:if test="${not empty currentBooking}">
                            <span class="badge g5-status-neutral g5-service-badge">Booking #<c:out value="${currentBooking.bookingID}" /></span>
                        </c:if>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Booked services">
                            <table class="table table-hover mb-0 align-middle text-center">
                                <thead class="table-light">
                                    <tr>
                                        <th class="text-start">Service Name</th>
                                        <th>Unit Price</th>
                                        <th>Quantity</th>
                                        <th>Status</th>
                                        <th class="text-end">Total</th>
                                        <c:if test="${canCancelService}">
                                            <th>Action</th>
                                        </c:if>
                                    </tr>
                                </thead>
                                <tbody>
                                    <c:set var="totalServiceCharges" value="0" />
                                    <c:choose>
                                        <c:when test="${empty myServices}">
                                            <tr>
                                                <td colspan="${canCancelService ? 6 : 5}" class="text-center text-muted py-5">
                                                    <i class="fa-solid fa-bell-concierge fa-3x mb-3 text-secondary opacity-50"></i><br>
                                                    No additional services have been added to this booking yet.
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
                                                                <span class="badge g5-status-success g5-service-badge">Free</span>
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
                                                    <td class="text-end g5-service-amount">
                                                        <fmt:formatNumber value="${subtotal}" pattern="#,##0.##" /> VND
                                                    </td>
                                                    <c:if test="${canCancelService}">
                                                        <td>
                                                            <c:choose>
                                                                <c:when test="${empty bs.serviceStatus or bs.serviceStatus eq 'PENDING'}">
                                                                    <form action="${pageContext.request.contextPath}/service" method="post" class="d-inline m-0" onsubmit="return confirm('Cancel this service request?');">
                                                                        <input type="hidden" name="action" value="cancelOrder">
                                                                        <input type="hidden" name="bookingID" value="${currentBooking.bookingID.trim()}">
                                                                        <input type="hidden" name="serviceID" value="${bs.service.serviceID.trim()}">
                                                                        <button type="submit" class="btn btn-sm btn-outline-danger py-0 px-2">
                                                                            <i class="fa-solid fa-xmark me-1"></i>Cancel
                                                                        </button>
                                                                    </form>
                                                                </c:when>
                                                                <c:otherwise>
                                                                    <span class="text-muted small">—</span>
                                                                </c:otherwise>
                                                            </c:choose>
                                                        </td>
                                                    </c:if>
                                                </tr>
                                            </c:forEach>
                                        </c:otherwise>
                                    </c:choose>
                                </tbody>
                                <c:if test="${not empty myServices}">
                                    <tfoot class="table-light">
                                        <tr>
                                            <td colspan="4" class="text-end fw-semibold">Total Service Charges</td>
                                            <td class="text-end g5-service-amount">
                                                <fmt:formatNumber value="${totalServiceCharges}" pattern="#,##0.##" /> VND
                                            </td>
                                            <c:if test="${canCancelService}">
                                                <td></td>
                                            </c:if>
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
