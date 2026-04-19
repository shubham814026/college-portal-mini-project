package com.college.servlets;

import com.college.utils.JsonUtil;
import com.college.utils.RmiClientUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");
            RmiClientUtil.safeLogEvent(userId, "LOGOUT");
            session.invalidate();
        }
        JsonUtil.sendSuccess(resp, "{\"status\":\"ok\"}");
    }
}
