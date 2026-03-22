<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html>
<head>
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<main class="container">
    <section class="card">
        <h2>Something went wrong</h2>
        <p>${not empty errorMessage ? errorMessage : 'An unexpected error occurred. Please try again.'}</p>
        <a class="btn btn-primary" href="${pageContext.request.contextPath}/dashboard">Go to Dashboard</a>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
