<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Hồ sơ khách hàng</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="card shadow-sm">
                <div class="card-header bg-primary text-white d-flex justify-content-between align-items-center py-3">
                    <h2 class="h4 mb-0">Hồ sơ khách hàng</h2>
                    <a href="customer?action=add" class="btn btn-success btn-sm">+ Thêm khách hàng</a>
                </div>
                <div class="card-body">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <form action="customer" method="get" class="d-flex gap-2">
                                <input type="hidden" name="action" value="list">
                                <div class="input-group">
                                    <span class="input-group-text bg-white">MS</span>
                                    <input type="text" name="searchId" class="form-control" value="${param.searchId}" placeholder="Nhập ID cần tìm...">
                                    <button type="submit" class="btn btn-primary">Tìm kiếm</button>
                                    <c:if test="${not empty param.searchId}">
                                        <a href="customer?action=list" class="btn btn-secondary">Hủy lọc</a>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle border">
                            <thead class="table-light">
                                <tr>
                                    <th>Mã KH</th>
                                    <th>Họ tên</th>
                                    <th>Điện thoại</th>
                                    <th>Quốc tịch</th>
                                    <th class="text-center">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="c" items="${customers}">
                                    <c:if test="${empty param.searchId or c.id.trim().equalsIgnoreCase(param.searchId.trim())}">
                                        <tr>
                                            <td class="fw-bold text-secondary">${c.id}</td>
                                            <td>${c.name}</td>
                                            <td>${c.phone}</td>
                                            <td>${c.nation}</td>
                                            <td class="text-center">
                                                <a href="customer?action=viewDetail&id=${c.id}" class="btn btn-outline-info btn-sm me-1">Chi tiết</a>
                                                <a href="customer?action=update&id=${c.id}" class="btn btn-outline-warning btn-sm">Sửa</a>
                                            </td>
                                        </tr>
                                    </c:if>
                                </c:forEach>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>