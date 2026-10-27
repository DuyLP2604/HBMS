<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Xác nhận Check-In" useBootstrap="true" bodyClass="bg-light">
    <div class="container py-5">
        <div class="row justify-content-center">
            <div class="col-md-6 col-lg-5">
                
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

                        <h5 class="fw-bold text-success mb-3 border-bottom pb-2">Guest Information by Room</h5>
                        
                        <form action="${pageContext.request.contextPath}/process-checkin" method="POST">
                            <input type="hidden" name="action" value="confirmCheckIn">
                            <input type="hidden" name="bookingID" value="${booking.bookingID}">

                            <c:forEach var="detail" items="${booking.bookingDetailCollection}">
                                <c:forEach var="assignment" items="${detail.roomAssignmentCollection}">
                                    <c:set var="roomNo" value="${assignment.roomID.roomNumber}" />
                                    
                                    <div class="card bg-light border-0 shadow-sm mb-4">
                                        <div class="card-body">
                                            <h6 class="fw-bold text-primary border-bottom pb-2 mb-3">
                                                <i class="fa-solid fa-door-closed me-2"></i>Room: <span class="fs-5">${roomNo}</span> <span class="text-muted fw-normal fs-6">(${detail.roomTypeID.typeName})</span>
                                            </h6>
                                            
                                            <div id="cccd-container-${roomNo}">
                                                <div class="mb-3">
                                                    <label class="form-label fw-bold">CCCD (Require) <span class="text-danger">*</span></label>
                                                    <input type="text" name="cccd" class="form-control border-primary" placeholder="Nhập 12 số CCCD đại diện phòng ${roomNo}..." required pattern="[0-9]{12}" minlength="12" maxlength="12" title="Vui lòng nhập đúng 12 số CCCD.">
                                                </div>
                                            </div>

                                            <div class="mb-2 text-center">
                                                <button type="button" class="btn btn-outline-primary btn-sm rounded-pill px-3" id="btnAddCccd-${roomNo}" onclick="addCccdField('${roomNo}')">
                                                    <i class="fa-solid fa-plus"></i> Add a roommate
                                                </button>
                                                <div class="form-text text-muted mt-1">(Add maxium 4 peoples / room)</div>
                                            </div>
                                        </div>
                                    </div>
                                </c:forEach>
                            </c:forEach>

                            <div class="d-grid gap-2 mt-4">
                                <button type="submit" class="btn btn-success btn-lg fw-bold shadow-sm">
                                    <i class="fa-solid fa-check me-1"></i> Check In Complete
                                </button>
                            </div>
                        </form>
                        
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script>
        const roomGuestCounts = {};
        const maxGuestsPerRoom = 4; // Tối đa 4 CCCD cho 1 phòng

        function addCccdField(roomNo) {
            if (!roomGuestCounts[roomNo]) {
                roomGuestCounts[roomNo] = 1;
            }

            if (roomGuestCounts[roomNo] < maxGuestsPerRoom) {
                roomGuestCounts[roomNo]++;
                const container = document.getElementById("cccd-container-" + roomNo);
                
                const div = document.createElement("div");
                div.className = "mb-3 input-group cccd-row-" + roomNo;
                div.innerHTML = `
                    <span class="input-group-text fw-bold cccd-label-` + roomNo + `"></span>
                    <input type="text" name="cccd" class="form-control" placeholder="Nhập 12 số CCCD người đi cùng..." required pattern="[0-9]{12}" minlength="12" maxlength="12" title="Vui lòng nhập đúng 12 số CCCD.">
                    <button class="btn btn-danger" type="button" onclick="removeCccdField(this, '` + roomNo + `')">
                        <i class="fa-solid fa-trash"></i>
                    </button>
                `;
                container.appendChild(div);
                
                updateCccdNumbering(roomNo);

                if (roomGuestCounts[roomNo] >= maxGuestsPerRoom) {
                    document.getElementById("btnAddCccd-" + roomNo).style.display = "none";
                }
            }
        }

        function removeCccdField(button, roomNo) {
            button.closest(".cccd-row-" + roomNo).remove();
            roomGuestCounts[roomNo]--;
            
            document.getElementById("btnAddCccd-" + roomNo).style.display = "inline-block";
            
            updateCccdNumbering(roomNo);
        }

        function updateCccdNumbering(roomNo) {
            const labels = document.querySelectorAll('.cccd-label-' + roomNo);
            labels.forEach((label, index) => {
                label.innerText = (index + 2) + '. CCCD';
            });
        }
    </script>
</layout:layout>