<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Update Customer Information" pageCss="customer.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-shell-form">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">GUEST DIRECTORY</p><h1>Update Customer Information</h1><p class="ui-description">Review and update the customer's personal details.</p></div></header>
        <section class="ui-panel" aria-label="Update customer information">
            <form action="${pageContext.request.contextPath}/customer?action=update" method="post">
                <div class="ui-panel-body">
                    <div class="ui-section-heading"><h2>Personal information</h2><p>Fields marked with * are required.</p></div>
                    <div class="ui-form-grid">
                        <div class="ui-field"><label for="customerID">Customer ID (Read-only)</label><input type="text" id="customerID" name="id" value="${fn:escapeXml(customer.customerID)}" readonly></div>
                        <div class="ui-field"><label for="name">Full Name <span class="ui-required">*</span></label><input type="text" id="name" name="name" value="${fn:escapeXml(customer.fullName)}" maxlength="100" required></div>
                        <div class="ui-field"><label for="phone">Phone Number</label><input type="tel" id="phone" name="phone" value="${fn:escapeXml(customer.phone)}" maxlength="15"></div>
                        <div class="ui-field"><label for="email">Email</label><input type="email" id="email" name="email" value="${fn:escapeXml(customer.email)}" maxlength="100"></div>
                        <div class="ui-field ui-field-full"><label for="address">Address</label><input type="text" id="address" name="address" value="${fn:escapeXml(customer.address)}" maxlength="200"></div>
                        <div class="ui-field ui-field-full">
                            <label for="nation">Nationality <span class="ui-required">*</span></label>
                            <select id="nation" name="nation" required>
                                <option value="" disabled ${empty customer.nationalityID ? 'selected' : ''}>Select nationality</option>
                                <c:forEach var="nat" items="${listNat}"><option value="${fn:escapeXml(nat.nationalityID)}" ${nat.nationalityID eq customer.nationalityID.nationalityID ? 'selected' : ''}><c:out value="${nat.nationalityName}" /></option></c:forEach>
                            </select>
                        </div>
                    </div>
                </div>
                <div class="ui-panel-footer ui-form-actions"><a href="${pageContext.request.contextPath}/customer?action=list" class="ui-button ui-button-secondary">Back</a><button type="submit" class="ui-button ui-button-primary"><i class="fa-solid fa-check" aria-hidden="true"></i> Update Customer</button></div>
            </form>
        </section>
    </div>
</layout:layout>
