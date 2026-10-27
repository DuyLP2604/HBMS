<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<c:set var="editName" value="${empty fn:trim(customerUpdate.fullName) ? sessionScope.user.username : customerUpdate.fullName}" />
<c:url var="profileViewUrl" value="/profile"><c:param name="action" value="view" /></c:url>
<layout:layout title="Edit Profile" pageCss="updateProfile.css" bodyClass="profile-edit-body">
    <div class="profile-edit">
        <header class="pe-heading"><div><p class="pe-eyebrow">YOUR ACCOUNT</p><h1>Edit profile</h1><p>Keep your personal information up to date.</p></div><a href="${profileViewUrl}" class="pe-back-link"><i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back to profile</a></header>
        <div class="pe-layout">
            <aside class="pe-identity" aria-labelledby="editAccountName"><div class="pe-identity-top"><div class="pe-identity-mark"><span class="pe-avatar" aria-hidden="true"><c:out value="${fn:toUpperCase(fn:substring(editName,0,1))}" /></span><span class="pe-role">Customer</span></div><h2 id="editAccountName"><c:out value="${editName}" /></h2></div><dl><div><dt>Username</dt><dd><c:out value="${sessionScope.user.username}" /></dd></div><div><dt>Customer ID</dt><dd><c:out value="${customerUpdate.customerID}" /></dd></div></dl><p class="pe-identity-note"><i class="fa-regular fa-address-card" aria-hidden="true"></i> Your account details, all in one place.</p></aside>
            <section class="pe-form-panel" aria-labelledby="editPersonalTitle"><header class="pe-panel-heading"><span class="pe-panel-icon"><i class="fa-regular fa-pen-to-square" aria-hidden="true"></i></span><div><h2 id="editPersonalTitle">Personal information</h2><p>Fields marked <span aria-hidden="true">*</span> are required.</p></div></header>
                <c:if test="${not empty profileErrorMessage}"><div class="pe-error-summary" role="alert"><i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><span><c:out value="${profileErrorMessage}" /></span></div></c:if>
                <form action="${pageContext.request.contextPath}/profile?action=update" method="post">
                    <div class="pe-fields">
                        <div class="pe-field"><label for="fullname">Full name <span aria-hidden="true">*</span></label><input type="text" id="fullname" name="fullname" value="${fn:escapeXml(customerUpdate.fullName)}" maxlength="100" autocomplete="name" required aria-invalid="${not empty profileErrors.fullname}" aria-describedby="fullnameError"><span class="pe-field-error" id="fullnameError"><c:out value="${profileErrors.fullname}" /></span></div>
                        <div class="pe-field"><label for="nationalityId">Nationality <span aria-hidden="true">*</span></label><select id="nationalityId" name="nationalityId" required aria-invalid="${not empty profileErrors.nationalityId}" aria-describedby="nationalityError"><option value="" disabled ${empty selectedNationalityId ? 'selected' : ''}>Select your nationality</option><c:forEach items="${nationalityUpdate}" var="n"><option value="${fn:escapeXml(n.nationalityID)}" ${selectedNationalityId eq n.nationalityID ? 'selected' : ''}><c:out value="${n.nationalityName}" /></option></c:forEach></select><span class="pe-field-error" id="nationalityError"><c:out value="${profileErrors.nationalityId}" /></span></div>
                        <div class="pe-field"><label for="phone">Phone number</label><input type="tel" id="phone" name="phone" value="${fn:escapeXml(customerUpdate.phone)}" maxlength="15" autocomplete="tel" aria-invalid="${not empty profileErrors.phone}" aria-describedby="phoneError"><span class="pe-field-error" id="phoneError"><c:out value="${profileErrors.phone}" /></span></div>
                        <div class="pe-field"><label for="email">Email address</label><input type="email" id="email" name="email" value="${fn:escapeXml(customerUpdate.email)}" maxlength="100" autocomplete="email" aria-invalid="${not empty profileErrors.email}" aria-describedby="emailError"><span class="pe-field-error" id="emailError"><c:out value="${profileErrors.email}" /></span></div>
                        <div class="pe-field pe-field-wide"><label for="address">Address</label><textarea id="address" name="address" rows="2" maxlength="200" autocomplete="street-address" aria-invalid="${not empty profileErrors.address}" aria-describedby="addressError">${fn:escapeXml(customerUpdate.address)}</textarea><span class="pe-field-error" id="addressError"><c:out value="${profileErrors.address}" /></span></div>
                    </div><div class="pe-actions"><a href="${profileViewUrl}" class="pe-button pe-button-secondary">Cancel</a><button type="submit" class="pe-button pe-button-primary"><i class="fa-solid fa-check" aria-hidden="true"></i> Save changes</button></div>
                </form>
            </section>
        </div>
    </div>
</layout:layout>
