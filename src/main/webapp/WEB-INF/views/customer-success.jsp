<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Customer Created Successfully"
    pageCss="customer.css"
    useBootstrap="true"
    bodyClass="bg-light">
    <div class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm border-0">
                    <div class="card-body text-center p-5">
                        <div class="mb-3">
                            <span class="text-success"
                                  style="font-size: 64px;">
                                &#10004;
                            </span>
                        </div>
                        <h2 class="h3 text-success mb-3">
                            Customer Created Successfully!
                        </h2>
                        <p class="text-muted mb-2">
                            The customer and login account have been
                            created successfully.
                        </p>
                        <c:if test="${not empty customer}">
                            <p class="fw-semibold mb-4">
                                Customer: 
                                <c:out value="${customer.fullName}"/>
                            </p>
                        </c:if>
                        <hr class="my-4">
                        <h3 class="h5 mb-4">
                            What would you like to do next?
                        </h3>
                        <div class="d-grid gap-3">
                            <a href="${pageContext.request.contextPath}/booking?action=add&amp;customerId=${customer.customerID}"
                               class="btn btn-primary btn-lg">
                                <i class="bi bi-calendar-check"></i>
                                1. Create Booking
                            </a>
                            <a href="${pageContext.request.contextPath}/customer?action=list"
                               class="btn btn-outline-secondary btn-lg">
                                2. Back to Customer List
                            </a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>