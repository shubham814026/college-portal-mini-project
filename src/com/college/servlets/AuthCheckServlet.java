package com.college.servlets;

import com.college.dao.UserDAO;
import com.college.models.User;
import com.college.utils.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/auth/*")
public class AuthCheckServlet extends BaseServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED, "Not authenticated");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");
        String username = (String) session.getAttribute("username");
        String role = (String) session.getAttribute("role");
        String fullName = (String) session.getAttribute("fullName");

        JsonUtil.sendSuccess(resp, JsonUtil.object(
                "userId", JsonUtil.num(userId),
                "username", JsonUtil.str(username),
                "fullName", JsonUtil.str(fullName != null ? fullName : username),
                "role", JsonUtil.str(role)
        ));
    }
}
