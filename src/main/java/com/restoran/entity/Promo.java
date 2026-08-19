package com.restoran.entity;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "promos")
public class Promo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String judul;

    @Column(nullable = false, unique = true, length = 30)
    private String kode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipePromo tipe;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal nilai;

    @Column(name = "berlaku_sampai", nullable = false)
    private LocalDate berlakuSampai;

    @Column(nullable = false)
    private Boolean aktif = true;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getJudul() { return judul; }
    public void setJudul(String judul) { this.judul = judul; }
    public String getKode() { return kode; }
    public void setKode(String kode) { this.kode = kode; }
    public TipePromo getTipe() { return tipe; }
    public void setTipe(TipePromo tipe) { this.tipe = tipe; }
    public BigDecimal getNilai() { return nilai; }
    public void setNilai(BigDecimal nilai) { this.nilai = nilai; }
    public LocalDate getBerlakuSampai() { return berlakuSampai; }
    public void setBerlakuSampai(LocalDate berlakuSampai) { this.berlakuSampai = berlakuSampai; }
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public boolean isMasihBerlaku() {
        return Boolean.TRUE.equals(aktif) && berlakuSampai != null && !berlakuSampai.isBefore(LocalDate.now());
    }
}
