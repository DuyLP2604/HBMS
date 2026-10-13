<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Search Rooms" useBootstrap="true">
    <div class="container py-5">

        <!-- Page Header & Action Buttons -->
        <div class="d-flex flex-column flex-md-row justify-content-between align-items-md-center mb-5 border-bottom pb-3">
            <div class="mb-3 mb-md-0">
                <h1 class="h3 fw-bold mb-1"><i class="fa-solid fa-bed text-primary me-2"></i>Available Rooms</h1>
                <p class="text-muted mb-0 fs-6">
                    <c:choose>
                        <c:when test="${not empty checkInDate and not empty checkOutDate}">
                            Showing availability from <strong class="text-dark">${checkInDate}</strong> to <strong class="text-dark">${checkOutDate}</strong>
                        </c:when>
                        <c:otherwise>
                            Please select dates to check availability.
                        </c:otherwise>
                    </c:choose>
                </p>
            </div>
            <div class="d-flex gap-2">
                <!-- ================= POP-UP DROPDOWN MỚI ================= -->
                <div class="dropdown">
                    <button type="button" id="btnSelectDates" class="btn btn-primary fw-bold shadow-sm d-flex align-items-center" data-bs-toggle="dropdown" aria-expanded="false" data-bs-auto-close="outside">
                        <i class="fa-solid fa-filter me-2"></i> Select Dates
                    </button>

                    <!-- Form ẩn trong Dropdown Menu -->
                    <div class="dropdown-menu dropdown-menu-end shadow-lg border-0 rounded-4 p-0 mt-2" style="width: 320px;">
                        <div class="bg-light border-bottom px-4 py-3 rounded-top-4 d-flex justify-content-between align-items-center">
                            <h6 class="mb-0 fw-bold text-dark"><i class="fa-solid fa-calendar-check text-primary me-2"></i>Select Dates</h6>
                            <!-- Nút X (Đóng menu bằng cách giả lập click ra ngoài body) -->
                            <button type="button" class="btn-close btn-sm" aria-label="Close" onclick="document.body.click()"></button>
                        </div>
                        <form id="searchRoomForm" action="${pageContext.request.contextPath}/room-types" method="get" class="p-4">
                            <div class="mb-3">
                                <label for="checkInDate" class="form-label fw-bold text-secondary small text-uppercase">Check-in Date</label>
                                <input type="date" id="checkInDate" name="checkInDate" value="${checkInDate}" class="form-control bg-light" required>
                            </div>
                            <div class="mb-4">
                                <label for="checkOutDate" class="form-label fw-bold text-secondary small text-uppercase">Check-out Date</label>
                                <input type="date" id="checkOutDate" name="checkOutDate" value="${checkOutDate}" class="form-control bg-light" required>
                            </div>
                            <button type="submit" class="btn btn-primary w-100 fw-bold shadow-sm py-2">
                                <i class="fa-solid fa-magnifying-glass me-2"></i> Search Availability
                            </button>
                        </form>
                    </div>
                </div>
                <!-- ======================================================== -->

                <a href="${pageContext.request.contextPath}/booking-wish-list" class="btn btn-outline-secondary fw-bold shadow-sm d-flex align-items-center">
                    <i class="fa-solid fa-clipboard-list me-2"></i> View Wishlist
                </a>
            </div>
        </div>

        <!-- Messages -->
        <c:if test="${not empty successMessage}">
            <div class="alert alert-success shadow-sm rounded-3"><i class="fa-solid fa-circle-check me-2"></i><c:out value="${successMessage}" /></div>
            </c:if>
            <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger shadow-sm rounded-3"><i class="fa-solid fa-triangle-exclamation me-2"></i><c:out value="${errorMessage}" /></div>
            </c:if>

        <!-- Search results (Grid Danh sách phòng) -->
        <c:if test="${searched}">
            <c:choose>
                <c:when test="${not empty availabilityList}">
                    <div class="row g-4">
                        <c:forEach var="roomType" items="${availabilityList}">
                            <div class="col-md-6 col-lg-4">
                                <div class="card h-100 shadow-sm border-0 rounded-4 hover-lift overflow-hidden">
                                    <!-- Room-type image -->
                                    <c:choose>
                                        <c:when test="${not empty roomType.roomTypeImage}">
                                            <c:url var="roomTypeImageUrl" value="/assets/images/room-types/${roomType.roomTypeImage}" />
                                            <img src="${roomTypeImageUrl}" class="card-img-top" alt="<c:out value='${roomType.typeName}' />" style="height: 240px; object-fit: cover;">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="bg-light text-secondary d-flex justify-content-center align-items-center border-bottom" style="height: 240px;">
                                                <div class="text-center opacity-50">
                                                    <i class="fa-solid fa-image fa-4x mb-2"></i>
                                                    <div>No image available</div>
                                                </div>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <!-- Room-type information -->
                                    <div class="card-body d-flex flex-column p-4">
                                        <h2 class="h4 mb-3 fw-bold text-primary"><c:out value="${roomType.typeName}" /></h2>
                                        <div class="mb-4 text-muted">
                                            <p class="mb-2">
                                                <i class="fa-solid fa-users text-primary me-2 width-20"></i> Capacity: <strong class="text-dark">${roomType.capacity} guest(s)</strong>
                                            </p>
                                            <p class="mb-2">
                                                <i class="fa-solid fa-money-bill-wave text-primary me-2 width-20"></i> Price:
                                                <strong class="text-danger fs-5">
                                                    <fmt:formatNumber value="${roomType.price}" type="number" groupingUsed="true" /> <span class="fs-6 text-muted fw-normal">VND/night</span>
                                                </strong>
                                            </p>
                                            <p class="mb-0">
                                                <i class="fa-solid fa-door-open text-primary me-2 width-20"></i> Available:
                                                <c:choose>
                                                    <c:when test="${roomType.availableRooms > 0}">
                                                        <strong class="badge bg-success px-2 py-1 fs-6">${roomType.availableRooms} room(s)</strong>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <strong class="badge bg-danger px-2 py-1 fs-6">Sold out</strong>
                                                    </c:otherwise>
                                                </c:choose>
                                            </p>
                                        </div>

                                        <!-- Booking form or sold-out button -->
                                        <div class="mt-auto border-top pt-3">
                                            <c:choose>
                                                <c:when test="${roomType.availableRooms > 0}">
                                                    <form action="${pageContext.request.contextPath}/booking-wish-list" method="post">
                                                        <input type="hidden" name="action" value="add">
                                                        <input type="hidden" name="roomTypeID" value="${roomType.roomTypeID}">
                                                        <input type="hidden" name="checkInDate" value="${checkInDate}">
                                                        <input type="hidden" name="checkOutDate" value="${checkOutDate}">
                                                        <div class="row g-2 mb-3">
                                                            <div class="col-6">
                                                                <label for="quantity_${roomType.roomTypeID}" class="form-label text-muted small mb-1 fw-bold">Rooms</label>
                                                                <input type="number" id="quantity_${roomType.roomTypeID}" name="quantity" class="form-control text-center" value="1" min="1" max="${roomType.availableRooms}" required>
                                                            </div>
                                                            <div class="col-6">
                                                                <label for="guestCount_${roomType.roomTypeID}" class="form-label text-muted small mb-1 fw-bold">Guests</label>
                                                                <input type="number" id="guestCount_${roomType.roomTypeID}" name="guestCount" class="form-control text-center" value="1" min="1" required>
                                                            </div>
                                                        </div>
                                                        <button type="submit" class="btn btn-success w-100 fw-bold shadow-sm py-2">
                                                            <i class="fa-solid fa-cart-plus me-2"></i> Add to Wishlist
                                                        </button>
                                                    </form>
                                                </c:when>
                                                <c:otherwise>
                                                    <button type="button" class="btn btn-secondary w-100 py-2 fw-bold" disabled>
                                                        <i class="fa-solid fa-ban me-2"></i> Currently Sold Out
                                                    </button>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>
                </c:when>
                <c:otherwise>
                    <div class="alert alert-warning text-center py-5 shadow-sm border-0 rounded-4 mt-4">
                        <i class="fa-solid fa-folder-open fa-4x text-warning opacity-50 mb-3"></i>
                        <h4 class="fw-bold">No Rooms Available</h4>
                        <p class="mb-0 text-muted">We couldn't find any rooms for the selected dates. Please try adjusting your check-in and check-out dates.</p>
                        <!-- Nút này khi bấm sẽ tự động bật cái Menu Dropdown phía trên -->
                        <button type="button" class="btn btn-primary mt-4 fw-bold px-4" onclick="document.getElementById('btnSelectDates').click()">
                            <i class="fa-solid fa-calendar-days me-2"></i> Change Dates
                        </button>
                    </div>
                </c:otherwise>
            </c:choose>
        </c:if>
    </div>

    <!-- Script xử lý Logic Ngày Tháng mặc định -->
    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const checkInInput = document.getElementById("checkInDate");
            const checkOutInput = document.getElementById("checkOutDate");

            function formatDate(date) {
                const year = date.getFullYear();
                const month = String(date.getMonth() + 1).padStart(2, '0');
                const day = String(date.getDate()).padStart(2, '0');
                return `\${year}-\${month}-\${day}`;
            }

            const today = new Date();
            const tomorrow = new Date(today);
            tomorrow.setDate(tomorrow.getDate() + 1);

            const todayStr = formatDate(today);
            const tomorrowStr = formatDate(tomorrow);

            // Chặn người dùng chọn ngày quá khứ
            checkInInput.min = todayStr;
            checkOutInput.min = tomorrowStr;

            // Nếu ô input đang trống (Lần đầu load trang) -> Gán giá trị mặc định
            let isFirstLoad = false;
            if (!checkInInput.value) {
                checkInInput.value = todayStr;
                isFirstLoad = true;
            }
            if (!checkOutInput.value) {
                checkOutInput.value = tomorrowStr;
            }

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

            // Nếu chưa submit lần nào -> Tự động submit bằng JS (Chạy nền)
        <c:if test="${not searched}">
            if (isFirstLoad) {
                document.getElementById('searchRoomForm').submit();
            }
        </c:if>
        });
    </script>

    <style>
        .hover-lift {
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .hover-lift:hover {
            transform: translateY(-5px);
            box-shadow: 0 .5rem 1.5rem rgba(0,0,0,.15)!important;
        }
        .width-20 {
            width: 20px;
            text-align: center;
        }
        /* Style bo góc form input gọn gàng hơn */
        .dropdown-menu input[type="date"] {
            border-radius: 8px;
            border: 1px solid #dee2e6;
        }
    </style>
</layout:layout>