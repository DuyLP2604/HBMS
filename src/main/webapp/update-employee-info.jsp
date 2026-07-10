<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Chỉnh sửa thông tin nhân viên</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card shadow-sm">
                        <div class="card-header bg-warning text-dark py-3">
                            <h2 class="h4 mb-0 fw-semibold">Chỉnh sửa thông tin nhân viên (Quản lý)</h2>
                        </div>
                        <div class="card-body p-4">
                            <form action="employee?action=update" method="post">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold text-muted">Mã nhân viên (Read-Only)</label>
                                        <input type="text" name="id" class="form-control bg-light" value="${employee.id}" readonly>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Họ và tên *</label>
                                        <input type="text" name="name" class="form-control" value="${employee.name}" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Chức vụ</label>
                                        <input type="text" name="position" class="form-control" value="${employee.position}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Lương cơ bản (VND)</label>
                                        <input type="number" name="salary" class="form-control" value="${employee.salary}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Ca làm việc</label>
                                        <input type="text" name="shift" class="form-control" value="${employee.shift}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số điện thoại</label>
                                        <input type="text" name="phone" class="form-control" value="${employee.phone}">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label fw-semibold">Địa chỉ</label>
                                        <input type="text" name="address" class="form-control" value="${employee.address}">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label fw-semibold">Mã cơ sở khách sạn (HotelID) *</label>
                                        <input type="text" name="hotelId" class="form-control" value="${employee.hotelId}" required>
                                    </div>
                                </div>
                                <div class="d-flex justify-content-end gap-2 mt-4">
                                    <a href="employee?action=list" class="btn btn-secondary">Quay lại</a>
                                    <button type="submit" class="btn btn-warning px-4 fw-semibold">Cập nhật thông tin</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>