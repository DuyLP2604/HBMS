<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Thêm khách hàng mới</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card shadow-sm">
                        <div class="card-header bg-success text-white py-3">
                            <h2 class="h4 mb-0">Thêm khách hàng mới</h2>
                        </div>
                        <div class="card-body p-4">
                            <form action="customer?action=add" method="post">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Mã khách hàng (ID) *</label>
                                        <input type="text" name="id" class="form-control" placeholder="VD: KH01" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Họ và tên *</label>
                                        <input type="text" name="name" class="form-control" placeholder="Nhập họ tên..." required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số điện thoại</label>
                                        <input type="text" name="phone" class="form-control" placeholder="Nhập số điện thoại...">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Email</label>
                                        <input type="email" name="email" class="form-control" placeholder="Nhập email...">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label fw-semibold">Địa chỉ</label>
                                        <input type="text" name="address" class="form-control" placeholder="Nhập địa chỉ...">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số CCCD</label>
                                        <input type="text" name="cccd" class="form-control" placeholder="Nhập số CCCD...">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số Passport</label>
                                        <input type="text" name="passport" class="form-control" placeholder="Nhập số hộ chiếu nếu có...">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label fw-semibold">Quốc tịch</label>
                                        <input type="text" name="nation" class="form-control" value="Việt Nam">
                                    </div>
                                </div>
                                <div class="d-flex justify-content-end gap-2 mt-4">
                                    <a href="customer?action=list" class="btn btn-secondary">Hủy bỏ</a>
                                    <button type="submit" class="btn btn-primary px-4">Lưu hồ sơ</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>