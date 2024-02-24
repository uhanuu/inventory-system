package com.example.inventorysystem.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productID;

    private long quantity;

    protected Stock() {
    }

    public Stock(Long productID, Long quantity) {
        this.productID = productID;
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void decrease(long quantity) {
        if (this.quantity - quantity < 0) {
            throw new IllegalArgumentException("재고는 0개 미만이 될 수 없습니다.");
        }

        this.quantity -= quantity;
    }
}
