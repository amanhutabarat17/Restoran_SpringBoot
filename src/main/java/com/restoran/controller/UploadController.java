package com.restoran.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Menangani upload file (gambar menu, dll). File disimpan ke folder fisik di
 * disk (di luar folder src, supaya tidak ikut ke-rebuild/ketimpa Maven), lalu
 * bisa diakses balik lewat URL publik /uploads/... (lihat WebConfig).
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private static final Set<String> TIPE_DIIZINKAN = Set.of("image/jpeg", "image/png", "image/webp");
    private static final long MAKS_UKURAN_BYTES = 5L * 1024 * 1024; // 5MB, samakan dengan batas di frontend

    @PostMapping("/menu-gambar")
    public ResponseEntity<?> uploadGambarMenu(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File tidak boleh kosong.");
        }
        if (file.getSize() > MAKS_UKURAN_BYTES) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ukuran file maksimal 5MB.");
        }
        String contentType = file.getContentType();
        if (contentType == null || !TIPE_DIIZINKAN.contains(contentType)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Format file harus JPG, PNG, atau WEBP.");
        }

        try {
            Path folderTujuan = Paths.get(uploadDir, "menu");
            Files.createDirectories(folderTujuan);

            String ekstensi = switch (contentType) {
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                default -> ".jpg";
            };
            String namaFileBaru = UUID.randomUUID() + ekstensi;
            Path tujuanAkhir = folderTujuan.resolve(namaFileBaru);

            file.transferTo(tujuanAkhir);

            // Path publik yang dipetakan WebConfig ke folder fisik di atas.
            String urlPublik = "/uploads/menu/" + namaFileBaru;
            return ResponseEntity.ok(Map.of("url", urlPublik));
        } catch (IOException e) {
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Gagal menyimpan file: " + e.getMessage());
        }
    }
}