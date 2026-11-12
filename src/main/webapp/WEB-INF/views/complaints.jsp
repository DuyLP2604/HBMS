<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Complaint List"
    pageCss="group5.css"
    useBootstrap="true"
    bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace">
        <div class="card g5-panel">
            <div class="card-header g5-section-header">
                <h1 class="h4 mb-0">Complaint List</h1>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Complaint list">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 80px; white-space: nowrap;">ID</th>
                                <th>Title</th>
                                <th style="white-space: nowrap;">Customer</th>
                                <th style="white-space: nowrap;">Created At</th>
                                <th style="white-space: nowrap;">Status</th>
                                <th class="text-center" style="white-space: nowrap;">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cp" items="${complaints}">
                                <tr>
                                    <td class="fw-bold text-secondary text-nowrap"><c:out value="${cp.complaintID}" /></td>
                                    <td style="word-break: break-word;"><strong><c:out value="${cp.title}" /></strong></td>
                                    <td style="white-space: nowrap;">
                                        <c:choose>
                                            <c:when test="${empty cp.customerID}">Anonymous</c:when>
                                            <c:otherwise>
                                                <div><c:out value="${cp.customerID.fullName}" /></div>
                                                <div class="small text-muted">ID: <c:out value="${cp.customerID.customerID}" /></div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td style="white-space: nowrap;"><fmt:formatDate value="${cp.createdAt}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${cp.status eq 'Đã xử lý'}">
                                                <span class="badge g5-status-success">Resolved</span>
                                            </c:when>
                                            <c:when test="${cp.status eq 'Đang xử lý'}">
                                                <span class="badge g5-status-pending">In Progress</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge g5-status-neutral">Pending</span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/complaint?action=viewDetail&amp;id=${cp.complaintID}"
                                           class="btn btn-outline-info btn-sm me-1">
                                            <i class="fa-solid fa-eye"></i> View
                                        </a>
                                    </td>
                                </tr>
                            </c:forEach>
                            <c:if test="${empty complaints}">
                                <tr>
                                    <td colspan="6" class="text-center text-muted py-4">No complaints found.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
            <c:if test="${not empty complaints}">
                <div class="card-footer bg-white d-flex justify-content-between align-items-center py-2 px-3">
                    <div class="small text-muted">
                        Showing <strong><c:out value="${startIndex}" />–<c:out value="${endIndex}" /></strong> of <strong><c:out value="${totalComplaints}" /></strong> complaints
                    </div>
                    <c:if test="${totalPages gt 1}">
                        <nav aria-label="Complaint pagination">
                            <ul class="pagination pagination-sm mb-0">
                                <li class="page-item ${currentPage <= 1 ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/complaint?action=list&amp;page=${currentPage - 1}">Previous</a>
                                </li>
                                <c:forEach var="p" begin="${pageStart}" end="${pageEnd}">
                                    <li class="page-item ${p == currentPage ? 'active' : ''}">
                                        <a class="page-link" href="${pageContext.request.contextPath}/complaint?action=list&amp;page=${p}">${p}</a>
                                    </li>
                                </c:forEach>
                                <li class="page-item ${currentPage >= totalPages ? 'disabled' : ''}">
                                    <a class="page-link" href="${pageContext.request.contextPath}/complaint?action=list&amp;page=${currentPage + 1}">Next</a>
                                </li>
                            </ul>
                        </nav>
                    </c:if>
                </div>
            </c:if>
        </div>
    </div>
</layout:layout>
