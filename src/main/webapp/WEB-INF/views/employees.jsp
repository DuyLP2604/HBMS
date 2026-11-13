<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows the employee list and account management actions.
--%>
<layout:layout title="Employee List" pageCss="employee.css" pageJs="searchEmployee.js" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-employee-workspace">
        <header class="ui-page-heading">
            <div><p class="ui-eyebrow">STAFF DIRECTORY</p><h1>Employee List</h1><p class="ui-description">Find and manage employee information.</p></div>
            <c:if test="${canManageAccounts}"><a href="${pageContext.request.contextPath}/employee?action=add" class="ui-button ui-button-primary"><i class="fa-solid fa-plus" aria-hidden="true"></i> Add Employee</a></c:if>
        </header>
        <section class="ui-panel" aria-label="Employee directory">
            <div class="ui-panel-body"><form action="${pageContext.request.contextPath}/employee" method="get" id="searchForm">
                            <input type="hidden" name="action" value="list">
                            <div class="employee-search-layout">
                                <!-- SEARCH TYPE -->
                                <div class="ui-field"><label for="searchType">Search by</label>
                                    <select name="searchType" id="searchType" class="form-select">
                                        <option value="name" ${param.searchType == 'name' || empty param.searchType ? 'selected' : ''}>Full Name</option>
                                        <option value="position" ${param.searchType == 'position' ? 'selected' : ''}>Position</option>
                                        <option value="shift" ${param.searchType == 'shift' ? 'selected' : ''}>Work Shift</option>
                                    </select>
                                </div>
                                <!-- SEARCH VALUE -->
                                <div class="ui-field"><span class="employee-search-label" id="employeeSearchValueLabel">Search value</span>
                                    <!-- FULL NAME INPUT -->
                                    <input type="text" id="inputKeyword" aria-labelledby="employeeSearchValueLabel" class="form-control" placeholder="Enter employee name..." value="<c:out value='${param.keyword}' />">
                                    <!-- POSITION DROPDOWN -->
                                    <select id="selectPosition" aria-labelledby="employeeSearchValueLabel" class="form-select d-none">
                                        <option value="">-- Select Position --</option>
                                        <c:forEach var="pos" items="${positions}">
                                            <option value="${fn:escapeXml(pos)}" ${param.keyword == pos ? 'selected' : ''}><c:out value="${pos}" /></option>
                                        </c:forEach>
                                    </select>
                                    <!-- SHIFT DROPDOWN -->
                                    <select id="selectShift" aria-labelledby="employeeSearchValueLabel" class="form-select d-none">
                                        <option value="">-- Select Work Shift --</option>
                                        <c:forEach var="s" items="${shifts}">
                                            <option value="${fn:escapeXml(s)}" ${param.keyword == s ? 'selected' : ''}><c:out value="${s}" /></option>
                                        </c:forEach>
                                    </select>
                                    <!-- REAL VALUE SENT TO SERVLET -->
                                    <input type="hidden" name="keyword" id="realKeyword" value="<c:out value='${param.keyword}' />">
                                </div>
                                <!-- BUTTON -->
                                <div class="employee-search-actions">
                                    <button type="submit" class="ui-button ui-button-primary"><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i> Search</button>
                                    <c:if test="${not empty param.keyword}">
                                        <a href="${pageContext.request.contextPath}/employee?action=list" class="ui-button ui-button-secondary">Clear Filter</a>
                                    </c:if>
                                </div>
                            </div>
                        </form></div>
            <div class="ui-table-scroll" role="region" aria-label="Employee list">
                <table class="ui-table employee-directory-table">
                    <thead><tr><th scope="col">Employee ID</th><th scope="col">Full Name</th><th scope="col">Email</th><th scope="col">Position</th><th scope="col">Work Shift</th>
                        <c:if test="${canManageAccounts}"><th scope="col">Account Status</th></c:if>
                        <th scope="col">Action</th></tr></thead>
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
                                        <td class="ui-id"><c:out value="${e.employeeID}" /></td>
                                        <td><c:out value="${e.fullName}" /></td>
                                        <td class="employee-email-cell"><c:out value="${empty e.email ? '—' : e.email}" /></td>
                                        <td>
                                            <span class="employee-position-badge"><c:out value="${e.position}" /></span>
                                        </td>
                                        <td>
                                            <span class="employee-shift-badge"><c:out value="${e.shift}" /></span>
                                        </td>
                                        <c:set var="staffAccount" value="${e.userID.role eq 'Staff'}" />
                                        <c:if test="${canManageAccounts}">
                                            <td>
                                                <c:choose>
                                                    <c:when test="${not staffAccount}">—</c:when>
                                                    <c:when test="${e.userID.active}"><span class="ui-status">Active</span></c:when>
                                                    <c:otherwise><span class="ui-status employee-account-inactive">Deactivated</span></c:otherwise>
                                                </c:choose>
                                            </td>
                                        </c:if>
                                        <td class="employee-action-cell">
                                            <a href="${pageContext.request.contextPath}/employee?action=viewDetail&id=${e.employeeID}" class="ui-button ui-button-soft ui-button-small">View Details</a>
                                            <a href="${pageContext.request.contextPath}/employee?action=update&id=${e.employeeID}" class="ui-button ui-button-secondary ui-button-small">Edit</a>
                                            <c:if test="${canManageAccounts and staffAccount}">
                                                <c:set var="accountActive" value="${e.userID.active}" />
                                                <form action="${pageContext.request.contextPath}/employee" method="post" class="d-inline-block m-0"
                                                      onsubmit="return confirm('${accountActive ? 'Deactivate this staff account? The employee will lose access to HBMS.' : 'Reactivate this staff account? The employee will be able to sign in again.'}');">
                                                    <input type="hidden" name="action" value="${accountActive ? 'deactivateAccount' : 'reactivateAccount'}">
                                                    <input type="hidden" name="id" value="${fn:escapeXml(e.employeeID)}">
                                                    <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                                                    <button type="submit" class="ui-button ui-button-small ${accountActive ? 'employee-deactivate-button' : 'ui-button-primary'}">
                                                        ${accountActive ? 'Deactivate' : 'Reactivate'}
                                                    </button>
                                                </form>
                                            </c:if>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${empty employees || matchCount == 0}">
                                <tr>
                                    <td colspan="${canManageAccounts ? 7 : 6}" class="ui-empty-state">No employees found.</td>
                                </tr>
                            </c:if>
                        </tbody>
                </table>
            </div>
        </section>
    </div>
</layout:layout>
