package com.zenalyst.backend_assignment.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Supplier {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private int deliveryDays;
    private int minimumOrderQuantity;
    private int maximumSupplyQuantity;

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getDeliveryDays() {
        return deliveryDays;
    }

    public int getMinimumOrderQuantity() {
        return minimumOrderQuantity;
    }

    public int getMaximumSupplyQuantity() {
        return maximumSupplyQuantity;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDeliveryDays(int deliveryDays) {
        this.deliveryDays = deliveryDays;
    }

    public void setMinimumOrderQuantity(int minimumOrderQuantity) {
        this.minimumOrderQuantity = minimumOrderQuantity;
    }

    public void setMaximumSupplyQuantity(int maximumSupplyQuantity) {
        this.maximumSupplyQuantity = maximumSupplyQuantity;
    }
}