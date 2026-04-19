<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Resource Library" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<jsp:include page="/common/alert_banner.jsp" />
<main class="container">
    <div class="row-between">
        <h2>Resource Library</h2>
        <c:if test="${sessionScope.role == 'STUDENT' || sessionScope.role == 'FACULTY'}">
            <a class="btn btn-primary" href="${pageContext.request.contextPath}/student/upload.jsp">Upload File</a>
        </c:if>
    </div>
    <c:if test="${param.success == 'uploaded' || param.success == '1'}"><div class="success-banner">File uploaded successfully.</div></c:if>
    <c:choose>
        <c:when test="${empty files}"><p class="empty-state">No files uploaded yet.</p></c:when>
        <c:otherwise>
            <table class="table">
                <thead>
                <tr><th>File Name</th><th>Subject</th><th>Uploaded By</th><th>Size</th><th>Action</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${files}" var="file">
                    <tr>
                        <td>${file.originalName}</td>
                        <td>${empty file.subjectTag ? '-' : file.subjectTag}</td>
                        <td>${file.uploadedByName}</td>
                        <td>${file.fileSizeDisplay}</td>
                        <td><a class="btn" href="${pageContext.request.contextPath}/files?action=download&fileId=${file.fileId}">Download</a></td>
                    </tr>
                </c:forEach>
                </tbody>
            </table>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
