<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Hotel Services"
    pageCss="service.css"
    useBootstrap="true"

    >

    <div class="container mt-5">

        <div class="card shadow">

            <div class="card-header bg-primary text-white">

                <h3 class="mb-0">
                    Select Hotel Services
                </h3>

            </div>


            <div class="card-body">

                <form
                    action="${pageContext.request.contextPath}/service"
                    method="post"
                    >

                    <input
                        type="hidden"
                        name="bookingID"
                        value="${bookingID}"
                        >


                    <div class="table-responsive">

                        <table class="table table-bordered table-hover align-middle">

                            <thead class="table-light">

                                <tr>

                                    <th class="text-center service-select-column">
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
                                    var="s"
                                    >

                                    <tr>

                                        <td class="text-center">

                                            <input
                                                class="form-check-input"
                                                type="radio"
                                                name="selectedService"
                                                value="${s.serviceName}|${s.unitPrice}"
                                                >

                                        </td>


                                        <td>
                                            <c:out value="${s.serviceName}" />
                                        </td>


                                        <td>
                                            <c:out value="${s.unitPrice}" />
                                            VND
                                        </td>

                                    </tr>

                                </c:forEach>


                                <c:if test="${empty list}">

                                    <tr>

                                        <td
                                            colspan="3"
                                            class="text-center text-muted py-4"
                                            >
                                            No services are currently available.
                                        </td>

                                    </tr>

                                </c:if>

                            </tbody>

                        </table>

                    </div>


                    <div class="text-end">

                        <a
                            href="${pageContext.request.contextPath}/"
                            class="btn btn-secondary"
                            >
                            Back
                        </a>

                        <button
                            type="submit"
                            class="btn btn-success"
                            >
                            Submit
                        </button>

                    </div>

                </form>

            </div>

        </div>

    </div>
</layout:layout>
