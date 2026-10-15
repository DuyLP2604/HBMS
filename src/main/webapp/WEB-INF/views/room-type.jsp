<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Room Type Management" pageCss="room.css" useBootstrap="true" bodyClass="bg-light">

    <style>
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
                <h2 class="h4 mb-0"><i class="fa-solid fa-list me-2"></i>Room Type Management</h2>
                <a href="${pageContext.request.contextPath}/room-types?action=create" class="btn btn-light btn-sm text-primary fw-bold shadow-sm">
                    <i class="fa-solid fa-plus me-1"></i> Add Room Type
                </a>
            </div>

            <div class="card-body bg-white p-4">
                <!-- Search & Filter Bar -->
                <div class="row mb-4">
                    <div class="col-md-8 col-lg-6">
                        <form onsubmit="event.preventDefault(); applyFilterAndSearch();" class="d-flex gap-2">
                            <div class="input-group shadow-sm">
                                <span class="input-group-text bg-white border-end-0 text-muted"><i class="fa-solid fa-magnifying-glass"></i></span>
                                <input type="text" id="searchInput" class="form-control border-start-0 ps-0" placeholder="Search (name, ID, description)..." onkeyup="applyFilterAndSearch()">
                                <button type="button" class="btn btn-primary px-4" onclick="applyFilterAndSearch()">Search</button>
                                <button type="button" class="btn btn-outline-secondary ms-2 rounded" data-bs-toggle="modal" data-bs-target="#filterModal">
                                    <i class="fa-solid fa-filter"></i> Filter
                                </button>
                            </div>
                        </form>
                    </div>
                </div>

                <!-- Table Data -->
                <div class="table-responsive shadow-sm rounded border">
                    <table class="table table-hover align-middle mb-0 bg-white">
                        <thead class="table-light border-bottom">
                            <tr>
                                <th class="ps-3">ID</th>
                                <th>Type Name</th>
                                <th>Image</th>
                                <th>Capacity</th>
                                <th>Price/Night</th>
                                <th>Description</th>
                                <th class="text-center pe-3">Action</th>
                            </tr>
                        </thead>
                        <tbody id="roomTypeTableBody">
                            <c:forEach items="${roomTypeList}" var="rt">
                                <tr class="room-row" data-search="<c:out value='${rt.roomTypeID} ${rt.typeName}${rt.description}'/>">
                                    <td class="fw-bold text-dark ps-3 room-id"><c:out value="${rt.roomTypeID}" /></td>
                                    <td class="fw-bold text-primary"><c:out value="${rt.typeName}" /></td>
                                    <td>
                                        <c:choose>
                                            <c:when test="${not empty rt.roomTypeImage}">
                                                <img class="room-thumbnail shadow-sm" src="${pageContext.request.contextPath}/assets/images/room-types/${rt.roomTypeImage}" alt="${rt.typeName}" style="width: 80px; height: 50px; object-fit: cover; border-radius: 4px;">
                                            </c:when>
                                            <c:otherwise>
                                                <div class="bg-secondary bg-opacity-10 rounded d-inline-flex align-items-center justify-content-center shadow-sm" style="width: 80px; height: 50px;">
                                                    <i class="fa-solid fa-image text-secondary"></i>
                                                </div>
                                            </c:otherwise>
                                        </c:choose>
                                    </td>
                                    <td>
                                        <span class="badge bg-info text-dark shadow-sm px-2 py-1"><i class="fa-solid fa-user me-1"></i> ${rt.capacity} Guests</span>
                                    </td>
                                    <td class="text-danger fw-bold room-price">
                                        <fmt:formatNumber value="${rt.price}" type="number" maxFractionDigits="0"/> VND
                                    </td>
                                    <td class="text-muted small" style="max-width: 250px;">
                                        <div class="room-desc"><c:out value="${rt.description}" /></div>
                                    </td>
                                    <td class="text-center pe-3">
                                        <!-- Edit Modal Trigger -->
                                        <button type="button" class="btn btn-outline-warning btn-sm me-1" data-bs-toggle="modal" data-bs-target="#editModal${rt.roomTypeID}" title="Edit">
                                            <i class="fa-solid fa-pen"></i>
                                        </button>
                                        <!-- Delete Modal Trigger -->
                                        <button type="button" class="btn btn-outline-danger btn-sm" data-bs-toggle="modal" data-bs-target="#deleteModal${rt.roomTypeID}" title="Delete">
                                            <i class="fa-solid fa-trash"></i>
                                        </button>
                                    </td>
                                </tr>

                                <!-- ================== MODAL: EDIT ROOM TYPE ================== -->
                            <div class="modal fade" id="editModal${rt.roomTypeID}" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog modal-dialog-centered modal-lg">
                                    <div class="modal-content border-0 shadow-lg">
                                        <div class="modal-header bg-warning text-dark border-0">
                                            <h5 class="modal-title fw-bold"><i class="fa-solid fa-pen-to-square me-2"></i>Edit Room Type</h5>
                                            <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <!-- Form Gửi Dữ liệu lên Servlet (action=update) -->
                                        <form action="${pageContext.request.contextPath}/room-types" method="post" enctype="multipart/form-data">
                                            <div class="modal-body p-4 bg-white">
                                                <input type="hidden" name="action" value="update">
                                                <!-- ID được ẩn đi để gửi lên server nhưng không cho phép người dùng sửa giá trị này trong input text -->
                                                <input type="hidden" name="id" value="${rt.roomTypeID}">

                                                <div class="row g-4">
                                                    <!-- ID (Read-only Display) -->
                                                    <div class="col-md-6">
                                                        <label class="form-label fw-bold text-secondary small text-uppercase">Room Type ID</label>
                                                        <input type="text" class="form-control bg-light text-muted fw-bold" value="${rt.roomTypeID}" disabled readonly>
                                                    </div>

                                                    <!-- Type Name -->
                                                    <div class="col-md-6">
                                                        <label for="typeName${rt.roomTypeID}" class="form-label fw-bold text-secondary small text-uppercase">Type Name <span class="text-danger">*</span></label>
                                                        <input type="text" class="form-control border-secondary-subtle" id="typeName${rt.roomTypeID}" name="typeName" value="<c:out value='${rt.typeName}'/>" required>
                                                    </div>

                                                    <!-- Capacity -->
                                                    <div class="col-md-6">
                                                        <label for="capacity${rt.roomTypeID}" class="form-label fw-bold text-secondary small text-uppercase">Capacity (Guests) <span class="text-danger">*</span></label>
                                                        <input type="number" class="form-control border-secondary-subtle" id="capacity${rt.roomTypeID}" name="capacity" value="${rt.capacity}" min="1" required>
                                                    </div>

                                                    <!-- Price -->
                                                    <div class="col-md-6">
                                                        <label for="price${rt.roomTypeID}" class="form-label fw-bold text-secondary small text-uppercase">Price per Night (VND) <span class="text-danger">*</span></label>
                                                        <input type="number" step="any" class="form-control border-secondary-subtle text-danger fw-bold" id="price${rt.roomTypeID}" name="price" value="${rt.price}" min="0" required>
                                                    </div>

                                                    <!-- Description -->
                                                    <div class="col-12">
                                                        <label for="desc${rt.roomTypeID}" class="form-label fw-bold text-secondary small text-uppercase">Description</label>
                                                        <textarea class="form-control border-secondary-subtle" id="desc${rt.roomTypeID}" name="description" rows="3"><c:out value='${rt.description}'/></textarea>
                                                    </div>

                                                    <!-- Image Upload -->
                                                    <div class="col-12">
                                                        <label class="form-label fw-bold text-secondary small text-uppercase">Update Image</label>
                                                        <input type="file" class="form-control border-secondary-subtle" name="roomTypeImage" accept="image/png, image/jpeg, image/webp">
                                                        <div class="form-text text-muted mb-2">Leave blank if you want to keep the current image.</div>
                                                        <c:if test="${not empty rt.roomTypeImage}">
                                                            <div class="d-flex align-items-center gap-3 p-2 bg-light rounded border">
                                                                <img src="${pageContext.request.contextPath}/assets/images/room-types/${rt.roomTypeImage}" class="img-thumbnail shadow-sm" style="width: 100px; height: 65px; object-fit: cover;">
                                                                <span class="text-muted small">Current active image</span>
                                                            </div>
                                                        </c:if>
                                                    </div>
                                                </div>
                                            </div>
                                            <div class="modal-footer bg-light border-0">
                                                <button type="button" class="btn btn-outline-secondary px-4" data-bs-dismiss="modal">Cancel</button>
                                                <button type="submit" class="btn btn-warning px-4 fw-bold shadow-sm"><i class="fa-solid fa-check me-2"></i>Save Changes</button>
                                            </div>
                                        </form>
                                    </div>
                                </div>
                            </div>

                            <!-- ================== MODAL: DELETE ROOM TYPE ================== -->
                            <div class="modal fade" id="deleteModal${rt.roomTypeID}" tabindex="-1" aria-hidden="true">
                                <div class="modal-dialog modal-dialog-centered">
                                    <div class="modal-content border-0 shadow-lg">
                                        <div class="modal-header bg-danger text-white border-0">
                                            <h5 class="modal-title fw-bold"><i class="fa-solid fa-triangle-exclamation me-2"></i>Confirm Delete</h5>
                                            <button type="button" class="btn-close btn-close-white" data-bs-dismiss="modal" aria-label="Close"></button>
                                        </div>
                                        <div class="modal-body text-center py-5">
                                            <h5 class="mb-2">Are you sure to delete this room type?</h5>
                                            <h3 class="text-primary fw-bold mb-4"><c:out value="${rt.typeName}" /></h3>

                                            <c:choose>
                                                <c:when test="${not empty rt.roomTypeImage}">
                                                    <img src="${pageContext.request.contextPath}/assets/images/room-types/${rt.roomTypeImage}" class="img-thumbnail shadow-sm mb-3" style="width: 160px; height: 100px; object-fit: cover;">
                                                </c:when>
                                                <c:otherwise>
                                                    <div class="bg-secondary bg-opacity-10 rounded d-inline-flex align-items-center justify-content-center img-thumbnail mb-3 shadow-sm" style="width: 160px; height: 100px;">
                                                        <i class="fa-solid fa-image fa-3x text-secondary opacity-50"></i>
                                                    </div>
                                                </c:otherwise>
                                            </c:choose>
                                            <p class="text-danger small fw-bold px-3"><i class="fa-solid fa-circle-info me-1"></i>Warning: Deletion will fail if there are active rooms linked to this type!</p>
                                        </div>
                                        <div class="modal-footer justify-content-center bg-light border-0">
                                            <button type="button" class="btn btn-outline-secondary px-4 fw-bold" data-bs-dismiss="modal">Cancel</button>
                                            <a href="${pageContext.request.contextPath}/room-types?action=delete&id=${rt.roomTypeID}" class="btn btn-danger px-5 fw-bold shadow-sm">Confirm Delete</a>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </c:forEach>

                        <c:if test="${empty roomTypeList}">
                            <tr id="noDataRow">
                                <td colspan="7" class="text-center text-muted py-5">
                                    <i class="fa-solid fa-folder-open fa-3x mb-3 text-secondary opacity-50"></i>
                                    <h5>No room types found.</h5>
                                </td>
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
            <div class="modal-content border-0 shadow">
                <div class="modal-header bg-light">
                    <h5 class="modal-title fw-bold text-dark" id="filterModalLabel"><i class="fa-solid fa-sliders me-2"></i>Filter & Sort</h5>
                    <button type="button" class="btn-close" data-bs-dismiss="modal" aria-label="Close"></button>
                </div>
                <div class="modal-body p-4">
                    <div class="mb-4">
                        <label class="fw-bold mb-3 text-primary">Sort/Filter By:</label>
                        <div class="d-flex gap-4">
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="filterCategory" value="id" id="catId" onchange="updateSubFilter()" checked>
                                <label class="form-check-label" for="catId">Type ID</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="filterCategory" value="price" id="catPrice" onchange="updateSubFilter()">
                                <label class="form-check-label" for="catPrice">Price</label>
                            </div>
                            <div class="form-check">
                                <input class="form-check-input" type="radio" name="filterCategory" value="capacity" id="catCapacity" onchange="updateSubFilter()">
                                <label class="form-check-label" for="catCapacity">Capacity (Guests)</label>
                            </div>
                        </div>
                    </div>
                    <hr class="text-muted">
                    <div>
                        <label class="fw-bold mb-3 text-primary">Options:</label>
                        <div id="subFilterContainer" class="ps-2"></div>
                    </div>
                </div>
                <div class="modal-footer bg-light">
                    <button type="button" class="btn btn-outline-secondary px-4" onclick="clearFilter()">Clear</button>
                    <button type="button" class="btn btn-primary px-4" onclick="applyFilterAndSearch(true)">Apply Filter</button>
                </div>
            </div>
        </div>
    </div>

    <script>
        document.addEventListener("DOMContentLoaded", function () {
            updateSubFilter();
        });

        function updateSubFilter() {
            let category = document.querySelector('input[name="filterCategory"]:checked').value;
            let container = document.getElementById("subFilterContainer");
            container.innerHTML = "";

            if (category === "id" || category === "price") {
                container.innerHTML = `
                <div class="d-flex gap-4">
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="subFilter" value="asc" id="subAsc" checked>
                        <label class="form-check-label" for="subAsc">Ascending (Low to High)</label>
                    </div>
                    <div class="form-check">
                        <input class="form-check-input" type="radio" name="subFilter" value="desc" id="subDesc">
                        <label class="form-check-label" for="subDesc">Descending (High to Low)</label>
                    </div>
                </div>
                `;
            } else if (category === "capacity") {
                let capacities = new Set();
                document.querySelectorAll("#roomTypeTableBody tr.room-row").forEach(item => {
                    let capText = item.cells[3].textContent.trim();
                    let capNum = capText.replace(/[^0-9]/g, "");
                    if (capNum)
                        capacities.add(capNum);
                });

                let html = '<div class="row g-3">';
                let count = 0;

                Array.from(capacities).sort((a, b) => a - b).forEach(cap => {
                    let isChecked = (count === 0) ? 'checked' : '';
                    html += `
                    <div class="col-6">
                        <div class="form-check">
                            <input class="form-check-input" type="radio" name="subFilter" value="\${cap}" id="cap_\${count}" \${isChecked}>
                            <label class="form-check-label" for="cap_\${count}">\${cap} Guests</label>
                        </div>
                    </div>
                    `;
                    count++;
                });

                if (count === 0) {
                    html += '<div class="text-muted fst-italic">No capacity data available</div>';
                }

                html += '</div>';
                container.innerHTML = html;
            }
        }

        function applyFilterAndSearch(fromModal = false) {
            let keyword = document.getElementById("searchInput").value.toLowerCase().trim();
            let wrapper = document.getElementById("roomTypeTableBody");
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
                let capacityText = item.cells[3].textContent.trim().replace(/[^0-9]/g, "");

                let matchSearch = searchData.includes(keyword);
                let matchFilter = true;

                if (categoryRadio && subFilterRadio) {
                    let category = categoryRadio.value;
                    let subVal = subFilterRadio.value;

                    if (category === "capacity") {
                        if (capacityText !== subVal)
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

            if (categoryRadio && subFilterRadio) {
                let category = categoryRadio.value;
                let order = subFilterRadio.value;

                if (category === "id" || category === "price") {
                    visibleItems.sort((a, b) => {
                        if (category === "price") {
                            let valA = a.querySelector('.room-price').textContent.trim();
                            let valB = b.querySelector('.room-price').textContent.trim();

                            let numA = parseInt(valA.replace(/[^0-9]/g, "")) || 0;
                            let numB = parseInt(valB.replace(/[^0-9]/g, "")) || 0;

                            return order === "asc" ? (numA - numB) : (numB - numA);
                        } else if (category === "id") {
                            let valA = a.querySelector('.room-id').textContent.trim();
                            let valB = b.querySelector('.room-id').textContent.trim();
                            if (order === "asc")
                                return valA.localeCompare(valB);
                            return valB.localeCompare(valA);
                        }
                    });

                    visibleItems.forEach(item => wrapper.appendChild(item));
                }
            }

            let noDataRow = document.getElementById("noDataRow");
            if (noDataRow)
                noDataRow.style.display = visibleItems.length === 0 ? "" : "none";
        }

        function clearFilter() {
            document.getElementById("catId").checked = true;
            updateSubFilter();
            document.getElementById("searchInput").value = "";
            applyFilterAndSearch();
        }
    </script>
</layout:layout>