package com.example.inventorysystem.service;

import com.example.inventorysystem.domain.Stock;
import com.example.inventorysystem.facade.LettuceLockStockFacade;
import com.example.inventorysystem.facade.NamedLockStockFacade;
import com.example.inventorysystem.facade.OptimisticLockStockFacade;
import com.example.inventorysystem.facade.RedissonLockStockFacade;
import com.example.inventorysystem.repository.StockRepository;
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

// test 따로 돌려야 된다.
@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private OptimisticLockStockFacade optimisticLockStockFacade;
    @Autowired
    private NamedLockStockFacade namedLockStockFacade;
    @Autowired
    private LettuceLockStockFacade lettuceLockStockFacade;
    @Autowired
    private RedissonLockStockFacade redissonLockStockFacade;

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

    //충돌이 빈번하게 일어나면 Optimistic Lock보다 성능이 좋다.
    @Test
    public void pessimisticLock을_통해서_동시에_100개의_요청() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decreaseWithPessimisticLock(stockId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0);
    }

    // 별도의 락을 잡지 않기 때문에 pessimisticLock보다 성능이 좋지만 충돌이 많으면 pessimisticLock이 더 좋다.
    @Test
    public void optimisticLock을_통해서_동시에_100개의_요청() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    optimisticLockStockFacade.decrease(stockId, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0);
        // 버전이 하나씩 올라감 +=1
        assertThat(stock.getVersion()).isEqualTo(100);
    }

    //todo 재시도가 필요하지 않으면 lettuce 필요하면 redisson으로 활용가능
    //get_lock을 통해서 lock을 거는데 stock에다가 락을 거는게 아니라 table내에 별도의 공간에 lock을 건다.
    // 분산락을 구현할 때 사용한다. (pessimistic lock은 타임아웃을 구현하기 힘들지만 namedLock은 구현하기 쉬움 트랜잭션 세션관리 주의하기)
    @Test
    public void namedLock을_통해서_동시에_100개의_요청() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    namedLockStockFacade.decrease(stockId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0);
    }

    // namedLock과 비슷하지만 세션관리를 신경쓰지 않아도 되는 장점이 있다. (구현이 간단함)
    // spin lock 방식이기 때문에 redis에 부하를 주게 된다. (여기서는 Thread.sleep을 이용함)
    @Test
    public void redis의_Lettuce를_통해서_동시에_100개의_요청() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    lettuceLockStockFacade.decrease(stockId, 1L);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0);
    }

    /**
     *
     * lettuce는 lock 획득을 계속 시도하는데 redisson은 Thread로 재우지 않는다.
     * lock을 획득해야 하는 스레드들에게 락을 획득하라고 메시지를 전달해준다.
     * 메시지를 받은 스레드들은 lock이 실제로 있을 때만 요청하기 때문에 redis 부하가 줄어든다.
     */
    @Test
    public void redisson를_통해서_동시에_100개의_요청() throws InterruptedException {
        //given
        long stockId = 1L;
        int threadCount = 100;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        //when
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    redissonLockStockFacade.decrease(stockId, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();

        //then
        Stock stock = stockRepository.findById(stockId).orElseThrow();
        assertThat(stock.getQuantity()).isEqualTo(0);
    }
}