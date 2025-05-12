package com.example.arthub.Admin;

public class Event {
    public String eventId;
    public String title;
    public String description;
    public long eventDate;
    public String time;
    public int maxArtists;
    public String bannerImageUrl;

    public Event() {}

    public Event(String eventId, String title, String description, long date, String time, int maxArtists, String bannerImageUrl) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.eventDate = date;
        this.time = time;
        this.maxArtists = maxArtists;
        this.bannerImageUrl = bannerImageUrl;
    }
}


