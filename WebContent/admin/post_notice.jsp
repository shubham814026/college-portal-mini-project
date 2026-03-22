<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Post Notice" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<main class="container">
    <section class="card">
        <h2>Post New Notice</h2>
        <c:if test="${param.success == 'posted' || param.success == '1'}"><div class="success-banner">Notice posted successfully.</div></c:if>
        <c:if test="${param.error == 'validation' || param.error == '1'}"><div class="error-banner">Title and message are required.</div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/notices" class="form-grid">
            <label for="title">Title</label>
            <input id="title" type="text" name="title" maxlength="200" required>

            <label for="body">Message</label>
            <textarea id="body" name="body" rows="5" required></textarea>

            <button class="btn btn-primary" type="submit">Post Notice</button>
        </form>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
