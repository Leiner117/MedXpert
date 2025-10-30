package com.tec.medxpert.ui.chat.model;

public class Message {
    public enum Type {
        SENT,
        RECEIVED
    }

    private String text;
    private String senderId;
    private long timestamp;

    // Solo para uso en app (no se guarda en Firebase)
    private transient Type type;

    // Constructor vacío para Firebase
    public Message() {}

    public Message(String text, String senderId, long timestamp) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
    }

    // Constructor extra para el adapter
    public Message(String text, String senderId, long timestamp, Type type) {
        this.text = text;
        this.senderId = senderId;
        this.timestamp = timestamp;
        this.type = type;
    }

    public String getText() { return text; }
    public String getSenderId() { return senderId; }
    public long getTimestamp() { return timestamp; }
    public Type getType() { return type; }
    public void setType(Type type) { this.type = type; }
}
