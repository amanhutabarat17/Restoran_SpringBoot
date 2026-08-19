package com.restoran.controller;

import com.restoran.entity.Order;
import com.restoran.entity.StatusPembayaran;
import com.restoran.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/laporan")
public class LaporanController {

    @Autowired private OrderRepository orderRepository;

    /** Rekap harian dalam rentang tanggal (default: 7 hari terakhir). */
    @GetMapping("/harian")
    public List<Map<String, Object>> laporanHarian(
            @RequestParam(required = false) String dari,
            @RequestParam(required = false) String sampai) {

        LocalDate tglSampai = sampai != null ? LocalDate.parse(sampai) : LocalDate.now();
        LocalDate tglDari = dari != null ? LocalDate.parse(dari) : tglSampai.minusDays(6);

        List<Order> orders = orderRepository.findByCreatedAtBetween(
                tglDari.atStartOfDay(), tglSampai.plusDays(1).atStartOfDay());

        List<Order> lunas = orders.stream()
                .filter(o -> o.getStatusPembayaran() == StatusPembayaran.PAID)
                .collect(Collectors.toList());

        Map<LocalDate, List<Order>> perTanggal = lunas.stream()
                .collect(Collectors.groupingBy(o -> o.getCreatedAt().toLocalDate()));

        List<Map<String, Object>> hasil = new ArrayList<>();
        for (LocalDate tgl = tglDari; !tgl.isAfter(tglSampai); tgl = tgl.plusDays(1)) {
            List<Order> daftar = perTanggal.getOrDefault(tgl, List.of());
            BigDecimal omzet = daftar.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
            Map<String, Object> baris = new LinkedHashMap<>();
            baris.put("tanggal", tgl.format(DateTimeFormatter.ISO_DATE));
            baris.put("transaksi", daftar.size());
            baris.put("omzet", omzet);
            hasil.add(baris);
        }
        return hasil;
    }

    /** Ringkasan total (dipakai kartu statistik di dashboard admin). */
    @GetMapping("/ringkasan")
    public Map<String, Object> ringkasan() {
        List<Order> lunas = orderRepository.findAll().stream()
                .filter(o -> o.getStatusPembayaran() == StatusPembayaran.PAID)
                .collect(Collectors.toList());

        BigDecimal totalOmzet = lunas.stream().map(Order::getTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        int totalTransaksi = lunas.size();
        BigDecimal rataRata = totalTransaksi > 0
                ? totalOmzet.divide(BigDecimal.valueOf(totalTransaksi), 0, java.math.RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String, Object> hasil = new LinkedHashMap<>();
        hasil.put("totalOmzet", totalOmzet);
        hasil.put("totalTransaksi", totalTransaksi);
        hasil.put("rataRataPerTransaksi", rataRata);
        return hasil;
    }
}
