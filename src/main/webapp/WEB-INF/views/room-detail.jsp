<%--
    Document   : room-detail
    Created on : Sep 30, 2026, 11:21:07 PM
    Author     : Asus
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Room Details" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                <h2 class="h4 mb-0">Room Details: <c:out value="${room.roomNumber}" /></h2>
                <span class="badge bg-light text-dark fs-6">ID: <c:out value="${room.roomID}" /></span>
            </div>
            <div class="card-body p-4">
                <div class="row">
                    <!-- Ảnh của phòng -->
                    <div class="col-md-5 text-center mb-4 mb-md-0">
                        <c:choose>
                            <c:when test="${not empty room.roomImage}">
                                <img src="${pageContext.request.contextPath}/assets/images/room/${room.roomImage}"
                                     alt="Room ${room.roomNumber}" class="img-fluid rounded shadow-sm" style="max-height: 300px; object-fit: cover;">
                            </c:when>
                            <c:otherwise>
                                <div class="bg-secondary bg-opacity-10 rounded d-flex align-items-center justify-content-center" style="height: 300px;">
                                    <i class="fa-solid fa-image fa-4x text-secondary"></i>
                                </div>
                            </c:otherwise>
                        </c:choose>
                    </div>

                    <!-- Thông tin chi tiết -->
                    <div class="col-md-7">
                        <table class="table table-borderless fs-5">
                            <tbody>
                                <tr class="border-bottom">
                                    <th class="text-muted w-40">Room Type:</th>
                                    <td class="fw-bold"><c:out value="${room.roomTypeID.typeName}" /></td>
                                </tr>
                                <tr class="border-bottom">
                                    <th class="text-muted">Capacity:</th>
                                    <td><c:out value="${room.roomTypeID.capacity}" /> Guests</td>
                                </tr>
                                <tr class="border-bottom">
                                    <th class="text-muted">Price:</th>
                                    <td class="text-danger fw-bold">
                            <fmt:formatNumber value="${room.roomTypeID.price}" type="number" /> VND/night
                            </td>
                            </tr>
                            <tr class="border-bottom">
                                <th class="text-muted">Current Status:</th>
                                <td>
                                    <c:choose>
                                        <c:when test="${room.status eq 'Available'}">
                                            <span class="badge bg-success">Available</span>
                                        </c:when>
                                        <c:when test="${room.status eq 'Occupied'}">
                                            <span class="badge bg-warning text-dark">Occupied</span>
                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary"><c:out value="${room.status}" /></span>
                                        </c:otherwise>
                                    </c:choose>
                                </td>
                            </tr>
                            </tbody>
                        </table>
                        <div class="mt-4">
                            <p class="text-muted"><c:out value="${room.roomTypeID.description}" /></p>
                        </div>
                    </div>
                </div>

                <div class="text-end mt-4">
                    <a href="${pageContext.request.contextPath}/room?action=update&id=${room.roomID}" class="btn btn-warning me-2">Edit Room</a>
                    <a href="${pageContext.request.contextPath}/room?action=list" class="btn btn-secondary">Back to List</a>
                </div>
            </div>
        </div>
    </div>
</layout:layout>