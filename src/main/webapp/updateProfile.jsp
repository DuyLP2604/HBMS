
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>

<!DOCTYPE html>
<html>
    <head>
        <meta charset="UTF-8">
        <title>Update Profile</title>
        <link rel ="stylesheet" href ="css/updateProfile.css">
    </head>
    <body>
        <div class ="container">
            <div class ="profile-card">
                <h1>Update Profile</h1>

                <form action="profile?action=update" method="post">

                    <table>
                        <tr>
                            <td>Full Name</td>
                            <td>
                                <input type="text"
                                       name="fullname"
                                       value="${customerUpdate.fullname}"
                                       required>
                            </td>
                        </tr>

                        <tr>
                            <td>Phone</td>
                            <td>
                                <input type="text"
                                       name="phone"
                                       value="${customerUpdate.phone}">
                            </td>
                        </tr>

                        <tr>
                            <td>Email</td>
                            <td>
                                <input type="email"
                                       name="email"
                                       value="${customerUpdate.email}">
                            </td>
                        </tr>

                        <tr>
                            <td>Address</td>
                            <td>
                                <input type="text"
                                       name="address"
                                       value="${customerUpdate.address}">
                            </td>
                        </tr>

                        <tr>
                            <td>CCCD</td>
                            <td>
                                <input type="text"
                                       name="cccd"
                                       value="${customerUpdate.cccd}">
                            </td>
                        </tr>

                        <tr>
                            <td>Passport Number</td>
                            <td>
                                <input type="text"
                                       name="passportNumber"
                                       value="${customerUpdate.passportNumber}">
                            </td>
                        </tr>

                        <tr>
                            <td>Nationality</td>
                            <td>
                                <select name="nationalityId" required>
                                    <c:forEach items="${nationalityUpdate}" var="n">
                                        <option value="${n.id}"
                                                <c:if test="${customerUpdate.nationality.id == n.id}">
                                                    selected
                                                </c:if>>
                                            ${n.name}
                                        </option>
                                    </c:forEach>
                                </select>
                            </td>
                        </tr>

                        <tr>
                            <td colspan="2">
                                <button type="submit">Update</button>
                                <a href="profile?action=view" class="btn-cancel">
                                    Cancel
                                </a>
                            </td>
                        </tr>
                    </table>

                </form>
            </div>
        </div>


    </body>
</html>

