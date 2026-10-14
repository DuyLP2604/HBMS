<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<layout:layout title="Submit Complaint" pageCss="complaint.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container-fluid px-lg-5 my-4">
        <div class="row g-4">
            <div class="col-lg-5 col-md-6">
                <div class="card shadow-sm sticky-top" style="top: 20px; z-index: 1;">
                    <div class="card-header bg-danger text-white py-3">
                        <h2 class="h5 mb-0">Submit Complaint</h2>
                    </div>
                    <div class="card-body">

                        <c:if test="${not empty errorMessage}">
                            <div class="alert alert-danger py-2"><c:out value="${errorMessage}"/></div>
                        </c:if>
                        <c:if test="${not empty flashMsg}">
                            <div class="alert alert-success py-2"><c:out value="${flashMsg}"/></div>
                        </c:if>

                        <form action="${pageContext.request.contextPath}/complaint" method="post">
                            <input type="hidden" name="action" value="add">

                            <div class="mb-5">
                                <label class="form-label fw-bold">Title <span class="text-danger">*</span></label>
                                <input type="text" name="title" class="form-control" required placeholder="Tóm tắt vấn đề...">
                            </div>

                            <div class="mb-5">
                                <label class="form-label fw-bold">Complaint Details <span class="text-danger">*</span></label>
                                <textarea name="content" class="form-control" rows="24" required placeholder="Mô tả chi tiết sự việc..."></textarea>
                            </div>

                            <div class="d-flex justify-content-end gap-2 mt-2">
                                <a href="${pageContext.request.contextPath}/home" class="btn btn-secondary">Cancel</a>
                                <button type="submit" class="btn btn-danger px-4">Submit</button>
                            </div>

                        </form>
                    </div>
                </div>
            </div>

            <div class="col-lg-7 col-md-6">
                <div class="card shadow-sm">
                    <div class="card-header bg-dark text-white py-3">
                        <h2 class="h5 mb-0">My Complaints & Replies</h2>
                    </div>
                    <div class="card-body p-0">
                        <div class="table-responsive" style="max-height: 80vh; overflow-y: auto;">
                            <table class="table table-hover table-bordered mb-0 align-middle">
                                <thead class="table-light sticky-top" style="z-index: 0;">
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
                                                <td colspan="4" class="text-center text-muted py-5">
                                                    <i class="fa-regular fa-comment-dots fa-3x mb-3 text-light"></i><br>
                                                    You haven't submitted any complaints yet.
                                                </td>
                                            </tr>
                                        </c:when>
                                        <c:otherwise>
                                            <c:forEach var="c" items="${myComplaints}">
                                                <tr>

                                                    <td class="text-muted small">
                                                        <fmt:formatDate value="${c.createdAt}" pattern="dd/MM/yyyy HH:mm" />
                                                    </td>

                                                    <td>
                                                        <strong class="d-block mb-1 text-dark"><c:out value="${c.title}" /></strong>
                                                        <div class="small text-muted" style="display: -webkit-box; -webkit-line-clamp: 3; -webkit-box-orient: vertical; overflow: hidden;">
                                                            <c:out value="${c.content}" />
                                                        </div>
                                                    </td>

                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${c.status eq 'Đã xử lý'}">
                                                                <span class="badge bg-success w-100">Đã xử lý</span>
                                                            </c:when>
                                                            <c:when test="${c.status eq 'Đang xử lý'}">
                                                                <span class="badge bg-warning text-dark w-100">Đang xử lý</span>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="badge bg-secondary w-100">Chưa xử lý</span>
                                                            </c:otherwise>
                                                        </c:choose>
                                                    </td>

                                                    <td>
                                                        <c:choose>
                                                            <c:when test="${not empty c.replyMessage}">
                                                                <div class="p-2 bg-light border-start border-4 border-success rounded-end" style="color: #0f5132; font-size: 0.95rem;">
                                                                    <c:out value="${c.replyMessage}" />
                                                                </div>
                                                            </c:when>
                                                            <c:otherwise>
                                                                <span class="text-muted fst-italic small">We are reviewing and will reply soon.</span>
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

        </div>
    </div>
</layout:layout>