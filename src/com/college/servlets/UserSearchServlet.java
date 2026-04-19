package com.college.servlets;

import com.college.dao.UserDAO;
import com.college.models.User;
import com.college.utils.JsonUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/api/users")
public class UserSearchServlet extends BaseServlet {
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String query = req.getParameter("q");
        try {
            List<User> users;
            if (query != null && !query.trim().isEmpty()) {
                users = userDAO.searchByName(query.trim());
            } else {
                users = userDAO.findAll();
            }

            int currentId = currentUserId(req);
            List<String> items = new ArrayList<>();
            for (User u : users) {
                if (u.getUserId() != currentId) {
                    items.add(JsonUtil.object(
                            "userId", JsonUtil.num(u.getUserId()),
                            "username", JsonUtil.str(u.getUsername()),
                            "fullName", JsonUtil.str(u.getFullName()),
                            "role", JsonUtil.str(u.getRole())
                    ));
                }
            }
            JsonUtil.sendSuccess(resp, JsonUtil.array(items));
        } catch (Exception e) {
            JsonUtil.sendError(resp, 500, "Could not search users.");
        }
    }
}
