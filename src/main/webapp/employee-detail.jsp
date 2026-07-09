<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
    <head>
        <title>Chi tiết hồ sơ nhân viên</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="row justify-content-center">
                <div class="col-md-6">
                    <div class="card shadow-sm border-0">
                        <div class="card-header text-white p-3 d-flex justify-content-between align-items-center" style="background-color: #0f766e;">
                            <h3 class="h5 mb-0">Hồ sơ nhân viên</h3>
                            <span class="badge bg-white text-dark p-2">Mã số: ${employee.id}</span>
                        </div>
                        <div class="card-body p-4">
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Họ tên nhân viên:</div>
                                <div class="col-sm-8 fw-bold">${employee.name}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Chức vụ / Vị trí:</div>
                                <div class="col-sm-8 fw-bold text-success">${employee.position}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Lương cơ bản:</div>
                                <div class="col-sm-8 fw-bold text-danger">${employee.salary} VND</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Ca làm việc gán:</div>
                                <div class="col-sm-8 fw-bold"><span class="badge bg-secondary">${employee.shift}</span></div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Địa chỉ liên hệ:</div>
                                <div class="col-sm-8 fw-bold">${employee.address}</div>
                            </div>
                            <div class="row mb-3 border-bottom pb-2">
                                <div class="col-sm-4 text-muted">Số điện thoại:</div>
                                <div class="col-sm-8 fw-bold">${employee.phone}</div>
                            </div>
                            <div class="row mb-3">
                                <div class="col-sm-4 text-muted">Mã cơ sở Hotel ID:</div>
                                <div class="col-sm-8 fw-bold text-primary">${employee.hotelId}</div>
                            </div>
                        </div>
                        <div class="card-footer bg-light text-end p-3">
                            <a href="employee?action=list" class="btn btn-secondary btn-sm">Quay lại danh sách</a>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>