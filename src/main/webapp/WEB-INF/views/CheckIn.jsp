<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Checked In" useBootstrap="true">
    <div class="container py-5">
        <h1 class="h3 mb-4 text-success">
            <i class="fa-solid fa-bell-concierge"></i> List of In-house Guests
        </h1>
        
        <c:if test="${not empty sessionScope.successMessage}">
            <div class="alert alert-success alert-dismissible fade show shadow-sm">
                <c:out value="${sessionScope.successMessage}" />
                <button type="button" class="btn-close" data-bs-dismiss="alert"></button>
            </div>
            <c:remove var="successMessage" scope="session"/>
        </c:if>

        <div class="card shadow-sm border-0">
            <div class="card-body p-0">
                <div class="table-responsive">
                    <table class="table table-hover align-middle mb-0 text-center">
                        <thead class="table-light">
                            <tr>
                                <th>Booking ID</th>
                                <th>Customer Name</th>
                                <th>Check-in</th>
                                <th>Check-out</th>
                                <th>Status</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="booking" items="${bookings}">
                                <tr>
                                    <td class="fw-bold text-primary">${booking.bookingID}</td>
                                    <td class="fw-medium">${booking.customerID.fullName}</td>
                                    <td><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><span class="badge bg-success px-3 py-2 rounded-pill">Staying</span></td>
                                    <td>
                                        <button type="button" class="btn btn-outline-info btn-sm rounded-pill px-3 shadow-sm" data-bs-toggle="modal" data-bs-target="#detailModal_${booking.bookingID}">
                                            <i class="fa-solid fa-eye me-1"></i> View Detail
                                        </button>
                                        
                                        <div class="modal fade text-start" id="detailModal_${booking.bookingID}" tabindex="-1" aria-labelledby="modalLabel_${booking.bookingID}" aria-hidden="true">
                                          <div class="modal-dialog modal-dialog-centered">
                                            <div class="modal-content border-0 shadow">
                                              <div class="modal-header bg-info text-white">
                                                <h5 class="modal-title" id="modalLabel_${booking.bookingID}">
                                                    <i class="fa-solid fa-address-card me-2"></i>Chi tiết Lưu trú
                                                </h5>
                                                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                              </div>
                                              <div class="modal-body p-4">
                                                <div class="row mb-2">
                                                    <div class="col-4 text-muted">Mã Booking:</div>
                                                    <div class="col-8 fw-bold text-primary">${booking.bookingID}</div>
                                                </div>
                                                <div class="row mb-2">
                                                    <div class="col-4 text-muted">Khách hàng:</div>
                                                    <div class="col-8 fw-bold">${booking.customerID.fullName}</div>
                                                </div>
                                                <div class="row mb-2">
                                                    <div class="col-4 text-muted">Điện thoại:</div>
                                                    <div class="col-8">${booking.customerID.phone}</div>
                                                </div>
                                                <div class="row mb-2">
                                                    <div class="col-4 text-muted">Email:</div>
                                                    <div class="col-8">${booking.customerID.email}</div>
                                                </div>
                                                
                                                <hr class="my-3">
                                                <h6 class="fw-bold text-success mb-3"><i class="fa-regular fa-id-card me-1"></i> Danh sách CCCD theo phòng:</h6>
                                                
                                                <c:choose>
                                                    <c:when test="${not empty booking.guestCCCDs}">
                                                        <c:set var="cccdList" value="${fn:split(booking.guestCCCDs, ',')}" />
                                                        <c:set var="cccdIndex" value="0" />
                                                        
                                                        <c:forEach var="detail" items="${booking.bookingDetailCollection}" varStatus="detailStatus">
                                                            <c:forEach var="assignment" items="${detail.roomAssignmentCollection}" varStatus="roomStatus">
                                                                <div class="mb-3 ps-3 border-start border-3 border-success">
                                                                    <div class="fw-bold text-dark mb-2">
                                                                        <i class="fa-solid fa-door-closed text-primary me-2"></i>Phòng ${assignment.roomID.roomNumber} <span class="text-muted fw-normal">(${detail.roomTypeID.typeName})</span>
                                                                    </div>
                                                                    
                                                                    <%-- Phân bổ 1 CCCD làm người đại diện cho phòng này --%>
                                                                    <c:if test="${cccdIndex < fn:length(cccdList)}">
                                                                        <div class="bg-light border rounded px-3 py-2 mb-2 font-monospace text-dark shadow-sm">
                                                                            <i class="fa-solid fa-user-tie text-primary me-2"></i> Đại diện: ${cccdList[cccdIndex].trim()}
                                                                        </div>
                                                                        <c:set var="cccdIndex" value="${cccdIndex + 1}" />
                                                                    </c:if>
                                                                    
                                                                    <%-- Nếu đây là phòng cuối cùng trong booking, hiển thị toàn bộ những người đi cùng còn lại --%>
                                                                    <c:if test="${detailStatus.last and roomStatus.last and cccdIndex < fn:length(cccdList)}">
                                                                        <c:forEach begin="${cccdIndex}" end="${fn:length(cccdList) - 1}" var="i">
                                                                            <div class="bg-light border rounded px-3 py-2 mb-2 font-monospace text-dark shadow-sm ms-4">
                                                                                <i class="fa-solid fa-user-check text-success me-2"></i> Đi cùng: ${cccdList[i].trim()}
                                                                            </div>
                                                                        </c:forEach>
                                                                    </c:if>
                                                                </div>
                                                            </c:forEach>
                                                        </c:forEach>
                                                    </c:when>
                                                    <c:otherwise>
                                                        <div class="text-danger fst-italic px-2">Chưa có dữ liệu CCCD</div>
                                                    </c:otherwise>
                                                </c:choose>
                                              </div>
                                              <div class="modal-footer bg-light">
                                                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Đóng</button>
                                              </div>
                                            </div>
                                          </div>
                                        </div>
                                        <!-- Kết thúc Modal -->
                                        
                                    </td>
                                </tr>
                            </c:forEach>
                            
                            <c:if test="${empty bookings}">
                                <tr>
                                    <td colspan="6" class="text-center text-muted py-5">
                                        <i class="fa-solid fa-folder-open fs-1 mb-3 text-secondary opacity-50"></i>
                                        <p class="mb-0 fs-5">There are currently no guests staying.</p>
                                    </td>
                                </tr>
                            </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
        
        <div class="mt-4">
            <a href="${pageContext.request.contextPath}/staff/dashboard" class="btn btn-secondary">
                <i class="fa-solid fa-arrow-left"></i> Back to dashboard
            </a>
        </div>
    </div>
</layout:layout>