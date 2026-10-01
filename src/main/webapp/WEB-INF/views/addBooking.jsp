<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Add New Reservation"
    pageCss="booking.css"
    useBootstrap="true"
    bodyClass="bg-light"

    >

    <div class="container my-5 booking-form-container">

        <div class="card shadow-sm">

            <div class="card-header bg-primary text-white py-3">
                <h2 class="h4 mb-0">
                    Add New Reservation
                </h2>
            </div>

            <div class="card-body">

                <form
                    action="${pageContext.request.contextPath}/booking"
                    method="post"
                    >

                    <input
                        type="hidden"
                        name="action"
                        value="add"
                        >


                    <div class="mb-3">

                        <label class="form-label">
                            Customer
                            <span class="text-danger">*</span>
                        </label>

                        <input
                            type="text"
                            name="customer"
                            class="form-control"
                            placeholder="Enter customer name"
                            required
                            >

                    </div>


                    <div class="mb-3">

                        <label class="form-label">
                            Phone Number
                            <span class="text-danger">*</span>
                        </label>

                        <input
                            type="text"
                            name="phone"
                            class="form-control"
                            placeholder="Enter customer phone number"
                            required
                            >

                    </div>


                    <div class="mb-3">

                        <label class="form-label">
                            Branch
                            <span class="text-danger">*</span>
                        </label>

                        <select
                            class="form-select"
                            name="hotelId"
                            required
                            >

                            <option value="" disabled selected>
                                -- Choose branch --
                            </option>

                            <c:forEach
                                items="${hotelList}"
                                var="h"
                                >

                                <option value="${h.hotelID}">
                                    <c:out value="${h.address}" />
                                </option>

                            </c:forEach>

                        </select>

                    </div>


                    <div class="mb-3">
                        <label class="form-label">
                            Room
                            <span class="text-danger">*</span>
                        </label>
                        <select class="form-select" name="roomID" required>
                            <option value="" disabled selected>
                                -- Choose room --
                            </option>
                            <c:forEach items="${roomList}" var="c" >
                                <option value="${c.roomID}">
                                    <c:out value="${c.roomNumber}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>


                    <div class="mb-3">

                        <label class="form-label">
                            Check-in Date
                            <span class="text-danger">*</span>
                        </label>

                        <input
                            type="date"
                            name="checkIn"
                            class="form-control"
                            required
                            >

                    </div>


                    <div class="d-flex justify-content-end gap-2">

                        <a
                            href="${pageContext.request.contextPath}/booking?action=list"
                            class="btn btn-secondary"
                            >
                            Back
                        </a>

                        <button
                            type="submit"
                            class="btn btn-success"
                            >
                            Add Reservation
                        </button>

                    </div>

                </form>

            </div>

        </div>

    </div>

</layout:layout>
