package com.example.arthub.Artist;


import java.io.Serializable;

public class Artwork implements Serializable {
    private String id;
    private String title;
    private String description;
    private String imageUrl;
    private String price;
    private String year;
    private String artistId;
    private String category;


    // Required empty constructor for Firebase
    public Artwork() {
    }

    public Artwork(String id, String title, String description, String imageUrl,
                   String price, String year, String artistId, String category) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.price = price;
        this.year = year;
        this.artistId = artistId;
        this.category = category;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getPrice() {
        return price;
    }

    public String getYear() {
        return year;
    }

    public String getArtistId() {
        return artistId;
    }


    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    // Setters
    public void setId(String id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public void setYear(String year) {
        this.year = year;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }


}

