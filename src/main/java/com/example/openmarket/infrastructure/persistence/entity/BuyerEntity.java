package com.example.openmarket.infrastructure.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "buyers")
@DiscriminatorValue("BUYER")
public class BuyerEntity extends ProfileEntity {

    @Column(name = "default_shipping_address")
    private String defaultShippingAddress;

    @Column(name = "total_orders")
    private Integer totalOrders;

    public String getDefaultShippingAddress() { return defaultShippingAddress; }
    public void setDefaultShippingAddress(String address) { this.defaultShippingAddress = address; }

    public Integer getTotalOrders() { return totalOrders; }
    public void setTotalOrders(Integer totalOrders) { this.totalOrders = totalOrders; }

}
