<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Lists complaints for staff to review.
--%>
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
            <div class="card-body">
                <div class="table-responsive g5-table-scroll" tabindex="0" role="region" aria-label="Complaint list">
                    <table class="table table-hover align-middle mb-0">
                        <thead class="table-light">
                            <tr>
                                <th>ID</th>
                                <th>Title</th>
                                <th>Customer</th>
                                <th>Created At</th>
                                <th>Status</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="cp" items="${complaints}">
                                <tr>
                                    <td class="fw-bold text-secondary"><c:out value="${cp.complaintID}" /></td>
                                    <td><strong><c:out value="${cp.title}" /></strong></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${empty cp.customerID}">Anonymous</c:when>
                                            <c:otherwise>
                                                <div><c:out value="${cp.customerID.fullName}" /></div>
                                                <div class="small text-muted">ID: <c:out value="${cp.customerID.customerID}" /></div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td><fmt:formatDate value="${cp.createdAt}" pattern="dd/MM/yyyy HH:mm:ss" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${cp.status eq 'Đã xử lý'}">
                                                <span class="badge g5-status-success"><c:out value="${cp.status}" /></span>
                                            </c:when>
                                            <c:when test="${cp.status eq 'Đang xử lý'}">
                                                <span class="badge g5-status-pending"><c:out value="${cp.status}" /></span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge g5-status-neutral"><c:out value="${cp.status}" /></span>
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
        </div>
    </div>
</layout:layout>
