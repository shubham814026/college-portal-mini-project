<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html>
<head>
    <c:set var="pageTitle" value="Notice Board" />
    <%@ include file="/common/header.jsp" %>
</head>
<body>
<jsp:include page="/common/navbar.jsp" />
<jsp:include page="/common/alert_banner.jsp" />
<main class="container">
    <h2>Notice Board</h2>
    <c:choose>
        <c:when test="${empty notices}">
            <p class="empty-state">No notices have been posted yet. Check back later.</p>
        </c:when>
        <c:otherwise>
            <c:forEach items="${notices}" var="notice">
                <article class="card notice-card">
                    <h3>${notice.title}</h3>
                    <p>${notice.body}</p>
                    <small>${notice.postedByName}</small>
                </article>
            </c:forEach>
        </c:otherwise>
    </c:choose>
</main>
<jsp:include page="/common/footer.jsp" />
</body>
</html>
