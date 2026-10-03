<%@ page contentType="text/html"
         pageEncoding="UTF-8" %>

<%@ taglib prefix="c"
           uri="http://java.sun.com/jsp/jstl/core" %>

<%@ taglib prefix="fn"
           uri="http://java.sun.com/jsp/jstl/functions" %>

<%@ taglib prefix="layout"
           tagdir="/WEB-INF/tags" %>

<layout:layout
    title="Hotel Information"
    useBootstrap="true"
    bodyClass="bg-light">

    <div class="container my-5">

        <div class="row justify-content-center">

            <div class="col-md-9">

                <div class="card shadow-sm">

                    <div class="card-header bg-warning text-dark py-3">

                        <h2 class="h4 mb-0 fw-semibold">
                            Hotel Information
                        </h2>

                    </div>

                    <div class="card-body p-4">

                        <%-- multipart is required because of the image upload --%>
                        <form
                            action="${pageContext.request.contextPath}/hotel"
                            method="post"
                            enctype="multipart/form-data">

                            <div class="row g-3">

                                <div class="col-12">

                                    <label
                                        for="hotelName"
                                        class="form-label fw-semibold text-muted">

                                        Hotel Name (cannot be changed)
                                    </label>

                                    <%-- No name attribute: the name is never sent to the server --%>
                                    <input
                                        id="hotelName"
                                        type="text"
                                        class="form-control bg-light"
                                        value="${fn:escapeXml(hotel.hotelName)}"
                                        readonly
                                    >

                                </div>

                                <div class="col-12">

                                    <label
                                        for="address"
                                        class="form-label fw-semibold">

                                        Addresses *
                                    </label>

                                    <textarea
                                        id="address"
                                        name="address"
                                        class="form-control"
                                        rows="4"
                                        required>${fn:escapeXml(addressText)}</textarea>

                                    <div class="form-text">
                                        Write one address per line.
                                        They are shown on the home page.
                                        Maximum 500 characters in total.
                                    </div>

                                </div>

                                <div class="col-12">
                                    <hr class="my-2">
                                    <h3 class="h6 fw-semibold mb-0">
                                        Images
                                    </h3>
                                    <div class="form-text">
                                        Optional. JPG, PNG or WEBP, maximum 2 MB
                                        for each image. Leave a field empty to keep
                                        the current image.
                                    </div>
                                </div>

                                <c:forEach var="f" items="${imageFields}">

                                    <div class="col-md-6">

                                        <label
                                            for="image-${f.key}"
                                            class="form-label fw-semibold">

                                            <c:out value="${f.label}" />
                                        </label>

                                        <div class="mb-2">
                                            <img
                                                id="preview-${f.key}"
                                                src="${pageContext.request.contextPath}/assets/images/hotel/${fn:escapeXml(f.file)}"
                                                alt="${fn:escapeXml(f.label)}"
                                                class="img-thumbnail"
                                                style="height: 150px; width: 100%; object-fit: cover;"
                                                >
                                        </div>

                                        <input
                                            id="image-${f.key}"
                                            type="file"
                                            name="${f.key}"
                                            class="form-control"
                                            accept=".jpg,.jpeg,.png,.webp"
                                            data-preview="preview-${f.key}"
                                        >

                                    </div>

                                </c:forEach>

                            </div>

                            <div class="d-flex justify-content-end gap-2 mt-4">

                                <a
                                    href="${pageContext.request.contextPath}/dashboard"
                                    class="btn btn-outline-secondary">

                                    Cancel
                                </a>

                                <button
                                    type="submit"
                                    class="btn btn-warning fw-semibold">

                                    Save Changes
                                </button>

                            </div>

                        </form>

                    </div>

                </div>

            </div>

        </div>

    </div>

    <script>
        // Show the chosen image before it is uploaded.
        (function () {
            var inputs = document.querySelectorAll('input[type="file"][data-preview]');

            inputs.forEach(function (input) {
                input.addEventListener("change", function () {
                    var file = input.files && input.files[0];
                    var preview = document.getElementById(input.getAttribute("data-preview"));

                    if (!file || !preview) {
                        return;
                    }

                    preview.src = URL.createObjectURL(file);
                });
            });
        })();
    </script>

</layout:layout>
