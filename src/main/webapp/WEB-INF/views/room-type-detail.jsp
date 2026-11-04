<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="${roomType.typeName} - Room Details" useBootstrap="true"
               pageCss="guest-room-catalog.css" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell room-catalog">
        <c:choose>
            <c:when test="${not empty roomType}">
                <header class="ui-page-heading">
                    <div>
                        <p class="ui-eyebrow">ROOM DETAILS</p>
                        <h1><c:out value="${roomType.typeName}" /></h1>
                    </div>
                    <button type="button" onclick="history.back()" class="ui-button ui-button-secondary">
                        <i class="fa-solid fa-arrow-left" aria-hidden="true"></i> Back
                    </button>
                </header>

                <div class="room-catalog-detail">
                    <section class="ui-panel room-catalog-overview" aria-labelledby="roomOverviewTitle">
                        <div class="room-catalog-photo">
                            <c:choose>
                                <c:when test="${not empty roomType.roomTypeImage}">
                                    <c:url var="roomTypeImageUrl" value="/assets/images/room-types/${roomType.roomTypeImage}" />
                                    <img src="<c:out value='${roomTypeImageUrl}' />" alt="<c:out value='${roomType.typeName}' />">
                                </c:when>
                                <c:otherwise>
                                    <div class="room-catalog-placeholder">
                                        <i class="fa-solid fa-image" aria-hidden="true"></i> No image available
                                    </div>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <div class="room-catalog-panel">
                            <div class="room-catalog-highlights">
                                <span><i class="fa-solid fa-users" aria-hidden="true"></i>
                                    Up to <c:out value="${roomType.capacity}" /> guest(s) per room</span>
                                <span><i class="fa-solid fa-utensils" aria-hidden="true"></i> Free buffet breakfast</span>
                            </div>
                            <h2 id="roomOverviewTitle">About This Room</h2>
                            <c:choose>
                                <c:when test="${not empty roomType.description}">
                                    <p class="room-catalog-description"><c:out value="${roomType.description}" /></p>
                                </c:when>
                                <c:otherwise>
                                    <p class="room-catalog-description">No description available for this room type.</p>
                                </c:otherwise>
                            </c:choose>
                        </div>
                    </section>

                    <section class="ui-panel room-catalog-panel room-catalog-booking" aria-labelledby="roomBookingTitle">
                        <h2 id="roomBookingTitle">Plan Your Stay</h2>
                        <p class="room-catalog-rate">
                            <strong><fmt:formatNumber value="${roomType.price}" type="number" groupingUsed="true" /> VND</strong>
                            <span>/ night</span>
                        </p>
                        <div class="room-catalog-availability">
                            <span>Available rooms</span>
                            <c:choose>
                                <c:when test="${not empty availableRooms}">
                                    <strong class="room-catalog-status ${availableRooms > 0 ? 'is-available' : 'is-sold-out'}">
                                        <c:out value="${availableRooms}" />
                                    </strong>
                                </c:when>
                                <c:otherwise>
                                    <strong class="room-catalog-status">N/A</strong>
                                </c:otherwise>
                            </c:choose>
                        </div>
                        <c:choose>
                            <c:when test="${not empty availableRooms and availableRooms > 0}">
                                <button type="button" class="ui-button ui-button-primary ui-button-wide"
                                        data-bs-toggle="collapse" data-bs-target="#roomBookingForm"
                                        aria-expanded="false" aria-controls="roomBookingForm">
                                    Add to Wishlist
                                </button>
                                <div class="collapse" id="roomBookingForm">
                                    <form action="${pageContext.request.contextPath}/booking-wish-list" method="post"
                                          class="room-catalog-booking-form">
                                        <input type="hidden" name="action" value="add">
                                        <input type="hidden" name="roomTypeID" value="<c:out value='${roomType.roomTypeID}' />">
                                        <div class="ui-field">
                                            <label for="detailCheckIn">Check-in Date</label>
                                            <input type="date" id="detailCheckIn" name="checkInDate" required>
                                        </div>
                                        <div class="ui-field">
                                            <label for="detailCheckOut">Check-out Date</label>
                                            <input type="date" id="detailCheckOut" name="checkOutDate" required>
                                        </div>
                                        <div class="room-catalog-fields">
                                            <div class="ui-field">
                                                <label for="quantity">Rooms</label>
                                                <input type="number" id="quantity" name="quantity"
                                                       value="1" min="1" max="${availableRooms}" required>
                                            </div>
                                            <div class="ui-field">
                                                <label for="guestCount">Guests</label>
                                                <input type="number" id="guestCount" name="guestCount"
                                                       value="1" min="1" required>
                                            </div>
                                        </div>
                                        <button type="submit" class="ui-button ui-button-primary ui-button-wide">
                                            Confirm Selection
                                        </button>
                                    </form>
                                </div>
                            </c:when>
                            <c:otherwise>
                                <button type="button" class="ui-button ui-button-secondary ui-button-wide" disabled>
                                    Currently Sold Out
                                </button>
                            </c:otherwise>
                        </c:choose>
                    </section>
                </div>
            </c:when>
            <c:otherwise>
                <section class="ui-panel ui-empty-state" aria-labelledby="roomNotFoundTitle">
                    <i class="fa-solid fa-bed" aria-hidden="true"></i>
                    <h1 id="roomNotFoundTitle">Room Type Not Found</h1>
                    <p class="mt-2">The room details you are looking for are unavailable.</p>
                    <button type="button" onclick="history.back()" class="ui-button ui-button-secondary mt-3">
                        Go Back
                    </button>
                </section>
            </c:otherwise>
        </c:choose>
    </div>

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
</layout:layout>
