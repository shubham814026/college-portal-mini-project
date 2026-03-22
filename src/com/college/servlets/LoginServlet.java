package com.college.servlets;

import com.college.models.User;
import com.college.service.AuthService;
import com.college.service.AuthService.AuthResult;
import com.college.service.AuthService.AuthStatus;
import com.college.utils.AppConstants;
import com.college.utils.InputSanitizer;
import com.college.utils.RmiClientUtil;
import com.college.utils.ServletResponseUtil;
import com.college.utils.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private final AuthService authService = new AuthService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException, ServletException {
        String username = InputSanitizer.normalizeText(req.getParameter("username"));
        String password = InputSanitizer.normalizeText(req.getParameter("password"));

        if (ValidationUtil.isBlank(username) || ValidationUtil.isBlank(password)) {
            resp.sendRedirect(req.getContextPath() + "/login?error=1");
            return;
        }

        try {
            AuthResult result = authService.authenticate(username, password);
            AuthStatus status = result.getStatus();

            if (status == AuthStatus.LOCKED) {
                resp.sendRedirect(req.getContextPath() + "/login?locked=1");
                return;
            }

            if (status == AuthStatus.INVALID_CREDENTIALS) {
                resp.sendRedirect(req.getContextPath() + "/login?error=1");
                return;
            }

            if (status == AuthStatus.SERVICE_UNAVAILABLE) {
                throw new IllegalStateException("Auth service unavailable");
            }

            User user = result.getUser();
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getUserId());
            session.setAttribute("username", user.getUsername());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(AppConstants.SESSION_TIMEOUT_SECONDS);

            RmiClientUtil.safeLogEvent(user.getUserId(), "LOGIN");
            resp.sendRedirect(req.getContextPath() + "/dashboard");
        } catch (Exception e) {
            ServletResponseUtil.forwardError(req, resp, HttpServletResponse.SC_SERVICE_UNAVAILABLE,
                    "Authentication service unavailable. Please try again shortly.");
        }
    }
}
