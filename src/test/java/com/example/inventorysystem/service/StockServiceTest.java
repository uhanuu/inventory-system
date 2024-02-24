package com.example.inventorysystem.service;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;
    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void setUp() {
        stockRepository.save(new Stock(1L, 100L));
    }

    @AfterEach
    void tearDown(){
        stockRepository.deleteAllInBatch();
    }

    @Test
    public void 재고감소() {
        //given
        long stockId = 1L;
        // when
        stockService.decrease(stockId, 1L);
        // then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertEquals(stock.getQuantity(), 99L);
    }

}