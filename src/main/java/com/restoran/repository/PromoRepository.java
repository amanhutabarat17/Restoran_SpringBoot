package com.restoran.repository;

import com.restoran.entity.Promo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PromoRepository extends JpaRepository<Promo, Long> {
    Optional<Promo> findByKodeIgnoreCase(String kode);
    List<Promo> findByAktifTrue();
}
