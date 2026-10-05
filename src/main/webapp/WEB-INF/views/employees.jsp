<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Employee List" pageCss="employee.css" pageJs="searchEmployee.js" useBootstrap="true" bodyClass="bg-light" >
    <div class="container my-5">
        <div class="card shadow-sm">
            <!-- HEADER -->
            <div class="card-header employee-header text-white d-flex justify-content-between align-items-center py-3">
                <h2 class="h4 mb-0">Employee List</h2>
                <a href="${pageContext.request.contextPath}/employee?action=add" class="btn btn-warning btn-sm fw-semibold">+ Add Employee</a>
            </div>
            <div class="card-body">
                <!-- SEARCH -->
                <div class="row mb-4">
                    <div class="col-12">
                        <form action="${pageContext.request.contextPath}/employee" method="get" id="searchForm">
                            <input type="hidden" name="action" value="list">
                            <div class="row g-2 align-items-center">
                                <!-- SEARCH TYPE -->
                                <div class="col-md-3">
                                    <select name="searchType" id="searchType" class="form-select">
                                        <option value="name" ${param.searchType == 'name' || empty param.searchType ? 'selected' : ''}>Full Name</option>
                                        <option value="position" ${param.searchType == 'position' ? 'selected' : ''}>Position</option>
                                        <option value="shift" ${param.searchType == 'shift' ? 'selected' : ''}>Work Shift</option>
                                    </select>
                                </div>
                                <!-- SEARCH VALUE -->
                                <div class="col-md-6">
                                    <!-- FULL NAME INPUT -->
                                    <input type="text" id="inputKeyword" class="form-control" placeholder="Enter employee name..." value="<c:out value='${param.keyword}' />">
                                    <!-- POSITION DROPDOWN -->
                                    <select id="selectPosition" class="form-select d-none">
                                        <option value="">-- Select Position --</option>
                                        <c:forEach var="pos" items="${positions}">
                                            <option value="${pos}" ${param.keyword == pos ? 'selected' : ''}>${pos}</option>
                                        </c:forEach>
                                    </select>
                                    <!-- SHIFT DROPDOWN -->
                                    <select id="selectShift" class="form-select d-none">
                                        <option value="">-- Select Work Shift --</option>
                                        <c:forEach var="s" items="${shifts}">
                                            <option value="${s}" ${param.keyword == s ? 'selected' : ''}>${s}</option>
                                        </c:forEach>
                                    </select>
                                    <!-- REAL VALUE SENT TO SERVLET -->
                                    <input type="hidden" name="keyword" id="realKeyword" value="<c:out value='${param.keyword}' />">
                                </div>
                                <!-- BUTTON -->
                                <div class="col-md-3 d-flex gap-2">
                                    <button type="submit" class="btn employee-btn text-white flex-grow-1">Search</button>
                                    <c:if test="${not empty param.keyword}">
                                        <a href="${pageContext.request.contextPath}/employee?action=list" class="btn btn-secondary">Clear Filter</a>
                                    </c:if>
                                </div>
                            </div>
                        </form>
                    </div>
                </div>
                <!-- EMPLOYEE TABLE -->
                <div class="table-responsive">
                    <table class="table table-hover align-middle border">
                        <thead class="table-light">
                            <tr>
                                <th>Employee ID</th>
                                <th>Full Name</th>
                                <th>Position</th>
                                <th>Work Shift</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:set var="matchCount" value="0" />
                            <c:forEach var="e" items="${employees}">
                                <c:set var="kw" value="${fn:toLowerCase(fn:trim(param.keyword))}" />
                                <c:set var="isMatch" value="false" />
                                <c:choose>
                                    <c:when test="${empty kw}">
                                        <c:set var="isMatch" value="true" />
                                    </c:when>
                                    <c:when test="${param.searchType == 'position'}">
                                        <c:if test="${fn:contains(fn:toLowerCase(fn:trim(e.position)), kw)}">
                                            <c:set var="isMatch" value="true" />
                                        </c:if>
                                    </c:when>
                                    <c:when test="${param.searchType == 'shift'}">
                                        <c:if test="${fn:contains(fn:toLowerCase(fn:trim(e.shift)), kw)}">
                                            <c:set var="isMatch" value="true" />
                                        </c:if>
                                    </c:when>
                                    <c:otherwise>
                                        <c:if test="${fn:contains(fn:toLowerCase(fn:trim(e.fullName)), kw)}">
                                            <c:set var="isMatch" value="true" />
                                        </c:if>
                                    </c:otherwise>
                                </c:choose>
                                <c:if test="${isMatch}">
                                    <c:set var="matchCount" value="${matchCount + 1}" />
                                    <tr>
                                        <td class="fw-bold text-secondary"><c:out value="${e.employeeID}" /></td>
                                        <td><c:out value="${e.fullName}" /></td>
                                        <td>
                                            <span class="badge bg-secondary-subtle text-secondary-emphasis"><c:out value="${e.position}" /></span>
                                        </td>
                                        <td>
                                            <span class="badge bg-info-subtle text-info-emphasis px-2 py-1"><c:out value="${e.shift}" /></span>
                                        </td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/employee?action=viewDetail&id=${e.employeeID}" class="btn btn-outline-info btn-sm me-1">View Details</a>
                                            <a href="${pageContext.request.contextPath}/employee?action=update&id=${e.employeeID}" class="btn btn-outline-warning btn-sm">Edit</a>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${empty employees || matchCount == 0}">
                                <tr>
                                    <td colspan="5" class="text-center text-muted py-4">No employees found.</td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</layout:layout>