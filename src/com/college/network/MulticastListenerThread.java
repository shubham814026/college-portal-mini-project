package com.college.network;

import com.college.utils.AppConstants;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastListenerThread implements Runnable {
    @Override
    public void run() {
        byte[] buffer = new byte[2048];

        try (MulticastSocket socket = new MulticastSocket(AppConstants.MULTICAST_PORT)) {
            socket.joinGroup(InetAddress.getByName(AppConstants.MULTICAST_GROUP));

            while (!Thread.currentThread().isInterrupted()) {
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                socket.receive(packet);
                String payload = new String(packet.getData(), 0, packet.getLength());
                NotificationQueue.addMessage(payload);
            }
        } catch (IOException e) {
            System.err.println("Multicast listener stopped: " + e.getMessage());
        }
    }
}
