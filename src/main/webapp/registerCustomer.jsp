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
        <style>
            * {
                margin: 0;
                padding: 0;
                box-sizing: border-box;
                font-family: "Segoe UI", sans-serif;
            }

            body {
                background:
                    linear-gradient(
                    rgba(0,0,0,0.45),
                    rgba(0,0,0,0.45)
                    ),
                    url("assets/images/hotel/hotel.jpg");

                background-size:cover;
                background-position:center;
                min-height: 100vh;
                display: flex;
                justify-content: center;
                align-items: center;
                padding: 30px;
            }

            form {
                width: 100%;
                max-width: 720px;
                background: rgba(255,255,255,0.15);
                backdrop-filter: blur(20px);
                -webkit-backdrop-filter: blur(20px);

                border:1px solid rgba(255,255,255,0.25);
                padding: 40px;
                border-radius: 24px;
                box-shadow:
                    0 25px 60px rgba(0,0,0,0.18),
                    0 10px 25px rgba(0,0,0,0.08);
                backdrop-filter: blur(10px);
            }

            h2 {
                text-align: center;
                color: #D4B36A;
                font-size: 34px;
                margin-bottom: 8px;
            }

            h2::after {
                content: "";
                display: block;
                width: 70px;
                height: 4px;
                margin: 12px auto 0;
                border-radius: 999px;
                background: #D4B36A;
            }

            h3 {
                color:white;
                margin-top: 20px;
                margin-bottom: 15px;
                border-bottom: 2px solid #e5e7eb;
                padding-bottom: 8px;
            }

            label {
                display: block;
                margin-bottom: 6px;
                margin-top: 14px;
                font-weight: 600;
                color: white;
            }

            input,
            select {
                background:rgba(255,255,255,0.2);
                border:1px solid rgba(255,255,255,0.3);
                color:white;
                width:100%;
                padding:12px;
                border-radius:8px;
                font-size:15px;
                transition:0.3s;
            }
            
            select option{
                color: black;
            }
            
            
            input:focus,
            select:focus {
                outline:none;
                border-color:#c8a96b;
                box-shadow:0 0 8px rgba(200,169,107,0.4);
            }

            .register-btn {
                width: 100%;
                margin-top: 28px;
                padding: 14px;
                border: none;
                border-radius: 10px;
                background: #1e3a5f;
                color: white;
                font-size: 16px;
                font-weight: 600;
                cursor: pointer;
                transition: 0.3s;
            }

            .register-btn:hover {
                background: #16304f;
            }

            .login-link {
                text-align: center;
                margin-top: 18px;
                color: #666;
            }

            .login-link a {
                text-decoration: none;
                color: #1e3a5f;
                font-weight: 600;
            }

            .login-link a:hover {
                text-decoration: underline;
            }

            .step-indicator {
                display:flex;
                justify-content:center;
                align-items:center;
                gap:80px;
                position:relative;
                margin-bottom:30px;
            }

            .step-indicator::before{
                content:"";
                position:absolute;
                width:100px;
                height:3px;
                background:#d1d5db;
            }

            .step-indicator span{
                width:45px;
                height:45px;

                display:grid;
                place-items:center;

                border-radius:50%;

                background:#d1d5db;
                color:white;

                font-weight:600;
                position:relative;
                z-index:1;
            }

            .step-indicator .active{
                background:#29466B;
            }

            .step-indicator .done{
                background:#D4B36A;
            }

            .step-indicator .active{
                background:#1e3a5f;
            }

            .step-indicator .done{
                background:#16a34a;
            }

            .next-btn,
            .back-btn,
            .register-btn{
                width:100%;
                padding:14px;
                border:none;
                border-radius:10px;
                cursor:pointer;
                font-size:15px;
                font-weight:600;
                margin-top:20px;
            }

            .next-btn,
            .register-btn {
                background:#D4B36A;
                color:white;
            }

            .next-btn:hover,
            .register-btn:hover {
                background:#c5a35a;
                transform: translateY(-2px);
                box-shadow: 0 8px 20px rgba(30,58,95,.25);
            }

            .back-btn{
                background:#e5e7eb;
            }

            .btn-group{
                display:flex;
                gap:10px;
            }

            .form-grid {
                display: grid;
                grid-template-columns: 1fr 1fr;
                gap: 18px;
            }
            @media (max-width: 768px) {
                form {
                    padding: 25px;
                }
            }
        </style>
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
