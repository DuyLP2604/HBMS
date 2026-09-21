<%@ page contentType="text/html"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Add New Employee"
    pageCss="employee.css"
    useBootstrap="true"
    bodyClass="bg-light">

    <div class="container my-5">

        <div class="row justify-content-center">

            <div class="col-md-8">

                <div class="card shadow-sm">

                    <div
                        class="card-header employee-header text-white py-3">

                        <h2 class="h4 mb-0">
                            Add New Employee
                        </h2>
                    </div>

                    <div class="card-body p-4">

                        <form
                            action="${pageContext.request.contextPath}/employee"
                            method="post">

                            <input
                                type="hidden"
                                name="action"
                                value="add"
                            >

                            <div class="row g-3">

                                <div class="col-md-6">

                                    <label
                                        for="employeeID"
                                        class="form-label fw-semibold">

                                        Employee ID *
                                    </label>

                                    <input
                                        id="employeeID"
                                        type="text"
                                        name="id"
                                        class="form-control"
                                        placeholder="Example: NV01"
                                        maxlength="6"
                                        pattern="NV[0-9]{2,4}"
                                        title="Employee ID must use the format NV01."
                                        required
                                    >

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="fullName"
                                        class="form-label fw-semibold">

                                        Full Name *
                                    </label>

                                    <input
                                        id="fullName"
                                        type="text"
                                        name="name"
                                        class="form-control"
                                        placeholder="Enter employee name"
                                        maxlength="100"
                                        autocomplete="name"
                                        required
                                    >

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="username"
                                        class="form-label fw-semibold">

                                        Username *
                                    </label>

                                    <input
                                        id="username"
                                        type="text"
                                        name="username"
                                        class="form-control"
                                        placeholder="Enter username"
                                        maxlength="50"
                                        autocomplete="username"
                                        required
                                    >

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="password"
                                        class="form-label fw-semibold">

                                        Password *
                                    </label>

                                    <input
                                        id="password"
                                        type="password"
                                        name="password"
                                        class="form-control"
                                        placeholder="Enter password"
                                        minlength="6"
                                        maxlength="255"
                                        autocomplete="new-password"
                                        required
                                    >

                                    <div class="form-text">
                                        The employee will use this password
                                        to sign in.
                                    </div>

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="position"
                                        class="form-label fw-semibold">

                                        Position *
                                    </label>

                                    <input
                                        id="position"
                                        type="text"
                                        name="position"
                                        class="form-control"
                                        placeholder="Example: Receptionist"
                                        maxlength="50"
                                        required
                                    >

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="salary"
                                        class="form-label fw-semibold">

                                        Base Salary (VND)
                                    </label>

                                    <input
                                        id="salary"
                                        type="number"
                                        name="salary"
                                        class="form-control"
                                        placeholder="Enter salary"
                                        min="0"
                                        step="0.01"
                                    >

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="shift"
                                        class="form-label fw-semibold">

                                        Work Shift
                                    </label>

                                    <select
                                        id="shift"
                                        name="shift"
                                        class="form-select">

                                        <option value="">
                                            Select work shift
                                        </option>

                                        <option value="Morning">
                                            Morning
                                        </option>

                                        <option value="Afternoon">
                                            Afternoon
                                        </option>

                                        <option value="Night">
                                            Night
                                        </option>

                                        <option value="Administrative">
                                            Administrative
                                        </option>

                                    </select>

                                </div>

                                <div class="col-md-6">

                                    <label
                                        for="phone"
                                        class="form-label fw-semibold">

                                        Phone Number
                                    </label>

                                    <input
                                        id="phone"
                                        type="tel"
                                        name="phone"
                                        class="form-control"
                                        placeholder="Enter phone number"
                                        maxlength="20"
                                        autocomplete="tel"
                                    >

                                </div>

                                <div class="col-12">

                                    <label
                                        for="address"
                                        class="form-label fw-semibold">

                                        Address
                                    </label>

                                    <input
                                        id="address"
                                        type="text"
                                        name="address"
                                        class="form-control"
                                        placeholder="Enter home address"
                                        maxlength="200"
                                        autocomplete="street-address"
                                    >

                                </div>

                            </div>

                            <div
                                class="d-flex justify-content-end gap-2 mt-4">

                                <a
                                    href="${pageContext.request.contextPath}/employee?action=list"
                                    class="btn btn-secondary">

                                    Cancel
                                </a>

                                <button
                                    type="submit"
                                    class="btn employee-btn text-white px-4">

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