<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows the selected employee and staff account information.
--%>
<layout:layout title="Employee Details" pageCss="employee.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <fmt:setLocale value="vi_VN" scope="page" />
    <div class="hbms-ui ui-shell ui-shell-detail ui-employee-workspace">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">STAFF DIRECTORY</p><h1>Employee Details</h1><p class="ui-description">Personal and employment information.</p></div></header>
        <section class="ui-panel" aria-label="Employee Profile">
            <div class="ui-panel-body employee-profile-layout">
                <div class="employee-profile-identity">
                    <span class="ui-avatar" aria-hidden="true"><i class="fa-regular fa-user"></i></span>
                    <h2><c:out value="${employee.fullName}" /></h2>
                    <span class="ui-id"><c:out value="${employee.employeeID}" /></span>
                    <div class="employee-profile-position"><span class="employee-position-badge"><c:out value="${employee.position}" /></span></div>
                </div>
                <div class="employee-profile-information">
                    <section aria-labelledby="employeeWorkTitle">
                        <h2 class="employee-section-title" id="employeeWorkTitle">Employment Details</h2>
                        <dl class="ui-facts">
                            <div><dt>Base Salary</dt><dd class="employee-salary"><c:choose><c:when test="${not empty employee.salary}"><fmt:formatNumber value="${employee.salary}" type="number" groupingUsed="true" maxFractionDigits="0" /> ₫</c:when><c:otherwise>—</c:otherwise></c:choose></dd></div>
                            <div><dt>Work Shift</dt><dd><span class="employee-shift-badge"><c:out value="${employee.shift}" default="—" /></span></dd></div>
                        </dl>
                    </section>
                    <section class="employee-profile-contact" aria-labelledby="employeeContactTitle">
                        <h2 class="employee-section-title" id="employeeContactTitle">Contact Information</h2>
                        <dl class="ui-facts">
                            <div><dt>Phone Number</dt><dd><c:out value="${employee.phone}" default="—" /></dd></div>
                            <div><dt>Email</dt><dd><c:out value="${empty employee.email ? '—' : employee.email}" /></dd></div>
                            <div class="ui-field-full"><dt>Address</dt><dd><c:out value="${employee.address}" default="—" /></dd></div>
                        </dl>
                    </section>
                </div>
            </div>
            <c:if test="${sessionScope.user.role eq 'Admin' and employee.userID.role eq 'Staff' and employee.userID.active and employee.userID.mustChangePassword}">
                <section class="employee-account-section" aria-label="Staff login credentials">
                    <div class="ui-notice ui-notice-info">This account is waiting for its first password change. Resending generates a new temporary password and invalidates earlier temporary credentials.</div>
                    <form action="${pageContext.request.contextPath}/employee" method="post">
                        <input type="hidden" name="action" value="resendCredentials">
                        <input type="hidden" name="id" value="${fn:escapeXml(employee.employeeID)}">
                        <input type="hidden" name="csrfToken" value="${fn:escapeXml(csrfToken)}">
                        <button type="submit" class="ui-button ui-button-soft"><i class="fa-regular fa-envelope" aria-hidden="true"></i> Resend login email</button>
                    </form>
                </section>
            </c:if>
            <div class="ui-panel-footer"><a href="${pageContext.request.contextPath}/employee?action=list" class="ui-button ui-button-secondary"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to Employee List</a></div>
        </section>
    </div>
</layout:layout>
