<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="System Logs" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<main class="container">
    <h2>System Activity Logs</h2>
    <c:choose>
        <c:when test="${empty logs}"><p class="empty-state">No activity logged yet.</p></c:when>
        <c:otherwise>
            <table class="table">
                <thead>
                <tr><th>Log ID</th><th>User</th><th>Action</th><th>Timestamp</th></tr>
                </thead>
                <tbody>
                <c:forEach items="${logs}" var="log">
                    <tr>
                        <td>${log.logId}</td>
                        <td>${log.username}</td>
                        <td>${log.action}</td>
                        <td>${log.loggedAt}</td>
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
