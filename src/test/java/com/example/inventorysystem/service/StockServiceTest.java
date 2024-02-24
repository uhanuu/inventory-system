package com.example.inventorysystem.service;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Test
    public void 동시에_100개의_요청시_실패() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(stockId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isNotEqualTo(0);
    }

    @Test
    public void synchronized와_transaction을_통해서_동시에_100개의_요청시_실패() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseWithSync(stockId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isNotEqualTo(0);
    }

}