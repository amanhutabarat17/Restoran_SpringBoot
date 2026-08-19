package com.restoran.controller;

import com.restoran.entity.MetodePembayaran;
import com.restoran.entity.Order;
import com.restoran.entity.StatusPembayaran;
import com.restoran.repository.OrderRepository;
import com.restoran.service.MidtransService;
import com.restoran.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * PENTING: midtrans.server-key di application.properties masih placeholder.
 * Endpoint di controller ini baru bisa dites end-to-end setelah kamu isi
 * server key & client key asli (sandbox atau production) dari dashboard Midtrans.
 */
@RestController
@RequestMapping("/api/payment")
public class PaymentController {

    @Autowired private OrderRepository orderRepository;
    @Autowired private MidtransService midtransService;
    @Autowired private OrderService orderService;

    /** Dipanggil frontend saat pelanggan/kasir memilih bayar NON TUNAI. Mengembalikan Snap token + redirect_url. */
    @PostMapping("/midtrans/create/{orderId}")
    public ResponseEntity<?> buatTransaksi(@PathVariable Long orderId) {
        return orderRepository.findById(orderId).<ResponseEntity<?>>map(order -> {
            try {
                Map<String, String> hasil = midtransService.createSnapTransaction(order);
                order.setMetodePembayaran(MetodePembayaran.NON_TUNAI);
                order.setMidtransOrderId(order.getOrderNumber());
                order.setMidtransSnapToken(hasil.get("token"));
                order.setMidtransRedirectUrl(hasil.get("redirect_url"));
                orderRepository.save(order);
                return ResponseEntity.ok(hasil);
            } catch (Exception e) {
                return ResponseEntity.status(502).body(Map.of(
                        "message", "Gagal menghubungi Midtrans. Pastikan server-key sudah diisi dengan benar.",
                        "detail", e.getMessage()
                ));
            }
        }).orElse(ResponseEntity.notFound().build());
    }

    /**
     * Webhook notifikasi Midtrans (server-to-server). Daftarkan URL ini
     * (mis. https://domainmu.com/api/payment/midtrans/notification) di
     * dashboard Midtrans -> Settings -> Configuration -> Payment Notification URL.
     * Endpoint ini publik (permitAll) karena dipanggil langsung oleh server Midtrans, bukan browser.
     */
    @PostMapping("/midtrans/notification")
    public ResponseEntity<?> notifikasi(@RequestBody Map<String, Object> payload) {
        String orderNumber = String.valueOf(payload.get("order_id"));
        String transactionStatus = String.valueOf(payload.get("transaction_status"));
        String fraudStatus = String.valueOf(payload.get("fraud_status"));

        return orderRepository.findByOrderNumber(orderNumber).<ResponseEntity<?>>map(order -> {
            if ("capture".equals(transactionStatus) || "settlement".equals(transactionStatus)) {
                if ("accept".equals(fraudStatus) || "null".equals(fraudStatus)) {
                    orderService.tandaiLunas(order);
                }
            } else if ("cancel".equals(transactionStatus) || "deny".equals(transactionStatus)) {
                order.setStatusPembayaran(StatusPembayaran.FAILED);
                orderRepository.save(order);
            } else if ("expire".equals(transactionStatus)) {
                order.setStatusPembayaran(StatusPembayaran.EXPIRED);
                orderRepository.save(order);
            }
            return ResponseEntity.ok(Map.of("status", "ok"));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Frontend polling status pembayaran sebuah order (untuk auto-refresh setelah bayar Snap). */
    @GetMapping("/status/{orderId}")
    public ResponseEntity<?> statusPembayaran(@PathVariable Long orderId) {
        return orderRepository.findById(orderId)
                .map(o -> ResponseEntity.ok(Map.of(
                        "statusPembayaran", o.getStatusPembayaran().name(),
                        "status", o.getStatus().name()
                )))
                .orElse(ResponseEntity.notFound().build());
    }
}
