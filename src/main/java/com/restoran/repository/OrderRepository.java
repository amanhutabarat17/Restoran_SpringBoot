package com.restoran.repository;

import com.restoran.entity.Order;
import com.restoran.entity.StatusPesanan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    Optional<Order> findByOrderNumber(String orderNumber);
    List<Order> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Order> findByStatusOrderByCreatedAtAsc(StatusPesanan status);
    List<Order> findAllByOrderByCreatedAtDesc();
    List<Order> findByCreatedAtBetween(LocalDateTime dari, LocalDateTime sampai);
    long countByCreatedAtBetween(LocalDateTime awal, LocalDateTime akhir);
}
