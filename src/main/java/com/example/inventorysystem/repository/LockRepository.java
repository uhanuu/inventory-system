package com.example.inventorysystem.repository;

import com.example.inventorysystem.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LockRepository extends JpaRepository<Stock, Long> {

    // 예제가 아니라 실제로 사용할 때는 별도의 데이터소스로 분리해서 사용하자 (connection pool 부족현상 생김)
    @Query(value = "select get_lock(:key, 3000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);
}
