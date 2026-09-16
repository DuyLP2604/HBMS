<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Add New Customer"
    pageCss="customer.css"
    pageJs="addCustomer.js"
    useBootstrap="true"
    bodyClass="bg-light"
    >

    <div class="container my-5">

        <div class="row justify-content-center">

            <div class="col-md-8">

                <div class="card shadow-sm">

                    <div class="card-header bg-success text-white py-3">
                        <h2 class="h4 mb-0">
                            Add New Customer
                        </h2>
                    </div>

                    <div class="card-body p-4">

                        <form
                            action="${pageContext.request.contextPath}/customer?action=add"
                            method="post"
                            >

                            <div class="row g-3">

                                <div class="col-12">

                                    <label class="form-label fw-semibold">
                                        Full Name *
                                    </label>

                                    <input
                                        type="text"
                                        name="name"
                                        class="form-control"
                                        placeholder="Enter full name..."
                                        required
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


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Email
                                    </label>

                                    <input
                                        type="email"
                                        name="email"
                                        class="form-control"
                                        placeholder="Enter email..."
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
                                        placeholder="Enter address..."
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Citizen ID
                                    </label>

                                    <input
                                        type="text"
                                        name="cccd"
                                        class="form-control"
                                        placeholder="Enter citizen ID..."
                                        >

                                </div>


                                <div class="col-md-6">

                                    <label class="form-label fw-semibold">
                                        Passport Number
                                    </label>

                                    <input
                                        type="text"
                                        name="passport"
                                        class="form-control"
                                        placeholder="Enter passport number if available..."
                                        >

                                </div>


                                <div class="col-12">

                                    <label class="form-label fw-semibold">
                                        Nationality *
                                    </label>

                                    <select
                                        name="nation"
                                        class="form-select"
                                        required
                                        >

                                        <option value="" disabled selected>
                                            Select nationality
                                        </option>

                                        <c:forEach
                                            items="${listNat}"
                                            var="n"
                                            >

                                            <option value="${n.nationalityID}">
                                                <c:out value="${n.nationalityName}" />
                                            </option>

                                        </c:forEach>

                                    </select>

                                </div>


                                <!-- Account creation option -->
                                <div class="col-12">

                                    <div class="form-check">

                                        <input
                                            class="form-check-input"
                                            type="checkbox"
                                            id="createAccount"
                                            name="createAccount"
                                            >

                                        <label
                                            class="form-check-label"
                                            for="createAccount"
                                            >
                                            Create an account for this customer
                                        </label>

                                    </div>

                                </div>


                                <!-- Account fields -->
                                <div
                                    id="accountFields"
                                    class="account-fields"
                                    >

                                    <div class="row g-3 mt-2">

                                        <div class="col-md-6">

                                            <label class="form-label fw-semibold">
                                                Username
                                            </label>

                                            <input
                                                type="text"
                                                name="username"
                                                class="form-control"
                                                >

                                        </div>


                                        <div class="col-md-6">

                                            <label class="form-label fw-semibold">
                                                Password
                                            </label>

                                            <input
                                                type="password"
                                                name="password"
                                                class="form-control"
                                                >

                                        </div>

                                    </div>

                                </div>

                            </div>


                            <div class="d-flex justify-content-end gap-2 mt-4">

                                <a
                                    href="${pageContext.request.contextPath}/customer?action=list"
                                    class="btn btn-secondary"
                                    >
                                    Cancel
                                </a>

                                <button
                                    type="submit"
                                    class="btn btn-primary px-4"
                                    >
                                    Save Customer
                                </button>

                            </div>

                        </form>

                    </div>

                </div>

            </div>

        </div>

    </div>

</layout:layout>
