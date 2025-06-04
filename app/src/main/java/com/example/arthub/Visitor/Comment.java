package com.example.arthub.Visitor;



public class Comment {
    public String comment;
    public long timestamp;
    public String userId;


    public Comment() { }

    public Comment(String comment, long timestamp, String userId) {
        this.comment = comment;
        this.timestamp = timestamp;
        this.userId = userId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }
}

