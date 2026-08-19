package com.restoran.controller;

import com.restoran.dto.KasirRequest;
import com.restoran.entity.Role;
import com.restoran.entity.User;
import com.restoran.repository.UserRepository;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * Khusus ADMIN: kelola akun kasir (tambah kasir baru, ubah shift, nonaktifkan).
 * Semua endpoint di sini dibatasi lewat SecurityConfig ("/api/admin/**" -> ROLE_ADMIN).
 */
@RestController
@RequestMapping("/api/admin/kasir")
public class KasirController {

    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @GetMapping
    public List<User> getSemuaKasir() {
        return userRepository.findByRole(Role.KASIR);
    }

    @PostMapping
    public ResponseEntity<?> tambahKasir(@Valid @RequestBody KasirRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.badRequest().body(Map.of("message", "Email sudah dipakai"));
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("message", "Password wajib diisi untuk kasir baru"));
        }

        User kasir = new User();
        kasir.setNama(request.getNama());
        kasir.setEmail(request.getEmail());
        kasir.setPassword(passwordEncoder.encode(request.getPassword()));
        kasir.setRole(Role.KASIR);
        kasir.setShift(request.getShift());
        kasir.setAktif(request.getAktif() != null ? request.getAktif() : true);
        return ResponseEntity.ok(userRepository.save(kasir));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateKasir(@PathVariable Long id, @Valid @RequestBody KasirRequest request) {
        return userRepository.findById(id).<ResponseEntity<?>>map(kasir -> {
            if (kasir.getRole() != Role.KASIR) {
                return ResponseEntity.badRequest().body(Map.of("message", "Pengguna ini bukan kasir"));
            }
            kasir.setNama(request.getNama());
            kasir.setShift(request.getShift());
            if (request.getAktif() != null) kasir.setAktif(request.getAktif());
            if (request.getPassword() != null && !request.getPassword().isBlank()) {
                kasir.setPassword(passwordEncoder.encode(request.getPassword()));
            }
            return ResponseEntity.ok(userRepository.save(kasir));
        }).orElse(ResponseEntity.notFound().build());
    }

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<?> toggleAktif(@PathVariable Long id) {
        return userRepository.findById(id).map(kasir -> {
            kasir.setAktif(!Boolean.TRUE.equals(kasir.getAktif()));
            return ResponseEntity.ok(userRepository.save(kasir));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> hapusKasir(@PathVariable Long id) {
        return userRepository.findById(id).map(kasir -> {
            userRepository.delete(kasir);
            return ResponseEntity.noContent().build();
        }).orElse(ResponseEntity.notFound().build());
    }
}
