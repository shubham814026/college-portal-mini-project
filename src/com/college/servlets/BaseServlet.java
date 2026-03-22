package com.college.servlets;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

public abstract class BaseServlet extends HttpServlet {
    protected int currentUserId(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return -1;
        }
        Object userId = session.getAttribute("userId");
        return userId == null ? -1 : (Integer) userId;
    }

    protected String currentRole(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return null;
        }
        Object role = session.getAttribute("role");
        return role == null ? null : role.toString();
    }
}
