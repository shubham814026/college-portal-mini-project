package com.college.network;

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
    }
}
