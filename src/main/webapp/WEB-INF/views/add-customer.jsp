<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Creates a customer record at the front desk.
--%>
<layout:layout title="Add New Customer" pageCss="customer.css" pageJs="addCustomer.js" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-shell-form">
        <header class="ui-page-heading"><div><p class="ui-eyebrow">GUEST DIRECTORY</p><h1>Add New Customer</h1><p class="ui-description">Enter the customer's personal and contact information.</p></div></header>
        <section class="ui-panel" aria-label="New customer information">
            <form id="addCustomerForm" action="${pageContext.request.contextPath}/customer?action=add" method="post">
                <input type="hidden" name="createAccount" value="true">
                <c:if test="${param.flow eq 'counter'}"><input type="hidden" name="flow" value="counter"><input type="hidden" name="csrfToken" value="${fn:escapeXml(sessionScope.counterCsrf)}"></c:if>
                <div class="ui-panel-body">
                    <div class="ui-section-heading"><h2>Personal information</h2><p>Fields marked with * are required.</p></div>
                    <div class="ui-form-grid">
                        <div class="ui-field ui-field-full"><label for="name">Full Name <span class="ui-required">*</span></label><input type="text" id="name" name="name" value="${fn:escapeXml(sessionScope.counterRegistration.name)}" maxlength="100" placeholder="Enter full name..." required></div>
                        <div class="ui-field"><label for="phone">Phone Number <span class="ui-required">*</span></label><input type="tel" id="phone" name="phone" value="${fn:escapeXml(empty sessionScope.counterRegistration ? param.phone : sessionScope.counterRegistration.phone)}" maxlength="15" placeholder="Enter phone number..." required></div>
                        <div class="ui-field"><label for="email">Email</label><input type="email" id="email" name="email" value="${fn:escapeXml(sessionScope.counterRegistration.email)}" maxlength="100" placeholder="Enter email..."></div>
                        <div class="ui-field ui-field-full"><label for="address">Address</label><input type="text" id="address" name="address" value="${fn:escapeXml(sessionScope.counterRegistration.address)}" maxlength="200" placeholder="Enter address..."></div>
                        <div class="ui-field ui-field-full">
                            <label for="nation">Nationality <span class="ui-required">*</span></label>
                            <select id="nation" name="nation" required>
                                <option value="" disabled selected>Select nationality</option>
                                <c:forEach items="${listNat}" var="n"><option value="${fn:escapeXml(n.nationalityID)}" ${sessionScope.counterRegistration.nation eq n.nationalityID ? 'selected' : ''}><c:out value="${n.nationalityName}" /></option></c:forEach>
                            </select>
                        </div>
                    </div>
                    <div class="ui-form-section">
                        <div class="ui-section-heading"><h2>Login Account</h2><p>Create login credentials for this customer.</p></div>
                        <div class="ui-form-grid">
                            <div class="ui-field"><label for="username">Username <span class="ui-required">*</span></label><input type="text" id="username" name="username" value="${fn:escapeXml(empty sessionScope.counterRegistration ? param.phone : sessionScope.counterRegistration.phone)}" maxlength="50" autocomplete="off" placeholder="Automatically filled from phone number" readonly required></div>
                            <div class="ui-field"><label for="password">Password <span class="ui-required">*</span></label><input type="password" id="password" name="password" autocomplete="new-password" placeholder="Enter password..." required></div>
                        </div>
                    </div>
                </div>
                <div class="ui-panel-footer ui-form-actions"><a href="${pageContext.request.contextPath}/customer?action=list" class="ui-button ui-button-secondary">Cancel</a><button type="submit" class="ui-button ui-button-primary"><i class="fa-solid fa-check" aria-hidden="true"></i> Save Customer</button></div>
            </form>
        </section>
    </div>
</layout:layout>
