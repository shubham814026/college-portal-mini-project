package com.college.service;

import com.college.dao.UserDAO;
import com.college.models.User;
import com.college.rmi.CollegeService;
import com.college.utils.PasswordUtil;
import com.college.utils.RequestContext;
import com.college.utils.RmiClientUtil;

import java.time.LocalDateTime;

public class AuthService {
    public enum AuthStatus {
        SUCCESS,
        INVALID_CREDENTIALS,
        LOCKED,
        SERVICE_UNAVAILABLE
    }

    public static final class AuthResult {
        private final AuthStatus status;
        private final User user;

        private AuthResult(AuthStatus status, User user) {
            this.status = status;
            this.user = user;
        }

        public AuthStatus getStatus() {
            return status;
        }

        public User getUser() {
            return user;
        }

        public static AuthResult of(AuthStatus status) {
            return new AuthResult(status, null);
        }

        public static AuthResult success(User user) {
            return new AuthResult(AuthStatus.SUCCESS, user);
        }
    }

    private final UserDAO userDAO = new UserDAO();

    public AuthResult authenticate(String username, String password) {
        long startedAt = System.currentTimeMillis();
        try {
            User existing = userDAO.findByUsername(username);
            if (existing != null && existing.getLockedUntil() != null
                    && existing.getLockedUntil().isAfter(LocalDateTime.now())) {
                return AuthResult.of(AuthStatus.LOCKED);
            }

            String hash = PasswordUtil.sha256(password);
            CollegeService service = RmiClientUtil.getService();
            String role = service.authenticate(username, hash);
            if (role == null) {
                userDAO.incrementFailedAttempts(username);
                return AuthResult.of(AuthStatus.INVALID_CREDENTIALS);
            }

            User user = userDAO.findByUsername(username);
            if (user == null) {
                return AuthResult.of(AuthStatus.INVALID_CREDENTIALS);
            }

            userDAO.resetFailedAttempts(username);
            System.out.println("[" + RequestContext.getRequestId() + "] AuthService.authenticate success in "
                    + (System.currentTimeMillis() - startedAt) + "ms");
            return AuthResult.success(user);
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] AuthService.authenticate failed: "
                    + e.getMessage());
            return AuthResult.of(AuthStatus.SERVICE_UNAVAILABLE);
        }
    }
}
