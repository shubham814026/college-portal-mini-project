<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Send Alert" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<main class="container">
    <section class="card">
        <h2>Send Urgent Alert</h2>
        <c:if test="${param.success == 'sent' || param.success == '1'}"><div class="success-banner">Alert sent successfully.</div></c:if>
        <c:if test="${param.error == 'validation' || param.error == '1'}"><div class="error-banner">Alert message cannot be empty.</div></c:if>
        <form method="post" action="${pageContext.request.contextPath}/alert" class="form-grid">
            <label for="alertMessage">Alert Message</label>
            <textarea id="alertMessage" name="alertMessage" rows="4" required></textarea>
            <button class="btn btn-primary" type="submit">Send Alert Now</button>
        </form>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
