<%--
    Document   : update-room
    Created on : Oct 1, 2026, 10:24:40 AM
    Author     : Asus
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Update Room" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="row justify-content-center">
            <div class="col-md-8">
                <div class="card shadow-sm border-0">
                    <div class="card-header bg-warning text-dark py-3">
                        <h2 class="h4 mb-0 fw-semibold">
                            Cập nhật thông tin phòng
                        </h2>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/room" method="post">
                            <input type="hidden" name="action" value="update">

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold text-muted">roomID (Read-only)</label>
                                    <input type="text" name="id" class="form-control bg-light"
                                           value="<c:out value='${room.roomID}'/>" readonly>
                                </div>

                                <!-- Số phòng -->
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Room number <span class="text-danger">*</span></label>
                                    <input type="text" name="roomNumber" class="form-control"
                                           value="<c:out value='${room.roomNumber}'/>" required>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Room type: <span class="text-danger">*</span></label>
                                    <select name="roomTypeID" class="form-select" required>
                                        <c:forEach var="rt" items="${roomTypes}">
                                            <option value="${rt.roomTypeID}"
                                                    ${rt.roomTypeID eq room.roomTypeID.roomTypeID ? 'selected' : ''}>
                                                <c:out value="${rt.typeName}" />
                                            </option>
                                        </c:forEach>
                                    </select>
                                </div>

                                <div class="col-md-6">
                                    <label class="form-label fw-semibold">Status <span class="text-danger">*</span></label>
                                    <select name="status" class="form-select" required>
                                        <option value="ACTIVE" ${room.status eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                                        <option value="MAINTENANCE" ${room.status eq 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                        <option value="INACTIVE" ${room.status eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                    </select>
                                </div>
                            </div>

                            <div class="d-flex justify-content-end gap-2 mt-4">
                                <a href="${pageContext.request.contextPath}/room?action=list" class="btn btn-secondary">
                                    Hủy bỏ
                                </a>
                                <button type="submit" class="btn btn-warning px-4 fw-semibold">
                                    Lưu thay đổi
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
