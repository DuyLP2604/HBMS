<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Xác nhận Check-In" useBootstrap="true" bodyClass="bg-light">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5">
                
                <!-- Nút quay lại -->
                <a href="${pageContext.request.contextPath}/staff/stays" class="btn btn-secondary mb-3 btn-sm">
                    <i class="fa-solid fa-arrow-left"></i> Back
                </a>

                <div class="card shadow border-0 rounded-3">
                    <div class="card-header bg-success text-white py-3">
                        <h4 class="mb-0 text-center"><i class="fa-solid fa-bell-concierge"></i> Check-in</h4>
                    </div>
                    
                    <div class="card-body p-4">
                        <h5 class="fw-bold text-success mb-3 border-bottom pb-2">Booking Information</h5>
                        
                        <div class="mb-2 d-flex justify-content-between">
                            <span class="text-muted">BookingID:</span>
                            <span class="fw-bold">${booking.bookingID}</span>
                        </div>
                        <div class="mb-2 d-flex justify-content-between">
                            <span class="text-muted">Customer name:</span>
                            <span class="fw-bold text-end">${booking.customerID.fullName}</span>
                        </div>
                        <div class="mb-2 d-flex justify-content-between">
                            <span class="text-muted">Email:</span>
                            <span class="fw-bold">${booking.customerID.email}</span>
                        </div>
                        <div class="mb-2 d-flex justify-content-between">
                            <span class="text-muted">Phone:</span>
                            <span class="fw-bold">${booking.customerID.phone}</span>
                        </div>
                        <div class="mb-4 d-flex justify-content-between align-items-start">
                            <span class="text-muted">Designated room:</span>
                            <div class="text-end">
                                <c:forEach var="detail" items="${booking.bookingDetailCollection}">
                                    <div class="mb-1">
                                        <span class="text-dark fw-semibold me-2"><c:out value="${detail.roomTypeID.typeName}"/>:</span>
                                        <c:forEach var="assignment" items="${detail.roomAssignmentCollection}">
                                            <span class="badge bg-primary fs-6 me-1 shadow-sm">Room <c:out value="${assignment.roomID.roomNumber}"/></span>
                                        </c:forEach>
                                    </div>
                                </c:forEach>
                            </div>
                        </div>

                        <h5 class="fw-bold text-success mb-3 border-bottom pb-2">Guest Information</h5>
                        
                        <form action="${pageContext.request.contextPath}/process-checkin" method="POST">
                            <input type="hidden" name="action" value="confirmCheckIn">
                            <input type="hidden" name="bookingID" value="${booking.bookingID}">

                            <div id="cccd-container">
                                <div class="mb-3">
                                    <label class="form-label fw-bold">1. CCCD (Require) <span class="text-danger">*</span></label>
                                    <input type="text" name="cccd" class="form-control" placeholder="Enter the 12-digit CCCD number......" required pattern="[0-9]{9,12}" title="Please enter a valid CCCD number.">
                                </div>
                            </div>

                            <div class="mb-4 text-center">
                                <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3" id="btnAddCccd" onclick="addCccdField()">
                                    <i class="fa-solid fa-plus"></i> Add a roommate
                                </button>
                                <div class="form-text text-muted mt-1">(Maximum of 3 additional people)</div>
                            </div>

                            <div class="d-grid gap-2">
                                <button type="submit" class="btn btn-success btn-lg fw-bold">
                                    <i class="fa-solid fa-check"></i> Check In Complete
                                </button>
                            </div>
                        </form>
                        
                    </div>
                </div>

            </div>
        </div>
    </div>

    <script>
        let currentGuest = 1;
        const maxGuests = 4; // 4 CCCD

        function addCccdField() {
            if (currentGuest < maxGuests) {
                currentGuest++;
                const container = document.getElementById("cccd-container");
                
                const div = document.createElement("div");
                div.className = "mb-3 input-group cccd-row";
                div.innerHTML = `
                    <span class="input-group-text fw-bold">` + currentGuest + `. CCCD</span>
                    <input type="text" name="cccd" class="form-control" placeholder="Nhập CCCD người đi cùng..." required pattern="[0-9]{9,12}">
                    <button class="btn btn-danger" type="button" onclick="removeCccdField(this)">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                `;
                container.appendChild(div);

                if (currentGuest >= maxGuests) {
                    document.getElementById("btnAddCccd").style.display = "none";
                }
            }
        }

        function removeCccdField(button) {
            button.closest(".cccd-row").remove();
            currentGuest--;
            document.getElementById("btnAddCccd").style.display = "inline-block";
        }
    </script>
</layout:layout>