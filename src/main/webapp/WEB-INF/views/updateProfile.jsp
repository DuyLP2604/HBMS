<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout title="Update Profile" pageCss="updateProfile.css" showNavbar="false">
    <div class="container">
        <div class="profile-card">
            <h1>Update Profile</h1>
            <form action="${pageContext.request.contextPath}/profile?action=update" method="post">
                <table>
                    <tr>
                        <td><label for="fullname">Full Name *</label></td>
                        <td>
                            <input type="text" id="fullname" name="fullname" value="${fn:escapeXml(customerUpdate.fullName)}" maxlength="100" required>
                        </td>
                    </tr>
                    <tr>
                        <td><label for="phone">Phone</label></td>
                        <td>
                            <input type="tel" id="phone" name="phone" value="${fn:escapeXml(customerUpdate.phone)}" maxlength="15">
                        </td>
                    </tr>
                    <tr>
                        <td><label for="email">Email</label></td>
                        <td>
                            <input type="email" id="email" name="email" value="${fn:escapeXml(customerUpdate.email)}" maxlength="100">
                        </td>
                    </tr>
                    <tr>
                        <td><label for="address">Address</label></td>
                        <td>
                            <input type="text" id="address" name="address" value="${fn:escapeXml(customerUpdate.address)}" maxlength="200">
                        </td>
                    </tr>
                    <tr>
                        <td><label for="nationalityId">Nationality *</label></td>
                        <td>
                            <select id="nationalityId" name="nationalityId" required>
                                <option value="" disabled ${empty customerUpdate.nationalityID ? 'selected' : ''}>-- Select Nationality --</option>
                                <c:forEach items="${nationalityUpdate}" var="n">
                                    <option value="${fn:escapeXml(n.nationalityID)}" ${customerUpdate.nationalityID.nationalityID eq n.nationalityID ? 'selected' : ''}><c:out value="${n.nationalityName}" /></option>
                                </c:forEach>
                            </select>
                        </td>
                    </tr>
                    <tr>
                        <td colspan="2">
                            <button type="submit">Update</button>
                            <a href="${pageContext.request.contextPath}/profile?action=view" class="btn-cancel">Cancel</a>
                        </td>
                    </tr>
                </table>
            </form>
        </div>
    </div>
</layout:layout>