<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Room List" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">
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

    <!-- JavaScript Xử lý Tìm kiếm mờ & Lọc/Sắp xếp mới -->
    <script>
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
                // Mặc định chọn Ascending (bằng thuộc tính checked)
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
                // Tự động quét và lấy ra các loại phòng hiện có trong bảng
                let types = new Set();
                document.querySelectorAll("#roomTableBody tr.room-row").forEach(row => {
                    types.add(row.cells[1].textContent.trim()); // index 1 là cột Room Type
                });

                let html = '<div class="row g-2">';
                let count = 0;
                types.forEach(type => {
                    // Mặc định chọn mục đầu tiên
                    let isChecked = (count === 0) ? 'checked' : '';
                    html += `
                    <div class="col-4">
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
                // Mặc định chọn ACTIVE
                container.innerHTML = `
                <div class="row g-2">
                    <div class="col-4">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="ACTIVE" id="statusActive" checked>
                            <label class="form-check-label" for="statusActive">ACTIVE</label>
                        </div>
                    </div>
                    <div class="col-4">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="INACTIVE" id="statusInactive">
                            <label class="form-check-label" for="statusInactive">INACTIVE</label>
                        </div>
                    </div>
                    <div class="col-4">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="MAINTENANCE" id="statusMaint">
                            <label class="form-check-label" for="statusMaint">MAINTENANCE</label>
                        </div>
                    </div>
                </div>
            `;
            }
        }

        function applyFilterAndSearch(fromModal = false) {
            let keyword = document.getElementById("searchInput").value.toLowerCase().trim();
            let tbody = document.getElementById("roomTableBody");
            let rows = Array.from(tbody.querySelectorAll("tr.room-row"));
            let visibleRows = [];

            // Kiểm tra trạng thái Modal
            let categoryRadio = document.querySelector('input[name="filterCategory"]:checked');
            let subFilterRadio = document.querySelector('input[name="subFilter"]:checked');

            // Đóng Modal
            if (fromModal) {
                var filterModal = bootstrap.Modal.getInstance(document.getElementById('filterModal'));
                if (filterModal)
                    filterModal.hide();
            }

            // 1. Kết hợp Tìm Kiếm Mờ (Search) & Lọc Loại Phòng/Trạng Thái (Lọc loại bỏ)
            rows.forEach(row => {
                let searchData = row.getAttribute("data-search").toLowerCase();
                let roomType = row.cells[1].textContent.trim();
                let roomStatus = row.cells[3].textContent.trim().toUpperCase();

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
                    row.style.display = "";
                    visibleRows.push(row);
                } else {
                    row.style.display = "none";
                }
            });

            // 2. Sắp Xếp theo Room Number (chỉ thực hiện nếu Hàng trên là Number)
            if (categoryRadio && subFilterRadio && categoryRadio.value === "number") {
                let order = subFilterRadio.value;

                visibleRows.sort((a, b) => {
                    let valA = a.cells[0].textContent.trim(); // index 0 là Room Number
                    let valB = b.cells[0].textContent.trim();

                    let numA = parseInt(valA.replace(/[^0-9]/g, "")) || 0;
                    let numB = parseInt(valB.replace(/[^0-9]/g, "")) || 0;

                    return order === "asc" ? (numA - numB) : (numB - numA);
                });

                // Cập nhật lại HTML
                visibleRows.forEach(row => tbody.appendChild(row));
            }

            // Xử lý dòng thông báo "No rooms found"
            let noDataRow = document.getElementById("noDataRow");
            if (noDataRow) {
                noDataRow.style.display = visibleRows.length === 0 ? "" : "none";
        }
        }

        function clearFilter() {
            // Trả hàng trên về mặc định (Room Number)
            document.getElementById("catNum").checked = true;
            // Hàm này sẽ tự động tick mục phụ đầu tiên "Ascending"
            updateSubFilter();

            // Xóa chữ trong thanh tìm kiếm
            document.getElementById("searchInput").value = "";

            // Áp dụng lại (Hiển thị tất cả phòng và tự sort theo Number -> Ascending)
            applyFilterAndSearch();
        }
    </script>
</layout:layout>