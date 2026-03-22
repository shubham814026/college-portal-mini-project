<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<c:choose>
    <c:when test="${empty onlineUsers}">
        <p class="empty-state">No other users are online right now.</p>
    </c:when>
    <c:otherwise>
        <ul>
            <c:forEach items="${onlineUsers}" var="u">
                <li>${u}</li>
            </c:forEach>
        </ul>
    </c:otherwise>
</c:choose>
