package com.restoran.service;

import com.restoran.dto.CreateOrderRequest;
import com.restoran.dto.OrderItemRequest;
import com.restoran.entity.*;
import com.restoran.repository.MenuRepository;
import com.restoran.repository.OrderRepository;
import com.restoran.repository.PromoRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class OrderService {

    private final MenuRepository menuRepository;
    private final PromoRepository promoRepository;
    private final OrderRepository orderRepository;
    private final OrderNumberGenerator orderNumberGenerator;

    @Value("${app.biaya-layanan}")
    private BigDecimal biayaLayananFlat;

    /** 1 poin untuk setiap kelipatan Rp10.000 belanja (bisa diubah sesuai kebijakan resto). */
    private static final BigDecimal RUPIAH_PER_POIN = BigDecimal.valueOf(10000);

    public OrderService(MenuRepository menuRepository, PromoRepository promoRepository,
                         OrderRepository orderRepository, OrderNumberGenerator orderNumberGenerator) {
        this.menuRepository = menuRepository;
        this.promoRepository = promoRepository;
        this.orderRepository = orderRepository;
        this.orderNumberGenerator = orderNumberGenerator;
    }

    @Transactional
    public Order buatPesanan(CreateOrderRequest request, User pemesan) {
        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.next());
        order.setUser(pemesan); // null jika guest
        order.setNoMeja(request.getNoMeja());
        order.setNomorAntrian(generateNomorAntrian());
        order.setStatus(StatusPesanan.MENUNGGU);
        order.setStatusPembayaran(StatusPembayaran.UNPAID);

        BigDecimal subtotal = BigDecimal.ZERO;

        for (OrderItemRequest itemReq : request.getItems()) {
            Menu menu = menuRepository.findById(itemReq.getMenuId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu tidak ditemukan: " + itemReq.getMenuId()));

            if (!Boolean.TRUE.equals(menu.getAktif())) {
                throw new IllegalStateException("Menu '" + menu.getNama() + "' sedang tidak tersedia");
            }
            if (menu.getStok() < itemReq.getQty()) {
                throw new IllegalStateException("Stok '" + menu.getNama() + "' tidak cukup (sisa " + menu.getStok() + ")");
            }

            menu.setStok(menu.getStok() - itemReq.getQty());
            menuRepository.save(menu);

            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setMenu(menu);
            item.setNamaMenu(menu.getNama());
            item.setHargaSatuan(menu.getHarga());
            item.setQty(itemReq.getQty());
            BigDecimal subItem = menu.getHarga().multiply(BigDecimal.valueOf(itemReq.getQty()));
            item.setSubtotal(subItem);

            order.getItems().add(item);
            subtotal = subtotal.add(subItem);
        }

        BigDecimal diskon = BigDecimal.ZERO;
        if (request.getKodePromo() != null && !request.getKodePromo().isBlank()) {
            Promo promo = promoRepository.findByKodeIgnoreCase(request.getKodePromo())
                    .orElseThrow(() -> new IllegalArgumentException("Kode promo tidak valid"));
            if (!promo.isMasihBerlaku()) {
                throw new IllegalStateException("Promo sudah tidak berlaku");
            }
            diskon = promo.getTipe() == TipePromo.PERSEN
                    ? subtotal.multiply(promo.getNilai()).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP)
                    : promo.getNilai();
            if (diskon.compareTo(subtotal) > 0) diskon = subtotal;
            order.setPromo(promo);
        }

        BigDecimal biayaLayanan = biayaLayananFlat;
        BigDecimal total = subtotal.subtract(diskon).add(biayaLayanan);

        order.setSubtotal(subtotal);
        order.setDiskon(diskon);
        order.setBiayaLayanan(biayaLayanan);
        order.setTotal(total);

        return orderRepository.save(order);
    }

    /** Dipanggil ketika pembayaran (tunai oleh kasir, atau notifikasi Midtrans) berhasil dikonfirmasi. */
    @Transactional
    public void tandaiLunas(Order order) {
        order.setStatusPembayaran(StatusPembayaran.PAID);
        if (order.getStatus() == StatusPesanan.MENUNGGU) {
            order.setStatus(StatusPesanan.DIPROSES);
        }

        // Beri poin ke member (hanya jika pemesan adalah member/CUSTOMER yang login)
        User pemesan = order.getUser();
        if (pemesan != null && pemesan.getRole() == Role.CUSTOMER) {
            int poinBaru = order.getTotal().divide(RUPIAH_PER_POIN, 0, RoundingMode.DOWN).intValue();
            pemesan.setPoin(pemesan.getPoin() + poinBaru);
        }

        orderRepository.save(order);
    }
    private Integer generateNomorAntrian() {
        LocalDate hariIni = LocalDate.now();
        LocalDateTime awalHari = hariIni.atStartOfDay();
        LocalDateTime akhirHari = hariIni.atTime(LocalTime.MAX);
        long jumlahHariIni = orderRepository.countByCreatedAtBetween(awalHari, akhirHari);
        return (int) jumlahHariIni + 1;
    }
}
