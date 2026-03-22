package com.college.models;

import java.time.LocalDateTime;

public class Alert {
    private int alertId;
    private String message;
    private int sentBy;
    private String sentByName;
    private LocalDateTime sentAt;

    public int getAlertId() {
        return alertId;
    }

    public void setAlertId(int alertId) {
        this.alertId = alertId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getSentBy() {
        return sentBy;
    }

    public void setSentBy(int sentBy) {
        this.sentBy = sentBy;
    }

    public String getSentByName() {
        return sentByName;
    }

    public void setSentByName(String sentByName) {
        this.sentByName = sentByName;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    public void setSentAt(LocalDateTime sentAt) {
        this.sentAt = sentAt;
    }
}
