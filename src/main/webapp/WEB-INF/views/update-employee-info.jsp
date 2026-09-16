<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Update Employee Information"
    pageCss="employee.css"
    useBootstrap="true"
    bodyClass="bg-light"
    >

    <div class="container my-5">

        <div class="row justify-content-center">

            <div class="col-md-8">

                <div class="card shadow-sm">

                    <div class="card-header bg-warning text-dark py-3">

                        <h2 class="h4 mb-0 fw-semibold">
                            Update Employee Information
                        </h2>

                    </div>


                    <div class="card-body p-4">

                        <form
                            action="${pageContext.request.contextPath}/employee?action=update"
                            method="post"
                            >

                            <div class="row g-3">


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold text-muted">
                                        Employee ID (Read-only)
                                    </label>

                                    <input
                                        type="text"
                                        name="id"
                                        class="form-control bg-light"
                                        value="<c:out value='${employeeID}' />"
                                        readonly
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Full Name *
                                    </label>

                                    <input
                                        type="text"
                                        name="name"
                                        class="form-control"
                                        value="<c:out value='${employee.fullName}' />"
                                        required
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Position
                                    </label>

                                    <input
                                        type="text"
                                        name="position"
                                        class="form-control"
                                        value="<c:out value='${employee.position}' />"
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Base Salary (VND)
                                    </label>

                                    <input
                                        type="number"
                                        name="salary"
                                        class="form-control"
                                        value="<c:out value='${employee.salary}' />"
                                        min="0"
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Work Shift
                                    </label>

                                    <input
                                        type="text"
                                        name="shift"
                                        class="form-control"
                                        value="<c:out value='${employee.shift}' />"
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Phone Number
                                    </label>

                                    <input
                                        type="text"
                                        name="phone"
                                        class="form-control"
                                        value="<c:out value='${employee.phone}' />"
                                        >

                                </div>


                                <div class="col-12">

                                    <label class="form-label fw-semibold">
                                        Address
                                    </label>

                                    <input
                                        type="text"
                                        name="address"
                                        class="form-control"
                                        value="<c:out value='${employee.address}' />"
                                        >

                                </div>


                                <div class="col-12">

                                    <label class="form-label fw-semibold">
                                        Hotel *
                                    </label>

                                    <select
                                        name="hotelId"
                                        class="form-select"
                                        required
                                        >

                                        <option value="" disabled>
                                            Select hotel
                                        </option>

                                        <c:forEach
                                            var="h"
                                            items="${listHotel}"
                                            >

                                            <option
                                                value="${h.hotelID}"
                                                ${h.hotelID eq employee.hotelID.hotelID ? 'selected' : ''}
                                                >
                                                <c:out value="${h.hotelName}" />
                                                (<c:out value="${h.hotelID}" />)
                                            </option>

                                        </c:forEach>

                                    </select>

                                </div>

                            </div>


                            <div class="d-flex justify-content-end gap-2 mt-4">

                                <a
                                    href="${pageContext.request.contextPath}/employee?action=list"
                                    class="btn btn-secondary"
                                    >
                                    Back
                                </a>

                                <button
                                    type="submit"
                                    class="btn btn-warning px-4 fw-semibold"
                                    >
                                    Update Employee
                                </button>

                            </div>

                        </form>

                    </div>

                </div>

            </div>

        </div>

    </div>

</layout:layout>
