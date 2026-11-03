<%@ page contentType="text/html" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
<%@ taglib prefix="layout" tagdir="/WEB-INF/tags" %>
<layout:layout
    title="Hotel Information"
    useBootstrap="true"
    pageCss="group5.css"
    bodyClass="hbms-ui-page group5-page">
    <div class="hbms-ui ui-shell g5-workspace g5-form-workspace">
        <header class="g5-heading"><h1>Hotel Information</h1></header>
        <div class="g5-panel g5-form-panel">
            <form action="${pageContext.request.contextPath}/hotel" method="post" enctype="multipart/form-data">
                <section aria-labelledby="hotel-info-title">
                    <h2 id="hotel-info-title" class="g5-subtitle">Property details</h2>
                    <div class="row g-3">
                        <div class="col-12">
                            <label for="hotelName" class="form-label fw-semibold text-muted">Hotel Name (cannot be changed)</label>
                            <%-- No name attribute: the name is never sent to the server --%>
                            <input id="hotelName" type="text" class="form-control bg-light" value="${fn:escapeXml(hotel.hotelName)}" readonly>
                        </div>
                        <div class="col-12">
                            <label for="address" class="form-label fw-semibold text-muted">Address (cannot be changed)</label>
                            <%-- No name attribute: the address is never sent to the server --%>
                            <textarea id="address" class="form-control bg-light" rows="3" readonly>${fn:escapeXml(hotel.address)}</textarea>
                        </div>
                    </div>
                </section>
                <section class="g5-image-section" aria-labelledby="hotel-images-title">
                    <h2 id="hotel-images-title" class="g5-subtitle">Images</h2>
                    <p class="form-text">Optional. JPG, PNG or WEBP, maximum 2 MB for each image. Leave a field empty to keep the current image.</p>
                    <c:forEach var="f" items="${imageFields}">
                        <div class="g5-image-row">
                            <img id="preview-${f.key}" src="${pageContext.request.contextPath}/assets/images/hotel/${fn:escapeXml(f.file)}" alt="${fn:escapeXml(f.label)}" class="g5-hotel-preview">
                            <div>
                                <label for="image-${f.key}" class="form-label fw-semibold"><c:out value="${f.label}" /></label>
                                <input id="image-${f.key}" type="file" name="${f.key}" class="form-control" accept=".jpg,.jpeg,.png,.webp" data-preview="preview-${f.key}">
                            </div>
                        </div>
                    </c:forEach>
                </section>
                <div class="g5-actions mt-4">
                    <a href="${pageContext.request.contextPath}/dashboard" class="btn g5-secondary">Cancel</a>
                    <button type="submit" class="btn g5-primary fw-semibold">Save Changes</button>
                </div>
            </form>
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
