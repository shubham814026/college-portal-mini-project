package com.college.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Set;

public class ChatStatusServer implements Runnable {
    private final int port;

    public ChatStatusServer(int port) {
        this.port = port;
    }

    @Override
    public void run() {
        try (ServerSocket statusSocket = new ServerSocket(port)) {
            System.out.println("Chat status server started on port " + port);
            while (!Thread.currentThread().isInterrupted()) {
                try (Socket client = statusSocket.accept();
                     BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                     PrintWriter out = new PrintWriter(client.getOutputStream(), true)) {

                    String command = in.readLine();
                    if (!"LIST".equals(command)) {
                        out.println("ERR");
                        continue;
                    }

                    Set<String> users = ChatServer.ACTIVE_CLIENTS.keySet();
                    out.println(String.join(",", users));
                } catch (IOException ignored) {
                }
            }
        } catch (IOException e) {
            System.err.println("Chat status server stopped: " + e.getMessage());
        }
    }
}
