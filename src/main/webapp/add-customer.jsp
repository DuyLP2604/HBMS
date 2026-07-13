<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Thêm khách hàng mới</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <jsp:include page="components/navbar.jsp" />
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
                                    <div class="col-12">
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
                                        <label class="form-label fw-semibold">Quốc tịch *</label>
                                        <select name="nation" class="form-select" required>
                                            <option value="" disabled selected>Chọn quốc tịch</option>
                                            <c:forEach items="${listNat}" var="n">
                                                <option value="${n.id}">
                                                    ${n.name}
                                                </option>
                                            </c:forEach>
                                        </select>
                                    </div>
                                    <!-- Create account for customer section -->
                                    <div class="col-12">
                                        <div class="form-check">
                                            <input class="form-check-input"
                                                   type="checkbox"
                                                   id="createAccount"
                                                   name="createAccount">

                                            <label class="form-check-label" for="createAccount">
                                                Create account for user
                                            </label>
                                        </div>
                                    </div>
                                    <div id="accountFields" style="display:none;">
                                        <div class="row g-3 mt-2">
                                            <div class="col-md-6">
                                                <label class="form-label fw-semibold">Username</label>
                                                <input type="text"
                                                       name="username"
                                                       class="form-control">
                                            </div>

                                            <div class="col-md-6">
                                                <label class="form-label fw-semibold">Password</label>
                                                <input type="password"
                                                       name="password"
                                                       class="form-control">
                                            </div>
                                        </div>
                                    </div>
                                </div>
                                <jsp:include page = "components/errorMessage.jsp" />
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
        <script>
            document.getElementById("createAccount")
                    .addEventListener("change", function () {

                        const accountFields =
                                document.getElementById("accountFields");

                        accountFields.style.display =
                                this.checked ? "block" : "none";
                    });
        </script>
    </body>
</html>