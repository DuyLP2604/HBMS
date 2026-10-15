<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="${roomType.typeName} - Room Details" useBootstrap="true">
    <style>
        .page-wrapper {
            background-color: #f8f9fa; /* Nền xám sáng nhẹ */
            min-height: 100vh;
            padding: 3rem 0;
            font-family: 'Segoe UI', system-ui, -apple-system, sans-serif;
        }

        .layout-container {
            max-width: 1140px;
            margin: 0 auto;
        }

        /* Chung cho các thẻ nội dung */
        .ui-box {
            background-color: #ffffff;
            border-radius: 16px;
            padding: 1.75rem;
            box-shadow: 0 4px 20px rgba(0, 0, 0, 0.04);
            border: 1px solid rgba(0,0,0,0.05);
        }

        /* Khối hình ảnh */
        .image-box {
            padding: 0;
            overflow: hidden;
            height: 350px;
            display: flex;
            align-items: center;
            justify-content: center;
            background-color: #e9ecef;
        }
        .image-box img {
            width: 100%;
            height: 100%;
            object-fit: cover;
            transition: transform 0.5s ease;
        }
        .image-box:hover img {
            transform: scale(1.02);
        }

        /* Khối tiện ích (Icon) */
        .info-row {
            display: flex;
            align-items: center;
            padding: 0.75rem 0;
            border-bottom: 1px solid #f1f3f5;
            color: #495057;
            font-size: 1.05rem;
        }
        .info-row:last-child {
            border-bottom: none;
            padding-bottom: 0;
        }
        .icon-wrapper {
            width: 32px;
            color: #0d6efd;
            font-size: 1.2rem;
            display: inline-block;
        }

        /* Nút bấm */
        .btn-action {
            padding: 0.85rem 1.5rem;
            border-radius: 10px;
            font-weight: 600;
            font-size: 1rem;
            transition: all 0.2s ease;
        }
        .btn-wishlist {
            background-color: #ff4757;
            color: white;
            border: none;
        }
        .btn-wishlist:hover {
            background-color: #ff6b81;
            color: white;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(255, 71, 87, 0.3);
        }
        .btn-back {
            background-color: #ffffff;
            color: #495057;
            border: 1px solid #ced4da;
        }
        .btn-back:hover {
            background-color: #f8f9fa;
            color: #212529;
            transform: translateY(-2px);
            box-shadow: 0 4px 12px rgba(0, 0, 0, 0.05);
        }

        .price-highlight {
            color: #ff4757;
            font-size: 1.5rem;
            font-weight: 700;
        }
    </style>

    <div class="page-wrapper">
        <div class="container layout-container">

            <c:if test="${not empty roomType}">
                <!-- Dùng g-4 để tạo khoảng cách đều đặn (gap) giữa 2 cột -->
                <div class="row g-4">

                    <!-- LEFT COLUMN -->
                    <div class="col-lg-5 d-flex flex-column gap-4">

                        <!-- Room Type Image -->
                        <div class="ui-box image-box">
                            <c:choose>
                                <c:when test="${not empty roomType.roomTypeImage}">
                                    <c:url var="roomTypeImageUrl" value="/assets/images/room-types/${roomType.roomTypeImage}" />
                                    <img src="${roomTypeImageUrl}" alt="<c:out value='${roomType.typeName}' />">
                                </c:when>
                                <c:otherwise>
                                    <div class="text-center text-secondary opacity-50">
                                        <i class="fa-solid fa-image fa-3x mb-2"></i>
                                        <h5 class="fw-normal mb-0">No Image Available</h5>
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>

                        <!-- Info Details Box -->
                        <div class="ui-box">
                            <div class="info-row pt-0">
                                <span class="icon-wrapper"><i class="fa-solid fa-user-group"></i></span>
                                <span><strong>Capacity:</strong> ${roomType.capacity} guest(s)</span>
                            </div>
                            <div class="info-row">
                                <span class="icon-wrapper text-success"><i class="fa-solid fa-utensils"></i></span>
                                <span><strong>Free buffet breakfast</strong></span>
                            </div>
                            <div class="info-row pb-0">
                                <span class="icon-wrapper text-info"><i class="fa-solid fa-door-open"></i></span>
                                <span><strong>Available rooms:</strong>
                                    <c:choose>
                                        <c:when test="${not empty availableRooms}">
                                            <span class="badge ${availableRooms == 0 ? 'bg-danger' : (availableRooms == 1 ? 'bg-warning' : 'bg-success')} ms-1">${availableRooms}</span>                                        </c:when>
                                        <c:otherwise>
                                            <span class="badge bg-secondary ms-1">N/A</span>
                                        </c:otherwise>
                                    </c:choose>
                                </span>
                            </div>
                        </div>
                    </div>

                    <!-- RIGHT COLUMN -->
                    <div class="col-lg-7 d-flex flex-column gap-4">

                        <!-- Room Type Name & Price Box -->
                        <div class="ui-box">
                            <h2 class="fw-bold text-dark mb-3"><c:out value="${roomType.typeName}" /></h2>
                            <div class="d-flex align-items-center">
                                <span class="text-muted fw-semibold me-2 fs-5">Price:</span>
                                <span class="price-highlight">
                                    <fmt:formatNumber value="${roomType.price}" type="number" groupingUsed="true" /> VND
                                </span>
                            </div>
                        </div>

                        <!-- Description Box (Tự động giãn ra để bằng chiều cao tổng với cột trái nhờ flex-grow-1) -->
                        <div class="ui-box flex-grow-1">
                            <h5 class="fw-bold text-dark mb-3">
                                <i class="fa-solid fa-align-left text-primary me-2"></i>Description
                            </h5>
                            <div class="text-secondary" style="line-height: 1.8;">
                                <c:choose>
                                    <c:when test="${not empty roomType.description}">
                                        <c:out value="${roomType.description}" />
                                    </c:when>
                                    <c:otherwise>
                                        <p class="fst-italic mb-0">No description available for this room type.</p>
                                    </c:otherwise>
                                </c:choose>
                            </div>
                        </div>

                        <!-- Action Buttons Box -->
                        <div class="d-flex gap-3 mt-auto">
                            <button onclick="history.back()" class="btn btn-back btn-action" style="min-width: 130px;">
                                <i class="fa-solid fa-arrow-left me-2"></i> Back
                            </button>

                            <!-- Logic Check Trạng Thái Phòng -->
                            <c:choose>
                                <c:when test="${not empty availableRooms and availableRooms > 0}">

                                    <!-- ================= POP-UP DROPDOWN ================= -->
                                    <div class="dropdown flex-grow-1">
                                        <!-- Nút Mở Menu -->
                                        <button type="button" class="btn btn-wishlist btn-action w-100 d-flex justify-content-center align-items-center gap-2"
                                                data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
                                            Add to wishlist
                                        </button>

                                        <!-- Form Ẩn Trong Dropdown Menu -->
                                        <div class="dropdown-menu dropdown-menu-end shadow-lg border-0 rounded-4 p-0 mt-2" style="width: 320px;">
                                            <div class="bg-light border-bottom px-4 py-3 rounded-top-4 d-flex justify-content-between align-items-center">
                                                <h6 class="mb-0 fw-bold text-dark"><i class="fa-solid fa-calendar-check text-primary me-2"></i>Booking Details</h6>
                                                <button type="button" class="btn-close btn-sm" aria-label="Close" onclick="document.body.click()"></button>
                                            </div>

                                            <form action="${pageContext.request.contextPath}/booking-wish-list" method="post" class="p-4">
                                                <!-- Dữ liệu ẩn -->
                                                <input type="hidden" name="action" value="add">
                                                <input type="hidden" name="roomTypeID" value="${roomType.roomTypeID}">

                                                <!-- Nhập Ngày -->
                                                <div class="mb-3">
                                                    <label for="detailCheckIn" class="form-label fw-bold text-secondary small text-uppercase">Check-in Date</label>
                                                    <input type="date" id="detailCheckIn" name="checkInDate" class="form-control bg-light" required>
                                                </div>
                                                <div class="mb-3">
                                                    <label for="detailCheckOut" class="form-label fw-bold text-secondary small text-uppercase">Check-out Date</label>
                                                    <input type="date" id="detailCheckOut" name="checkOutDate" class="form-control bg-light" required>
                                                </div>

                                                <!-- Chọn Số lượng -->
                                                <div class="row g-2 mb-4">
                                                    <div class="col-6">
                                                        <label for="quantity" class="form-label text-muted small mb-1 fw-bold text-uppercase">Rooms</label>
                                                        <!-- Max = availableRooms lấy từ Servlet -->
                                                        <input type="number" id="quantity" name="quantity" class="form-control text-center" value="1" min="1" max="${availableRooms}" required>
                                                    </div>
                                                    <div class="col-6">
                                                        <label for="guestCount" class="form-label text-muted small mb-1 fw-bold text-uppercase">Guests</label>
                                                        <input type="number" id="guestCount" name="guestCount" class="form-control text-center" value="1" min="1" required>
                                                    </div>
                                                </div>

                                                <!-- Nút Xác nhận -->
                                                <button type="submit" class="btn btn-success w-100 fw-bold shadow-sm py-2">
                                                    <i class="fa-solid fa-check me-2"></i> Confirm Selection
                                                </button>
                                            </form>
                                        </div>
                                    </div>
                                    <!-- ======================================================== -->

                                </c:when>
                                <c:otherwise>
                                    <!-- Nút Sold Out hiển thị mờ khi hết phòng -->
                                    <button type="button" class="btn btn-secondary btn-action flex-grow-1" disabled>
                                        <i class="fa-solid fa-ban me-2"></i> Currently Sold Out
                                    </button>
                                </c:otherwise>
                            </c:choose>
                        </div>

                    </div>
                </div>
            </c:if>

            <!-- Error State -->
            <c:if test="${empty roomType}">
                <div class="ui-box text-center py-5 mx-auto" style="max-width: 500px;">
                    <i class="fa-solid fa-triangle-exclamation fa-4x text-danger mb-4 opacity-75"></i>
                    <h3 class="fw-bold text-dark">Room Type Not Found</h3>
                    <p class="text-muted mb-4">The room details you are looking for are unavailable.</p>
                    <button onclick="history.back()" class="btn btn-back btn-action w-100">
                        Go Back
                    </button>
                </div>
            </c:if>

        </div>
    </div>
    <!-- Script xử lý Logic Ngày Tháng cho Dropdown Form -->
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const checkInInput = document.getElementById("detailCheckIn");
            const checkOutInput = document.getElementById("detailCheckOut");

            if (checkInInput && checkOutInput) {
                // Định dạng ngày YYYY-MM-DD dùng cộng chuỗi an toàn cho JSP
                function formatDate(date) {
                    const year = date.getFullYear();
                    const month = String(date.getMonth() + 1).padStart(2, '0');
                    const day = String(date.getDate()).padStart(2, '0');
                    return year + '-' + month + '-' + day;
                }

                const today = new Date();
                const tomorrow = new Date(today);
                tomorrow.setDate(tomorrow.getDate() + 1);

                const todayStr = formatDate(today);
                const tomorrowStr = formatDate(tomorrow);

                // Chặn người dùng chọn ngày quá khứ
                checkInInput.min = todayStr;
                checkOutInput.min = tomorrowStr;

                // Gán giá trị mặc định nếu ô nhập trống
                if (!checkInInput.value)
                    checkInInput.value = todayStr;
                if (!checkOutInput.value)
                    checkOutInput.value = tomorrowStr;

                // Logic tự động tịnh tiến ngày Check-out nếu ngày Check-in thay đổi
                checkInInput.addEventListener("change", function () {
                    if (this.value) {
                        const selectedCheckIn = new Date(this.value);
                        const minCheckOut = new Date(selectedCheckIn);
                        minCheckOut.setDate(minCheckOut.getDate() + 1);

                        const minCheckOutStr = formatDate(minCheckOut);
                        checkOutInput.min = minCheckOutStr;

                        if (!checkOutInput.value || checkOutInput.value < minCheckOutStr) {
                            checkOutInput.value = minCheckOutStr;
                        }
                    }
                });
            }
        });
    </script>

    <!-- Thêm xíu style bo góc input cho form đẹp hơn -->
    <style>
        .dropdown-menu input[type="date"],
        .dropdown-menu input[type="number"] {
            border-radius: 8px;
            border: 1px solid #dee2e6;
        }
    </style>
</layout:layout>