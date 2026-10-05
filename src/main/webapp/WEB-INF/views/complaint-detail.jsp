<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="canManageComplaints" value="${sessionScope.user.role eq 'Admin' or sessionScope.user.role eq 'Staff'}" />
<layout:layout
    title="Complaint Details"
    pageCss="complaint.css"
    useBootstrap="true"
    bodyClass="bg-light">
    <div class="container my-5 complaint-detail-container">
        <div class="card shadow-sm">
            <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center py-3">
                <h2 class="h4 mb-0">Complaint Details #<c:out value="${complaint.complaintID}" /></h2>
                <c:choose>
                    <c:when test="${canManageComplaints}">
                        <a href="${pageContext.request.contextPath}/complaint?action=list" class="btn btn-light btn-sm">Back</a>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/complaint?action=add" class="btn btn-light btn-sm">Back</a>
                    </c:otherwise>
                </c:choose>
            </div>
            <div class="card-body">
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
                    <dd class="col-sm-9" style="white-space: pre-wrap; overflow-wrap: anywhere;"><c:out value="${complaint.content}" /></dd>
                    <dt class="col-sm-3">Status</dt>
                    <dd class="col-sm-9">
                        <c:choose>
                            <c:when test="${complaint.status eq 'Đã xử lý'}">
                                <span class="badge bg-success"><c:out value="${complaint.status}" /></span>
                            </c:when>
                            <c:when test="${complaint.status eq 'Đang xử lý'}">
                                <span class="badge bg-warning text-dark"><c:out value="${complaint.status}" /></span>
                            </c:when>
                            <c:otherwise>
                                <span class="badge bg-secondary"><c:out value="${complaint.status}" /></span>
                            </c:otherwise>
                        </c:choose>
                    </dd>
                    <c:if test="${not empty complaint.replyMessage}">
                        <dt class="col-sm-3 text-danger mt-3">Staff Reply</dt>
                        <dd class="col-sm-9 mt-3" style="white-space: pre-wrap; overflow-wrap: anywhere; background-color: #f8d7da; color: #842029; padding: 12px; border-radius: 8px; border: 1px solid #f5c2c7;"><strong><c:out value="${complaint.replyMessage}" /></strong></dd>
                    </c:if>
                </dl>
                <c:if test="${canManageComplaints}">
                    <hr>
                    <form action="${pageContext.request.contextPath}/complaint" method="post" class="d-flex gap-2 align-items-end">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="id" value="${complaint.complaintID}">
                        <div class="flex-grow-1">
                            <label for="complaintStatus" class="form-label fw-bold">Update Status</label>
                            <select id="complaintStatus" name="status" class="form-select" required>
                                <option value="" disabled ${complaint.status ne 'Chưa xử lý' and complaint.status ne 'Đang xử lý' and complaint.status ne 'Đã xử lý' ? 'selected' : ''}>Select status</option>
                                <option value="Chưa xử lý" ${complaint.status eq 'Chưa xử lý' ? 'selected' : ''}>Chưa xử lý</option>
                                <option value="Đang xử lý" ${complaint.status eq 'Đang xử lý' ? 'selected' : ''}>Đang xử lý</option>
                                <option value="Đã xử lý" ${complaint.status eq 'Đã xử lý' ? 'selected' : ''}>Đã xử lý</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-danger">Update Status</button>
                    </form>
                    <hr class="mt-4">
                    <form action="${pageContext.request.contextPath}/complaint" method="post" class="mt-4">
                        <input type="hidden" name="action" value="reply">
                        <input type="hidden" name="id" value="${complaint.complaintID}">
                        <div class="mb-3">
                            <label for="replyComplaintStatus" class="form-label fw-bold">Status for Reply</label>
                            <select id="replyComplaintStatus" name="status" class="form-select" required>
                                <option value="" disabled ${complaint.status ne 'Chưa xử lý' and complaint.status ne 'Đang xử lý' and complaint.status ne 'Đã xử lý' ? 'selected' : ''}>Select status</option>
                                <option value="Chưa xử lý" ${complaint.status eq 'Chưa xử lý' ? 'selected' : ''}>Chưa xử lý</option>
                                <option value="Đang xử lý" ${complaint.status eq 'Đang xử lý' ? 'selected' : ''}>Đang xử lý</option>
                                <option value="Đã xử lý" ${complaint.status eq 'Đã xử lý' ? 'selected' : ''}>Đã xử lý</option>
                            </select>
                        </div>
                        <div class="mb-3">
                            <label for="replyMessage" class="form-label fw-bold">Reply Message</label>
                            <textarea id="replyMessage" name="replyMessage" class="form-control" rows="5" placeholder="Nhập nội dung giải thích, hỗ trợ khách hàng vào đây..." required><c:out value="${complaint.replyMessage}" /></textarea>
                        </div>
                        <button type="submit" class="btn btn-danger px-4">Update &amp; Reply</button>
                    </form>
                </c:if>
            </div>
        </div>
    </div>
</layout:layout>