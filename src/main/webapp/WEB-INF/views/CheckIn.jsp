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
                            <c:forEach var="booking" items="${bookings}" varStatus="bookingStatus">
                                <tr>
                                    <td class="fw-bold text-primary"><c:out value="${booking.bookingID}" /></td>
                                    <td class="fw-medium"><c:out value="${booking.customerID.fullName}" /></td>
                                    <td><fmt:formatDate value="${booking.checkInDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><fmt:formatDate value="${booking.checkOutDate}" pattern="dd/MM/yyyy" /></td>
                                    <td><span class="badge bg-success px-3 py-2 rounded-pill">Staying</span></td>
                                    <td>
                                        <button type="button" class="btn btn-outline-info btn-sm rounded-pill px-3 shadow-sm" data-bs-toggle="modal" data-bs-target="#detailModal_${bookingStatus.index}">
                                            <i class="fa-solid fa-eye me-1"></i> View Detail
                                        </button>
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

    <c:forEach var="booking" items="${bookings}" varStatus="bookingStatus">
        <div class="modal fade text-start" id="detailModal_${bookingStatus.index}" tabindex="-1" aria-labelledby="modalLabel_${bookingStatus.index}" aria-hidden="true">
          <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
              <div class="modal-header bg-info text-white">
                <h5 class="modal-title" id="modalLabel_${bookingStatus.index}">
                    <i class="fa-solid fa-address-card me-2"></i>Chi tiết Lưu trú
                </h5>
                <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
              </div>
              <div class="modal-body p-4">
                <div class="row mb-2">
                    <div class="col-4 text-muted">Mã Booking:</div>
                    <div class="col-8 fw-bold text-primary"><c:out value="${booking.bookingID}" /></div>
                </div>
                <div class="row mb-2">
                    <div class="col-4 text-muted">Khách hàng:</div>
                    <div class="col-8 fw-bold"><c:out value="${booking.customerID.fullName}" /></div>
                </div>
                <div class="row mb-2">
                    <div class="col-4 text-muted">Điện thoại:</div>
                    <div class="col-8"><c:out value="${booking.customerID.phone}" /></div>
                </div>
                <div class="row mb-2">
                    <div class="col-4 text-muted">Email:</div>
                    <div class="col-8"><c:out value="${booking.customerID.email}" /></div>
                </div>
                
                <hr class="my-3">
                <h6 class="fw-bold text-success mb-3">Phòng đã gán</h6>
                <c:set var="hasAssignedRoom" value="${false}" />
                <c:forEach var="detail" items="${booking.bookingDetailCollection}">
                    <c:forEach var="assignment" items="${detail.roomAssignmentCollection}">
                        <c:set var="hasAssignedRoom" value="${true}" />
                        <div class="mb-2 ps-3 border-start border-3 border-success">
                            Phòng <strong><c:out value="${assignment.roomID.roomNumber}" /></strong>
                            (<c:out value="${detail.roomTypeID.typeName}" />)
                        </div>
                    </c:forEach>
                </c:forEach>
                <c:if test="${not hasAssignedRoom}">
                    <p class="text-muted">Chưa có dữ liệu phòng được gán.</p>
                </c:if>
                <hr class="my-3">
                <h6 class="fw-bold text-success mb-3">Giấy tờ khách trong booking</h6>
                <c:choose>
                    <c:when test="${not empty fn:trim(booking.guestCCCDs)}">
                        <ul class="list-group">
                            <c:forEach var="document" items="${fn:split(booking.guestCCCDs, ',')}">
                                <c:if test="${not empty fn:trim(document)}">
                                    <li class="list-group-item font-monospace"><c:out value="${fn:trim(document)}" /></li>
                                </c:if>
                            </c:forEach>
                        </ul>
                    </c:when>
                    <c:otherwise>
                        <p class="text-muted">Chưa có dữ liệu giấy tờ.</p>
                    </c:otherwise>
                </c:choose>
              </div>
              <div class="modal-footer bg-light">
                <button type="button" class="btn btn-secondary btn-sm" data-bs-dismiss="modal">Đóng</button>
              </div>
            </div>
          </div>
        </div>

    </c:forEach>
</layout:layout>