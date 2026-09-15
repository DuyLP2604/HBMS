<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Complaint Details"
    pageCss="complaint.css"
    useBootstrap="true"
    bodyClass="bg-light"

    >

    <div class="container my-5 complaint-detail-container">

        <div class="card shadow-sm">

            <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center py-3">

                <h2 class="h4 mb-0">
                    Complaint Details #
                    <c:out value="${complaint.id}" />
                </h2>

                <a
                    href="${pageContext.request.contextPath}/complaint?action=list"
                    class="btn btn-light btn-sm"
                    >
                    Back
                </a>

            </div>


            <div class="card-body">

                <dl class="row mb-0">

                    <dt class="col-sm-3">
                        Title
                    </dt>

                    <dd class="col-sm-9">
                        <c:out value="${complaint.title}" />
                    </dd>


                    <dt class="col-sm-3">
                        Customer
                    </dt>

                    <dd class="col-sm-9">

                        <c:choose>

                            <c:when test="${empty complaint.customerName}">
                                Anonymous
                            </c:when>

                            <c:otherwise>
                                <c:out value="${complaint.customerName}" />
                            </c:otherwise>

                        </c:choose>

                    </dd>


                    <dt class="col-sm-3">
                        Created At
                    </dt>

                    <dd class="col-sm-9">

                        <fmt:formatDate
                            value="${complaint.createdAt}"
                            pattern="dd/MM/yyyy HH:mm"
                            />

                    </dd>


                    <dt class="col-sm-3">
                        Content
                    </dt>

                    <dd class="col-sm-9 complaint-content">
                        <c:out value="${complaint.content}" />
                    </dd>


                    <dt class="col-sm-3">
                        Status
                    </dt>

                    <dd class="col-sm-9">

                        <c:choose>

                            <c:when test="${complaint.status eq 'Chưa xử lý'}">
                                Pending
                            </c:when>

                            <c:when test="${complaint.status eq 'Đang xử lý'}">
                                In Progress
                            </c:when>

                            <c:when test="${complaint.status eq 'Đã xử lý'}">
                                Resolved
                            </c:when>

                            <c:otherwise>
                                <c:out value="${complaint.status}" />
                            </c:otherwise>

                        </c:choose>

                    </dd>

                </dl>


                <hr>


                <form
                    action="${pageContext.request.contextPath}/complaint"
                    method="post"
                    class="d-flex gap-2 align-items-end"
                    >

                    <input
                        type="hidden"
                        name="action"
                        value="updateStatus"
                        >

                    <input
                        type="hidden"
                        name="id"
                        value="${complaint.id}"
                        >


                    <div class="flex-grow-1">

                        <label class="form-label">
                            Update Status
                        </label>

                        <select
                            name="status"
                            class="form-select"
                            required
                            >

                            <option
                                value="Chưa xử lý"
                                ${complaint.status eq 'Chưa xử lý' ? 'selected' : ''}
                                >
                                Pending
                            </option>

                            <option
                                value="Đang xử lý"
                                ${complaint.status eq 'Đang xử lý' ? 'selected' : ''}
                                >
                                In Progress
                            </option>

                            <option
                                value="Đã xử lý"
                                ${complaint.status eq 'Đã xử lý' ? 'selected' : ''}
                                >
                                Resolved
                            </option>

                        </select>

                    </div>


                    <button
                        type="submit"
                        class="btn btn-danger"
                        >
                        Update
                    </button>

                </form>

            </div>

        </div>

    </div>
</layout:layout>
