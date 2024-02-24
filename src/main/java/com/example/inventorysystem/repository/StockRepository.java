package com.example.inventorysystem.repository;

import com.example.inventorysystem.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {
}
