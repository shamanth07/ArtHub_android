package com.example.arthub.Admin;

import java.io.Serializable;

public class Event implements Serializable {
    public String eventId;
    public String title;
    public String description;
    public long eventDate;
    public String time;
    public int maxArtists;
    public String bannerImageUrl;

    private String locationName;



    public double latitude;
    public double longitude;

    public Event() {}

    public Event(String eventId, String title, String description, long date, String time, int maxArtists, String bannerImageUrl,String locationName, double latitude, double longitude) {
        this.eventId = eventId;
        this.title = title;
        this.description = description;
        this.eventDate = date;
        this.time = time;
        this.maxArtists = maxArtists;
        this.bannerImageUrl = bannerImageUrl;
        this.locationName = locationName;

        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public long getEventDate() {
        return eventDate;
    }

    public void setEventDate(long eventDate) {
        this.eventDate = eventDate;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getMaxArtists() {
        return maxArtists;
    }

    public void setMaxArtists(int maxArtists) {
        this.maxArtists = maxArtists;
    }

    public String getBannerImageUrl() {
        return bannerImageUrl;
    }

    public void setBannerImageUrl(String bannerImageUrl) {
        this.bannerImageUrl = bannerImageUrl;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
