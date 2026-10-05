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
                            Update Room Detail
                        </h2>
                    </div>
                    <div class="card-body p-4">
                        <form action="${pageContext.request.contextPath}/room" method="post" enctype="multipart/form-data">
                            <input type="hidden" name="action" value="update">

                            <div class="row g-3">
                                <div class="col-md-6">
                                    <label class="form-label fw-semibold text-muted">Room ID (Read-only)</label>
                                    <input type="text" name="id" class="form-control bg-light"
                                           value="<c:out value='${room.roomID}'/>" readonly>
                                </div>

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
                                        <option value="INACTIVE" ${room.status eq 'INACTIVE' ? 'selected' : ''}>INACTIVE</option>
                                        <option value="MAINTENANCE" ${room.status eq 'MAINTENANCE' ? 'selected' : ''}>MAINTENANCE</option>
                                    </select>
                                </div>

                                <div class="col-md-12 mt-4">
                                    <label class="form-label fw-semibold">Room Image</label>
                                    <div class="d-flex align-items-center gap-4 p-3 border rounded bg-white shadow-sm">

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

                                        <div class="ms-auto">
                                            <label for="roomImageInput" class="btn btn-outline-primary mb-0">
                                                <i class="fa-solid fa-upload me-1"></i> Change room image
                                            </label>
                                            <input type="file" id="roomImageInput" name="roomImage" class="form-control d-none" accept="image/*" onchange="previewImage(this)">
                                            <div class="text-muted small mt-1">Upload image if you want to change</div>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            <div class="d-flex justify-content-end gap-2 mt-4">
                                <a href="${pageContext.request.contextPath}/room?action=list" class="btn btn-secondary">
                                    Cancel
                                </a>
                                <button type="submit" class="btn btn-warning px-4 fw-semibold">
                                    Save
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            </div>
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