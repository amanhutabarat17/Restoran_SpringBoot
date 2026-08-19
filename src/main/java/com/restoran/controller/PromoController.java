package com.restoran.controller;

import com.restoran.dto.PromoRequest;
import com.restoran.entity.Promo;
import com.restoran.repository.PromoRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/promo")
public class PromoController {

    @Autowired
    private PromoRepository promoRepository;

    /** Publik: dipakai untuk validasi kode promo saat checkout. */
    @GetMapping("/aktif")
    public List<Promo> getPromoAktif() {
        return promoRepository.findByAktifTrue();
    }

    @GetMapping
    public List<Promo> getSemuaPromo() {
        return promoRepository.findAll();
    }

    @PostMapping
    public ResponseEntity<Promo> tambah(@Valid @RequestBody PromoRequest request) {
        Promo promo = new Promo();
        isi(promo, request);
        return ResponseEntity.ok(promoRepository.save(promo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody PromoRequest request) {
        return promoRepository.findById(id).map(promo -> {
            isi(promo, request);
            return ResponseEntity.ok(promoRepository.save(promo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAktif(@PathVariable Long id) {
        return promoRepository.findById(id).map(promo -> {
            promo.setAktif(!Boolean.TRUE.equals(promo.getAktif()));
            return ResponseEntity.ok(promoRepository.save(promo));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hapus(@PathVariable Long id) {
        if (!promoRepository.existsById(id)) return ResponseEntity.notFound().build();
        promoRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void isi(Promo promo, PromoRequest request) {
        promo.setJudul(request.getJudul());
        promo.setKode(request.getKode());
        promo.setTipe(request.getTipe());
        promo.setNilai(request.getNilai());
        promo.setBerlakuSampai(request.getBerlakuSampai());
        promo.setAktif(request.getAktif() != null ? request.getAktif() : true);
    }
}
