package com.college.rmi;

import com.college.dao.LogDAO;
import com.college.dao.UserDAO;
import com.college.models.User;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.sql.Timestamp;

public class CollegeServiceImpl extends UnicastRemoteObject implements CollegeService {
    private final UserDAO userDAO = new UserDAO();
    private final LogDAO logDAO = new LogDAO();

    public CollegeServiceImpl() throws RemoteException {
        super();
    }

    @Override
    public String authenticate(String username, String passwordHash) throws RemoteException {
        try {
            User user = userDAO.findByUsernameAndPasswordHash(username, passwordHash);
            return user == null ? null : user.getRole();
        } catch (SQLException e) {
            throw new RemoteException("Authentication failed", e);
        }
    }

    @Override
    public void logEvent(int userId, String action, Timestamp time) throws RemoteException {
        try {
            logDAO.insertLog(userId, action, time);
        } catch (SQLException e) {
            throw new RemoteException("Log event failed", e);
        }
    }
}
