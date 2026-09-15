<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Employee Details"
    pageCss="employee.css"
    useBootstrap="true"
    bodyClass="bg-light"

    >

    <div class="container my-5">

        <div class="row justify-content-center">

            <div class="col-md-6">

                <div class="card shadow-sm border-0">

                    <div class="card-header employee-detail-header text-white p-3 d-flex justify-content-between align-items-center">

                        <h3 class="h5 mb-0">
                            Employee Profile
                        </h3>

                        <span class="badge bg-white text-dark p-2">
                            ID:
                            <c:out value="${employee.id}" />
                        </span>

                    </div>


                    <div class="card-body p-4">

                        <div class="row mb-3 border-bottom pb-2">

                            <div class="col-sm-4 text-muted">
                                Full Name:
                            </div>

                            <div class="col-sm-8 fw-bold">
                                <c:out value="${employee.fullname}" />
                            </div>

                        </div>


                        <div class="row mb-3 border-bottom pb-2">

                            <div class="col-sm-4 text-muted">
                                Position:
                            </div>

                            <div class="col-sm-8 fw-bold text-success">
                                <c:out value="${employee.position}" />
                            </div>

                        </div>


                        <div class="row mb-3 border-bottom pb-2">

                            <div class="col-sm-4 text-muted">
                                Base Salary:
                            </div>

                            <div class="col-sm-8 fw-bold text-danger">
                                <c:out value="${employee.salary}" />
                                VND
                            </div>

                        </div>


                        <div class="row mb-3 border-bottom pb-2">

                            <div class="col-sm-4 text-muted">
                                Work Shift:
                            </div>

                            <div class="col-sm-8 fw-bold">

                                <span class="badge bg-secondary">
                                    <c:out value="${employee.shift}" />
                                </span>

                            </div>

                        </div>


                        <div class="row mb-3 border-bottom pb-2">

                            <div class="col-sm-4 text-muted">
                                Address:
                            </div>

                            <div class="col-sm-8 fw-bold">
                                <c:out value="${employee.address}" />
                            </div>

                        </div>


                        <div class="row mb-3 border-bottom pb-2">

                            <div class="col-sm-4 text-muted">
                                Phone Number:
                            </div>

                            <div class="col-sm-8 fw-bold">
                                <c:out value="${employee.phone}" />
                            </div>

                        </div>


                        <div class="row mb-3">

                            <div class="col-sm-4 text-muted">
                                Hotel ID:
                            </div>

                            <div class="col-sm-8 fw-bold text-primary">
                                <c:out value="${employee.hotel.id}" />
                            </div>

                        </div>

                    </div>


                    <div class="card-footer bg-light text-end p-3">

                        <a
                            href="${pageContext.request.contextPath}/employee?action=list"
                            class="btn btn-secondary btn-sm"
                            >
                            Back to Employee List
                        </a>

                    </div>

                </div>

            </div>

        </div>

    </div>
</layout:layout>
