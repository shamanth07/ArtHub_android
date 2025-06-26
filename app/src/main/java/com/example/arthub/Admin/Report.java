package com.example.arthub.Admin;

import java.util.List;

public class Report {
    private String title;
    private String bannerImageUrl;
    private int interestedCount;
    private int rsvpCount;
    private String mostLikedArtist;
    private List<String> confirmedVisitors;
    private int totalLikes;

    public Report() {
        // Default constructor required for calls to DataSnapshot.getValue(Report.class)
    }

    public Report(String title, String bannerImageUrl, int interestedCount, int rsvpCount,
                  String mostLikedArtist, List<String> confirmedVisitors) {
        this.title = title;
        this.bannerImageUrl = bannerImageUrl;
        this.interestedCount = interestedCount;
        this.rsvpCount = rsvpCount;
        this.mostLikedArtist = mostLikedArtist;
        this.confirmedVisitors = confirmedVisitors;
    }

    // Getters and Setters
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getBannerImageUrl() { return bannerImageUrl; }
    public void setBannerImageUrl(String bannerImageUrl) { this.bannerImageUrl = bannerImageUrl; }

    public int getInterestedCount() { return interestedCount; }
    public void setInterestedCount(int interestedCount) { this.interestedCount = interestedCount; }

    public int getRsvpCount() { return rsvpCount; }
    public void setRsvpCount(int rsvpCount) { this.rsvpCount = rsvpCount; }

    public String getMostLikedArtist() { return mostLikedArtist; }
    public void setMostLikedArtist(String mostLikedArtist) { this.mostLikedArtist = mostLikedArtist; }

    public List<String> getConfirmedVisitors() { return confirmedVisitors; }
    public void setConfirmedVisitors(List<String> confirmedVisitors) { this.confirmedVisitors = confirmedVisitors; }

    public int getTotalLikes() { return totalLikes; }
    public void setTotalLikes(int totalLikes) { this.totalLikes = totalLikes; }
}
