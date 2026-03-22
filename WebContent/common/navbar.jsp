<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<nav class="navbar">
    <a class="brand" href="${pageContext.request.contextPath}/dashboard">College Portal</a>
    <ul class="nav-links">
        <c:choose>
            <c:when test="${sessionScope.role == 'ADMIN'}">
                <li><a href="${pageContext.request.contextPath}/admin/notices/new">Post Notice</a></li>
                <li><a href="${pageContext.request.contextPath}/admin/alerts/new">Send Alert</a></li>
                <li><a href="${pageContext.request.contextPath}/logs">View Logs</a></li>
            </c:when>
            <c:otherwise>
                <li><a href="${pageContext.request.contextPath}/notices">Notices</a></li>
                <li><a href="${pageContext.request.contextPath}/files">Files</a></li>
                <li><a href="${pageContext.request.contextPath}/chat">Chat</a></li>
            </c:otherwise>
        </c:choose>
    </ul>
    <div class="user-info">
        <span>${sessionScope.username}</span>
        <a class="btn btn-outline" href="${pageContext.request.contextPath}/logout">Logout</a>
    </div>
</nav>
