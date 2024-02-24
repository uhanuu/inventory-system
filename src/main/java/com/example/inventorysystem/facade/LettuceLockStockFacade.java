package com.example.inventorysystem.facade;

import com.example.inventorysystem.repository.RedisLockRepository;
import com.example.inventorysystem.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class LettuceLockStockFacade {

    private final RedisLockRepository redisLockRepository;
    private final StockService stockService;

    public LettuceLockStockFacade(RedisLockRepository redisLockRepository, StockService stockService) {
        this.redisLockRepository = redisLockRepository;
        this.stockService = stockService;
    }

    public void decrease(Long id, long quantity) throws InterruptedException {
        while (!redisLockRepository.lock(id)) {
            // redis 부하를 줄이기 위해서 sleep 쓰는데 다른 방법 고민해보기
            Thread.sleep(100);
        }

        try {
            stockService.decreaseWithRedisLettuce(id, quantity);
        } finally {
            redisLockRepository.unlock(id);
        }
    }
}
