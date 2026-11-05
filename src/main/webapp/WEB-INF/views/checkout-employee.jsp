<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %><%--
    Lists checked-in stays and issued invoices for reception.
--%>
<layout:layout title="Checkout Management" pageCss="checkout.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell ui-checkout-workspace">
        <header class="ui-page-heading">
            <div><p class="ui-eyebrow">FRONT DESK / CHECKOUT</p><h1>Checkout Management</h1><p class="ui-description">Review guest stays and prepare for checkout.</p></div>
        </header>
        <c:if test="${not empty successMessage}">
            <div class="alert alert-dismissible fade show ui-notice ui-notice-success" role="alert">
                <i class="fa-solid fa-circle-check" aria-hidden="true"></i><span><c:out value="${successMessage}" /></span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-dismissible fade show ui-notice ui-notice-danger" role="alert">
                <i class="fa-solid fa-circle-exclamation" aria-hidden="true"></i><span><c:out value="${errorMessage}" /></span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <c:if test="${empty successMessage and empty errorMessage and not empty msg}">
            <div class="alert alert-dismissible fade show ui-notice ui-notice-info" role="alert">
                <i class="fa-solid fa-circle-info" aria-hidden="true"></i><span><c:out value="${msg}" /></span>
                <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
            </div>
        </c:if>
        <section class="ui-panel" aria-label="Bookings awaiting checkout">
            <div class="ui-panel-body">
                <form action="${pageContext.request.contextPath}/CheckoutEmployee" method="get" class="ui-search-form">
                    <div class="ui-field ui-search-field">
                        <label for="checkoutCustomerName">Customer name</label>
                        <input id="checkoutCustomerName" type="text" name="customerName" aria-label="Customer name" placeholder="Search by customer name..." value="<c:out value='${param.customerName}' />">
                    </div>
                    <button type="submit" class="ui-button ui-button-primary"><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i> Search</button>
                </form>
            </div>
            <div class="ui-table-scroll" role="region" aria-label="Checkout list">
                <table class="ui-table checkout-stay-table">
                    <thead><tr><th scope="col">Booking ID</th><th scope="col">Room(s)</th><th scope="col">Customer name</th><th scope="col">Check-in</th><th scope="col">Checkout</th></tr></thead>
                    <tbody>
                        <c:forEach var="room" items="${occupiedRooms}">
                            <c:url var="checkoutDetailUrl" value="/CheckoutEmployee"><c:param name="action" value="detail" /><c:param name="bookingID" value="${room.bookingID}" /></c:url>
                            <tr>
                                <td class="ui-id"><c:out value="${room.bookingID}" /></td>
                                <td><span class="checkout-room-chip"><c:out value="${room.roomNumber}" /></span></td>
                                <td class="ui-name"><c:out value="${room.customerName}" /></td>
                                <td><c:out value="${room.checkInDate}" /></td>
                                <td><a href="<c:out value='${checkoutDetailUrl}' />" class="ui-button ui-button-soft ui-button-small">View Details <i class="fa-solid fa-arrow-right" aria-hidden="true"></i></a></td>
                            </tr>
                        </c:forEach>
                        <c:if test="${empty occupiedRooms}"><tr><td colspan="5" class="ui-empty-state"><i class="fa-solid fa-door-open" aria-hidden="true"></i><p>No bookings are awaiting checkout.</p></td></tr></c:if>
                    </tbody>
                </table>
            </div>
        </section>
    </div>
</layout:layout>
