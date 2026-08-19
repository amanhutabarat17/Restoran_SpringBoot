package com.restoran.controller;

import com.restoran.dto.CreateOrderRequest;
import com.restoran.dto.UpdateStatusRequest;
import com.restoran.entity.*;
import com.restoran.repository.OrderRepository;
import com.restoran.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;

    /**
     * Buat pesanan baru. Dipakai oleh:
     * - Guest (scan QR meja, tanpa login) -> Authorization header kosong, user = null
     * - Member (scan QR meja, sudah login) -> Authorization header ada, dapat poin nanti
     * - Kasir (input pesanan manual walk-in) -> kasir tetap kirim tanpa user, lalu klaim lewat /kasir
     */
    @PostMapping
    public ResponseEntity<?> buatPesanan(@Valid @RequestBody CreateOrderRequest request,
                                          @AuthenticationPrincipal User user) {
        try {
            User pemesan = (user != null && user.getRole() == Role.CUSTOMER) ? user : null;
            Order order = orderService.buatPesanan(request, pemesan);
            return ResponseEntity.ok(order);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        }
    }

    /** Riwayat pesanan milik member yang sedang login. */
    @GetMapping("/saya")
    public ResponseEntity<?> pesananSaya(@AuthenticationPrincipal User user) {
        if (user == null) return ResponseEntity.status(401).build();
        return ResponseEntity.ok(orderRepository.findByUserIdOrderByCreatedAtDesc(user.getId()));
    }

    /** Kasir/Admin: lihat semua pesanan, atau filter berdasar status. */
    @GetMapping
    public List<Order> semuaPesanan(@RequestParam(required = false) StatusPesanan status) {
        if (status != null) return orderRepository.findByStatusOrderByCreatedAtAsc(status);
        return orderRepository.findAllByOrderByCreatedAtDesc();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getById(@PathVariable Long id) {
        return orderRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    /** Kasir mengklaim & mengubah status pesanan (menunggu -> diproses -> selesai / dibatalkan). */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> updateStatus(@PathVariable Long id, @Valid @RequestBody UpdateStatusRequest request,
                                           @AuthenticationPrincipal User user) {
        return orderRepository.findById(id).<ResponseEntity<?>>map(order -> {
            order.setStatus(request.getStatus());
            if (user != null && user.getRole() == Role.KASIR && order.getKasir() == null) {
                order.setKasir(user);
            }
            return ResponseEntity.ok(orderRepository.save(order));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Kasir menandai pesanan dibayar TUNAI langsung di kasir. */
    @PostMapping("/{id}/bayar-tunai")
    public ResponseEntity<?> bayarTunai(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return orderRepository.findById(id).<ResponseEntity<?>>map(order -> {
            if (order.getStatusPembayaran() == StatusPembayaran.PAID) {
                return ResponseEntity.badRequest().body(Map.of("message", "Pesanan ini sudah lunas"));
            }
            order.setMetodePembayaran(MetodePembayaran.TUNAI);
            if (user != null && user.getRole() == Role.KASIR) order.setKasir(user);
            orderService.tandaiLunas(order);
            return ResponseEntity.ok(order);
        }).orElse(ResponseEntity.notFound().build());
    }
}
