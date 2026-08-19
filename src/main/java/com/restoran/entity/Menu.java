package com.restoran.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

@Entity
@Table(name = "menus")
public class Menu {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String nama;

    @Column(columnDefinition = "TEXT")
    private String deskripsi;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal harga;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private KategoriMenu kategori = KategoriMenu.MAKANAN;

    @Column(nullable = false)
    private Integer stok = 0;

    private String icon;

    /** Path/URL gambar asli menu (hasil upload admin), mis. "/uploads/menu/abc123.jpg". Boleh kosong -> fallback ke icon emoji di frontend. */
    @Column(name = "gambar_url")
    private String gambarUrl;

    private String badge;

    @Column(name = "rating_total")
    private Integer ratingTotal = 0; // jumlah bintang terkumpul

    @Column(name = "rating_jumlah")
    private Integer ratingJumlah = 0; // jumlah orang yang menilai

    @Column(nullable = false)
    private Boolean aktif = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getDeskripsi() { return deskripsi; }
    public void setDeskripsi(String deskripsi) { this.deskripsi = deskripsi; }
    public BigDecimal getHarga() { return harga; }
    public void setHarga(BigDecimal harga) { this.harga = harga; }
    public KategoriMenu getKategori() { return kategori; }
    public void setKategori(KategoriMenu kategori) { this.kategori = kategori; }
    public Integer getStok() { return stok; }
    public void setStok(Integer stok) { this.stok = stok; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getGambarUrl() { return gambarUrl; }
    public void setGambarUrl(String gambarUrl) { this.gambarUrl = gambarUrl; }
    public String getBadge() { return badge; }
    public void setBadge(String badge) { this.badge = badge; }
    public Integer getRatingTotal() { return ratingTotal; }
    public void setRatingTotal(Integer ratingTotal) { this.ratingTotal = ratingTotal; }
    public Integer getRatingJumlah() { return ratingJumlah; }
    public void setRatingJumlah(Integer ratingJumlah) { this.ratingJumlah = ratingJumlah; }
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /** Rating rata-rata, dihitung dari ratingTotal / ratingJumlah. Dipakai frontend. */
    public double getRating() {
        if (ratingJumlah == null || ratingJumlah == 0) return 0.0;
        return BigDecimal.valueOf(ratingTotal)
                .divide(BigDecimal.valueOf(ratingJumlah), 1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}