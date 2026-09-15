<%-- 
    Document   : invoice
    Created on : Jul 13, 2026, 7:26:50 PM
    Author     : Admin
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
        <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/css/bootstrap.min.css"
              rel="stylesheet">
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
    </head>
    <body>
        <div class ="container">
            <div class="invoice-container shadow-sm p-4 p-md-5 mt-4 mb-4 border rounded">
                <a href="invoice?action=checkout" class="btn btn-secondary shadow-sm">
                    <i class="fa-solid fa-arrow-left me-1"></i> Back to Checkout
                </a>
                <h1 class="text-center text-uppercase fs-4 mb-4 fw-bold">Service Invoice</h1>

                <div class="row border-bottom pb-2 mb-3 fs-6">
                    <div class="col-sm-4">Invoice ID: <span class="fw-bold">${selectedRoom.bookingId}</span></div>
                    <div class="col-sm-4 text-sm-center">Date printed: <span class="fw-bold" id="printDate"></span></div>
                    <div class="col-sm-4 text-sm-end">Booking ID: <span class="fw-bold">${selectedRoom.bookingId}</span></div>
                </div>

                <div class="row mb-4 fs-6">
                    <div class="col-md-6">
                        <div class="row mb-2">
                            <div class="col-5 text-secondary">Customer</div>
                            <div class="col-7 fw-bold">${selectedRoom.customerName}</div>
                        </div>
                        <div class="row mb-2">
                            <div class="col-5 text-secondary">Checkin</div>
                            <div class="col-7 fw-bold">${selectedRoom.checkInDate}</div>
                        </div>
                    </div>
                    <div class="col-md-6">
                        <div class="row mb-2">
                            <div class="col-5 text-secondary">Room</div>
                            <div class="col-7 fw-bold">${selectedRoom.roomNumber}</div>
                        </div>
                    </div>
                </div>

                <div class="table-responsive">
                    <table class="table table-bordered align-middle text-center mb-0">
                        <thead class="table-light">
                            <tr>
                                <th scope="col">#</th>
                                <th scope="col" class="text-start">Context</th>
                                <th scope="col">Amount</th>
                                <th scope="col">Price</th>
                                <th scope="col" class="text-end">Room price (${selectedRoom.totalDays} days):</th>
                            </tr>
                        </thead>
                        <tbody>
                            <tr>
                                <td>1</td>

                                <td class="text-start">
                                    Room charge
                                    (Code:
                                    ${selectedRoom.roomNumber})
                                </td>

                                <td>
                                    ${selectedRoom.totalDays}
                                </td>

                                <td class="format-money"
                                    data-val="${selectedRoom.price}">

                                    ${selectedRoom.price}
                                </td>

                                <td class="text-end fw-bold
                                    format-money"
                                    data-val="${selectedRoom.roomTotal}">

                                    ${selectedRoom.roomTotal}
                                </td>

                            </tr>

                            <c:forEach
                                items="${bookingServices}"
                                var="srv"
                                varStatus="loop">

                                <tr>

                                    <td>
                                        ${loop.index + 2}
                                    </td>

                                    <td class="text-start">
                                        ${srv.serviceName}
                                    </td>

                                    <td>1</td>

                                    <td class="format-money"
                                        data-val="${srv.unitPrice}">

                                        ${srv.unitPrice}
                                    </td>

                                    <td class="text-end fw-bold
                                        format-money"
                                        data-val="${srv.unitPrice}">

                                        ${srv.unitPrice}
                                    </td>

                                </tr>

                            </c:forEach>

                            <tr>
                                <td colspan="4"
                                    class="text-end fw-bold pe-3">

                                    VAT (8%)
                                </td>

                                <td class="text-end fw-bold
                                    format-money"
                                    data-val="${vat}">

                                    ${vat}
                                </td>
                            </tr>

                            <tr>
                                <td colspan="4" class="text-end fw-bold pe-3">Total Payment:</td>

                                <td class="text-end fw-bold
                                    fs-5 text-danger
                                    format-money"
                                    data-val="${grandTotal}">

                                    ${grandTotal}
                                </td>

                            </tr>
                        </tbody>
                    </table>
                </div>

                <div class="row text-center mt-5 pt-3">
                    <div class="col-6">
                        <p class="fw-bold mb-0">Receptionist</p>
                        <div class="signature-space d-flex align-items-center justify-content-center">
                            <span class="text-danger fw-bold text-uppercase" 
                                  style="border: 3px dashed #dc3545; padding: 8px 15px; border-radius: 8px; transform: rotate(-10deg); opacity: 0.8;">
                                Payment has been made.
                            </span>
                        </div>
                    </div>
                    <div class="col-6">
                        <p class="fw-bold mb-0">Customer</p>
                        <div class="signature-space"></div>
                        <p class="fw-bold fst-italic">${selectedRoom.customerName}</p>
                    </div>
                </div>

            </div>
        </div>


        <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.2/dist/js/bootstrap.bundle.min.js"></script>
        <script>

            function formatVND(amount) {

                let num = parseFloat(amount);

                if (isNaN(num))
                    return amount;

                return num.toLocaleString('vi-VN') + " đ";
            }

            function getFormattedDate() {

                let d = new Date();

                let day =
                        ("0" + d.getDate()).slice(-2);

                let month =
                        ("0" + (d.getMonth() + 1))
                        .slice(-2);

                let year =
                        d.getFullYear();

                let hours =
                        ("0" + d.getHours())
                        .slice(-2);

                let minutes =
                        ("0" + d.getMinutes())
                        .slice(-2);

                let seconds =
                        ("0" + d.getSeconds())
                        .slice(-2);

                return day + "/"
                        + month + "/"
                        + year + " "
                        + hours + ":"
                        + minutes + ":"
                        + seconds;
            }

            document.getElementById(
                    "printDate")
                    .innerText =
                    getFormattedDate();

        </script>
    </body>
</html>