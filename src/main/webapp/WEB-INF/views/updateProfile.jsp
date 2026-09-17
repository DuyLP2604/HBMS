<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Update Profile"
    pageCss="updateProfile.css"
    >

    <div class="container">

        <div class="profile-card">

            <h1>
                Update Profile
            </h1>


            <form
                action="${pageContext.request.contextPath}/profile?action=update"
                method="post"
                >

                <table>

                    <tr>

                        <td>
                            Full Name
                        </td>

                        <td>

                            <input
                                type="text"
                                name="fullname"
                                value="<c:out value='${customerUpdate.fullName}' />"
                                required
                                >

                        </td>

                    </tr>


                    <tr>

                        <td>
                            Phone
                        </td>

                        <td>

                            <input
                                type="text"
                                name="phone"
                                value="<c:out value='${customerUpdate.phone}' />"
                                >

                        </td>

                    </tr>


                    <tr>

                        <td>
                            Email
                        </td>

                        <td>

                            <input
                                type="email"
                                name="email"
                                value="<c:out value='${customerUpdate.email}' />"
                                >

                        </td>

                    </tr>


                    <tr>

                        <td>
                            Address
                        </td>

                        <td>

                            <input
                                type="text"
                                name="address"
                                value="<c:out value='${customerUpdate.address}' />"
                                >

                        </td>

                    </tr>


                    <tr>

                        <td>
                            Identity Number
                        </td>

                        <td>

                            <input
                                type="text"
                                name="cccd"
                                value="<c:out value='${customerUpdate.cccd}' />"
                                >

                        </td>

                    </tr>


                    <tr>

                        <td>
                            Passport Number
                        </td>

                        <td>

                            <input
                                type="text"
                                name="passportNumber"
                                value="<c:out value='${customerUpdate.passportNumber}' />"
                                >

                        </td>

                    </tr>


                    <tr>

                        <td>
                            Nationality
                        </td>

                        <td>

                            <select
                                name="nationalityId"
                                required
                                >

                                <c:forEach
                                    items="${nationalityUpdate}"
                                    var="n"
                                    >

                                    <option
                                        value="${n.nationalityID}"
                                        ${customerUpdate.nationalityID.nationalityID eq n.nationalityID ? 'selected' : ''}
                                        >
                                        <c:out value="${n.nationalityName}" />
                                    </option>

                                </c:forEach>

                            </select>

                        </td>

                    </tr>


                    <tr>

                        <td colspan="2">

                            <button type="submit">
                                Update
                            </button>

                            <a
                                href="${pageContext.request.contextPath}/profile?action=view"
                                class="btn-cancel"
                                >
                                Cancel
                            </a>

                        </td>

                    </tr>

                </table>

            </form>

        </div>

    </div>

</layout:layout>

