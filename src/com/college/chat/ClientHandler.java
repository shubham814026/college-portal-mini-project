package com.college.chat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        String username = null;

        try (BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            String login = in.readLine();
            if (login == null || !login.startsWith("LOGIN:")) {
                return;
            }

            username = login.substring("LOGIN:".length()).trim();
            ChatServer.ACTIVE_CLIENTS.put(username, socket);

            String line;
            while ((line = in.readLine()) != null) {
                String[] parts = line.split(":", 3);
                if (parts.length != 3 || !"TO".equals(parts[0])) {
                    continue;
                }

                String recipient = parts[1];
                String message = parts[2];
                Socket recipientSocket = ChatServer.ACTIVE_CLIENTS.get(recipient);
                if (recipientSocket == null) {
                    continue;
                }

                PrintWriter recipientOut = new PrintWriter(recipientSocket.getOutputStream(), true);
                recipientOut.println("FROM:" + username + ":" + message);
            }

            out.flush();
        } catch (IOException ignored) {
        } finally {
            if (username != null) {
                ChatServer.ACTIVE_CLIENTS.remove(username);
            }
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }
}
