<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Student Dashboard" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<jsp:include page="/common/alert_banner.jsp" />
<main class="container">
    <h2>Welcome, ${sessionScope.username}</h2>
    <section class="grid-3">
        <article class="card">
            <h3>Recent Notices</h3>
            <c:choose>
                <c:when test="${empty recentNotices}"><p class="empty-state">Nothing here yet.</p></c:when>
                <c:otherwise>
                    <ul>
                        <c:forEach items="${recentNotices}" var="n" end="2"><li>${n.title}</li></c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
            <a class="btn" href="${pageContext.request.contextPath}/notices">View All</a>
        </article>
        <article class="card">
            <h3>Recent Files</h3>
            <c:choose>
                <c:when test="${empty recentFiles}"><p class="empty-state">Nothing here yet.</p></c:when>
                <c:otherwise>
                    <ul>
                        <c:forEach items="${recentFiles}" var="f" end="2"><li>${f.originalName}</li></c:forEach>
                    </ul>
                </c:otherwise>
            </c:choose>
            <a class="btn" href="${pageContext.request.contextPath}/files">View All</a>
        </article>
        <article class="card">
            <h3>Chat</h3>
            <p>See online users and start messaging.</p>
            <a class="btn" href="${pageContext.request.contextPath}/chat">Open Chat</a>
        </article>
    </section>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
