<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Submit Complaint"
    pageCss="complaint.css"
    useBootstrap="true"
    bodyClass="bg-light"
    >

    <div class="container my-5 complaint-container">

        <div class="card shadow-sm">

            <div class="card-header bg-danger text-white py-3">
                <h2 class="h4 mb-0">
                    Submit Complaint
                </h2>
            </div>

            <div class="card-body">

                <form
                    action="${pageContext.request.contextPath}/complaint"
                    method="post"
                    >

                    <input
                        type="hidden"
                        name="action"
                        value="add"
                        >

                    <div class="mb-3">

                        <label class="form-label">
                            Title
                            <span class="text-danger">*</span>
                        </label>

                        <input
                            type="text"
                            name="title"
                            class="form-control"
                            required
                            >

                    </div>

                    <div class="mb-3">

                        <label class="form-label">
                            Complaint Details
                            <span class="text-danger">*</span>
                        </label>

                        <textarea
                            name="content"
                            class="form-control"
                            rows="5"
                            required
                            ></textarea>

                    </div>

                    <div class="d-flex justify-content-end gap-2">

                        <button
                            type="submit"
                            class="btn btn-danger"
                            >
                            Submit Complaint
                        </button>

                    </div>

                </form>

            </div>

        </div>

    </div>

</layout:layout>
