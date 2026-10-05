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