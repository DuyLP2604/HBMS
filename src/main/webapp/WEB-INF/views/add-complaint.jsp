<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>

<layout:layout title="Submit Complaint" pageCss="group5.css" useBootstrap="true" bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace">
        <div class="row g-4">
            <div class="col-12">
                <div class="card g5-panel">
                    <div class="card-header g5-section-header">
                        <h1 class="h4 mb-0">Submit Complaint</h1>
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

                            <div class="mb-3">
                                <label for="complaintTitle" class="form-label">Title <span class="text-danger">*</span></label>
                                <input type="text" id="complaintTitle" name="title" class="form-control" required placeholder="Tóm tắt vấn đề...">
                            </div>

                            <div class="mb-3">
                                <label for="complaintContent" class="form-label">Complaint Details <span class="text-danger">*</span></label>
                                <textarea id="complaintContent" name="content" class="form-control" rows="6" required placeholder="Mô tả chi tiết sự việc..."></textarea>
                            </div>

                            <div class="g5-actions mt-2">
                                <a href="${pageContext.request.contextPath}/home" class="btn g5-secondary">Cancel</a>
                                <button type="submit" class="btn g5-primary px-4">Submit</button>
                            </div>

                        </form>
                    </div>
                </div>
            </div>

            <div class="col-12">
                <div class="card g5-panel">
                    <div class="card-header g5-section-header">
                        <h2 class="h5 mb-0">My Complaints & Replies</h2>
                    </div>
                    <div class="card-body p-0">
                        <c:choose>
                            <c:when test="${empty myComplaints}">
                                <p class="text-center text-muted p-4 mb-0">You haven't submitted any complaints yet.</p>
                            </c:when>
                            <c:otherwise>
                                <ul class="list-unstyled g5-history mb-0">
                                    <c:forEach var="c" items="${myComplaints}">
                                        <li class="g5-history-item">
                                            <div class="g5-history-heading">
                                                <div>
                                                    <h3><c:out value="${c.title}" /></h3>
                                                    <span class="text-muted small"><fmt:formatDate value="${c.createdAt}" pattern="dd/MM/yyyy HH:mm" /></span>
                                                </div>
                                                <div>
                                                    <c:choose>
                                                        <c:when test="${c.status eq 'Đã xử lý'}">
                                                            <span class="badge g5-status-success w-100">Đã xử lý</span>
                                                        </c:when>
                                                        <c:when test="${c.status eq 'Đang xử lý'}">
                                                            <span class="badge g5-status-pending w-100">Đang xử lý</span>
                                                        </c:when>
                                                        <c:otherwise>
                                                            <span class="badge g5-status-neutral w-100">Chưa xử lý</span>
                                                        </c:otherwise>
                                                    </c:choose>
                                                </div>
                                            </div>
                                            <p class="g5-message"><c:out value="${c.content}" /></p>
                                            <h4>Staff Reply</h4>
                                            <c:choose>
                                                <c:when test="${not empty c.replyMessage}">
                                                    <div class="g5-message">
                                                        <c:out value="${c.replyMessage}" />
                                                    </div>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="text-muted fst-italic small">We are reviewing and will reply soon.</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </li>
                                    </c:forEach>
                                </ul>
                            </c:otherwise>
                        </c:choose>
                    </div>
                </div>
            </div>

        </div>
    </div>
</layout:layout>
