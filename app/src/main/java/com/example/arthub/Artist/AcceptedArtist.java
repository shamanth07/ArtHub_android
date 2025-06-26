package com.example.arthub.Artist;

public class AcceptedArtist {
    private String artistId;
    private String artistName;

    private int likeCount;

    public AcceptedArtist() {}  // Empty constructor for Firebase

    public AcceptedArtist(String artistId, String artistName, int likeCount) {
        this.artistId = artistId;
        this.artistName = artistName;

        this.likeCount = likeCount;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }


    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }
}
