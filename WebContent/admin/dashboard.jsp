<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Admin Dashboard" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<main class="container">
    <h2>Admin Panel</h2>
    <section class="grid-3">
        <article class="card"><h3>Recent Notices</h3><a class="btn" href="${pageContext.request.contextPath}/admin/notices/new">Post New</a></article>
        <article class="card"><h3>Send Alert</h3><a class="btn" href="${pageContext.request.contextPath}/admin/alerts/new">Open Alert Console</a></article>
        <article class="card"><h3>Recent Logs</h3><a class="btn" href="${pageContext.request.contextPath}/logs">View Logs</a></article>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
