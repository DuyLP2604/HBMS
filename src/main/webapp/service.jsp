<%-- 
    Document   : service
    Created on : Jul 19, 2026, 11:07:22 PM
    Author     : ADMIN
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Service</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css" rel="stylesheet">
        <link rel ="stylesheet" href ="css/service.css">
    </head>
    <body>
        <jsp:include page="components/navbar.jsp" />

        <div class="container mt-5">
            <div class="card shadow">
                <div class="card-header bg-primary text-white">
                    <h3 class="mb-0">Select Hotel Services</h3>
                </div>
                <div class="card-body">
                    <form action="service" method="post">
                        <input type="hidden"
                               name="bookingID"
                               value="${bookingID}">

                        <table class="table table-bordered table-hover align-middle">

                            <thead class="table-light">
                                <tr>
                                    <th width="10%" class="text-center">
                                        Select
                                    </th>
                                    <th>
                                        Service Name
                                    </th>
                                    <th width="20%">
                                        Price
                                    </th>
                                </tr>
                            </thead>
                            <tbody>
                                <c:forEach items="${list}" var="s">
                                    <tr>
                                        <td class="text-center">
                                            <input class="form-check-input" type="radio" name="selectedService" value="${s.serviceName}|${s.unitPrice}">
                                        </td>
                                        <td>
                                            ${s.serviceName}
                                        </td>
                                        <td>
                                            ${s.unitPrice}
                                        </td>
                                    </tr>
                                </c:forEach>
                            </tbody>
                        </table>
                        <div class="text-end">

                            <a href="index.jsp" class="btn btn-secondary">
                                Back
                            </a>
                            <button type="submit" class="btn btn-success" >
                                Submit
                            </button>
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
            document.addEventListener("click", function (e) {
                const dropdown = document.querySelector(".dropdown");
                if (!dropdown)
                    return;
                const menu = document.getElementById("userDropdown");
                if (!dropdown.contains(e.target)) {
                    menu.classList.remove("show");
                }
            });
        </script>
    </body>
</html>
