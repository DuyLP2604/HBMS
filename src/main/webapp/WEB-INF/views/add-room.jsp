<%--
    Document   : add-room
    Created on : Oct 3, 2026, 9:32:03 PM
    Author     : Asus
--%>

<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Collects the details for a new physical room. --%>
<layout:layout title="Add New Room" pageCss="room.css" useBootstrap="true" bodyClass="hbms-ui-page room-management-page">
    <div class="hbms-ui ui-shell room-workspace room-form-workspace">
        <header class="room-page-header">
            <div>
                <h1>Add New Room</h1>
                <p>Set the room number, type and status.</p>
            </div>
        </header>
        <div class="room-panel room-form-panel">
            <form action="${pageContext.request.contextPath}/room" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="create">

                <div class="row g-3">
                    <div class="col-md-6">
                        <label for="roomNumber" class="form-label">Room Number <span class="text-danger">*</span></label>
                        <input type="text" id="roomNumber" name="roomNumber" class="form-control" placeholder="Ví dụ: 101, 102..." required>
                    </div>

                    <div class="col-md-6">
                        <label for="roomTypeID" class="form-label">Room Type<span class="text-danger">*</span></label>
                        <select id="roomTypeID" name="roomTypeID" class="form-select" required>
                            <option value="" disabled selected>-- Select room type --</option>
                            <c:forEach var="rt" items="${roomType}">
                                <option value="${rt.roomTypeID}">
                                    <c:out value="${rt.typeName}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-md-12">
                        <label for="roomStatus" class="form-label">Status<span class="text-danger">*</span></label>
                        <select id="roomStatus" name="status" class="form-select" required>
                            <option value="ACTIVE" selected>ACTIVE</option>
                            <option value="MAINTENANCE">MAINTENANCE</option>
                            <option value="INACTIVE">INACTIVE</option>
                        </select>
                    </div>
                    <div class="col-md-12">
                        <label for="roomImageInput" class="form-label">Room image</label>
                        <input type="file" id="roomImageInput" name="roomImage" class="form-control" accept="image/*">
                    </div>
                </div>

                <div class="room-form-actions">
                    <a href="${pageContext.request.contextPath}/room?action=list" class="btn room-button-secondary">
                        Cancel
                    </a>
                    <button type="submit" class="btn room-button-primary px-4 fw-semibold">
                        Add
                    </button>
                </div>
            </form>
        </div>
    </div>

</layout:layout>
