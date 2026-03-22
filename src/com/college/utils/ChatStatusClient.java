package com.college.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public final class ChatStatusClient {
    private ChatStatusClient() {
    }

    public static List<String> fetchOnlineUsers() {
        List<String> users = new ArrayList<>();

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(AppConstants.TCP_CHAT_HOST, AppConstants.TCP_CHAT_STATUS_PORT),
                    AppConstants.TCP_CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(AppConstants.TCP_READ_TIMEOUT_MS);

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                 BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                out.println("LIST");
                String line = in.readLine();
                if (line == null || line.trim().isEmpty() || "ERR".equals(line)) {
                    return users;
                }

                String[] split = line.split(",");
                for (String entry : split) {
                    String user = InputSanitizer.normalizeText(entry);
                    if (user != null) {
                        users.add(user);
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("[" + RequestContext.getRequestId() + "] Chat status fetch failed: " + e.getMessage());
        }

        return users;
    }
}
