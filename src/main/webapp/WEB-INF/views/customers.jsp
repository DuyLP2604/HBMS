<%@page contentType="text/html" pageEncoding="UTF-8" %>
<%@taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Customer List" pageCss="customer.css" useBootstrap="true" bodyClass="bg-light"  >

    <div class="container my-5">
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                <h2 class="h4 mb-0">
                    Customer List
                </h2>
                <a href="${pageContext.request.contextPath}/customer?action=add" class="btn btn-success btn-sm" >
                    + Add Customer
                </a>
            </div>
            <div class="card-body">
                <div class="row mb-4">
                    <div class="col-md-6">
                        <form action="${pageContext.request.contextPath}/customer" method="get" class="d-flex gap-2" >
                            <input type="hidden" name="action" value="list" >
                            <div class="input-group">
                                <span class="input-group-text bg-white">
                                    ID
                                </span>
                                <input type="text" name="searchId" class="form-control"
                                       value="<c:out value='${param.searchId}' />"
                                       placeholder="Enter customer ID..." >

                                <button type="submit" class="btn btn-primary" > Search </button>

                                <c:if test="${not empty param.searchId}">
                                    <a href="${pageContext.request.contextPath}/customer?action=list" class="btn btn-secondary" >Clear Filter</a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle border">
                        <thead class="table-light">
                            <tr>
                                <th>Customer ID</th>
                                <th>Full Name</th>
                                <th>Phone Number</th>
                                <th>Nationality</th>
                                <th class="text-center">
                                    Action
                                </th>
                            </tr>
                        </thead>

                        <tbody>
                            <c:forEach var="c" items="${customers}" >
                                <c:if test="${empty param.searchId or c.customerID.trim().equalsIgnoreCase(param.searchId.trim())}">

                                    <tr>
                                        <td class="fw-bold text-secondary">
                                            <c:out value="${c.customerID}" />
                                        </td>
                                        <td>
                                            <c:out value="${c.fullName}" />
                                        </td>
                                        <td>
                                            <c:out value="${c.phone}" />
                                        </td>
                                        <td>
                                            <c:out value="${c.nationalityID.nationalityName}" />
                                        </td>
                                        <td class="text-center">
                                            <a href="${pageContext.request.contextPath}/customer?action=viewDetail&id=${c.customerID}"
                                               class="btn btn-outline-info btn-sm me-1" >
                                                View Details
                                            </a>
                                            <a href="${pageContext.request.contextPath}/customer?action=update&id=${c.customerID}"
                                               class="btn btn-outline-warning btn-sm">
                                                Edit
                                            </a>
                                        </td>
                                    </tr>
                                </c:if>
                            </c:forEach>
                            <c:if test="${empty customers}">
                                <tr>
                                    <td colspan="5"  class="text-center text-muted py-4" >
                                        No customers found.
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
