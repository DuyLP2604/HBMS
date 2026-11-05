<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Shows available room types for the selected dates and guest count. --%>
<layout:layout title="Search Rooms" useBootstrap="true"
               pageCss="guest-room-catalog.css" bodyClass="hbms-ui-page">
    <div class="hbms-ui ui-shell room-catalog">
        <header class="ui-page-heading">
            <div>
                <p class="ui-eyebrow">PLAN YOUR STAY</p>
                <h1>Find a Room</h1>
                <p class="ui-description">Choose your dates to see the rooms available for your stay.</p>
            </div>
            <a href="${pageContext.request.contextPath}/booking-wish-list" class="ui-button ui-button-secondary">
                <i class="fa-solid fa-clipboard-list" aria-hidden="true"></i> View Wishlist
            </a>
        </header>

        <div class="ui-panel room-catalog-panel">
            <form id="searchRoomForm" action="${pageContext.request.contextPath}/room-types" method="get"
                  class="room-catalog-search">
                <div class="ui-field">
                    <label for="checkInDate">Check-in Date</label>
                    <input type="date" id="checkInDate" name="checkInDate"
                           value="<c:out value='${checkInDate}' />" min="<c:out value='${today}' />" required>
                </div>
                <div class="ui-field">
                    <label for="checkOutDate">Check-out Date</label>
                    <input type="date" id="checkOutDate" name="checkOutDate"
                           value="<c:out value='${checkOutDate}' />" required>
                </div>
                <button type="submit" class="ui-button ui-button-primary">
                    <i class="fa-solid fa-magnifying-glass" aria-hidden="true"></i> Search Availability
                </button>
            </form>
            <p class="room-catalog-note">
                Same-day bookings close at 10:00 Vietnam time. Check-in is at 14:00.
            </p>
        </div>

        <c:if test="${not empty successMessage}">
            <div class="alert alert-success mt-3" role="status"><c:out value="${successMessage}" /></div>
        </c:if>
        <c:if test="${not empty errorMessage}">
            <div class="alert alert-danger mt-3" role="alert"><c:out value="${errorMessage}" /></div>
        </c:if>

        <c:if test="${searched}">
            <section class="room-catalog-results" aria-labelledby="availableRoomsTitle">
                <div class="ui-section-heading">
                    <h2 id="availableRoomsTitle">Available Rooms</h2>
                    <c:if test="${not empty checkInDate and not empty checkOutDate}">
                        <p>Showing availability from <strong><c:out value="${checkInDate}" /></strong>
                            to <strong><c:out value="${checkOutDate}" /></strong>.</p>
                    </c:if>
                </div>
                <c:choose>
                    <c:when test="${not empty availabilityList}">
                        <div class="room-catalog-grid">
                            <c:forEach var="roomType" items="${availabilityList}">
                                <c:url var="roomTypeDetailUrl" value="/room-types">
                                    <c:param name="action" value="detail" />
                                    <c:param name="id" value="${roomType.roomTypeID}" />
                                </c:url>
                                <article class="ui-panel room-catalog-card">
                                    <c:choose>
                                        <c:when test="${not empty roomType.roomTypeImage}">
                                            <c:url var="roomTypeImageUrl" value="/assets/images/room-types/${roomType.roomTypeImage}" />
                                            <a href="<c:out value='${roomTypeDetailUrl}' />" class="room-catalog-photo">
                                                <img src="<c:out value='${roomTypeImageUrl}' />"
                                                     alt="View details for <c:out value='${roomType.typeName}' />">
                                            </a>
                                        </c:when>
                                        <c:otherwise>
                                            <div class="room-catalog-photo room-catalog-placeholder">
                                                <i class="fa-solid fa-image" aria-hidden="true"></i> No image available
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <div class="room-catalog-card-body">
                                        <div class="room-catalog-card-heading">
                                            <h3><c:out value="${roomType.typeName}" /></h3>
                                            <span class="room-catalog-status ${roomType.availableRooms > 0 ? 'is-available' : 'is-sold-out'}">
                                                <c:choose>
                                                    <c:when test="${roomType.availableRooms > 0}">
                                                        <c:out value="${roomType.availableRooms}" /> available
                                                    </c:when>
                                                    <c:otherwise>Sold out</c:otherwise>
                                                </c:choose>
                                            </span>
                                        </div>
                                        <p class="room-catalog-capacity">
                                            <i class="fa-solid fa-users" aria-hidden="true"></i>
                                            Up to <c:out value="${roomType.capacity}" /> guest(s) per room
                                        </p>
                                        <p class="room-catalog-rate">
                                            <strong><fmt:formatNumber value="${roomType.price}" type="number" groupingUsed="true" /> VND</strong>
                                            <span>/ night</span>
                                        </p>
                                        <a href="<c:out value='${roomTypeDetailUrl}' />" class="room-catalog-link">
                                            View room details <i class="fa-solid fa-arrow-right" aria-hidden="true"></i>
                                        </a>
                                    </div>

                                    <div class="room-catalog-card-actions">
                                        <c:choose>
                                            <c:when test="${roomType.availableRooms > 0}">
                                                <form action="${pageContext.request.contextPath}/booking-wish-list" method="post">
                                                    <input type="hidden" name="action" value="add">
                                                    <input type="hidden" name="roomTypeID" value="<c:out value='${roomType.roomTypeID}' />">
                                                    <input type="hidden" name="checkInDate" value="<c:out value='${checkInDate}' />">
                                                    <input type="hidden" name="checkOutDate" value="<c:out value='${checkOutDate}' />">
                                                    <div class="room-catalog-fields">
                                                        <div class="ui-field">
                                                            <label for="quantity_${roomType.roomTypeID}">Rooms</label>
                                                            <input type="number" id="quantity_${roomType.roomTypeID}" name="quantity"
                                                                   value="1" min="1" max="${roomType.availableRooms}" required>
                                                        </div>
                                                        <div class="ui-field">
                                                            <label for="guestCount_${roomType.roomTypeID}">Guests</label>
                                                            <input type="number" id="guestCount_${roomType.roomTypeID}" name="guestCount"
                                                                   value="1" min="1" required>
                                                        </div>
                                                    </div>
                                                    <button type="submit" class="ui-button ui-button-primary ui-button-wide">
                                                        Add to Wishlist
                                                    </button>
                                                </form>
                                            </c:when>
                                            <c:otherwise>
                                                <button type="button" class="ui-button ui-button-secondary ui-button-wide" disabled>
                                                    Currently Sold Out
                                                </button>
                                            </c:otherwise>
                                        </c:choose>
                                    </div>
                                </article>
                            </c:forEach>
                        </div>
                    </c:when>
                    <c:otherwise>
                        <div class="ui-panel ui-empty-state">
                            <i class="fa-solid fa-calendar-days" aria-hidden="true"></i>
                            <h3>No Rooms Available</h3>
                            <p>Try another check-in or check-out date.</p>
                            <button type="button" class="ui-button ui-button-secondary mt-3"
                                    onclick="document.getElementById('checkInDate').focus()">
                                Change Dates
                            </button>
                        </div>
                    </c:otherwise>
                </c:choose>
            </section>
        </c:if>
    </div>

    <script>
        document.addEventListener("DOMContentLoaded", function () {
            const checkInInput = document.getElementById("checkInDate");
            const checkOutInput = document.getElementById("checkOutDate");
            if (!checkInInput || !checkOutInput) {
                return;
            }

            // The server supplies the earliest date using 10:00 Vietnam time.
            const earliestCheckIn = checkInInput.min;
            let isFirstLoad = false;
            if (!checkInInput.value && earliestCheckIn) {
                checkInInput.value = earliestCheckIn;
                isFirstLoad = true;
            }

            function nextDate(value) {
                const parts = value.split("-").map(Number);
                if (parts.length !== 3 || !parts.every(Number.isFinite)) {
                    return "";
                }
                const date = new Date(Date.UTC(parts[0], parts[1] - 1, parts[2] + 1));
                return Number.isFinite(date.getTime()) ? date.toISOString().slice(0, 10) : "";
            }

            function updateCheckOut() {
                const minimumCheckOut = nextDate(checkInInput.value || earliestCheckIn);
                if (minimumCheckOut) {
                    checkOutInput.min = minimumCheckOut;
                    if (!checkOutInput.value || checkOutInput.value < minimumCheckOut) {
                        checkOutInput.value = minimumCheckOut;
                    }
                }
            }

            checkInInput.addEventListener("change", updateCheckOut);
            updateCheckOut();

            // Keep main's automatic first search, with dates allowed by the server.
            <c:if test="${not searched}">
                if (isFirstLoad && checkInInput.value && checkOutInput.value) {
                    document.getElementById("searchRoomForm").submit();
                }
            </c:if>
        });
    </script>
</layout:layout>
