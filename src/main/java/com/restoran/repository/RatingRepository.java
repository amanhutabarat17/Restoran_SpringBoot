package com.restoran.repository;

import com.restoran.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RatingRepository extends JpaRepository<Rating, Long> {
    List<Rating> findByMenuIdOrderByCreatedAtDesc(Long menuId);
    boolean existsByOrderIdAndMenuId(Long orderId, Long menuId);
}
