<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Service Catalog" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace">
        <header class="g5-heading">
            <div>
                <h1>Service Catalog</h1>
                <p class="ui-description">Manage hotel services, unit pricing, and availability.</p>
            </div>
            <div class="g5-actions">
                <a href="${pageContext.request.contextPath}/service?action=orders" class="btn g5-secondary">
                    <i class="fa-solid fa-clipboard-list me-1" aria-hidden="true"></i>Booked Services
                </a>
                <c:if test="${sessionScope.user.role == 'Admin'}">
                    <button type="button" class="btn g5-primary" data-bs-toggle="modal" data-bs-target="#createServiceModal">
                        <i class="fa-solid fa-plus me-1" aria-hidden="true"></i>Add Service
                    </button>
                </c:if>
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
                <h2 class="h5 mb-0 fw-bold">Service List (${services.size()})</h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Service List">
                    <table class="table table-hover mb-0 align-middle">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 12%" class="text-center">Service ID</th>
                                <th style="width: 38%">Service Name</th>
                                <th style="width: 18%" class="text-end">Unit Price</th>
                                <th style="width: 14%" class="text-center">Status</th>
                                    <c:if test="${sessionScope.user.role == 'Admin'}">
                                    <th style="width: 18%" class="text-center">Actions</th>
                                    </c:if>
                            </tr>
                        </thead>
                        <tbody>
                            <c:if test="${empty services}">
                                <tr>
                                    <td colspan="${sessionScope.user.role eq 'Admin' ? 5 : 4}" class="text-center py-4 text-muted">
                                        No services found in the catalog.
                                    </td>
                                </tr>
                            </c:if>
                            <c:forEach var="s" items="${services}">
                                <c:set var="cleanID" value="${s.serviceID.trim()}" />
                                <tr>
                                    <td class="text-center">
                                        <span class="badge g5-status-neutral g5-service-badge"><c:out value="${cleanID}" /></span>
                                    </td>
                                    <td class="fw-semibold"><c:out value="${s.serviceName}" /></td>
                                    <td class="text-end g5-service-amount">
                                        <fmt:formatNumber value="${s.unitPrice}" pattern="#,##0.##" /> VND
                                    </td>
                                    <td class="text-center">
                                        <c:choose>
                                            <c:when test="${s.serviceStatus eq 'SUSPENDED'}">
                                                <span class="badge bg-danger px-2 py-1">
                                                    <i class="fa-solid fa-ban me-1"></i>Suspended
                                                </span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge bg-success px-2 py-1">
                                                    <i class="fa-solid fa-circle-check me-1"></i>Active
                                                </span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <c:if test="${sessionScope.user.role == 'Admin'}">
                                        <td class="text-center">
                                            <div class="g5-actions justify-content-center">
                                                <form action="${pageContext.request.contextPath}/service" method="post" class="m-0">
                                                    <input type="hidden" name="action" value="toggleStatus">
                                                    <input type="hidden" name="serviceID" value="${cleanID}">
                                                    <c:choose>
                                                        <c:when test="${s.serviceStatus eq 'SUSPENDED'}">
                                                            <button type="submit" class="btn btn-sm btn-outline-success fw-semibold" title="Activate this service">
                                                                <i class="fa-solid fa-play me-1"></i>Activate
                                                            </button>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <button type="submit" class="btn btn-sm btn-outline-warning text-dark fw-semibold"
                                                                    onclick="return confirm('Suspend this service? Guests will not be able to order it.');"
                                                                    title="Suspend this service">
                                                                <i class="fa-solid fa-pause me-1"></i>Suspend
                                                            </button>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </form>
                                                <button type="button" class="btn btn-sm g5-secondary fw-semibold"
                                                        data-bs-toggle="modal" data-bs-target="#editModal_${cleanID}">
                                                    <i class="fa-solid fa-pen-to-square me-1"></i>Edit
                                                </button>
                                                <form action="${pageContext.request.contextPath}/service" method="post" class="m-0"
                                                      onsubmit="return confirm('Are you sure you want to delete this service?');">
                                                    <input type="hidden" name="action" value="delete">
                                                    <input type="hidden" name="serviceID" value="${cleanID}">
                                                    <button type="submit" class="btn btn-sm btn-outline-danger">Delete</button>
                                                </form>
                                            </div>
                                        </td>
                                    </c:if>
                                </tr>
                            </c:forEach>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <div class="modal fade" id="createServiceModal" tabindex="-1" aria-hidden="true" aria-labelledby="createServiceTitle">
        <div class="modal-dialog">
            <div class="modal-content">
                <form action="${pageContext.request.contextPath}/service" method="post">
                    <input type="hidden" name="action" value="create">
                    <div class="modal-header g5-section-header">
                        <h5 class="modal-title" id="createServiceTitle"><i class="fa-solid fa-plus me-2"></i>Add New Service</h5>
                        <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                    </div>
                    <div class="modal-body">
                        <div class="mb-3">
                            <label for="createServiceName" class="form-label fw-bold">Service Name <span class="text-danger">*</span></label>
                            <input type="text" class="form-control" id="createServiceName" name="serviceName" maxlength="60" required placeholder="e.g. Skin Care Spa, Car Rental...">
                        </div>
                        <div class="mb-3">
                            <label for="createUnitPrice" class="form-label fw-bold">Unit Price (VND) <span class="text-danger">*</span></label>
                            <input type="number" class="form-control" id="createUnitPrice" name="unitPrice" min="0" step="1000" required placeholder="e.g. 150000 (enter 0 if complimentary)">
                            <div class="form-text">Enter 0 if this service is complimentary with stay.</div>
                        </div>
                    </div>
                    <div class="modal-footer">
                        <button type="button" class="btn g5-secondary" data-bs-dismiss="modal">Cancel</button>
                        <button type="submit" class="btn g5-primary"><i class="fa-solid fa-check me-1"></i>Save Service</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <c:if test="${sessionScope.user.role == 'Admin'}">
        <c:forEach var="s" items="${services}">
            <c:set var="cleanID" value="${s.serviceID.trim()}" />
            <div class="modal fade" id="editModal_${cleanID}" tabindex="-1" aria-hidden="true" aria-labelledby="editTitle_${cleanID}">
                <div class="modal-dialog modal-dialog-centered">
                    <div class="modal-content">
                        <form action="${pageContext.request.contextPath}/service" method="post">
                            <input type="hidden" name="action" value="update">
                            <input type="hidden" name="serviceID" value="${cleanID}">

                            <div class="modal-header g5-section-header">
                                <h5 class="modal-title fw-bold" id="editTitle_${cleanID}">Update Service</h5>
                                <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                            </div>
                            <div class="modal-body">
                                <div class="mb-3">
                                    <label for="editServiceName_${cleanID}" class="form-label fw-bold">Service Name</label>
                                    <input id="editServiceName_${cleanID}" type="text" name="serviceName" value="<c:out value='${s.serviceName}' />" class="form-control" required="required">
                                </div>
                                <div class="mb-3">
                                    <label for="editUnitPrice_${cleanID}" class="form-label fw-bold">Unit Price (VND)</label>
                                    <input id="editUnitPrice_${cleanID}" type="text" name="unitPrice" value="${s.unitPrice}" class="form-control" required="required">
                                </div>
                            </div>
                            <div class="modal-footer">
                                <button type="button" class="btn g5-secondary" data-bs-dismiss="modal">Cancel</button>
                                <button type="submit" class="btn g5-primary fw-bold">Save Changes</button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </c:forEach>
    </c:if>
</layout:layout>