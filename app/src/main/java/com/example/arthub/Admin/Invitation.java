package com.example.arthub.Admin;



import java.io.Serializable;

public class Invitation  implements Serializable {
    private String artistId;
    private String artistName;
    private String email;
    private String status;
    private long appliedAt;
    private String eventId;

    public Invitation() {

    }

    public Invitation(String artistId, String artistName, String email, String status, long appliedAt, String eventId) {
        this.artistId = artistId;
        this.artistName = artistName;
        this.email = email;
        this.status = status;
        this.appliedAt = appliedAt;
        this.eventId = eventId;
    }

    public String getArtistId() {
        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getAppliedAt() {
        return appliedAt;
    }

    public void setAppliedAt(long appliedAt) {
        this.appliedAt = appliedAt;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }
}
