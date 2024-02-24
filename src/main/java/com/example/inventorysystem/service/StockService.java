package com.example.inventorysystem.service;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long stockId, long quantity) {
        // stock 조회
        // 재고를 감소
        // 갱신된 값을 저장
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        stock.decrease(quantity);
    }

    @Transactional
    public synchronized void decreaseWithSync(Long stockId, long quantity) {
        // stock 조회
        // 재고를 감소
        // 갱신된 값을 저장
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        stock.decrease(quantity);
    }

    @Transactional
    public synchronized void decreaseWithPessimisticLock(Long stockId, long quantity) {
        // stock 조회
        // 재고를 감소
        // 갱신된 값을 저장
        Stock stock = stockRepository.findByIdWithPessimisticLock(stockId).orElseThrow();
        stock.decrease(quantity);
    }
}
