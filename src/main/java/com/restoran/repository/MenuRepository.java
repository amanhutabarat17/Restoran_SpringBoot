package com.restoran.repository;

import com.restoran.entity.KategoriMenu;
import com.restoran.entity.Menu;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MenuRepository extends JpaRepository<Menu, Long> {
    List<Menu> findByAktifTrue();
    List<Menu> findByAktifTrueAndKategori(KategoriMenu kategori);
}
