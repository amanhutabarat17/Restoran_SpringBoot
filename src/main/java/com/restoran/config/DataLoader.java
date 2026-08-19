package com.restoran.config;

import com.restoran.entity.*;
import com.restoran.repository.MenuRepository;
import com.restoran.repository.PromoRepository;
import com.restoran.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(MenuRepository menuRepository,
                                    UserRepository userRepository,
                                    PromoRepository promoRepository,
                                    PasswordEncoder passwordEncoder) {
        return args -> {
            if (menuRepository.count() == 0) {
                Menu m1 = new Menu();
                m1.setNama("Nasi Goreng Spesial");
                m1.setDeskripsi("Nasi goreng dengan telur mata sapi, ayam suwir, dan sosis premium.");
                m1.setHarga(new BigDecimal("25000"));
                m1.setKategori(KategoriMenu.MAKANAN);
                m1.setStok(42);
                m1.setIcon("🍛");
                m1.setBadge("Terlaris");

                Menu m2 = new Menu();
                m2.setNama("Mie Tek-Tek Nyemek");
                m2.setDeskripsi("Mie rebus bumbu kental pedas manis dengan sayuran segar.");
                m2.setHarga(new BigDecimal("20000"));
                m2.setKategori(KategoriMenu.MAKANAN);
                m2.setStok(30);
                m2.setIcon("🍜");

                Menu m3 = new Menu();
                m3.setNama("Ayam Bakar Madu");
                m3.setDeskripsi("Ayam bakar lembut dengan bumbu madu karamelisasi khas.");
                m3.setHarga(new BigDecimal("30000"));
                m3.setKategori(KategoriMenu.MAKANAN);
                m3.setStok(15);
                m3.setIcon("🍗");
                m3.setBadge("Favorit");

                Menu m4 = new Menu();
                m4.setNama("Es Teh Melati");
                m4.setDeskripsi("Teh melati segar dengan es batu, manisnya pas.");
                m4.setHarga(new BigDecimal("5000"));
                m4.setKategori(KategoriMenu.MINUMAN);
                m4.setStok(80);
                m4.setIcon("🍹");

                Menu m5 = new Menu();
                m5.setNama("Kopi Susu Aren");
                m5.setDeskripsi("Espresso blend house dengan susu segar dan gula aren asli.");
                m5.setHarga(new BigDecimal("18000"));
                m5.setKategori(KategoriMenu.MINUMAN);
                m5.setStok(25);
                m5.setIcon("☕");
                m5.setBadge("Baru");

                Menu m6 = new Menu();
                m6.setNama("Dimsum Mentai");
                m6.setDeskripsi("4 pcs dimsum udang ayam dengan saus mentai bakar.");
                m6.setHarga(new BigDecimal("22000"));
                m6.setKategori(KategoriMenu.MAKANAN);
                m6.setStok(18);
                m6.setIcon("🥟");

                menuRepository.saveAll(List.of(m1, m2, m3, m4, m5, m6));
                System.out.println("✅ Data awal Menu berhasil dimasukkan!");
            }

            if (userRepository.count() == 0) {
                User admin = new User();
                admin.setNama("Admin RestoKu");
                admin.setEmail("admin@restoku.id");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setAktif(true);

                User kasir1 = new User();
                kasir1.setNama("Siti Nurhaliza");
                kasir1.setEmail("siti@restoku.id");
                kasir1.setPassword(passwordEncoder.encode("kasir123"));
                kasir1.setRole(Role.KASIR);
                kasir1.setShift(ShiftKasir.PAGI);
                kasir1.setAktif(true);

                User kasir2 = new User();
                kasir2.setNama("Budi Santoso");
                kasir2.setEmail("budi@restoku.id");
                kasir2.setPassword(passwordEncoder.encode("kasir123"));
                kasir2.setRole(Role.KASIR);
                kasir2.setShift(ShiftKasir.MALAM);
                kasir2.setAktif(true);

                User member = new User();
                member.setNama("Pelanggan Contoh");
                member.setEmail("pelanggan@mail.com");
                member.setPassword(passwordEncoder.encode("member123"));
                member.setRole(Role.CUSTOMER);
                member.setPoin(0);
                member.setAktif(true);

                userRepository.saveAll(List.of(admin, kasir1, kasir2, member));
                System.out.println("✅ Akun awal dibuat! admin@restoku.id / admin123 | siti@restoku.id / kasir123 | pelanggan@mail.com / member123");
            }

            if (promoRepository.count() == 0) {
                Promo p1 = new Promo();
                p1.setJudul("Diskon Jam Makan Siang");
                p1.setKode("SIANG15");
                p1.setTipe(TipePromo.PERSEN);
                p1.setNilai(new BigDecimal("15"));
                p1.setBerlakuSampai(LocalDate.now().plusMonths(1));

                Promo p2 = new Promo();
                p2.setJudul("Gratis Es Teh");
                p2.setKode("FREETEH");
                p2.setTipe(TipePromo.NOMINAL);
                p2.setNilai(new BigDecimal("5000"));
                p2.setBerlakuSampai(LocalDate.now().plusWeeks(1));

                promoRepository.saveAll(List.of(p1, p2));
                System.out.println("✅ Data awal Promo berhasil dimasukkan!");
            }
        };
    }
}
