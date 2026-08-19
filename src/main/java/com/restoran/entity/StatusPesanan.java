package com.restoran.entity;

public enum StatusPesanan {
    MENUNGGU,   // baru dibuat, menunggu konfirmasi/diproses dapur
    DIPROSES,   // sedang disiapkan
    SELESAI,    // sudah selesai & diberikan ke pelanggan
    DIBATALKAN
}
