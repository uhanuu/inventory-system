package com.example.inventorysystem.domain;

import jakarta.persistence.*;

@Entity
public class Stock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productID;

    private long quantity;

    //Optimistic Lock을 위한 필드
    @Version
    private Long version;


    protected Stock() {
    }

    public Stock(Long productID, Long quantity) {
        this.productID = productID;
        this.quantity = quantity;
    }

    public void decrease(long quantity) {
        if (this.quantity - quantity < 0) {
            throw new IllegalArgumentException("재고는 0개 미만이 될 수 없습니다.");
        }

        this.quantity -= quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public Long getVersion() {
        return version;
    }
}
