package com.example.inventorysystem.facade;

import com.example.inventorysystem.service.StockService;
import org.springframework.stereotype.Component;

@Component
public class OptimisticLockStockFacade {

    private final StockService stockService;

    public OptimisticLockStockFacade(StockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, long quantity) throws InterruptedException {
        while (true) {
            try {
                stockService.decreaseWithOptimisticLock(id, quantity);
                break;
            } catch (Exception e) {
                //예제니까 그냥 스레드로 재우지 실제로는 다른 방법 고민하자.
                Thread.sleep(50);
            }
        }
    }
}
