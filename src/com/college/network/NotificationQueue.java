package com.college.network;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public final class NotificationQueue {
    public static final class Event {
        private final long id;
        private final String payload;

        private Event(long id, String payload) {
            this.id = id;
            this.payload = payload;
        }

        public long getId() {
            return id;
        }

        public String getPayload() {
            return payload;
        }
    }

    private static final List<Event> ALERT_EVENTS = new ArrayList<>();
    private static final List<Event> NOTICE_EVENTS = new ArrayList<>();
    private static final AtomicLong ALERT_ID = new AtomicLong();
    private static final AtomicLong NOTICE_ID = new AtomicLong();
    private static final ConcurrentHashMap<String, Long> ALERT_LAST_SEEN = new ConcurrentHashMap<>();
    private static final ConcurrentHashMap<String, Long> NOTICE_LAST_SEEN = new ConcurrentHashMap<>();
    private static final int MAX_BUFFER_SIZE = 1000;

    private NotificationQueue() {
    }

    public static void addMessage(String payload) {
        if (payload == null) {
            return;
        }
        if (payload.startsWith("ALERT:")) {
            addEvent(ALERT_EVENTS, ALERT_ID, payload.substring("ALERT:".length()));
            return;
        }
        if (payload.startsWith("NEW_NOTICE:")) {
            addEvent(NOTICE_EVENTS, NOTICE_ID, payload.substring("NEW_NOTICE:".length()));
        }
    }

    public static void registerSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        ALERT_LAST_SEEN.putIfAbsent(sessionId, 0L);
        NOTICE_LAST_SEEN.putIfAbsent(sessionId, 0L);
    }

    public static void unregisterSession(String sessionId) {
        if (sessionId == null) {
            return;
        }
        ALERT_LAST_SEEN.remove(sessionId);
        NOTICE_LAST_SEEN.remove(sessionId);
    }

    public static Event pollAlert(String sessionId) {
        return pollEventForSession(sessionId, ALERT_EVENTS, ALERT_LAST_SEEN);
    }

    public static Event pollNotice(String sessionId) {
        return pollEventForSession(sessionId, NOTICE_EVENTS, NOTICE_LAST_SEEN);
    }

    private static void addEvent(List<Event> bucket, AtomicLong idCounter, String payload) {
        synchronized (bucket) {
            bucket.add(new Event(idCounter.incrementAndGet(), payload));
            if (bucket.size() > MAX_BUFFER_SIZE) {
                bucket.remove(0);
            }
        }
    }

    private static Event pollEventForSession(String sessionId, List<Event> bucket,
                                             ConcurrentHashMap<String, Long> seenMap) {
        if (sessionId == null) {
            return null;
        }

        long lastSeen = seenMap.getOrDefault(sessionId, 0L);
        synchronized (bucket) {
            for (Event event : bucket) {
                if (event.getId() > lastSeen) {
                    seenMap.put(sessionId, event.getId());
                    return event;
                }
            }
        }

        return null;
    }
}
