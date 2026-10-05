<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Room List" pageCss="room.css" pageJs="room.js" useBootstrap="true" bodyClass="bg-light">
    <div class="container my-5">
        <div class="card shadow-sm">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                <h2 class="h4 mb-0">Room List</h2>
                <a href="${pageContext.request.contextPath}/room?action=create" class="btn btn-success btn-sm">
                    + Add New Room
                </a>
            </div>

            <div class="card-body">
                <!-- Search & Filter Bar -->
                <div class="row mb-4">
                    <div class="col-md-8">
                        <form onsubmit="event.preventDefault(); applyFilterAndSearch();" class="d-flex gap-2">
                            <div class="input-group">
                                <span class="input-group-text bg-white"><i class="fa-solid fa-magnifying-glass"></i></span>
                                <!-- Ô nhập tìm kiếm, bắt sự kiện onkeyup để tìm live -->
                                <input type="text" id="searchInput" class="form-control" placeholder="Search (number, type, status)..." onkeyup="applyFilterAndSearch()">
                                <button type="button" class="btn btn-primary" onclick="applyFilterAndSearch()">Search</button>
                                <!-- Nút gọi Pop-up Filter -->
                                <button type="button" class="btn btn-secondary ms-2 rounded" data-bs-toggle="modal" data-bs-target="#filterModal">
                                    <i class="fa-solid fa-filter"></i> Filter
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Room Table -->
                <div class="table-responsive">
                    <table class="table table-hover align-middle border">
                        <thead class="table-light">
                            <tr>
                                <th>Room Number</th>
                                <th>Room Type</th>
                                <th>Image</th>
                                <th>Status</th>
                                <th class="text-center">Action</th>
                            </tr>
                        </thead>
                        <tbody id="roomTableBody">
                            <c:forEach items="${roomList}" var="c">
                                <!-- Lưu trữ chuỗi data-search để JS quét (tránh quét nhầm chữ trong modal xóa) -->
                                <tr class="room-row" data-search="<c:out value='${c.roomNumber} ${c.roomTypeID.typeName} ${c.status}'/>">
                                    <td class="fw-bold text-secondary"><c:out value="${c.roomNumber}" /></td>
                                    <td><c:out value="${c.roomTypeID.typeName}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty c.roomImage}">
                                                <img class="room-thumbnail" src="${pageContext.request.contextPath}/assets/images/room/${c.roomImage}" alt="Room ${c.roomNumber}" style="width: 80px; height: 50px; object-fit: cover; border-radius: 4px;">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="bg-secondary bg-opacity-10 rounded d-inline-flex align-items-center justify-content-center" style="width: 80px; height: 50px;">
                                                    <i class="fa-solid fa-image text-secondary"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${c.status eq 'Available'}">
                                                <span class="badge rounded-pill bg-success">Available</span>
                                            </c:when>
                                            <c:when test="${c.status eq 'Occupied'}">
                                                <span class="badge rounded-pill bg-warning text-dark">Occupied</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="badge rounded-pill bg-danger"><c:out value="${c.status}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center">
                                        <a href="${pageContext.request.contextPath}/room?action=viewDetail&id=${c.roomID}" class="btn btn-outline-info btn-sm me-1">View Details</a>
                                        <a href="${pageContext.request.contextPath}/room?action=update&id=${c.roomID}" class="btn btn-outline-warning btn-sm me-1">Edit</a>
                                        <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#deleteModal${c.roomID}">Delete</button>
                                    </td>
                                </tr>

                                <!-- Pop-up delete room -->
                            <div class="modal fade" id="deleteModal${c.roomID}" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog modal-dialog-centered">
                                    <div class="modal-content">
                                        <div class="modal-header bg-danger text-white">
                                            <h5 class="modal-title">Confirm Delete</h5>
                                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body text-center py-4">
                                            <h5 class="mb-3">Are you sure to delete this room?</h5>
                                            <h4 class="text-primary fw-bold mb-3">Room: ${c.roomNumber}</h4>
                                            <c:choose>
                                                <c:when test="${not empty c.roomImage}">
                                                    <img src="${pageContext.request.contextPath}/assets/images/room/${c.roomImage}" class="img-thumbnail shadow-sm mb-2" style="width: 150px; height: 100px; object-fit: cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="bg-secondary bg-opacity-10 rounded d-inline-flex align-items-center justify-content-center img-thumbnail mb-2" style="width: 150px; height: 100px;">
                                                        <i class="fa-solid fa-image fa-2x text-secondary"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <div class="modal-footer justify-content-center bg-light">
                                            <button type="button" class="btn btn-secondary px-4" data-bs-dismiss="modal">Cancel</button>
                                            <a href="${pageContext.request.contextPath}/room?action=delete&id=${c.roomID}" class="btn btn-danger px-4">Delete</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <c:if test="${empty roomList}">
                            <tr id="noDataRow">
                                <td colspan="5" class="text-center text-muted py-4">No rooms found.</td>
                            </tr>
                        </c:if>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>

    <!-- Pop-up Modal Lọc (Filter & Sort) -->
    <div class="modal fade" id="filterModal" tabindex="-1" aria-labelledby="filterModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content">
                <div class="modal-header">
                    <h5 class="modal-title" id="filterModalLabel">Filter & Sort</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body">
                    <!-- Hàng trên: Chọn Mục (Column) -->
                    <div class="mb-3">
                        <label class="fw-bold mb-2">Column:</label>
                        <div class="d-flex gap-4">
                            <div class="form-check">
                                <!-- Đã thêm checked làm mặc định ban đầu -->
                                <input class="form-check-input" type="radio" name="filterCategory" value="number" id="catNum" onchange="updateSubFilter()" checked>
                                <label class="form-check-label" for="catNum">Room Number</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="filterCategory" value="type" id="catType" onchange="updateSubFilter()">
                                <label class="form-check-label" for="catType">Room Type</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="filterCategory" value="status" id="catStatus" onchange="updateSubFilter()">
                                <label class="form-check-label" for="catStatus">Status</label>
                            </div>
                        </div>
                    </div>
                    <hr>
                    <!-- Hàng dưới: Lọc phụ (Options) sẽ hiển thị dựa theo Hàng trên -->
                    <div>
                        <!-- Đã bỏ chữ (Required) -->
                        <label class="fw-bold mb-2">Options:</label>
                        <div id="subFilterContainer">
                            <!-- Nội dung sẽ được JS fill tự động -->
                        </div>
                    </div>
                </div>
                <div class="modal-footer">
                    <button type="button" class="btn btn-outline-secondary" onclick="clearFilter()">Clear</button>
                    <button type="button" class="btn btn-primary" onclick="applyFilterAndSearch(true)">Apply Filter</button>
                </div>
            </div>
        </div>
    </div>
</layout:layout>