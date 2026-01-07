package com.example.openmarket.application.domain;

public class ProductImage {

    private final String url;
    private final int position;
    private final boolean primary;

    public ProductImage(String url, int position, boolean primary) {
        this.url = url;
        this.position = position;
        this.primary = primary;
    }

    public String getUrl() { return url; }
    public int getPosition() { return position; }
    public boolean isPrimary() { return primary; }
}

