<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Thêm nhân viên mới</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card shadow-sm">
                        <div class="card-header text-white py-3" style="background-color: #0d9488;">
                            <h2 class="h4 mb-0">Thêm nhân viên mới</h2>
                        </div>
                        <div class="card-body p-4">
                            <form action="employee?action=add" method="post">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Mã nhân viên (ID) *</label>
                                        <input type="text" name="id" class="form-control" placeholder="VD: NV01" required>
                                        end
                                        <div class="col-md-6">
                                            <label class="form-label fw-semibold">Họ tên đầy đủ *</label>
                                            <input type="text" name="name" class="form-control" placeholder="Nhập tên nhân viên..." required>
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-semibold">Chức vụ / Vị trí</label>
                                            <input type="text" name="position" class="form-control" placeholder="VD: Receptionist, Manager...">
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-semibold">Mức lương cơ bản (VND)</label>
                                            <input type="number" name="salary" class="form-control" placeholder="Nhập số tiền lương...">
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-semibold">Ca phân công làm việc</label>
                                            <input type="text" name="shift" class="form-control" placeholder="VD: Sáng, Chiều, Đêm...">
                                        </div>
                                        <div class="col-md-6">
                                            <label class="form-label fw-semibold">Số điện thoại liên hệ</label>
                                            <input type="text" name="phone" class="form-control" placeholder="Nhập SĐT...">
                                        </div>
                                        <div class="col-12">
                                            <label class="form-label fw-semibold">Địa chỉ thường trú</label>
                                            <input type="text" name="address" class="form-control" placeholder="Nhập địa chỉ nhà...">
                                        </div>
                                        <div class="col-12">
                                            <label class="form-label fw-semibold">Mã cơ sở khách sạn (HotelID) *</label>
                                            <input type="text" name="hotelId" class="form-control" placeholder="VD: H01" required>
                                        </div>
                                    </div>
                                    <div class="d-flex justify-content-end gap-2 mt-4">
                                        <a href="employee?action=list" class="btn btn-secondary">Hủy</a>
                                        <button type="submit" class="btn text-white px-4" style="background-color: #0d9488;">Thêm mới</button>
                                    </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>