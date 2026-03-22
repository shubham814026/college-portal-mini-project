package com.college.chat;

import com.college.utils.AppConstants;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatServer {
    public static final Map<String, Socket> ACTIVE_CLIENTS = new ConcurrentHashMap<>();

    public static void main(String[] args) throws IOException {
        Thread statusThread = new Thread(new ChatStatusServer(AppConstants.TCP_CHAT_STATUS_PORT), "chat-status-server");
        statusThread.setDaemon(true);
        statusThread.start();

        try (ServerSocket serverSocket = new ServerSocket(AppConstants.TCP_CHAT_PORT)) {
            System.out.println("Chat server started on port " + AppConstants.TCP_CHAT_PORT);
            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread handlerThread = new Thread(new ClientHandler(clientSocket));
                handlerThread.start();
            }
        }
    }
}
