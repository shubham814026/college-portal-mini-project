package com.college.rmi;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.sql.Timestamp;

public interface CollegeService extends Remote {
    String authenticate(String username, String passwordHash) throws RemoteException;

    void logEvent(int userId, String action, Timestamp time) throws RemoteException;
}
