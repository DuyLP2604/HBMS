<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Cập nhật thông tin khách hàng</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="row justify-content-center">
                <div class="col-md-8">
                    <div class="card shadow-sm">
                        <div class="card-header bg-warning text-dark py-3">
                            <h2 class="h4 mb-0 fw-semibold">Cập nhật thông tin khách hàng</h2>
                        </div>
                        <div class="card-body p-4">
                            <form action="customer?action=update" method="post">
                                <div class="row g-3">
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold text-muted">Mã khách hàng (Không thể sửa)</label>
                                        <input type="text" name="id" class="form-control bg-light" value="${customer.id}" readonly>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Họ và tên *</label>
                                        <input type="text" name="name" class="form-control" value="${customer.name}" required>
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số điện thoại</label>
                                        <input type="text" name="phone" class="form-control" value="${customer.phone}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Email</label>
                                        <input type="email" name="email" class="form-control" value="${customer.email}">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label fw-semibold">Địa chỉ</label>
                                        <input type="text" name="address" class="form-control" value="${customer.address}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số CCCD</label>
                                        <input type="text" name="cccd" class="form-control" value="${customer.cccd}">
                                    </div>
                                    <div class="col-md-6">
                                        <label class="form-label fw-semibold">Số Passport</label>
                                        <input type="text" name="passport" class="form-control" value="${customer.passport}">
                                    </div>
                                    <div class="col-12">
                                        <label class="form-label fw-semibold">Quốc tịch</label>
                                        <input type="text" name="nation" class="form-control" value="${customer.nation}">
                                    </div>
                                </div>
                                <div class="d-flex justify-content-end gap-2 mt-4">
                                    <a href="customer?action=list" class="btn btn-secondary">Quay lại</a>
                                    <button type="submit" class="btn btn-warning px-4">Cập nhật</button>
                                </div>
                            </form>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>