<%@ page contentType="text/html"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fmt"
           uri="http://java.sun.com/jsp/jstl/fmt" %>

<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Hotel Services"
    pageCss="service.css"
    useBootstrap="true">

    <div class="container mt-5">

        <div class="card shadow">

            <div class="card-header bg-primary text-white">
                <h3 class="mb-0">
                    Select Hotel Services
                </h3>
            </div>

            <div class="card-body">

                <%-- Success message --%>
                <c:if test="${not empty sessionScope.successMessage}">
                    <div
                        class="alert alert-success alert-dismissible fade show"
                        role="alert">

                        <c:out
                            value="${sessionScope.successMessage}"
                        />

                        <button
                            type="button"
                            class="btn-close"
                            data-bs-dismiss="alert"
                            aria-label="Close">
                        </button>
                    </div>

                    <c:remove
                        var="successMessage"
                        scope="session"
                    />
                </c:if>

                <%-- Error message --%>
                <c:if test="${not empty sessionScope.errorMessage}">
                    <div
                        class="alert alert-danger alert-dismissible fade show"
                        role="alert">

                        <c:out
                            value="${sessionScope.errorMessage}"
                        />

                        <button
                            type="button"
                            class="btn-close"
                            data-bs-dismiss="alert"
                            aria-label="Close">
                        </button>
                    </div>

                    <c:remove
                        var="errorMessage"
                        scope="session"
                    />
                </c:if>

                <form
                    action="${pageContext.request.contextPath}/service"
                    method="post">

                    <div class="table-responsive">

                        <table
                            class="table table-bordered table-hover align-middle">

                            <thead class="table-light">
                                <tr>
                                    <th
                                        class="text-center service-select-column">
                                        Select
                                    </th>

                                    <th>
                                        Service Name
                                    </th>

                                    <th class="service-price-column">
                                        Price
                                    </th>
                                </tr>
                            </thead>

                            <tbody>

                                <c:forEach
                                    items="${list}"
                                    var="service">

                                    <tr>
                                        <td class="text-center">
                                            <input
                                                class="form-check-input"
                                                type="radio"
                                                name="selectedService"
                                                value="${service.serviceID}"
                                                required
                                            >
                                        </td>

                                        <td>
                                            <c:out
                                                value="${service.serviceName}"
                                            />
                                        </td>

                                        <td>
                                            <fmt:formatNumber
                                                value="${service.unitPrice}"
                                                type="number"
                                                groupingUsed="true"
                                                maxFractionDigits="0"
                                            />
                                            VND
                                        </td>
                                    </tr>

                                </c:forEach>

                                <c:if test="${empty list}">
                                    <tr>
                                        <td
                                            colspan="3"
                                            class="text-center text-muted py-4">

                                            No services are currently available.
                                        </td>
                                    </tr>
                                </c:if>

                            </tbody>

                        </table>

                    </div>

                    <c:if test="${not empty list}">

                        <div class="row justify-content-end mb-3">

                            <div class="col-md-3">

                                <label
                                    for="quantity"
                                    class="form-label fw-semibold">

                                    Quantity
                                </label>

                                <input
                                    id="quantity"
                                    class="form-control"
                                    type="number"
                                    name="quantity"
                                    value="1"
                                    min="1"
                                    max="99"
                                    step="1"
                                    required
                                >

                            </div>

                        </div>

                    </c:if>

                    <div class="d-flex justify-content-between">

                        <a
                            href="${pageContext.request.contextPath}/"
                            class="btn btn-secondary">

                            Back
                        </a>

                        <button
                            type="submit"
                            class="btn btn-success"
                            ${empty list ? "disabled" : ""}>

                            Add Service
                        </button>

                    </div>

                </form>

            </div>

        </div>

    </div>

</layout:layout>