package com.example.inventorysystem.service;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long stockId, long quantity) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        stock.decrease(quantity);
    }

    @Transactional
    public synchronized void decreaseWithSync(Long stockId, long quantity) {
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        stock.decrease(quantity);
    }

    @Transactional
    public void decreaseWithPessimisticLock(Long stockId, long quantity) {
        Stock stock = stockRepository.findByIdWithPessimisticLock(stockId).orElseThrow();
        stock.decrease(quantity);
    }

    @Transactional
    public void decreaseWithOptimisticLock(Long stockId, long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(stockId).orElseThrow();
        stock.decrease(quantity);
    }

    // 부모 트랜잭션과 별도로 실행하기 위해서 설정
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void decreaseWithNamedLock(Long stockId, long quantity) {
        Stock stock = stockRepository.findByIdWithOptimisticLock(stockId).orElseThrow();
        stock.decrease(quantity);
    }
}
