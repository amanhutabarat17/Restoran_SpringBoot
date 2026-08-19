package com.restoran.dto;

import com.restoran.entity.ShiftKasir;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class KasirRequest {
    @NotBlank
    private String nama;
    @NotBlank @Email
    private String email;
    private String password; // opsional saat update
    private ShiftKasir shift;
    private Boolean aktif;

    public String getNama() { return nama; }
    public void setNama(String nama) { this.nama = nama; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public ShiftKasir getShift() { return shift; }
    public void setShift(ShiftKasir shift) { this.shift = shift; }
    public Boolean getAktif() { return aktif; }
    public void setAktif(Boolean aktif) { this.aktif = aktif; }
}
