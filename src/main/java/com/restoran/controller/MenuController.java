package com.restoran.controller;

import com.restoran.dto.MenuRequest;
import com.restoran.entity.KategoriMenu;
import com.restoran.entity.Menu;
import com.restoran.repository.MenuRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/menus")
public class MenuController {

    @Autowired
    private MenuRepository menuRepository;

    /** Publik: daftar menu aktif saja (dipakai halaman pelanggan). Bisa difilter ?kategori=MAKANAN */
    @GetMapping
    public List<Menu> getMenuAktif(@RequestParam(required = false) KategoriMenu kategori) {
        if (kategori != null) return menuRepository.findByAktifTrueAndKategori(kategori);
        return menuRepository.findByAktifTrue();
    }

    /** Admin: semua menu termasuk yang nonaktif, untuk halaman Kelola Menu. */
    @GetMapping("/semua")
    public List<Menu> getSemuaMenu() {
        return menuRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Menu> getById(@PathVariable Long id) {
        return menuRepository.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Menu> tambahMenu(@Valid @RequestBody MenuRequest request) {
        Menu menu = new Menu();
        isiDariRequest(menu, request);
        return ResponseEntity.ok(menuRepository.save(menu));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMenu(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        return menuRepository.findById(id).map(menu -> {
            isiDariRequest(menu, request);
            return ResponseEntity.ok(menuRepository.save(menu));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Update cepat stok saja (dipakai admin/kasir). Body: {"stok": 10} */
    @PatchMapping("/{id}/stok")
    public ResponseEntity<?> updateStok(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        return menuRepository.findById(id).map(menu -> {
            menu.setStok(body.getOrDefault("stok", menu.getStok()));
            return ResponseEntity.ok(menuRepository.save(menu));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAktif(@PathVariable Long id) {
        return menuRepository.findById(id).map(menu -> {
            menu.setAktif(!Boolean.TRUE.equals(menu.getAktif()));
            return ResponseEntity.ok(menuRepository.save(menu));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hapusMenu(@PathVariable Long id) {
        if (!menuRepository.existsById(id)) return ResponseEntity.notFound().build();
        menuRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void isiDariRequest(Menu menu, MenuRequest request) {
        menu.setNama(request.getNama());
        menu.setDeskripsi(request.getDeskripsi());
        menu.setHarga(request.getHarga());
        menu.setKategori(request.getKategori());
        menu.setStok(request.getStok() != null ? request.getStok() : 0);
        menu.setIcon(request.getIcon());
        // gambarUrl sengaja tidak ditimpa di sini kalau request tidak mengirim nilai baru,
        // supaya update field lain (harga/stok/dll) tidak menghapus gambar yang sudah diupload.
        if (request.getGambarUrl() != null) menu.setGambarUrl(request.getGambarUrl());
        menu.setBadge(request.getBadge());
        menu.setAktif(request.getAktif() != null ? request.getAktif() : true);
    }
}