<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Customer Created Successfully" pageCss="customer.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-shell-success">
        <section class="ui-panel ui-success-panel" aria-labelledby="customerSuccessTitle">
            <span class="ui-success-icon" aria-hidden="true"><i class="fa-solid fa-check"></i></span>
            <h1 id="customerSuccessTitle">Customer Created Successfully!</h1>
            <p class="ui-description">The customer and login account have been created successfully.</p>
            <c:if test="${not empty customer}"><div class="ui-success-customer"><span>Customer</span><strong><c:out value="${customer.fullName}"/></strong></div></c:if>
            <p class="ui-success-question">What would you like to do next?</p>
            <div class="ui-success-actions">
                <c:url var="counterBookingUrl" value="/staff/counter-booking">
                    <c:param name="customerID" value="${customer.customerID}" />
                </c:url>
                <a href="${counterBookingUrl}" class="ui-button ui-button-primary"><i class="fa-solid fa-calendar-check" aria-hidden="true"></i> Create Booking</a>
                <a href="${pageContext.request.contextPath}/customer?action=list" class="ui-button ui-button-secondary">Back to Customer List</a>
            </div>
        </section>
    </div>
</layout:layout>
