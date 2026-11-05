<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Collects the details for a new room type. --%>
<layout:layout title="Add New Room Type" pageCss="room.css" useBootstrap="true" bodyClass="hbms-ui-page room-management-page">
    <div class="hbms-ui ui-shell room-workspace room-form-workspace">
        <header class="room-page-header">
            <div>
                <h1>Add New Room Type</h1>
                <p>Define the capacity, nightly price and description.</p>
            </div>
        </header>
        <div class="room-panel room-form-panel">
            <form action="${pageContext.request.contextPath}/room-types" method="POST" enctype="multipart/form-data">
                <!-- Action hidden -->
                <input type="hidden" name="action" value="create">

                <div class="row g-3">
                    <!-- Type Name -->
                    <div class="col-md-12">
                        <label for="typeName" class="form-label">Type Name <span class="text-danger">*</span></label>
                        <input type="text" class="form-control form-control-lg border-secondary-subtle" id="typeName" name="typeName" placeholder="e.g. Standard, Deluxe, Family Suite..." required autofocus>
                    </div>

                    <!-- Capacity -->
                    <div class="col-md-6">
                        <label for="capacity" class="form-label">Capacity (Guests) <span class="text-danger">*</span></label>
                        <div class="input-group">
                            <span class="input-group-text bg-light"><i class="fa-solid fa-user-group"></i></span>
                            <input type="number" class="form-control border-secondary-subtle" id="capacity" name="capacity" min="1" placeholder="e.g. 2" required>
                        </div>
                    </div>

                    <!-- Price -->
                    <div class="col-md-6">
                        <label for="price" class="form-label">Price per Night <span class="text-danger">*</span></label>
                        <div class="input-group">
                            <input type="number" step="any" class="form-control border-secondary-subtle text-danger fw-bold" id="price" name="price" min="0" placeholder="e.g. 500000" required>
                            <span class="input-group-text bg-light">VND</span>
                        </div>
                    </div>

                    <!-- Image Upload -->
                    <div class="col-12">
                        <label for="roomTypeImage" class="form-label">Room Type Image</label>
                        <input type="file" class="form-control border-secondary-subtle" id="roomTypeImage" name="roomTypeImage" accept="image/png, image/jpeg, image/webp">
                        <div class="form-text text-muted mb-2"><i class="fa-solid fa-circle-info me-1"></i>Supported formats: JPG, PNG, WEBP. Max size: 5MB.</div>

                        <!-- Image Preview Container -->
                        <div class="mt-2 d-none" id="imagePreviewContainer">
                            <p class="text-muted small fw-bold mb-1">Image Preview:</p>
                            <img id="imagePreview" src="" alt="Preview" class="img-thumbnail shadow-sm" style="max-height: 250px; border-radius: 8px; object-fit: cover;">
                        </div>
                    </div>

                    <!-- Description -->
                    <div class="col-12">
                        <label for="description" class="form-label">Description</label>
                        <textarea class="form-control border-secondary-subtle" id="description" name="description" rows="4" placeholder="Enter details, features, and amenities of this room type..."></textarea>
                    </div>
                </div>


                <!-- Action Buttons -->
                <div class="room-form-actions">
                    <a href="${pageContext.request.contextPath}/room-types?action=list" class="btn room-button-secondary px-4 fw-bold">Cancel</a>
                    <button type="submit" class="btn room-button-primary px-5 fw-bold shadow-sm"><i class="fa-solid fa-check me-2"></i>Save & Create</button>
                </div>
            </form>
        </div>
    </div>

    <!-- Script xử lý xem trước ảnh (Image Preview) ngay khi chọn file -->
    <script>
        document.getElementById('roomTypeImage').addEventListener('change', function (event) {
            const [file] = event.target.files;
            const previewContainer = document.getElementById('imagePreviewContainer');
            const previewImage = document.getElementById('imagePreview');

            if (file) {
                previewImage.src = URL.createObjectURL(file);
                previewContainer.classList.remove('d-none');
            } else {
                previewImage.src = '';
                previewContainer.classList.add('d-none');
            }
        });
    </script>
</layout:layout>
