<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout title="Employee List" pageCss="employee.css" useBootstrap="true" bodyClass="bg-light" >
    <div class="container my-5">
        <div class="card shadow-sm">
            <div class="card-header employee-header text-white d-flex justify-content-between align-items-center py-3">
                <h2 class="h4 mb-0">
                    Employee List
                </h2>

                <a href="${pageContext.request.contextPath}/employee?action=add" class="btn btn-warning btn-sm fw-semibold" >
                    + Add Employee
                </a>
            </div>
            <div class="card-body">
                <div class="row mb-4">
                    <div class="col-md-6">
                        <form action="${pageContext.request.contextPath}/employee" method="get" class="d-flex gap-2" >

                            <input type="hidden" name="action" value="list" >

                            <div class="input-group">

                                <span class="input-group-text bg-white">
                                    ID
                                </span>

                                <input type="text" name="searchId" class="form-control"
                                       value="<c:out value='${param.searchId}' />" placeholder="Enter employee ID..." >

                                <button type="submit",class="btn employee-btn text-white" >
                                    Search
                                </button>

                                <c:if test="${not empty param.searchId}">
                                    <a
                                        href="${pageContext.request.contextPath}/employee?action=list"
                                        class="btn btn-secondary"
                                        >
                                        Clear Filter
                                    </a>
                                </c:if>
                            </div>
                        </form>
                    </div>
                </div>
                <div class="table-responsive">
                    <table class="table table-hover align-middle border">
                        <thead class="table-light">
                            <tr>
                                <th>Employee ID</th>
                                <th>Full Name</th>
                                <th>Position</th>
                                <th>Work Shift</th>
                                <th class="text-center">
                                    Action
                                </th>
                            </tr>
                        </thead>
                        <tbody>
                            <c:forEach var="e"items="${employees}">

                                <c:if test="${empty param.searchId
                                              or e.employeeID.trim().equalsIgnoreCase(param.searchId.trim())}">
                                      <tr>

                                          <td class="fw-bold text-secondary">
                                              <c:out value="${e.employeeID}" />
                                          </td>

                                          <td>
                                              <c:out value="${e.fullName}" />
                                          </td>

                                          <td>
                                              <span class="badge bg-secondary-subtle text-secondary-emphasis">
                                                  <c:out value="${e.position}" />
                                              </span>
                                          </td>

                                          <td>
                                              <span class="badge bg-info-subtle text-info-emphasis px-2 py-1">
                                                  <c:out value="${e.shift}" />
                                              </span>
                                          </td>
                                          <td class="text-center">
                                              <a href="${pageContext.request.contextPath}/employee?action=viewDetail&id=${e.employeeID}"
                                                 class="btn btn-outline-info btn-sm me-1">
                                                  View Details
                                              </a>
                                              <a href="${pageContext.request.contextPath}/employee?action=update&id=${e.employeeID}" class="btn btn-outline-warning btn-sm" >
                                                  Edit
                                              </a>
                                          </td>
                                      </tr>
                                </c:if>
                            </c:forEach>


                            <c:if test="${empty employees}">
                                <tr>
                                    <td colspan="5" class="text-center text-muted py-4">
                                        No employees found.
                                    </td>
                                </tr>
                            </c:if>

                        </tbody>
                    </table>
                </div>
            </div>
        </div>
    </div>
</layout:layout>
