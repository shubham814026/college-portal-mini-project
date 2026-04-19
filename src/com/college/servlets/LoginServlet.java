package com.college.servlets;

import com.college.models.User;
import com.college.service.AuthService;
import com.college.service.AuthService.AuthResult;
import com.college.service.AuthService.AuthStatus;
import com.college.utils.AppConstants;
import com.college.utils.InputSanitizer;
import com.college.utils.JsonUtil;
import com.college.utils.RmiClientUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/api/login")
public class LoginServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String body = JsonUtil.readBody(req);
        String username = JsonUtil.extractString(body, "username");
        String password = JsonUtil.extractString(body, "password");

        if (username != null) username = InputSanitizer.normalizeText(username);
        if (password != null) password = InputSanitizer.normalizeText(password);

        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_BAD_REQUEST,
                    "Username and password are required");
            return;
        }

        try {
            AuthResult result = authService.authenticate(username, password);
            AuthStatus status = result.getStatus();

            if (status == AuthStatus.LOCKED) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_FORBIDDEN,
                        "Account locked. Try again after 15 minutes.");
                return;
            }

            if (status == AuthStatus.INVALID_CREDENTIALS) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_UNAUTHORIZED,
                        "Invalid username or password");
                return;
            }

            if (status == AuthStatus.SERVICE_UNAVAILABLE) {
                JsonUtil.sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                        "Authentication service unavailable. Please try again shortly.");
                return;
            }

            User user = result.getUser();
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setAttribute("fullName", user.getFullName());
            session.setMaxInactiveInterval(AppConstants.SESSION_TIMEOUT_SECONDS);

            RmiClientUtil.safeLogEvent(user.getUserId(), "LOGIN");

            JsonUtil.sendSuccess(resp, JsonUtil.object(
                    "status", JsonUtil.str("ok"),
                    "user", userToJson(user)
            ));
        } catch (Exception e) {
            JsonUtil.sendError(resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication service unavailable. Please try again shortly.");
        }
    }

    static String userToJson(User u) {
        return JsonUtil.object(
                "userId", JsonUtil.num(u.getUserId()),
                "username", JsonUtil.str(u.getUsername()),
                "fullName", JsonUtil.str(u.getFullName()),
                "role", JsonUtil.str(u.getRole()),
                "email", JsonUtil.str(u.getEmail())
        );
    }
}
