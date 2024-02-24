package com.example.inventorysystem.facade;

import com.example.inventorysystem.repository.LockRepository;
import com.example.inventorysystem.service.StockService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class NamedLockStockFacade {
    private final LockRepository lockRepository;
    private final StockService stockService;

    public NamedLockStockFacade(LockRepository lockRepository, StockService stockService) {
        this.lockRepository = lockRepository;
        this.stockService = stockService;
    }

    @Transactional
    public void decrease(Long id, long quantity) {
        String key = String.valueOf(id);
        try {
            lockRepository.getLock(key);
            stockService.decreaseWithNamedLock(id, quantity);
        } finally {
            lockRepository.releaseLock(key);
        }
    }
}
