package com.example.arthub.Chat;

public class ChatMessage {
    private String message;
    private String sender;
    private long timestamp;

    public ChatMessage() {

    }

    public ChatMessage(String message, String sender, long timestamp) {
        this.message = message;
        this.sender = sender;
        this.timestamp = timestamp;
    }

    public String getMessage() {
        return message;
    }

    public String getSender() {
        return sender;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
