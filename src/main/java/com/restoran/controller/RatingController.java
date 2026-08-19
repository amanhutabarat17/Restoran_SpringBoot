package com.restoran.controller;

import com.restoran.dto.RatingRequest;
import com.restoran.entity.*;
import com.restoran.repository.MenuRepository;
import com.restoran.repository.OrderRepository;
import com.restoran.repository.RatingRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingRepository ratingRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private MenuRepository menuRepository;

    @GetMapping("/menu/{menuId}")
    public List<Rating> ratingMenu(@PathVariable Long menuId) {
        return ratingRepository.findByMenuIdOrderByCreatedAtDesc(menuId);
    }

    /** Member memberi rating untuk menu yang sudah dia pesan & pesanan sudah SELESAI. */
    @PostMapping
    public ResponseEntity<?> beriRating(@Valid @RequestBody RatingRequest request,
                                         @AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();

        Order order = orderRepository.findById(request.getOrderId()).orElse(null);
        if (order == null) return ResponseEntity.badRequest().body(Map.of("message", "Pesanan tidak ditemukan"));

        if (order.getUser() == null || !order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "Ini bukan pesanan Anda"));
        }
        if (order.getStatus() != StatusPesanan.SELESAI) {
            return ResponseEntity.badRequest().body(Map.of("message", "Pesanan belum selesai, belum bisa dinilai"));
        }
        boolean menuAdaDiPesanan = order.getItems().stream()
                .anyMatch(item -> item.getMenu() != null && item.getMenu().getId().equals(request.getMenuId()));
        if (!menuAdaDiPesanan) {
            return ResponseEntity.badRequest().body(Map.of("message", "Menu ini tidak ada di pesanan tersebut"));
        }
        if (ratingRepository.existsByOrderIdAndMenuId(order.getId(), request.getMenuId())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Anda sudah memberi rating untuk menu ini di pesanan ini"));
        }

        Menu menu = menuRepository.findById(request.getMenuId())
                .orElseThrow(() -> new IllegalArgumentException("Menu tidak ditemukan"));

        Rating rating = new Rating();
        rating.setUser(user);
        rating.setMenu(menu);
        rating.setOrder(order);
        rating.setBintang(request.getBintang());
        rating.setKomentar(request.getKomentar());
        ratingRepository.save(rating);

        menu.setRatingTotal(menu.getRatingTotal() + request.getBintang());
        menu.setRatingJumlah(menu.getRatingJumlah() + 1);
        menuRepository.save(menu);

        return ResponseEntity.ok(rating);
    }
}
