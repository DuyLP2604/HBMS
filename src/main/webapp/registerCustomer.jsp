<%-- 
    Document   : register.jsp
    Created on : Jul 9, 2026, 6:43:34 PM
    Author     : default
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c"%>
<!DOCTYPE html>
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Register</title>
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
        <link rel="stylesheet" href="css/registerCustomer.css">
    </head>
    <body>
        <form action ="register" method ="post">
            <h2>Create Account</h2>
            <div id="step1">
                <div class="step-indicator">
                    <span class="active">1</span>
                    <span>2</span>
                </div>

                <h3>Account Information</h3>

                <label>Username</label>
                <input type="text" id="username" name="username" required>

                <label>Password</label>
                <input type="password" id="password" name="password" required>

                <label>Confirm Password</label>
                <input type="password" id="confirmPassword" required>

                <button type="button" class="next-btn" onclick="nextStep()">
                    Next
                </button>
            </div>

            <div id="step2" style="display:none;">

                <div class="step-indicator">
                    <span class="done">✓</span>
                    <span class="active">2</span>
                </div>

                <h3>Personal Information</h3>

                <div class ="form-grid">
                    <label>Full Name</label>
                    <input type="text" name="fullname" required>

                    <label>Phone Number</label>
                    <input type="text" name="phone" required>

                    <label>Email</label>
                    <input type="email" name="email" required>

                    <label>Nationality</label>
                    <select name="nationalityID" required>
                        <option value="">-- Select Nationality --</option>
                        <c:forEach var="n" items="${nationalities}">
                            <option value="${n.getId()}">
                                ${n.getName()}
                            </option>
                        </c:forEach>
                    </select>
                </div>

                <div>
                    <label>Address</label>
                    <input type="text" name="address" required>
                </div>

                <label>Identity Number (Vietnamese)</label>
                <input type="text" name="cccd">

                <label>Passport Number</label>
                <input type="text" name="passportNumber">
                 
                <jsp:include page="components/errorMessage.jsp" />
                <div class="btn-group">
                    <button type="button" class="back-btn" onclick="backStep()">
                        Back
                    </button>

                    <button type="submit" class="register-btn">
                        Register
                    </button>
                </div>
            </div>
        </form>

        <script src ="js/register.js"></script>
    </body>
</html>
