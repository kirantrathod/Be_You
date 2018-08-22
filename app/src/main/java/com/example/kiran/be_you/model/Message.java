package com.example.kiran.be_you.model;

public class Message {

    private String message, type, from;
    private boolean seen;
    private long time;

    public Message() {} // Required empty public constructor

    public Message(String message, String type, String from, long time, boolean seen) {
        this.message = message;
        this.type = type;
        this.from = from;
        this.time = time;
        this.seen = seen;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public String getFrom() {
        return from;
    }

    public long getTime() {
        return time;
    }

    public boolean getSeen() {
        return seen;
    }
}

