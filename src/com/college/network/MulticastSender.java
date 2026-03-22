package com.college.network;

import com.college.utils.AppConstants;
import com.college.utils.RequestContext;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;

public final class MulticastSender {
    private MulticastSender() {
    }

    public static void broadcast(String message) {
        try (DatagramSocket socket = new DatagramSocket()) {
            InetAddress group = InetAddress.getByName(AppConstants.MULTICAST_GROUP);
            byte[] payload = message.getBytes(StandardCharsets.UTF_8);
            DatagramPacket packet = new DatagramPacket(
                    payload,
                    payload.length,
                    group,
                    AppConstants.MULTICAST_PORT
            );
            socket.send(packet);
        } catch (IOException e) {
            System.err.println("[" + RequestContext.getRequestId() + "] Multicast send failed: " + e.getMessage());
        }
    }
}
