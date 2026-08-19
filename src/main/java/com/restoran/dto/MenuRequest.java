package com.restoran.dto;

import com.restoran.entity.KategoriMenu;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class MenuRequest {
    @NotBlank
    private String nama;
    private String deskripsi;
    @NotNull
    private BigDecimal harga;
    @NotNull
    private KategoriMenu kategori;
    private Integer stok = 0;
    private String icon;
    private String gambarUrl;
    private String badge;
    private Boolean aktif = true;

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
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
}