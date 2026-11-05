<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Shows customer information for staff.
--%>
<layout:layout title="Customer Details" pageCss="customer.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-shell-detail">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">GUEST DIRECTORY</p><h1>Customer Details</h1><p class="ui-description">Personal and contact information.</p></div></header>
        <section class="ui-panel" aria-label="Customer Profile">
            <div class="ui-panel-body ui-customer-profile">
                <div class="ui-profile-identity"><span class="ui-avatar" aria-hidden="true"><i class="fa-regular fa-user"></i></span><h2><c:out value="${customer.fullName}" /></h2><span class="ui-id"><c:out value="${customer.customerID}" /></span></div>
                <dl class="ui-facts">
                    <div><dt>Full Name</dt><dd><c:out value="${customer.fullName}" /></dd></div>
                    <div><dt>Phone Number</dt><dd><c:out value="${empty customer.phone ? 'Not provided' : customer.phone}" /></dd></div>
                    <div><dt>Email</dt><dd><c:out value="${empty customer.email ? 'Not provided' : customer.email}" /></dd></div>
                    <div><dt>Nationality</dt><dd><c:out value="${empty customer.nationalityID ? 'Not provided' : customer.nationalityID.nationalityName}" /></dd></div>
                    <div class="ui-field-full"><dt>Address</dt><dd><c:out value="${empty customer.address ? 'Not provided' : customer.address}" /></dd></div>
                </dl>
            </div>
            <div class="ui-panel-footer"><a href="${pageContext.request.contextPath}/customer?action=list" class="ui-button ui-button-secondary"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to Customer List</a></div>
        </section>
    </div>
</layout:layout>
