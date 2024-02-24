package com.example.inventorysystem.facade;

import com.example.inventorysystem.service.StockService;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedissonLockStockFacade {

    private final RedissonClient redissonClient;
    private final StockService stockService;
    private final Logger logger = LoggerFactory.getLogger(RedissonLockStockFacade.class);
    public RedissonLockStockFacade(RedissonClient redissonClient, StockService stockService) {
        this.redissonClient = redissonClient;
        this.stockService = stockService;
    }

    public void decrease(Long id, long quantity) {
        RLock lock = redissonClient.getLock(String.valueOf(id));

        //몇 초 동안 lock 획득을 시도할 것인지, 몇 초 동안 점유할 것인지 설정
        try {
            //test가 실패하면 lock을 기다리는 시간을 늘려보자 1번째 파라미터
            boolean available = lock.tryLock(15, 1, TimeUnit.SECONDS);

            if (!available) {
                logger.info("lock 획득 실패");
                return;
            }

            stockService.decreaseWithRedisson(id, quantity);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            lock.unlock();
        }
    }
}
