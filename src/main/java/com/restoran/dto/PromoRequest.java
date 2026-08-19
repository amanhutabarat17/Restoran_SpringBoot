package com.restoran.dto;

import com.restoran.entity.TipePromo;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;

public class PromoRequest {
    @NotBlank
    private String judul;
    @NotBlank
    private String kode;
    @NotNull
    private TipePromo tipe;
    @NotNull
    private BigDecimal nilai;
    @NotNull
    private LocalDate berlakuSampai;
    private Boolean aktif = true;

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
}
