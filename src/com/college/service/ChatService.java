package com.college.service;

import com.college.dao.MessageDAO;
import com.college.dao.UserDAO;
import com.college.models.Message;
import com.college.models.User;
import com.college.utils.AppConstants;
import com.college.utils.RequestContext;

import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ChatService {
    private final MessageDAO messageDAO = new MessageDAO();
    private final UserDAO userDAO = new UserDAO();

    public void sendDirectMessage(String fromUser, String toUser, String message, int senderId) throws Exception {
        long startedAt = System.currentTimeMillis();
        User receiver = userDAO.findByUsername(toUser);
        if (receiver == null) {
            throw new IllegalArgumentException("Recipient not found");
        }

        // Persist first so chat history remains reliable even if live socket delivery fails.
        messageDAO.saveMessage(senderId, receiver.getUserId(), "direct", message);

        try (Socket socket = new Socket()) {
            socket.connect(new InetSocketAddress(AppConstants.TCP_CHAT_HOST, AppConstants.TCP_CHAT_PORT),
                    AppConstants.TCP_CONNECT_TIMEOUT_MS);
            socket.setSoTimeout(AppConstants.TCP_READ_TIMEOUT_MS);

            try (PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {
                out.println("LOGIN:" + fromUser);
                out.println("TO:" + toUser + ":" + message);
            }
        } catch (Exception e) {
            System.err.println("[" + RequestContext.getRequestId() + "] Live chat socket delivery failed: "
                    + e.getMessage());
        }

        System.out.println("[" + RequestContext.getRequestId() + "] ChatService.sendDirectMessage success in "
                + (System.currentTimeMillis() - startedAt) + "ms");
    }

    public java.util.List<Message> getDirectHistory(int currentUserId, String withUsername) throws Exception {
        User peer = userDAO.findByUsername(withUsername);
        if (peer == null) {
            return java.util.Collections.emptyList();
        }
        return messageDAO.getRecentDirectMessagesBetweenUsers(currentUserId, peer.getUserId(), 50);
    }
}
