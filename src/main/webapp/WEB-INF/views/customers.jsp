<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Customer List" pageCss="customer.css" useBootstrap="true" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell">
        <header class="ui-page-heading">
            <div><p class="ui-eyebrow">GUEST DIRECTORY</p><h1>Customer List</h1><p class="ui-description">Find and manage customer information.</p></div>
            <c:if test="${sessionScope.user.role eq 'Staff'}">
                <a href="${pageContext.request.contextPath}/customer?action=add" class="ui-button ui-button-primary"><i class="fa-solid fa-plus" aria-hidden="true"></i> Add Customer</a>
            </c:if>
        </header>
        <section class="ui-panel" aria-label="Customer directory">
            <div class="ui-panel-body">
                <form action="${pageContext.request.contextPath}/customer" method="get" class="ui-search-form">
                    <input type="hidden" name="action" value="list">
                    <div class="ui-field ui-search-field">
                        <label for="customerKeyword">Customer name or phone number</label>
                        <input id="customerKeyword" type="text" name="keyword" value="<c:out value='${keyword}' />" placeholder="Enter customer name or phone number...">
                    </div>
                    <div class="ui-field ui-customer-page-size">
                        <label for="customerPageSize">Customers per page</label>
                        <select id="customerPageSize" name="pageSize" onchange="this.form.submit()">
                            <option value="10" ${pageSize eq 10 ? 'selected' : ''}>10</option>
                            <option value="20" ${pageSize eq 20 ? 'selected' : ''}>20</option>
                            <option value="50" ${pageSize eq 50 ? 'selected' : ''}>50</option>
                            <option value="100" ${pageSize eq 100 ? 'selected' : ''}>100</option>
                        </select>
                    </div>
                    <button type="submit" class="ui-button ui-button-primary"><i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i> Search</button>
                    <c:if test="${not empty keyword}">
                        <c:url var="clearCustomerFilterUrl" value="/customer"><c:param name="action" value="list"/><c:param name="pageSize" value="${pageSize}"/></c:url>
                        <a href="${clearCustomerFilterUrl}" class="ui-button ui-button-secondary">Clear Filter</a>
                    </c:if>
                </form>
            </div>
            <div class="ui-table-scroll" role="region" aria-label="Customer list">
                <table class="ui-table ui-customer-table">
                    <thead><tr><th scope="col">Customer ID</th><th scope="col">Full Name</th><th scope="col">Phone Number</th><th scope="col">Nationality</th><th scope="col">Action</th></tr></thead>
                    <tbody>
                        <c:forEach var="c" items="${customers}">
                                <tr>
                                    <td class="ui-id"><c:out value="${c.customerID}" /></td>
                                    <td class="ui-name"><c:out value="${c.fullName}" /></td>
                                    <td><c:out value="${c.phone}" /></td>
                                    <td><c:out value="${c.nationalityID.nationalityName}" /></td>
                                    <td><div class="ui-row-actions">
                                        <a href="${pageContext.request.contextPath}/customer?action=viewDetail&id=${c.customerID}" class="ui-button ui-button-soft ui-button-small">View Details</a>
                                        <c:if test="${sessionScope.user.role eq 'Staff'}"><a href="${pageContext.request.contextPath}/customer?action=update&id=${c.customerID}" class="ui-button ui-button-secondary ui-button-small">Edit</a></c:if>
                                    </div></td>
                                </tr>
                        </c:forEach>
                        <c:if test="${empty customers}"><tr><td colspan="5" class="ui-empty-state"><i class="fa-regular fa-address-book" aria-hidden="true"></i><p>No customers found.</p></td></tr></c:if>
                    </tbody>
                </table>
            </div>
            <div class="ui-panel-footer ui-customer-pagination">
                <p class="ui-customer-count" id="customerResultCount">
                    Showing <strong><c:out value="${startIndex}"/>–<c:out value="${endIndex}"/></strong>
                    of <strong><c:out value="${totalCustomers}"/></strong> customers
                </p>
                <c:if test="${totalPages gt 1}">
                    <nav aria-label="Customer list pages">
                        <ul class="ui-customer-pages">
                            <li>
                                <c:choose>
                                    <c:when test="${currentPage gt 1}">
                                        <c:url var="previousCustomerPageUrl" value="/customer">
                                            <c:param name="action" value="list"/><c:param name="keyword" value="${keyword}"/>
                                            <c:param name="pageSize" value="${pageSize}"/><c:param name="page" value="${currentPage - 1}"/>
                                        </c:url>
                                        <a href="${previousCustomerPageUrl}" class="ui-button ui-button-secondary ui-button-small" rel="prev">Previous</a>
                                    </c:when>
                                    <c:otherwise><span class="ui-button ui-button-secondary ui-button-small ui-page-disabled" aria-disabled="true">Previous</span></c:otherwise>
                                </c:choose>
                            </li>
                            <c:if test="${pageStart gt 1}">
                                <c:url var="firstCustomerPageUrl" value="/customer">
                                    <c:param name="action" value="list"/><c:param name="keyword" value="${keyword}"/>
                                    <c:param name="pageSize" value="${pageSize}"/><c:param name="page" value="1"/>
                                </c:url>
                                <li><a href="${firstCustomerPageUrl}" class="ui-button ui-button-secondary ui-button-small" aria-label="Page 1">1</a></li>
                                <c:if test="${pageStart gt 2}"><li><span class="ui-page-gap" aria-hidden="true">…</span></li></c:if>
                            </c:if>
                            <c:forEach var="pageNumber" begin="${pageStart}" end="${pageEnd}">
                                <li>
                                    <c:choose>
                                        <c:when test="${pageNumber eq currentPage}"><span class="ui-button ui-button-primary ui-button-small ui-page-current" aria-current="page" aria-label="Page ${pageNumber}"><c:out value="${pageNumber}"/></span></c:when>
                                        <c:otherwise>
                                            <c:url var="customerPageUrl" value="/customer">
                                                <c:param name="action" value="list"/><c:param name="keyword" value="${keyword}"/>
                                                <c:param name="pageSize" value="${pageSize}"/><c:param name="page" value="${pageNumber}"/>
                                            </c:url>
                                            <a href="${customerPageUrl}" class="ui-button ui-button-secondary ui-button-small" aria-label="Page ${pageNumber}"><c:out value="${pageNumber}"/></a>
                                        </c:otherwise>
                                    </c:choose>
                                </li>
                            </c:forEach>
                            <c:if test="${pageEnd lt totalPages}">
                                <c:if test="${pageEnd lt totalPages - 1}"><li><span class="ui-page-gap" aria-hidden="true">…</span></li></c:if>
                                <c:url var="lastCustomerPageUrl" value="/customer">
                                    <c:param name="action" value="list"/><c:param name="keyword" value="${keyword}"/>
                                    <c:param name="pageSize" value="${pageSize}"/><c:param name="page" value="${totalPages}"/>
                                </c:url>
                                <li><a href="${lastCustomerPageUrl}" class="ui-button ui-button-secondary ui-button-small" aria-label="Page ${totalPages}"><c:out value="${totalPages}"/></a></li>
                            </c:if>
                            <li>
                                <c:choose>
                                    <c:when test="${currentPage lt totalPages}">
                                        <c:url var="nextCustomerPageUrl" value="/customer">
                                            <c:param name="action" value="list"/><c:param name="keyword" value="${keyword}"/>
                                            <c:param name="pageSize" value="${pageSize}"/><c:param name="page" value="${currentPage + 1}"/>
                                        </c:url>
                                        <a href="${nextCustomerPageUrl}" class="ui-button ui-button-secondary ui-button-small" rel="next">Next</a>
                                    </c:when>
                                    <c:otherwise><span class="ui-button ui-button-secondary ui-button-small ui-page-disabled" aria-disabled="true">Next</span></c:otherwise>
                                </c:choose>
                            </li>
                        </ul>
                    </nav>
                </c:if>
            </div>
        </section>
    </div>
</layout:layout>
