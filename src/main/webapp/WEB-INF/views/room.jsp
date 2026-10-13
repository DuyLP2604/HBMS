<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Room List" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">

    <style>
        .hover-lift {
            transition: transform 0.2s ease, box-shadow 0.2s ease;
        }
        .hover-lift:hover {
            transform: translateY(-5px);
            box-shadow: 0 .5rem 1.5rem rgba(0,0,0,.15)!important;
        }
        .room-desc {
            display: -webkit-box;
            -webkit-line-clamp: 2;
            -webkit-box-orient: vertical;
            overflow: hidden;
            text-overflow: ellipsis;
        }
    </style>

    <div class="container my-5">
        <div class="card shadow-sm border-0">
            <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3 rounded-top">
                <h2 class="h4 mb-0">Room List</h2>
                <%-- Nút Add New Room chỉ dành cho Admin --%>
                <c:if test="${sessionScope.user.role eq 'Admin'}">
                    <a href="${pageContext.request.contextPath}/room?action=create" class="btn btn-light btn-sm text-primary fw-bold shadow-sm">
                        <i class="fa-solid fa-plus me-1"></i> Add New Room
                    </a>
                </c:if>
            </div>

            <div class="card-body bg-white p-4">
                <!-- Search & Filter Bar chung cho mọi role -->
                <div class="row mb-4">
                    <div class="col-md-8 col-lg-6">
                        <form onsubmit="event.preventDefault(); applyFilterAndSearch();" class="d-flex gap-2">
                            <div class="input-group shadow-sm">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="fa-solid fa-magnifying-glass"></i></span>
                                <input type="text" id="searchInput" class="form-control border-start-0 ps-0" placeholder="Search (number, type, status)..." onkeyup="applyFilterAndSearch()">
                                <button type="button" class="btn btn-primary px-4" onclick="applyFilterAndSearch()">Search</button>
                                <button type="button" class="btn btn-outline-secondary ms-2 rounded" data-bs-toggle="modal" data-bs-target="#filterModal">
                                    <i class="fa-solid fa-filter"></i> Filter
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <%-- ======================= GIAO DIỆN ADMIN (TABLE) ======================= --%>
                <c:if test="${sessionScope.user.role eq 'Admin'}">
                    <div class="table-responsive shadow-sm rounded border">
                        <table class="table table-hover align-middle mb-0 bg-white">
                            <thead class="table-light border-bottom">
                                <tr>
                                    <th class="ps-3">Room Number</th>
                                    <th>Room Type</th>
                                    <th>Image</th>
                                    <th>Status</th>
                                    <th class="text-center pe-3">Action</th>
                                </tr>
                            </thead>
                            <tbody id="roomTableBody">
                                <c:forEach items="${roomList}" var="c">
                                    <tr class="room-row" data-search="<c:out value='${c.roomNumber} ${c.roomTypeID.typeName}${c.status}'/>">
                                        <td class="fw-bold text-dark ps-3"><c:out value="${c.roomNumber}" /></td>
                                        <td><c:out value="${c.roomTypeID.typeName}" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${not empty c.roomImage}">
                                                    <img class="room-thumbnail shadow-sm" src="${pageContext.request.contextPath}/assets/images/room/${c.roomImage}" alt="Room ${c.roomNumber}" style="width: 80px; height: 50px; object-fit: cover; border-radius: 4px;">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="bg-secondary bg-opacity-10 rounded d-inline-flex align-items-center justify-content-center shadow-sm" style="width: 80px; height: 50px;">
                                                        <i class="fa-solid fa-image text-secondary"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${c.status eq 'ACTIVE'}">
                                                    <span class="badge bg-success shadow-sm px-2 py-1">ACTIVE</span>
                                                </c:when>
                                                <c:when test="${c.status eq 'MAINTENANCE'}">
                                                    <span class="badge bg-warning text-dark shadow-sm px-2 py-1">MAINTENANCE</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger shadow-sm px-2 py-1"><c:out value="${c.status}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center pe-3">
                                            <a href="${pageContext.request.contextPath}/room?action=viewDetail&id=${c.roomID}" class="btn btn-outline-info btn-sm me-1" title="View Details"><i class="fa-solid fa-eye"></i></a>
                                            <a href="${pageContext.request.contextPath}/room?action=update&id=${c.roomID}" class="btn btn-outline-warning btn-sm me-1" title="Edit"><i class="fa-solid fa-pen"></i></a>
                                            <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#deleteModal${c.roomID}" title="Delete"><i class="fa-solid fa-trash"></i></button>
                                        </td>
                                    </tr>

                                    <!-- Pop-up delete room -->
                                <div class="modal fade" id="deleteModal${c.roomID}" tabindex="-1" aria-hidden="true">
                                    <div class="modal-dialog modal-dialog-centered">
                                        <div class="modal-content border-0 shadow">
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
                                    <td colspan="5" class="text-center text-muted py-5">
                                        <i class="fa-solid fa-folder-open fa-3x mb-3 text-secondary opacity-50"></i>
                                        <h5>No rooms found.</h5>
                                    </td>
                                </tr>
                            </c:if>
                            </tbody>
                        </table>
                    </div>
                </c:if>

                <%-- ======================= GIAO DIỆN STAFF (CARD GRID) ======================= --%>
                <c:if test="${sessionScope.user.role eq 'Staff'}">
                    <div class="row row-cols-1 row-cols-md-2 row-cols-lg-3 g-4" id="roomCardContainer">
                        <c:forEach items="${roomList}" var="c">
                            <div class="col room-item" data-search="<c:out value='${c.roomNumber} ${c.roomTypeID.typeName}${c.status}'/>">
                                <div class="card h-100 shadow-sm border-0 hover-lift overflow-hidden">
                                    <c:choose>
                                        <c:when test="${not empty c.roomImage}">
                                            <img src="${pageContext.request.contextPath}/assets/images/room/${c.roomImage}" class="card-img-top" alt="Room ${c.roomNumber}" style="height: 220px; object-fit: cover;">
                                        </c:when>
                                        <c:otherwise>
                                            <div class="card-img-top bg-light d-flex align-items-center justify-content-center border-bottom" style="height: 220px;">
                                                <i class="fa-solid fa-image fa-4x text-secondary opacity-25"></i>
                                            </div>
                                        </c:otherwise>
                                    </c:choose>

                                    <div class="card-body d-flex flex-column p-4">
                                        <div class="d-flex justify-content-between align-items-center mb-2">
                                            <h4 class="card-title mb-0 fw-bold text-primary room-number"><c:out value="${c.roomNumber}" /></h4>
                                            <c:choose>
                                                <c:when test="${c.status eq 'ACTIVE'}">
                                                    <span class="badge bg-success px-2 py-1 shadow-sm room-status">ACTIVE</span>
                                                </c:when>
                                                <c:when test="${c.status eq 'MAINTENANCE'}">
                                                    <span class="badge bg-warning text-dark px-2 py-1 shadow-sm room-status">MAINTENANCE</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-danger px-2 py-1 shadow-sm room-status"><c:out value="${c.status}" /></span>
                                                </c:otherwise>
                                            </c:choose>
                                        </div>
                                        <h6 class="card-subtitle text-muted mb-3 room-type"><i class="fa-solid fa-tag me-1"></i><c:out value="${c.roomTypeID.typeName}" /></h6>

                                        <div class="mb-3">
                                            <span class="text-danger fw-bold fs-4"><fmt:formatNumber value="${c.roomTypeID.price}" type="number" maxFractionDigits="0"/> VND</span>
                                            <span class="text-muted fs-6">/ night</span>
                                        </div>

                                        <p class="card-text text-muted small flex-grow-1 room-desc"><c:out value="${c.roomTypeID.description}" /></p>

                                        <a href="${pageContext.request.contextPath}/room?action=viewDetail&id=${c.roomID}" class="btn btn-outline-primary w-100 mt-2 fw-bold">View Details</a>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>
                    </div>

                    <!-- Fallback if empty array on load or empty search -->
                    <div id="noDataMsg" class="text-center text-muted py-5" style="${empty roomList ? '' : 'display: none;'}">
                        <i class="fa-solid fa-folder-open fa-3x mb-3 text-secondary opacity-50"></i>
                        <h5>No rooms found.</h5>
                    </div>
                </c:if>

            </div>
        </div>
    </div>

    <!-- Pop-up Modal Lọc (Filter & Sort) chung -->
    <div class="modal fade" id="filterModal" tabindex="-1" aria-labelledby="filterModalLabel" aria-hidden="true">
        <div class="modal-dialog modal-dialog-centered">
            <div class="modal-content border-0 shadow">
                <div class="modal-header bg-light">
                    <h5 class="modal-title fw-bold text-dark" id="filterModalLabel"><i class="fa-solid fa-sliders me-2"></i>Filter & Sort</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <!-- Hàng trên: Chọn Mục (Column) -->
                    <div class="mb-4">
                        <label class="fw-bold mb-3 text-primary">Sort/Filter By:</label>
                        <div class="d-flex gap-4">
                            <div class="form-check">
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
                    <hr class="text-muted">
                    <!-- Hàng dưới: Lọc phụ (Options) sẽ hiển thị dựa theo Hàng trên -->
                    <div>
                        <label class="fw-bold mb-3 text-primary">Options:</label>
                        <div id="subFilterContainer" class="ps-2">
                            <!-- Nội dung sẽ được JS fill tự động -->
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-outline-secondary px-4" onclick="clearFilter()">Clear</button>
                    <button type="button" class="btn btn-primary px-4" onclick="applyFilterAndSearch(true)">Apply Filter</button>
                </div>
            </div>
        </div>
    </div>

    <!-- JavaScript Xử lý Tìm kiếm mờ & Lọc/Sắp xếp hỗ trợ đa giao diện -->
    <script>
        // Kiểm tra xem hiện tại đang render giao diện Bảng (Admin) hay Lưới (Staff/Customer)
        function isTableView() {
            return document.getElementById("roomTableBody") !== null;
        }

        // Tự động kích hoạt hiển thị mục chọn phụ (Options) ngay khi trang tải xong
        document.addEventListener("DOMContentLoaded", function () {
            updateSubFilter();
        });

        // Hàm này tự động thay đổi giao diện hàng dưới (Sub-filter) khi tick chọn hàng trên
        function updateSubFilter() {
            let category = document.querySelector('input[name="filterCategory"]:checked').value;
            let container = document.getElementById("subFilterContainer");
            container.innerHTML = ""; // Xóa nội dung cũ
            container.classList.remove("text-muted", "fst-italic");

            if (category === "number") {
                container.innerHTML = `
                <div class="d-flex gap-4">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="subFilter" value="asc" id="subAsc" checked>
                        <label class="form-check-label" for="subAsc">Ascending</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="subFilter" value="desc" id="subDesc">
                        <label class="form-check-label" for="subDesc">Descending</label>
                    </div>
                </div>
            `;
            } else if (category === "type") {
                // Tự động quét và lấy ra các loại phòng hiện có
                let types = new Set();
                let isTable = isTableView();
                let selector = isTable ? "#roomTableBody tr.room-row" : "#roomCardContainer div.room-item";

                document.querySelectorAll(selector).forEach(item => {
                    let type = isTable ? item.cells[1].textContent.trim() : item.querySelector('.room-type').textContent.trim();
                    types.add(type);
                });

                let html = '<div class="row g-3">';
                let count = 0;
                types.forEach(type => {
                    let isChecked = (count === 0) ? 'checked' : '';
                    html += `
                    <div class="col-6">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="\${type}" id="type_\${count}" \${isChecked}>
                            <label class="form-check-label text-truncate w-100" title="\${type}" for="type_\${count}">\${type}</label>
                        </div>
                    </div>
                `;
                    count++;
                });
                html += '</div>';
                container.innerHTML = html;
            } else if (category === "status") {
                container.innerHTML = `
                <div class="row g-3">
                    <div class="col-6">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="ACTIVE" id="statusActive" checked>
                            <label class="form-check-label text-success fw-bold" for="statusActive">ACTIVE</label>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="INACTIVE" id="statusInactive">
                            <label class="form-check-label text-secondary fw-bold" for="statusInactive">INACTIVE</label>
                        </div>
                    </div>
                    <div class="col-6">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="MAINTENANCE" id="statusMaint">
                            <label class="form-check-label text-warning fw-bold" for="statusMaint">MAINTENANCE</label>
                        </div>
                    </div>
                </div>
            `;
            }
        }

        function applyFilterAndSearch(fromModal = false) {
            let keyword = document.getElementById("searchInput").value.toLowerCase().trim();
            let isTable = isTableView();
            let wrapper = document.getElementById(isTable ? "roomTableBody" : "roomCardContainer");
            let items = Array.from(wrapper.querySelectorAll(isTable ? "tr.room-row" : "div.room-item"));
            let visibleItems = [];

            let categoryRadio = document.querySelector('input[name="filterCategory"]:checked');
            let subFilterRadio = document.querySelector('input[name="subFilter"]:checked');

            if (fromModal) {
                var filterModal = bootstrap.Modal.getInstance(document.getElementById('filterModal'));
                if (filterModal)
                    filterModal.hide();
            }

            // Lọc dữ liệu
            items.forEach(item => {
                let searchData = item.getAttribute("data-search").toLowerCase();
                let roomType = isTable ? item.cells[1].textContent.trim() : item.querySelector('.room-type').textContent.trim();
                let roomStatus = isTable ? item.cells[3].textContent.trim().toUpperCase() : item.querySelector('.room-status').textContent.trim().toUpperCase();

                let matchSearch = searchData.includes(keyword);
                let matchFilter = true;

                if (categoryRadio && subFilterRadio) {
                    let category = categoryRadio.value;
                    let subVal = subFilterRadio.value;

                    if (category === "type") {
                        if (roomType !== subVal)
                            matchFilter = false;
                    } else if (category === "status") {
                        if (roomStatus !== subVal.toUpperCase())
                            matchFilter = false;
                    }
                }

                if (matchSearch && matchFilter) {
                    item.style.display = "";
                    visibleItems.push(item);
                } else {
                    item.style.display = "none";
                }
            });

            // Sắp xếp
            if (categoryRadio && subFilterRadio && categoryRadio.value === "number") {
                let order = subFilterRadio.value;

                visibleItems.sort((a, b) => {
                    let valA = isTable ? a.cells[0].textContent.trim() : a.querySelector('.room-number').textContent.trim();
                    let valB = isTable ? b.cells[0].textContent.trim() : b.querySelector('.room-number').textContent.trim();

                    let numA = parseInt(valA.replace(/[^0-9]/g, "")) || 0;
                    let numB = parseInt(valB.replace(/[^0-9]/g, "")) || 0;

                    return order === "asc" ? (numA - numB) : (numB - numA);
                });

                visibleItems.forEach(item => wrapper.appendChild(item));
            }

            // Xử lý thông báo Empty Data
            let noDataRow = document.getElementById("noDataRow"); // Admin Table
            if (noDataRow)
                noDataRow.style.display = visibleItems.length === 0 ? "" : "none";

            let noDataMsg = document.getElementById("noDataMsg"); // Staff/Customer Card Grid
            if (noDataMsg)
                noDataMsg.style.display = visibleItems.length === 0 ? "" : "none";
        }

        function clearFilter() {
            document.getElementById("catNum").checked = true;
            updateSubFilter();
            document.getElementById("searchInput").value = "";
            applyFilterAndSearch();
        }
    </script>
</layout:layout>