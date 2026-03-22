<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Login" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<main class="container auth-container">
    <section class="card auth-card">
        <h1>College Portal Login</h1>

        <c:if test="${param.error == '1'}"><div class="error-banner">Invalid username or password.</div></c:if>
        <c:if test="${param.locked == '1'}"><div class="warning-banner">Account locked. Try again after 15 minutes.</div></c:if>
        <c:if test="${param.timeout == '1'}"><div class="info-banner">Your session has expired. Please log in again.</div></c:if>
        <c:if test="${param.rmi == '1'}"><div class="warning-banner">Authentication service unavailable. Try again later.</div></c:if>

        <form method="post" action="${pageContext.request.contextPath}/login" class="form-grid">
            <label for="username">Username</label>
            <input id="username" type="text" name="username" required>

            <label for="password">Password</label>
            <input id="password" type="password" name="password" required>

            <button class="btn btn-primary" type="submit">Login</button>
        </form>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
