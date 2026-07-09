<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Chi tiết khiếu nại</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5" style="max-width: 700px;">
            <div class="card shadow-sm">
                <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center py-3">
                    <h2 class="h4 mb-0">Chi tiết khiếu nại #${complaint.id}</h2>
                    <a href="complaint?action=list" class="btn btn-light btn-sm">Quay lại</a>
                </div>
                <div class="card-body">
                    <dl class="row mb-0">
                        <dt class="col-sm-3">Tiêu đề</dt>
                        <dd class="col-sm-9">${complaint.title}</dd>

                        <dt class="col-sm-3">Khách hàng</dt>
                        <dd class="col-sm-9">${empty complaint.customerName ? "Ẩn danh" : complaint.customerName}</dd>

                        <dt class="col-sm-3">Ngày tạo</dt>
                        <dd class="col-sm-9"><fmt:formatDate value="${complaint.createdAt}" pattern="dd/MM/yyyy HH:mm" /></dd>

                        <dt class="col-sm-3">Nội dung</dt>
                        <dd class="col-sm-9" style="white-space: pre-wrap;">${complaint.content}</dd>

                        <dt class="col-sm-3">Trạng thái</dt>
                        <dd class="col-sm-9">${complaint.status}</dd>
                    </dl>

                    <hr>
                    <form action="complaint" method="post" class="d-flex gap-2 align-items-end">
                        <input type="hidden" name="action" value="updateStatus">
                        <input type="hidden" name="id" value="${complaint.id}">
                        <div class="flex-grow-1">
                            <label class="form-label">Cập nhật trạng thái</label>
                            <select name="status" class="form-select">
                                <option value="Chưa xử lý" ${complaint.status eq 'Chưa xử lý' ? 'selected' : ''}>Chưa xử lý</option>
                                <option value="Đang xử lý" ${complaint.status eq 'Đang xử lý' ? 'selected' : ''}>Đang xử lý</option>
                                <option value="Đã xử lý" ${complaint.status eq 'Đã xử lý' ? 'selected' : ''}>Đã xử lý</option>
                            </select>
                        </div>
                        <button type="submit" class="btn btn-danger">Cập nhật</button>
                    </form>
                </div>
            </div>
        </div>
    </body>
</html>
