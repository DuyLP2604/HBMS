<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<layout:layout title="Submit Complaint" pageCss="complaint.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5 complaint-container">
        <div class="card shadow-sm mb-5">
            <div class="card-header bg-danger text-white py-3">
                <h2 class="h4 mb-0">Submit Complaint</h2>
            </div>
            <div class="card-body">
                <c:if test="${not empty errorMessage}">
                    <div class="alert alert-danger"><c:out value="${errorMessage}"/></div>
                </c:if>
                <c:if test="${not empty flashMsg}">
                    <div class="alert alert-success"><c:out value="${flashMsg}"/></div>
                </c:if>

                <form action="${pageContext.request.contextPath}/complaint" method="post">
                    <input type="hidden" name="action" value="add">
                    <div class="mb-3">
                        <label class="form-label">Title <span class="text-danger">*</span></label>
                        <input type="text" name="title" class="form-control" required>
                    </div>
                    <div class="mb-3">
                        <label class="form-label">Complaint Details <span class="text-danger">*</span></label>
                        <textarea name="content" class="form-control" rows="5" required></textarea>
                    </div>
                    <div class="d-flex justify-content-end gap-2">
                        <a href="${pageContext.request.contextPath}/WEB-INF/views/index.jsp" class="btn btn-secondary">Cancel</a>
                        <button type="submit" class="btn btn-danger">Submit</button>
                    </div>
                </form>
            </div>
        </div>
        <div class="card shadow-sm">
            <div class="card-header bg-dark text-white py-3">
                <h2 class="h5 mb-0">My Complaints & Replies</h2>
            </div>
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover table-bordered mb-0 align-middle">
                        <thead class="table-light">
                            <tr>
                                <th style="width: 15%">Date</th>
                                <th style="width: 30%">My Complaint</th>
                                <th style="width: 15%">Status</th>
                                <th style="width: 40%">Staff Reply</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:choose>
                                <c:when test="${empty myComplaints}">
                                    <tr>
                                        <td colspan="4" class="text-center text-muted py-4">
                                            You haven't submitted any complaints yet.
                                        </td>
                                    </tr>
                                </c:when>
                                <c:otherwise>
                                    <c:forEach var="c" items="${myComplaints}">
                                        <tr>
                                            <td>
                                                <fmt:formatDate value="${c.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                            </td>
                                            <td>
                                                <strong><c:out value="${c.title}" /></strong>
                                                <div class="small text-muted mt-1 text-truncate" style="max-width: 250px;">
                                                    <c:out value="${c.content}" />
                                                </div>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${c.status eq 'Đã xử lý'}">
                                                        <span class="badge bg-success">Đã xử lý</span>
                                                    </c:when>
                                                    <c:when test="${c.status eq 'Đang xử lý'}">
                                                        <span class="badge bg-warning text-dark">Đang xử lý</span>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="badge bg-secondary">Chưa xử lý</span>
                                                    </c:otherwise>
                                                </c:choose>
                                            </td>
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not empty c.replyMessage}">
                                                        <div class="p-2 bg-light border border-success rounded" style="color: #0f5132;">
                                                            <strong><c:out value="${c.replyMessage}" /></strong>
                                                        </div>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <span class="text-muted fst-italic">We will reply to your complaint soon.</span>
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