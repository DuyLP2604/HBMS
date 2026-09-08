<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Hotel Management - Login</title> 
        <link rel ="stylesheet" href ="css/login.css">
    </head>

    <body>
        <div class="login-container">
            <div class="logo"><i class="fa-solid fa-hotel"></i></div>

            <h2>Hotel Management System</h2>
            <p class="subtitle">Welcome Back</p>

            <form action="login" method="post">

                <div class="form-group">
                    <label><i class="fa-solid fa-user"></i>
                        Username</label>
                    <input type="text" name="username" required>
                </div>

                <div class="form-group">
                    <label>Password</label>

                    <div class="password-container">
                        <input type="password" id="password" name="password" required>

                        <i class="fa-solid fa-eye toggle-password"
                           id="togglePassword"></i>
                    </div>
                </div>

                <jsp:include page="components/errorMessage.jsp" />
                <div class="register-link">
                    Don't have an account?
                    <a href="register">Register</a>
                </div>
                <button type="submit" class="login-btn">
                    Sign In
                </button>

            </form>
        </div>

        <script src ="js/togglePassword.js"></script>
    </body>
</html>
