<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<%-- Shows the list of physical rooms and room maintenance actions. --%>
<layout:layout title="Room List" pageCss="room.css" useBootstrap="true" bodyClass="hbms-ui-page room-management-page">

    <div class="hbms-ui ui-shell room-workspace">
        <div class="room-list">
            <div class="room-page-header">
                <h1>Room List</h1>
                <%-- Nút Add New Room chỉ dành cho Admin --%>
                <c:if test="${sessionScope.user.role eq 'Admin'}">
                    <a href="${pageContext.request.contextPath}/room?action=create" class="btn room-button-primary">
                        <i class="fa-solid fa-plus me-1"></i> Add New Room
                    </a>
                </c:if>
            </div>

            <div class="room-panel">
                <!-- Search & Filter Bar -->
                <form onsubmit="event.preventDefault(); applyFilterAndSearch();" class="room-toolbar">
                    <div class="room-search">
                        <span class="room-search-icon" aria-hidden="true"><i class="fa-solid fa-magnifying-glass"></i></span>
                        <input type="text" id="searchInput" class="form-control" aria-label="Search rooms" placeholder="Search (number, type, status)..." onkeyup="applyFilterAndSearch()">
                        <button type="button" class="btn room-button-primary px-4" onclick="applyFilterAndSearch()">Search</button>
                        <button type="button" class="btn room-button-secondary" data-bs-toggle="modal" data-bs-target="#filterModal">
                            <i class="fa-solid fa-filter"></i> Filter
                        </button>
                    </div>
                </form>

                <%-- ======================= GIAO DIỆN CHUNG (TABLE CHO CẢ ADMIN & STAFF) ======================= --%>
                <div class="table-responsive room-table-scroll" role="region" aria-label="Rooms" tabindex="0">
                    <table class="table table-hover align-middle mb-0 room-table">
                        <thead>
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
                                                <span class="room-status room-status-active">ACTIVE</span>
                                            </c:when>
                                            <c:when test="${c.status eq 'MAINTENANCE'}">
                                                <span class="room-status room-status-warning">MAINTENANCE</span>
                                            </c:when>
                                            <c:otherwise>
                                                <span class="room-status"><c:out value="${c.status}" /></span>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td class="text-center pe-3">
                                        <a href="${pageContext.request.contextPath}/room?action=viewDetail&id=${c.roomID}" class="btn room-button-secondary btn-sm me-1" title="View Details"><i class="fa-solid fa-eye"></i></a>

                                        <c:if test="${sessionScope.user.role eq 'Admin'}">
                                            <a href="${pageContext.request.contextPath}/room?action=update&id=${c.roomID}" class="btn room-button-secondary btn-sm me-1" title="Edit"><i class="fa-solid fa-pen"></i></a>
                                            <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#deleteModal${c.roomID}" title="Delete"><i class="fa-solid fa-trash"></i></button>
                                            </c:if>
                                    </td>
                                </tr>
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

            </div>
        </div>
    </div>

    <c:forEach items="${roomList}" var="c">
        <%-- Pop-up delete room (Chỉ sinh ra nếu là Admin) --%>
        <c:if test="${sessionScope.user.role eq 'Admin'}">
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
                            <button type="button" class="btn room-button-secondary px-4" data-bs-dismiss="modal">Cancel</button>
                            <a href="${pageContext.request.contextPath}/room?action=delete&id=${c.roomID}" class="btn btn-danger px-4">Delete</a>
                        </div>
                    </div>
                </div>
            </div>
        </c:if>
    </c:forEach>

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
                    <!-- Hàng dưới: Lọc phụ (Options) -->
                    <div>
                        <label class="fw-bold mb-3 text-primary">Options:</label>
                        <div id="subFilterContainer" class="ps-2">
                            <!-- Nội dung sẽ được JS fill tự động -->
                        </div>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn room-button-secondary px-4" onclick="clearFilter()">Clear</button>
                    <button type="button" class="btn room-button-primary px-4" onclick="applyFilterAndSearch(true)">Apply Filter</button>
                </div>
            </div>
        </div>
    </div>

    <!-- JavaScript Xử lý Tìm kiếm & Lọc/Sắp xếp -->
    <script>
        // Vì cả 2 role đều dùng bảng (table), hàm này luôn trả về true
        function isTableView() {
            return document.getElementById("roomTableBody") !== null;
        }

        document.addEventListener("DOMContentLoaded", function () {
            updateSubFilter();
        });

        function updateSubFilter() {
            let category = document.querySelector('input[name="filterCategory"]:checked').value;
            let container = document.getElementById("subFilterContainer");
            container.innerHTML = "";
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
                let types = new Set();
                document.querySelectorAll("#roomTableBody tr.room-row").forEach(item => {
                    let type = item.cells[1].textContent.trim();
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
            let wrapper = document.getElementById("roomTableBody");
            let items = Array.from(wrapper.querySelectorAll("tr.room-row"));
            let visibleItems = [];

            let categoryRadio = document.querySelector('input[name="filterCategory"]:checked');
            let subFilterRadio = document.querySelector('input[name="subFilter"]:checked');

            if (fromModal) {
                var filterModal = bootstrap.Modal.getInstance(document.getElementById('filterModal'));
                if (filterModal)
                    filterModal.hide();
            }

            items.forEach(item => {
                let searchData = item.getAttribute("data-search").toLowerCase();
                let roomType = item.cells[1].textContent.trim();
                let roomStatus = item.cells[3].textContent.trim().toUpperCase();

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

            if (categoryRadio && subFilterRadio && categoryRadio.value === "number") {
                let order = subFilterRadio.value;
                visibleItems.sort((a, b) => {
                    let valA = a.cells[0].textContent.trim();
                    let valB = b.cells[0].textContent.trim();

                    let numA = parseInt(valA.replace(/[^0-9]/g, "")) || 0;
                    let numB = parseInt(valB.replace(/[^0-9]/g, "")) || 0;

                    return order === "asc" ? (numA - numB) : (numB - numA);
                });

                visibleItems.forEach(item => wrapper.appendChild(item));
            }

            let noDataRow = document.getElementById("noDataRow");
            if (noDataRow)
                noDataRow.style.display = visibleItems.length === 0 ? "" : "none";
        }

        function clearFilter() {
            document.getElementById("catNum").checked = true;
            updateSubFilter();
            document.getElementById("searchInput").value = "";
            applyFilterAndSearch();
        }
    </script>
</layout:layout>
