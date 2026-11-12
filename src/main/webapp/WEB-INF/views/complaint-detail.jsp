<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Complaint Details"
    pageCss="group5.css"
    useBootstrap="true"
    bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace g5-form-workspace">
        <div class="card g5-panel">
            <div class="card-header g5-section-header">
                <h1 class="h4 mb-0">Complaint Details #<c:out value="${complaint.complaintID}" /></h1>
                <div class="g5-header-status">
                    <c:choose>
                        <c:when test="${complaint.status eq 'Đã xử lý'}">
                            <span class="badge g5-status-success">Resolved</span>
                        </c:when>
                        <c:when test="${complaint.status eq 'Đang xử lý'}">
                            <span class="badge g5-status-pending">In Progress</span>
                        </c:when>
                        <c:otherwise>
                            <span class="badge g5-status-neutral">Pending</span>
                        </c:otherwise>
                    </c:choose>
                </div>
                <c:choose>
                    <c:when test="${canManageComplaints}">
                        <a href="${pageContext.request.contextPath}/complaint?action=list" class="btn g5-secondary btn-sm">Back</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/complaint?action=add" class="btn g5-secondary btn-sm">Back</a>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
                <h2 class="g5-subtitle">Guest complaint</h2>
                <dl class="row mb-0">
                    <dt class="col-sm-3">Title</dt>
                    <dd class="col-sm-9"><c:out value="${complaint.title}" /></dd>
                    <dt class="col-sm-3">Customer</dt>
                    <dd class="col-sm-9">
                        <c:choose>
                            <c:when test="${empty complaint.customerID}">Anonymous</c:when>
                            <c:otherwise>
                                <div><c:out value="${complaint.customerID.fullName}" /></div>
                                <div class="small text-muted">ID: <c:out value="${complaint.customerID.customerID}" /></div>
                            </c:otherwise>
                        </c:choose>
                    </dd>
                    <dt class="col-sm-3">Created At</dt>
                    <dd class="col-sm-9"><fmt:formatDate value="${complaint.createdAt}" pattern="dd/MM/yyyy HH:mm:ss" /></dd>
                    <dt class="col-sm-3">Content</dt>
                    <dd class="col-sm-9 g5-message"><c:out value="${complaint.content}" /></dd>
                    <dt class="col-sm-3">Handled by</dt>
                    <dd class="col-sm-9">
                        <c:choose>
                            <c:when test="${not empty complaint.employeeID}"><c:out value="${complaint.employeeID.fullName}" /></c:when>
                            <c:otherwise><span class="text-muted">Not recorded</span></c:otherwise>
                        </c:choose>
                    </dd>
                    <c:if test="${not empty complaint.replyMessage}">
                        <dt class="col-sm-3 mt-3">Staff Reply</dt>
                        <dd class="col-sm-9 mt-3 g5-message"><strong><c:out value="${complaint.replyMessage}" /></strong></dd>
                            </c:if>
                </dl>
                <c:if test="${sessionScope.user.role ne 'Admin' and canManageComplaints}">
                    <hr class="mt-4">
                    <h2 class="g5-subtitle">Handle Complaint</h2>
                    <form action="${pageContext.request.contextPath}/complaint" method="post" class="mt-2">
                        <input type="hidden" name="action" value="reply">
                        <input type="hidden" name="id" value="${complaint.complaintID}">
                        <input type="hidden" name="csrfToken" value="<c:out value='${complaintCsrfToken}' />">
                        <div class="mb-3">
                            <label for="complaintStatus" class="form-label fw-bold">Update Status</label>
                            <select id="complaintStatus" name="status" class="form-select g5-status-select" required>
                                <option value="Chưa xử lý" ${complaint.status eq 'Chưa xử lý' ? 'selected' : ''}>Pending</option>
                                <option value="Đang xử lý" ${complaint.status eq 'Đang xử lý' ? 'selected' : ''}>In Progress</option>
                                <option value="Đã xử lý" ${complaint.status eq 'Đã xử lý' ? 'selected' : ''}>Resolved</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="replyMessage" class="form-label fw-bold">Reply Message</label>
                            <textarea id="replyMessage" name="replyMessage" class="form-control" rows="5" placeholder="Enter response or assistance message here..." required><c:out value="${complaint.replyMessage}" /></textarea>
                        </div>
                        <button type="submit" class="btn g5-primary px-4">Reply</button>
                    </form>
                </c:if>
            </div>
        </div>
    </div>
</layout:layout>
