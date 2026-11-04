<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Update Room" pageCss="room.css" useBootstrap="true" bodyClass="hbms-ui-page room-management-page">
    <div class="hbms-ui ui-shell room-workspace room-form-workspace">
        <header class="room-page-header">
            <div>
                <h1>Update Room</h1>
                <p>Update the room information and photo.</p>
            </div>
        </header>
        <div class="room-panel room-form-panel">
            <form action="${pageContext.request.contextPath}/room" method="post" enctype="multipart/form-data">
                <input type="hidden" name="action" value="update">

                <div class="row g-3">
                    <div class="col-md-6">
                        <label for="roomId" class="form-label">Room ID (Read-only)</label>
                        <input type="text" id="roomId" name="id" class="form-control bg-light"
                               value="<c:out value='${room.roomID}'/>" readonly>
                    </div>

                    <div class="col-md-6">
                        <label for="roomNumber" class="form-label">Room number <span class="text-danger">*</span></label>
                        <input type="text" id="roomNumber" name="roomNumber" class="form-control"
                               value="<c:out value='${room.roomNumber}'/>" required>
                    </div>

                    <div class="col-md-6">
                        <label for="roomTypeID" class="form-label">Room type: <span class="text-danger">*</span></label>
                        <select id="roomTypeID" name="roomTypeID" class="form-select" required>
                            <c:forEach var="rt" items="${roomTypes}">
                                <option value="${rt.roomTypeID}"
                                        ${rt.roomTypeID eq room.roomTypeID.roomTypeID ? 'selected' : ''}>
                                    <c:out value="${rt.typeName}" />
                                </option>
                            </c:forEach>
                        </select>
                    </div>

                    <div class="col-md-6">
                        <label for="roomStatus" class="form-label">Status <span class="text-danger">*</span></label>
                        <select id="roomStatus" name="status" class="form-select" required>
                            <option value="ACTIVE" ${room.status eq 'ACTIVE' ? 'selected' : ''}>ACTIVE</option>
                            <option value="INACTIVE" ${room.status eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                            <option value="MAINTENANCE" ${room.status eq 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                        </select>
                    </div>

                    <div class="col-12">
                        <p class="form-label">Room Image</p>
                        <div class="room-image-panel">

                            <!-- Hiển thị ảnh hiện tại -->
                            <div>
                                <span class="d-block text-muted small mb-1">Current Image</span>
                                <c:choose>
                                    <c:when test="${not empty room.roomImage}">
                                        <img src="${pageContext.request.contextPath}/assets/images/room/${room.roomImage}"
                                             alt="Current" class="img-thumbnail border-secondary" style="width: 140px; height: 100px; object-fit: cover;">
                                    </c:when>
                                    <c:otherwise>
                                        <div class="bg-light text-secondary d-flex align-items-center justify-content-center img-thumbnail border-secondary" style="width: 140px; height: 100px;">
                                            No Image
                                        </div>
                                    </c:otherwise>
                                </c:choose>
                            </div>

                            <div id="previewContainer" class="d-none">
                                <span class="d-block text-success small mb-1 fw-bold">Room image after change:</span>
                                <img id="imagePreview" src="#" alt="New Preview" class="img-thumbnail border-success" style="width: 140px; height: 100px; object-fit: cover;">
                            </div>

                            <div class="room-image-upload">
                                <label for="roomImageInput" class="btn room-button-secondary mb-0">
                                    <i class="fa-solid fa-upload me-1"></i> Change room image
                                </label>
                                <input type="file" id="roomImageInput" name="roomImage" class="form-control visually-hidden room-file-input" accept="image/*" onchange="previewImage(this)">
                                <div class="text-muted small mt-1">Upload image if you want to change</div>
                            </div>
                        </div>
                    </div>
                </div>

                <div class="room-form-actions">
                    <a href="${pageContext.request.contextPath}/room?action=list" class="btn room-button-secondary">
                        Cancel
                    </a>
                    <button type="submit" class="btn room-button-primary px-4 fw-semibold">
                        Save
                    </button>
                </div>
            </form>
        </div>
    </div>

    <!-- Script provide preview Image -->
    <script>
        function previewImage(input) {
            if (input.files && input.files[0]) {
                var reader = new FileReader();
                reader.onload = function (e) {
                    var preview = document.getElementById('imagePreview');
                    var container = document.getElementById('previewContainer');
                    preview.src = e.target.result;
                    container.classList.remove('d-none'); // Hiển thị khung preview
                };
                reader.readAsDataURL(input.files[0]);
            }
        }
    </script>
</layout:layout>
