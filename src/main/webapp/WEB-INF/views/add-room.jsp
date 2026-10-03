<%--
    Document   : add-room
    Created on : Oct 3, 2026, 9:32:03 PM
    Author     : Asus
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Add New Room" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-success text-white py-3">
                        <h2 class="h4 mb-0 fw-semibold">
                            Add New Room
                        </h2>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/room" method="post">
                            <input type="hidden" name="action" value="create">

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Room Number <span class="text-danger">*</span></label>
                                    <input type="text" name="roomNumber" class="form-control" placeholder="Ví dụ: 101, 102..." required>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Room Type<span class="text-danger">*</span></label>
                                    <!-- Iterating qua attribute 'roomType' đã set ở doGet -->
                                    <select name="roomTypeID" class="form-select" required>
                                        <option value="" disabled selected>-- Select room type --</option>
                                        <c:forEach var="rt" items="${roomType}">
                                            <option value="${rt.roomTypeID}">
                                                <c:out value="${rt.typeName}" />
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-md-12">
                                    <label class="form-label fw-semibold">Status<span class="text-danger">*</span></label>
                                    <select name="status" class="form-select" required>
                                        <option value="ACTIVE" selected>ACTIVE</option>
                                        <option value="MAINTENANCE">MAINTENANCE</option>
                                        <option value="INACTIVE">INACTIVE</option>
                                    </select>
                                </div>
                                <div class="col-md-12">
                                    <label class="form-label fw-semibold">Room image</label>
                                    <input type="file" name="roomImage" class="form-control" accept="image/*">
                                </div>
                            </div>

                            <div class="d-flex justify-content-end gap-2 mt-4">
                                <a href="${pageContext.request.contextPath}/room?action=list" class="btn btn-secondary">
                                    Cancel
                                </a>
                                <button type="submit" class="btn btn-success px-4 fw-semibold">
                                    Add
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
