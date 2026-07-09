<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri = "http://java.sun.com/jsp/jstl/core" prefix = "c"%>
<!DOCTYPE html>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Hotel Management - Login</title>  
        <link rel="stylesheet" href="fontawesome/css/all.min.css">
        <style>
            *{
                margin:0;
                padding:0;
                box-sizing:border-box;
                font-family: "Segoe UI", sans-serif;
            }

            body{
                height:100vh;
                display:flex;
                justify-content:center;
                align-items:center;
                background:
                    linear-gradient(rgba(0,0,0,0.45),
                    rgba(0,0,0,0.45)),
                    url("assets/images/hotel/hotel.jpg");
                background-size:cover;
                background-position:center;
            }

            .login-container{
                width:400px;
                padding:40px;

                background: rgba(255,255,255,0.15);
                backdrop-filter: blur(12px);
                -webkit-backdrop-filter: blur(12px);

                border:1px solid rgba(255,255,255,0.2);
                border-radius:15px;

                box-shadow:0 8px 32px rgba(0,0,0,0.2);
            }

            .logo{
                text-align:center;
                font-size:48px;
                color:#c8a96b;
                margin-bottom:15px;
            }

            h2{
                text-align:center;
                color:white;
                margin-bottom:30px;
            }

            .form-group{
                margin-bottom:20px;
            }

            label{
                display:block;
                margin-bottom:8px;
                color:white;
                font-weight:600;
            }

            input{
                background:rgba(255,255,255,0.2);
                border:1px solid rgba(255,255,255,0.3);
                color:white;
                width:100%;
                padding:12px;
                border-radius:8px;
                font-size:15px;
                transition:0.3s;
            }

            input:focus{
                outline:none;
                border-color:#c8a96b;
                box-shadow:0 0 8px rgba(200,169,107,0.4);
            }

            .login-btn{
                width:100%;
                padding:12px;
                border:none;
                border-radius:8px;
                background:#c8a96b;
                color:white;
                font-size:16px;
                font-weight:bold;
                cursor:pointer;
                transition:0.3s;
            }

            .login-btn:hover{
                background:#b08f4f;
            }

            .subtitle{
                text-align:center;
                color:rgba(255,255,255,0.8);
                margin-bottom:25px;
                font-size:14px;
            }

            .password-container{
                position: relative;
            }

            .password-container input{
                width: 100%;
                padding: 12px;
                padding-right: 45px;
            }

            .toggle-password{
                position: absolute;
                right: 15px;
                top: 50%;
                transform: translateY(-50%);
                cursor: pointer;
                color: white;
            }

            .toggle-password:hover{
                color: #c8a96b;
            }

            .error-message{
                margin-bottom:15px;
                padding:12px;

                background:rgba(255,0,0,0.15);
                border:1px solid rgba(255,255,255,0.15);
                border-left:4px solid #ff6b6b;

                border-radius:8px;

                color:#ffffff;
            }

            .error-message i{
                color:#ff6b6b;
                margin-right:6px;
            }
        </style>
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

                <c:if test="${not empty loginError}">
                    <div class="error-message">
                        <i class="fa-solid fa-circle-exclamation"></i>
                        ${loginError}
                    </div>
                </c:if>
                <button type="submit" class="login-btn">
                    Sign In
                </button>

            </form>
        </div>

        <script src ="js/login.js"></script>
    </body>
</html>
