package com.example.arthub.Visitor;

public class Reply {
    public String comment;
    public long timestamp;
    public String userId;

    public Reply() { }

    public Reply(String comment, long timestamp, String userId) {
        this.comment = comment;
        this.timestamp = timestamp;
        this.userId = userId;
    }
}
