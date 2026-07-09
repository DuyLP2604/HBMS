<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Chi tiết hồ sơ khách hàng</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="row justify-content-center">
                <div class="col-md-6">
                    <div class="card shadow-sm border-0">
                        <div class="card-header bg-dark text-white p-3 d-flex justify-content-between align-items-center">
                            <h3 class="h5 mb-0">Hồ sơ khách hàng</h3>
                            <span class="badge bg-secondary p-2">Mã số: ${customer.id}</span>
                        </div>
                        <div class="card-body p-4">
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Họ tên:</div>
                                <div class="col-sm-8 fw-bold">${customer.name}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Điện thoại:</div>
                                <div class="col-sm-8 fw-bold">${customer.phone}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Email:</div>
                                <div class="col-sm-8 fw-bold text-primary">${customer.email}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Địa chỉ:</div>
                                <div class="col-sm-8 fw-bold">${customer.address}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">CCCD:</div>
                                <div class="col-sm-8 fw-bold">${not empty customer.cccd ? customer.cccd : '...'}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Số Passport:</div>
                                <div class="col-sm-8 fw-bold">${not empty customer.passport ? customer.passport : '...'}</div>
                            </div>
                            <div class="row mb-3">
                                <div class="col-sm-4 text-muted">Quốc tịch:</div>
                                <div class="col-sm-8 fw-bold"><span class="badge bg-info text-dark">${customer.nation}</span></div>
                            </div>
                        </div>
                        <div class="card-footer bg-light text-end p-3">
                            <a href="customer?action=list" class="btn btn-secondary btn-sm">Quay lại danh sách</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>