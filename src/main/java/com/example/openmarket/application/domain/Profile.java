package com.example.openmarket.application.domain;

import java.time.LocalDateTime;

public abstract class Profile {

    private Double rating;
    private LocalDateTime createdAt;

    protected Profile() {
        this.createdAt = LocalDateTime.now();
        this.rating = null;
    }

    protected Profile(LocalDateTime createdAt, Double rating) {
        this.createdAt = createdAt;
        this.rating = rating;
    }

    public void updateRating(double newRating) {
        if (newRating < 0 || newRating > 5) {
            throw new IllegalArgumentException("Rating must be between 0 and 5");
        }
        this.rating = newRating;
    }

    public abstract Role getProfileRole();

    public LocalDateTime getCreatedAt() { return createdAt; }
    public Double getRating() { return rating; }

}
