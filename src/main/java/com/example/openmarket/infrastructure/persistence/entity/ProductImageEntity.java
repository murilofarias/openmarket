package com.example.openmarket.infrastructure.persistence.entity;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "product_images")
public class ProductImageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "position", nullable = false)
    private int position;

    @Column(name = "is_primary", nullable = false)
    private boolean primary;

}
