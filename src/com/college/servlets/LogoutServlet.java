package com.college.servlets;

import com.college.utils.RmiClientUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/logout")
public class LogoutServlet extends BaseServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        HttpSession session = req.getSession(false);
        if (session != null) {
            Integer userId = (Integer) session.getAttribute("userId");
            RmiClientUtil.safeLogEvent(userId, "LOGOUT");
            session.invalidate();
        }

        resp.sendRedirect(req.getContextPath() + "/login");
    }
}
