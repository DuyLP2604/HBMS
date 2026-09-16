<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Add New Employee"
    pageCss="employee.css"
    useBootstrap="true"
    bodyClass="bg-light"
    >
    <div class="container my-5">

        <div class="row justify-content-center">

            <div class="col-md-8">

                <div class="card shadow-sm">

                    <div class="card-header employee-header text-white py-3">
                        <h2 class="h4 mb-0">
                            Add New Employee
                        </h2>
                    </div>

                    <div class="card-body p-4">

                        <form
                            action="${pageContext.request.contextPath}/employee?action=add"
                            method="post"
                            >

                            <div class="row g-3">

                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Employee ID *
                                    </label>

                                    <input
                                        type="text"
                                        name="id"
                                        class="form-control"
                                        placeholder="Example: NV01"
                                        required
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
                                        placeholder="Enter employee name..."
                                        required
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Username *
                                    </label>

                                    <input
                                        type="text"
                                        name="username"
                                        class="form-control"
                                        placeholder="Enter username..."
                                        required
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Password *
                                    </label>

                                    <input
                                        type="password"
                                        name="password"
                                        class="form-control"
                                        placeholder="Enter password..."
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
                                        placeholder="Example: Receptionist, Manager..."
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Base Salary (VND) *
                                    </label>

                                    <input
                                        type="number"
                                        step="0.01"
                                        min="0"
                                        name="salary"
                                        class="form-control"
                                        placeholder="Enter salary..."
                                        required
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
                                        placeholder="Example: Morning, Afternoon, Night..."
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
                                        placeholder="Enter phone number..."
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
                                        placeholder="Enter home address..."
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

                                        <option value="" disabled selected>
                                            Select hotel
                                        </option>

                                        <c:forEach
                                            var="h"
                                            items="${listHotel}"
                                            >

                                            <option value="${h.hotelID}">
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
                                    Cancel
                                </a>

                                <button
                                    type="submit"
                                    class="btn employee-btn text-white px-4"
                                    >
                                    Add Employee
                                </button>

                            </div>

                        </form>

                    </div>

                </div>

            </div>

        </div>

    </div>

</layout:layout>
