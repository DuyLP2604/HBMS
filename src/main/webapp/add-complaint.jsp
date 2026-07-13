<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <title>Gửi khiếu nại</title>
        <meta name="viewport" content="width=device-width, initial-scale=1">
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
    </head>
    <body class="bg-light">
        <jsp:include page="components/navbar.jsp" />
        <div class="container my-5" style="max-width: 600px;">
            <div class="card shadow-sm">
                <div class="card-header bg-danger text-white py-3">
                    <h2 class="h4 mb-0">Gửi khiếu nại</h2>
                </div>
                <div class="card-body">
                    <c:if test="${param.success == '1'}">
                        <div class="alert alert-success">Gửi khiếu nại thành công! Chúng tôi sẽ xử lý sớm nhất.</div>
                    </c:if>

                    <form action="complaint" method="post">
                        <input type="hidden" name="action" value="add">

                        <div class="mb-3">
                            <label class="form-label">Tiêu đề <span class="text-danger">*</span></label>
                            <input type="text" name="title" class="form-control" required>
                        </div>

                        <div class="mb-3">
                            <label class="form-label">Nội dung khiếu nại <span class="text-danger">*</span></label>
                            <textarea name="content" class="form-control" rows="5" required></textarea>
                        </div>

                        <div class="d-flex justify-content-end gap-2">
                            <button type="submit" class="btn btn-danger">Gửi khiếu nại</button>
                        </div>
                    </form>
                </div>
            </div>
        </div>
        <script>
            function toggleMenu() {
                document.getElementById("userDropdown")
                        .classList.toggle("show");
            }

            document.addEventListener("click", function (event) {
                const dropdown = document.querySelector(".dropdown");
                const menu = document.getElementById("userDropdown");

                if (!dropdown.contains(event.target)) {
                    menu.classList.remove("show");
                }
            });
        </script>
    </body>
</html>
