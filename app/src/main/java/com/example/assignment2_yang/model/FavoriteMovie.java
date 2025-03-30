package com.example.assignment2_yang.model;

public class FavoriteMovie {
    private String userId;
    private String movieId;
    private String title;
    private String year;
    private String poster;
    private String rating;
    private String studio;
    private String description;

    public FavoriteMovie() {} // Required for Firestore

    public FavoriteMovie(String userId, String movieId, String title, String year, String poster, String rating, String studio) {
        this.userId = userId;
        this.movieId = movieId;
        this.title = title;
        this.year = year;
        this.poster = poster;
        this.rating = rating;
        this.studio = studio;
    }

    // Getters and Setters
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getMovieId() { return movieId; }
    public void setMovieId(String movieId) { this.movieId = movieId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getYear() { return year; }
    public void setYear(String year) { this.year = year; }

    public String getPoster() { return poster; }
    public void setPoster(String poster) { this.poster = poster; }

    public String getRating() { return rating; }
    public void setRating(String rating) { this.rating = rating; }

    public String getStudio() { return studio; }
    public void setStudio(String studio) { this.studio = studio; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}