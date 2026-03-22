<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Upload File" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<main class="container">
    <section class="card">
        <h2>Upload Resource</h2>
        <c:if test="${param.error == 'size'}"><div class="error-banner">File size exceeds the 10 MB limit.</div></c:if>
        <c:if test="${param.error == 'type'}"><div class="error-banner">Only .pdf, .docx, .pptx, and .zip files are allowed.</div></c:if>
        <c:if test="${param.error == 'storage'}"><div class="error-banner">Storage unavailable. Contact admin.</div></c:if>
        <c:if test="${param.error == 'nofile'}"><div class="error-banner">Please select a file before uploading.</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/files" enctype="multipart/form-data" class="form-grid">
            <label for="file">Choose File</label>
            <input id="file" type="file" name="file" required>

            <label for="subjectTag">Subject Tag</label>
            <input id="subjectTag" type="text" name="subjectTag" placeholder="Computer Networks">

            <button class="btn btn-primary" type="submit">Upload</button>
        </form>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
