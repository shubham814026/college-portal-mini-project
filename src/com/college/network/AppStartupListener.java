package com.college.network;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

@WebListener
public class AppStartupListener implements ServletContextListener {
    private Thread multicastThread;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        multicastThread = new Thread(new MulticastListenerThread(), "multicast-listener");
        multicastThread.setDaemon(true);
        multicastThread.start();
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        if (multicastThread != null) {
            multicastThread.interrupt();
        }

        // Deregister JDBC drivers to prevent Tomcat memory leaks
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            Driver driver = drivers.nextElement();
            try {
                DriverManager.deregisterDriver(driver);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
