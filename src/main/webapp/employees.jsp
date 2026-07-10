<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Hồ sơ nhân viên</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <div class="container my-5">
            <div class="card shadow-sm">
                <div class="card-header text-white d-flex justify-content-between align-items-center py-3" style="background-color: #0d9488;">
                    <h2 class="h4 mb-0">Hồ sơ nhân viên</h2>
                    <a href="employee?action=add" class="btn btn-warning btn-sm fw-semibold">+ Thêm nhân viên</a>
                </div>
                <div class="card-body">
                    <div class="row mb-4">
                        <div class="col-md-6">
                            <form action="employee" method="get" class="d-flex gap-2">
                                <input type="hidden" name="action" value="list">
                                <div class="input-group">
                                    <span class="input-group-text bg-white">MS</span>
                                    <input type="text" name="searchId" class="form-control" value="${param.searchId}" placeholder="Nhập mã nhân viên...">
                                    <button type="submit" class="btn text-white" style="background-color: #0d9488;">Tìm kiếm</button>
                                    <c:if test="${not empty param.searchId}">
                                        <a href="employee?action=list" class="btn btn-secondary">Hủy lọc</a>
                                    </c:if>
                                </div>
                            </form>
                        </div>
                    </div>

                    <div class="table-responsive">
                        <table class="table table-hover align-middle border">
                            <thead class="table-light">
                                <tr>
                                    <th>Mã NV</th>
                                    <th>Họ tên</th>
                                    <th>Chức vụ</th>
                                    <th>Ca làm việc</th>
                                    <th class="text-center">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="e" items="${employees}">
                                    <c:if test="${empty param.searchId or e.id.trim().equalsIgnoreCase(param.searchId.trim())}">
                                        <tr>
                                            <td class="fw-bold text-secondary">${e.id}</td>
                                            <td>${e.name}</td>
                                            <td><span class="badge bg-secondary-subtle text-secondary-emphasis">${e.position}</span></td>
                                            <td><span class="badge bg-info-subtle text-info-emphasis px-2.5 py-1.5">${e.shift}</span></td>
                                            <td class="text-center">
                                                <a href="employee?action=viewDetail&id=${e.id}" class="btn btn-outline-info btn-sm me-1">Chi tiết</a>
                                                <a href="employee?action=update&id=${e.id}" class="btn btn-outline-warning btn-sm">Sửa</a>
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