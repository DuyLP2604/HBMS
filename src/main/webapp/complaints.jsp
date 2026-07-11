<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Danh sách khiếu nại</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <jsp:include page="components/navbar.jsp" />
        <div class="container my-5">
            <div class="card shadow-sm">
                <div class="card-header bg-danger text-white d-flex justify-content-between align-items-center py-3">
                    <h2 class="h4 mb-0">Danh sách khiếu nại</h2>
                </div>
                <div class="card-body">
                    <div class="table-responsive">
                        <table class="table table-hover align-middle border">
                            <thead class="table-light">
                                <tr>
                                    <th>Mã</th>
                                    <th>Tiêu đề</th>
                                    <th>Khách hàng</th>
                                    <th>Ngày tạo</th>
                                    <th>Trạng thái</th>
                                    <th class="text-center">Hành động</th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach var="cp" items="${complaints}">
                                    <tr>
                                        <td class="fw-bold text-secondary">${cp.id}</td>
                                        <td>${cp.title}</td>
                                        <td>${empty cp.customerName ? "Ẩn danh" : cp.customerName}</td>
                                        <td><fmt:formatDate value="${cp.createdAt}" pattern="dd/MM/yyyy HH:mm" /></td>
                                        <td>
                                            <c:choose>
                                                <c:when test="${cp.status eq 'Đã xử lý'}">
                                                    <span class="badge bg-success">${cp.status}</span>
                                                </c:when>
                                                <c:when test="${cp.status eq 'Đang xử lý'}">
                                                    <span class="badge bg-warning text-dark">${cp.status}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge bg-secondary">${cp.status}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="text-center">
                                            <a href="complaint?action=viewDetail&id=${cp.id}" class="btn btn-outline-info btn-sm">Chi tiết</a>
                                        </td>
                                    </tr>
                                </c:forEach>
                                <c:if test="${empty complaints}">
                                    <tr>
                                        <td colspan="6" class="text-center text-muted py-4">Chưa có khiếu nại nào.</td>
                                    </tr>
                                </c:if>
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
        </div>
    </body>
</html>
