package com.example.openmarket.application.domain;

public class ProductImage {

    private final String filename;
    private final int position;
    private final boolean primary;

    public ProductImage(String filename, int position, boolean primary) {
        this.filename = filename;
        this.position = position;
        this.primary = primary;
    }

    public String getFilename() { return filename; }
    public int getPosition() { return position; }
    public boolean isPrimary() { return primary; }
}
